package ru.itmo.blps.taiga.jca;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TaigaPhysicalConnection {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private static final ConcurrentHashMap<Integer, Map<String, Long>> TYPE_CACHE = new ConcurrentHashMap<>();

    private static final Set<String> BUG_STATUSES = Set.of("CANCELLED", "REFUNDED");
    private static final Set<String> QUESTION_STATUSES = Set.of(
            "CHECKED_OUT", "FULFILLMENT_SELECTED", "WAITING_PAYMENT");

    private final TaigaManagedConnection managedConnection;
    private final TaigaManagedConnectionFactory mcf;
    private volatile boolean closed;

    public TaigaPhysicalConnection(TaigaManagedConnection managedConnection, TaigaManagedConnectionFactory mcf) {
        this.managedConnection = managedConnection;
        this.mcf = mcf;
    }

    public void syncOrderStatus(long orderId, String status, String eventId) throws IOException, InterruptedException {
        if (closed) {
            throw new IOException("Connection closed");
        }
        String base = mcf.getBaseUrl().replaceAll("/+$", "");
        String token = mcf.getBearerToken();
        String storyId = mcf.getUserStoryId();
        if (token == null || token.isBlank()) {
            throw new IOException("Taiga bearerToken is not configured on the JCA resource adapter");
        }

        int projectId = fetchProjectId(base, token, storyId);
        String tag = "order-" + orderId;
        String newSubject = "Order #" + orderId + " – " + status;
        String newDescription = "[" + eventId + "] orderId=" + orderId + " status=" + status;
        long typeId = resolveTypeId(base, token, projectId, status);

        JsonNode existing = findExistingIssue(base, token, projectId, tag);
        if (existing != null) {
            updateIssue(base, token, existing.path("id").asLong(), existing.path("version").asInt(),
                    typeId, newSubject, newDescription);
        } else {
            createIssue(base, token, projectId, tag, typeId, newSubject, newDescription);
        }
    }

    private long resolveTypeId(String base, String token, int projectId, String status)
            throws IOException, InterruptedException {
        Map<String, Long> types = TYPE_CACHE.computeIfAbsent(projectId, id -> new ConcurrentHashMap<>());
        if (types.isEmpty()) {
            HttpRequest req = HttpRequest.newBuilder(
                            URI.create(base + "/api/v1/issue-types?project=" + projectId))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            System.err.println("[TaigaJCA] issue-types status=" + res.statusCode() + " body=" + res.body());
            if (res.statusCode() / 100 != 2) {
                throw new IOException("Taiga GET issue-types failed: HTTP " + res.statusCode());
            }
            for (JsonNode t : MAPPER.readTree(res.body())) {
                types.put(t.path("name").asText().toLowerCase(), t.path("id").asLong());
            }
        }
        String typeName = BUG_STATUSES.contains(status) ? "bug"
                : QUESTION_STATUSES.contains(status) ? "question"
                : "enhancement";
        Long id = types.get(typeName);
        if (id == null) {
            id = types.values().iterator().next();
        }
        System.err.println("[TaigaJCA] resolveTypeId status=" + status + " typeName=" + typeName + " typeId=" + id);
        return id;
    }

    private int fetchProjectId(String base, String token, String storyId) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(base + "/api/v1/userstories/" + storyId))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (res.statusCode() / 100 != 2) {
            throw new IOException("Taiga GET userstory failed: HTTP " + res.statusCode() + " body=" + res.body());
        }
        return MAPPER.readTree(res.body()).path("project").asInt();
    }

    private JsonNode findExistingIssue(String base, String token, int projectId, String tag)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(
                        URI.create(base + "/api/v1/issues?project=" + projectId + "&tags=" + tag))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.err.println("[TaigaJCA] findExistingIssue tag=" + tag + " status=" + res.statusCode() + " body=" + res.body());
        if (res.statusCode() / 100 != 2) {
            throw new IOException("Taiga GET issues failed: HTTP " + res.statusCode() + " body=" + res.body());
        }
        JsonNode list = MAPPER.readTree(res.body());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return findExistingIssueByFullScan(base, token, projectId, tag);
    }

    private JsonNode findExistingIssueByFullScan(String base, String token, int projectId, String tag)
            throws IOException, InterruptedException {
        int page = 1;
        while (true) {
            HttpRequest req = HttpRequest.newBuilder(
                            URI.create(base + "/api/v1/issues?project=" + projectId + "&page=" + page + "&page_size=100"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            System.err.println("[TaigaJCA] fullScan issues page=" + page + " status=" + res.statusCode());
            if (res.statusCode() / 100 != 2) {
                throw new IOException("Taiga list issues failed: HTTP " + res.statusCode());
            }
            JsonNode list = MAPPER.readTree(res.body());
            if (list.isEmpty()) {
                return null;
            }
            for (JsonNode node : list) {
                for (JsonNode t : node.path("tags")) {
                    String val = t.isArray() ? t.path(0).asText() : t.asText();
                    if (tag.equals(val)) {
                        System.err.println("[TaigaJCA] found existing issue id=" + node.path("id").asLong());
                        return node;
                    }
                }
            }
            if (list.size() < 100) {
                return null;
            }
            page++;
        }
    }

    private void updateIssue(String base, String token, long id, int version, long typeId, String subject, String description)
            throws IOException, InterruptedException {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("subject", subject);
        body.put("description", description);
        body.put("version", version);
        body.put("type", typeId);
        String bodyStr = MAPPER.writeValueAsString(body);
        System.err.println("[TaigaJCA] updateIssue id=" + id + " body=" + bodyStr);
        HttpRequest req = HttpRequest.newBuilder(URI.create(base + "/api/v1/issues/" + id))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(bodyStr, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.err.println("[TaigaJCA] updateIssue status=" + res.statusCode() + " response=" + res.body());
        if (res.statusCode() / 100 != 2) {
            throw new IOException("Taiga PATCH issue failed: HTTP " + res.statusCode() + " body=" + res.body());
        }
    }

    private void createIssue(String base, String token, int projectId, String tag, long typeId, String subject, String description)
            throws IOException, InterruptedException {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("project", projectId);
        body.put("type", typeId);
        body.put("subject", subject);
        body.put("description", description);
        body.putArray("tags").add(tag);
        String bodyStr = MAPPER.writeValueAsString(body);
        System.err.println("[TaigaJCA] createIssue body=" + bodyStr);
        HttpRequest req = HttpRequest.newBuilder(URI.create(base + "/api/v1/issues"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyStr, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.err.println("[TaigaJCA] createIssue status=" + res.statusCode() + " response=" + res.body());
        if (res.statusCode() / 100 != 2) {
            throw new IOException("Taiga POST issue failed: HTTP " + res.statusCode() + " body=" + res.body());
        }
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        managedConnection.notifyClosed(this);
    }
}
