package tools;

import components.Expr;
import components.Poly;

public class StrPolyExchanger {
    public static Poly exchange(String exprStr, boolean changeSign) {
        Expr expr = strToExpr(exprStr);
        if (changeSign) {
            Poly poly = expr.toPoly();
            poly.polyNegate();
            return poly;
        } else {
            return expr.toPoly();
        }
    }

    public static Expr strToExpr(String exprStr) {
        String newExprStr = Processor.process(exprStr);
        Lexer lexer = new Lexer(newExprStr);
        Parser parser = new Parser(lexer);
        return parser.parseExpr();
    }
}
