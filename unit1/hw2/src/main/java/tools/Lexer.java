package tools;

import java.util.HashSet;

public class Lexer {
    private String input;
    private String curToken;
    private int pos = 0;

    private HashSet<Character> operators;

    private void initOperators() {
        operators = new HashSet<>();
        operators.add('+');
        operators.add('-');
        operators.add('*');
        operators.add('(');
        operators.add(')');
        operators.add('^');
        operators.add('[');
        operators.add(']');
        operators.add('=');
        operators.add('?');
        operators.add(':');
    }

    public Lexer(String input) {
        this.input = input;
        initOperators();
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

    private void getOperators(char c) {
        StringBuilder sb = new StringBuilder();
        sb.append(c);
        if (c == '=') {
            pos += 2;
            sb.append(c);
        } else {
            pos++;
        }
        curToken = sb.toString();
    }

    private boolean isOperator(char c) {
        return operators.contains(c);
    }

    public void next() {
        if (pos == input.length()) {
            return;
        }

        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            curToken = getNumber();
        } else if (isOperator(c)) {
            getOperators(c);
        } else {
            switch (c) {
                case 'x':
                    curToken = "x";
                    pos++;
                    break;
                case 'e':
                    curToken = "exp";
                    pos += 3;
                    break;
                case 'f':
                    curToken = "f";
                    pos++;
                    break;
                default:
                    System.out.println("can't recognize " + c);
                    break;
            }
        }
    }

    public String peek() {
        return curToken;
    }

    public int getPosition() {
        return pos;
    }
}
