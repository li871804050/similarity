package work;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern whitespacePattern = Pattern.compile("\\s+");
    private static final Pattern caratPattern = Pattern.compile("\\^");

    static String stripBoost(String query) {
        // woman^2 bag^3 => woman bag
        if (query != null) {
            String[] words = query.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                String[] parts = word.split("\\^");
                sb.append(parts[0]).append(" ");
            }
            return sb.toString();
        }
        return "";
    }

    public static Map<String, Float> parseQueryBoosts(String query) {
        if (null == query || query.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Float> out = new HashMap<>(7);
        String[] bb = whitespacePattern.split(query.trim());
        for (String s : bb) {
            String[] bbb = caratPattern.split(s);
            out.put(bbb[0], 1 == bbb.length ? 1 : Float.valueOf(bbb[1]));
        }
        return out;
    }

    public static void main(String args[]) {
        System.out.println(stripBoost("woman^2 bag^3"));
        System.out.println(stripBoost("query"));
        System.out.println(stripBoost("bag^3 for men^2"));
    }

}
