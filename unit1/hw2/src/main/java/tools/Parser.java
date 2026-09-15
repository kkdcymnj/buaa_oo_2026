package tools;

import components.Expr;
import components.Term;
import components.Factor;
import components.Num;
import components.ExpFactor;
import components.FuncFactor;
import components.SelectFactor;
import components.PowerFactor;

import java.math.BigInteger;
import java.util.ArrayList;

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
        // System.out.println("sign: " + sign + " term: " + term);
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
        } else if (token.equals("[")) {
            return parseSelectFactor();
        }
        else if (token.equals("+") || token.equals("-")
                || Character.isDigit(token.charAt(0))) {
            return parseNum();
        } else if (token.equals("x")) {
            return parsePowerFactor();
        } else if (token.equals("exp")) {
            return parseExpFactor();
        } else if (token.equals("f")) {
            return parseFuncFactor("f");
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

    public FuncFactor parseFuncFactor(String funcName) {
        lexer.next();
        lexer.next();
        Expr realArg = parseExpr();
        lexer.next();
        BigInteger exponent = BigInteger.ONE;
        if (lexer.peek().equals("^")) {
            exponent = parseExponent();
        }
        ArrayList<Expr> realArgs = new ArrayList<Expr>();
        realArgs.add(realArg);
        return new FuncFactor(funcName, realArgs, exponent);
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

    public ExpFactor parseExpFactor() {
        lexer.next();
        lexer.next();      //跳过exp和最外层左括号
        Factor expr = parseFactor();
        lexer.next();
        BigInteger exponent = BigInteger.ONE;
        if (lexer.peek().equals("^")) {
            exponent = parseExponent();
        }
        /*System.out.println("exp_factor inter: " + expr);
        System.out.println("position: " + lexer.getPosition());*/
        return new ExpFactor(expr, exponent);
    }

    public SelectFactor parseSelectFactor() {
        lexer.next();   //跳过左中括号
        lexer.next();   //跳过左小括号
        final Factor exprA;
        exprA = parseExpr();
        lexer.next();
        final Factor exprB;
        exprB = parseExpr();
        do {
            lexer.next();
        } while (!lexer.peek().equals("?"));
        lexer.next();
        final Factor exprC;
        exprC = parseExpr();
        lexer.next();
        final Factor exprD;
        exprD = parseExpr();
        lexer.next();
        return new SelectFactor(exprA, exprB, exprC, exprD);
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
