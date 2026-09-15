import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        input = Processor.process(input);

        // System.out.println("Processed input: " + input);

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        // System.out.println("expr: " + expr);

        Poly exprPoly = expr.toPoly();
        // System.out.println(exprPoly.getMonos());
        String result = exprPoly.toString();

        if (result.charAt(0) == '+') {
            result = result.substring(1);
        }

        System.out.println(Processor.process(result));
    }
}
