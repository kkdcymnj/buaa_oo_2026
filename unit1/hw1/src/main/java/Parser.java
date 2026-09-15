import java.math.BigInteger;

public class Parser {
    private Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        char sign = '+';
        Expr expr = new Expr();
        if (lexer.peek().equals("-")) {
            sign = '-';
            lexer.next();
        } else if (lexer.peek().equals("+")) {
            lexer.next();
        }
        expr.addTerm(parseTerm(sign));

        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            if (lexer.peek().equals("+")) {
                sign = '+';
            } else {
                sign = '-';
            }

            lexer.next();
            expr.addTerm(parseTerm(sign));
        }

        return expr;
    }

    public Term parseTerm(char sign) {
        Term term = new Term(sign);
        term.addFactor(parseFactor());
        while (lexer.peek().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor());
        }
        return term;
    }

    public Factor parseFactor() {
        String token = lexer.peek();
        if (token.equals("(")) {
            lexer.next();
            Expr expr = parseExpr();
            lexer.next();
            // 还要设定幂次
            BigInteger exponent = BigInteger.ONE;
            if (lexer.peek().equals("^")) {
                exponent = parseExponent();
            }
            expr.setExponent(exponent);
            return expr;
        } else if (token.equals("+") || token.equals("-")
                || Character.isDigit(token.charAt(0))) {
            return parseNum();
        } else if (Character.isLetter(token.charAt(0))) {
            return parsePowerFactor();
        } else {
            return new Num(BigInteger.ZERO);
        }
    }

    public Num parseNum() {
        char sign = '+';
        String numStr;
        BigInteger numInt;
        if (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            if (lexer.peek().equals("-")) {
                sign = '-';
            }
            lexer.next();
            numStr = lexer.peek();
        } else {
            numStr = lexer.peek();
        }
        lexer.next();
        numInt = new BigInteger(sign + numStr);
        return new Num(numInt);
    }

    public PowerFactor parsePowerFactor() {
        String varName = lexer.peek();
        lexer.next();
        BigInteger exponent = BigInteger.ONE;
        if (lexer.peek().equals("^")) {
            exponent = parseExponent();
        }
        return new PowerFactor(varName, exponent);
    }

    public BigInteger parseExponent() {
        BigInteger exponent = BigInteger.ONE;
        lexer.next();
        if (lexer.peek().equals("+")) {
            lexer.next();
        }
        exponent = new BigInteger(lexer.peek());
        lexer.next();
        return exponent;
    }
}
