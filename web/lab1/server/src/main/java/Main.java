import com.fastcgi.FCGIInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        while (new FCGIInterface().FCGIaccept() >= 0) {
            try {
                HashMap<String, String> params = Params.parse(FCGIInterface.request.params.getProperty("QUERY_STRING"));
                Float x = Float.parseFloat(params.get("x"));
                Float y = Float.parseFloat(params.get("y"));
                Float r = Float.parseFloat(params.get("r"));

                var validation = new Validation(x, y, r);
                if (validation.valid()) {
                    var checker = new Checker(x, y, r);
                    sendResponse(String.format("{\"hit\": %b}", checker.check()));
                    logger.info("{} success", checker.toString());
                } else {
                    sendResponse("{\"error\": \"invalid data\"}");
                    logger.error("invalid data");
                }
            } catch (NumberFormatException e) {
                sendResponse("{\"error\": \"wrong query param type\"}");
                logger.error(e.getMessage());
            } catch (NullPointerException e) {
                sendResponse(String.format("{\"error\": \"missed necessary query param\"}"));
                logger.error(e.getMessage());
            } catch (Exception e) {
                sendResponse(String.format("{\"error\": %s}", e));
                logger.error(e.getMessage());
            }
        }
    }

    private static void sendResponse(String response) {
        System.out.println(String.format("Content-Type: application/json\n" +
                "Content-Length: %d\n\n%s", response.getBytes(StandardCharsets.UTF_8).length, response));
    }
}
