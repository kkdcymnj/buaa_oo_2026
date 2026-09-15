package tools;

import components.Expr;
import components.Poly;

import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        // 读入自定义函数定义
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        scanner.nextLine();
        for (int i = 0; i < n; i++) {
            String line = scanner.nextLine();
            FuncManager.addFunc(Processor.process(line));
        }


        // 读入递归函数定义
        int m = scanner.nextInt();
        scanner.nextLine();
        for (int i = 0; i < m * 3; i++) {
            String line = scanner.nextLine();
            if (i < 2) {
                FuncManager.initRecursiveFunc(Processor.process(line));
                continue;
            }
            FuncManager.parseRecursiveFunc(Processor.process(line));
        }

        // 读入最终表达式
        String input = scanner.nextLine();
        Expr expr = StrPolyExchanger.strToExpr(input);
        Poly exprPoly = expr.toPoly();
        String result = exprPoly.toString();
        if (result.charAt(0) == '+') {
            result = result.substring(1);
        }

        // 答案输出
        System.out.println(Processor.process(result));
    }
}
