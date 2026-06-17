package ru.itmo.blps.app.security.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.itmo.blps.app.models.enums.UserRole;

@Component
public class XmlUserStore {
    private final Path storePath;

    public XmlUserStore(@Value("${app.security.user-store.path}") String storePath) {
        this.storePath = Paths.get(storePath).toAbsolutePath().normalize();
    }

    public synchronized Optional<StoredUserAccount> findByUsername(String username) {
        Document document = readDocument();
        NodeList users = document.getDocumentElement().getElementsByTagName("user");
        for (int i = 0; i < users.getLength(); i++) {
            Element element = (Element) users.item(i);
            if (username.equals(element.getAttribute("username"))) {
                return Optional.of(toAccount(element));
            }
        }
        return Optional.empty();
    }

    public synchronized void save(StoredUserAccount account) {
        Document document = readDocument();
        NodeList users = document.getDocumentElement().getElementsByTagName("user");
        for (int i = 0; i < users.getLength(); i++) {
            Element element = (Element) users.item(i);
            if (account.username().equals(element.getAttribute("username"))) {
                throw new IllegalStateException("Пользователь с username уже существует: " + account.username());
            }
        }

        Element user = document.createElement("user");
        user.setAttribute("username", account.username());
        user.setAttribute("passwordHash", account.passwordHash());
        user.setAttribute("role", account.role().name());
        user.setAttribute("participantId", String.valueOf(account.participantId()));
        document.getDocumentElement().appendChild(user);
        writeDocument(document);
    }

    public synchronized void reset() {
        writeDocument(createEmptyDocument());
    }

    private StoredUserAccount toAccount(Element element) {
        return new StoredUserAccount(
                element.getAttribute("username"),
                element.getAttribute("passwordHash"),
                UserRole.valueOf(element.getAttribute("role")),
                Long.parseLong(element.getAttribute("participantId"))
        );
    }

    private Document readDocument() {
        try {
            ensureStoreExists();
            try (InputStream inputStream = Files.newInputStream(storePath)) {
                Document document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(inputStream);
                document.getDocumentElement().normalize();
                return document;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось прочитать XML-хранилище пользователей", exception);
        }
    }

    private void writeDocument(Document document) {
        try {
            ensureStoreExists();
            Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            try (OutputStream outputStream = Files.newOutputStream(storePath)) {
                transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось записать XML-хранилище пользователей", exception);
        }
    }

    private void ensureStoreExists() throws Exception {
        Path parent = storePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(storePath)) {
            Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            try (OutputStream outputStream = Files.newOutputStream(storePath)) {
                transformer.transform(new DOMSource(createEmptyDocument()), new StreamResult(outputStream));
            }
        }
    }

    private Document createEmptyDocument() {
        try {
            Document document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
            document.appendChild(document.createElement("users"));
            return document;
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось создать XML-хранилище пользователей", exception);
        }
    }
}
