public class Lexer {
    private String input;
    private String curToken;
    private int pos = 0;

    public Lexer(String input) {
        this.input = input;
        next();
    }

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }

        return sb.toString();
    }

    public void next() {
        if (pos == input.length()) {
            return;
        }

        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            curToken = getNumber();
        } else {
            switch (c) {
                case '+':
                    curToken = "+";
                    break;
                case '-':
                    curToken = "-";
                    break;
                case '*':
                    curToken = "*";
                    break;
                case '^':
                    curToken = "^";
                    break;
                case '(':
                    curToken = "(";
                    break;
                case ')':
                    curToken = ")";
                    break;
                case 'x':
                    curToken = "x";
                    break;
                default:
                    System.out.println("can't recognize " + c);
                    break;
            }
            pos++;
        }
    }

    public String peek() {
        return curToken;
    }
}
