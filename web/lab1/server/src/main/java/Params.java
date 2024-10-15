import java.util.HashMap;

public class Params {
    public static HashMap<String, String> parse(String input) {
        HashMap<String, String> map = new HashMap<>();

        for (String line : input.split("&")) {
            String[] keyValue = line.split("=");
            if (keyValue.length > 1) {
                map.put(keyValue[0], keyValue[1]);
            } else {
                map.put(keyValue[0], "");
            }
        }

        return map;
    }
}
