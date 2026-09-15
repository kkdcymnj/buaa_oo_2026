public class Processor {
    private static String inputStr;
    // private String output;

    public static String process(String input) {
        if (input == null) {
            return null;
        }

        String temp = input.replaceAll("[ \\t]", "");;
        String previous = temp;

        do {
            previous = temp;
            temp = temp.replaceAll("\\+\\-", "-");
            temp = temp.replaceAll("\\-\\+", "-");
            temp = temp.replaceAll("\\+\\+", "+");
            temp = temp.replaceAll("\\-\\-", "+");
            temp = temp.replaceAll("\\(\\+", "(");
        } while (!temp.equals(previous));

        return temp;
    }
}