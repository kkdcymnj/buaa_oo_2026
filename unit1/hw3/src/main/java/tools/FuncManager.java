package tools;

import components.FuncFactor;
import components.Poly;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class FuncManager {
    private static HashMap<String, String> funcMap = new HashMap<>();
    private static HashMap<String, ArrayList<String>> argMap = new HashMap<>();

    private static HashMap<HashMap<String, Integer>, String> recursiveFuncMap = new HashMap<>();
    private static BigInteger recursiveCoeffA;
    private static BigInteger recursiveCoeffB;
    private static String recursiveArgA;
    private static String recursiveArgB;
    private static String recursiveAlterExpr;

    private static int pos;

    private static Poly funAPoly;
    private static Poly funBPoly;
    private static Poly coeAPoly;
    private static Poly coeBPoly;
    private static Poly alterPoly;

    public static void addFunc(String input) {
        String funcName = extractFuncName(input);
        String funcDef = extractFuncDef(input);
        funcMap.put(funcName, funcDef);
        ArrayList<String> args = extractFuncArgs(input);
        argMap.put(funcName, args);
    }

    public static String getFuncDef(String funcName) {
        return funcMap.get(funcName);
    }

    public static ArrayList<String> getFuncArgs(String funcName) {
        return argMap.get(funcName);
    }

    public static String extractFuncName(String input) {
        return String.valueOf(input.charAt(0));
    }

    public static String extractFuncDef(String input) {
        String def = input.split("=")[1];
        def = Processor.process(def);
        def = StrPolyExchanger.exchange(def, false).toString();
        def = Processor.process(def);
        return def;
    }

    public static ArrayList<String> extractFuncArgs(String input) {
        ArrayList<String> args = new ArrayList<>();
        for (int i = input.indexOf('('); input.charAt(i) != ')'; i++) {
            if (input.charAt(i) == 'x' || input.charAt(i) == 'y') {
                args.add(String.valueOf(input.charAt(i)));
            }
        }
        return args;
    }

    // 有关递推函数
    public static Integer extractFuncLayer(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = input.indexOf('{'); input.charAt(i) != '}'; i++) {
            if (Character.isDigit(input.charAt(i))) {
                sb.append(input.charAt(i));
            }
        }
        return Integer.parseInt(sb.toString());
    }

    public static void initRecursiveFunc(String input) {
        // 输入预处理过后的input
        HashMap<String, Integer> key = new HashMap<>();
        key.put(extractFuncName(input), extractFuncLayer(input));
        recursiveFuncMap.put(key, extractFuncDef(input));
    }

    // 计算递归函数的定义式，而非代入实参的结果
    public static String calcRecursiveFunc(String funcName, int layer) {
        HashMap<String, Integer> key = new HashMap<>();
        key.put(funcName, layer);
        if (recursiveFuncMap.containsKey(key)) {
            return recursiveFuncMap.get(key);   // 如果有就不再计算
        }

        // 寻找已经计算过的最大阶数
        int maxLayer = 0;
        for (HashMap<String, Integer> hashKey : recursiveFuncMap.keySet()) {
            maxLayer = Math.max(maxLayer, hashKey.get(funcName));
        }
        // System.out.println("current max layer: " + maxLayer);

        // 递归计算到所需阶数
        for (int i = maxLayer + 1; i <= layer; i++) {
            final Poly coeAPoly = Poly.intToPoly(recursiveCoeffA);
            final Poly coeBPoly = Poly.intToPoly(recursiveCoeffB);
            final Poly alterPoly = StrPolyExchanger.exchange(recursiveAlterExpr, false);

            HashMap<String, Integer> funcKey = new HashMap<>();
            funcKey.put(funcName, i - 1);
            final String funcDefA = recursiveFuncMap.get(funcKey);
            ArrayList<String> argList = new ArrayList<>();
            argList.add(recursiveArgA);
            final Poly funAPoly = new FuncFactor(funcDefA, argList).toPoly();

            funcKey.clear();
            funcKey.put(funcName, i - 2);
            final String funcDefB = recursiveFuncMap.get(funcKey);
            argList.clear();
            argList.add(recursiveArgB);
            final Poly funBPoly = new FuncFactor(funcDefB, argList).toPoly();

            funcKey.clear();
            funcKey.put(funcName, i);
            String funcDef =
                    coeAPoly.multPoly(funAPoly).
                            addPoly(coeBPoly.multPoly(funBPoly)).
                            addPoly(alterPoly).toString();
            recursiveFuncMap.put(funcKey, Processor.process(funcDef));
        }

        // 拿取结果
        return recursiveFuncMap.get(key);
    }

    // 解析含有f{n}，f{n-1}，f{n-2}的定义式
    public static void parseRecursiveFunc(String input) {
        // 输入预处理过后的input

        // 解析f{n-1}的常数系数/实参
        pos = input.indexOf('=') + 1;
        parseCoeAndArg(input, 1);

        // 解析f{n-2}的常数系数/实参
        parseCoeAndArg(input, 2);

        // 解析最后附加的表达式
        if (pos != input.length()) {
            recursiveAlterExpr = StrPolyExchanger.exchange(input.substring(pos), false).toString();
            recursiveAlterExpr = Processor.process(recursiveAlterExpr);
        } else {
            recursiveAlterExpr = "0";
        }

        // 调试：输出解析信息
        // parseInfo();
    }

    public static void parseCoeAndArg(String input, int node) {
        final BigInteger Coe = parseCoefficient(input);

        while (input.charAt(pos) != '(') {
            pos++;
        }
        pos++;
        String arg = StrPolyExchanger.exchange(parseRealArg(input), false).toString();
        arg = Processor.process(arg);
        pos++;

        if (node == 1) {
            recursiveCoeffA = Coe;
            recursiveArgA = arg;
        } else {
            recursiveCoeffB = Coe;
            recursiveArgB = arg;
        }
    }

    public static BigInteger parseCoefficient(String input) {
        StringBuilder sb = new StringBuilder();
        if (input.charAt(pos) == '+' || input.charAt(pos) == '-') {
            sb.append(input.charAt(pos));
            pos++;
        }
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        if (sb.length() > 0) {
            return new BigInteger(sb.toString());
        } else {
            return BigInteger.ONE;
        }
    }

    public static String parseRealArg(String input) {
        StringBuilder sb = new StringBuilder();
        int count = 1;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
                if (count == 0) {
                    break;
                }
            }
            sb.append(c);
            pos++;
        }
        return sb.toString();
    }

    public static void parseInfo() {
        System.out.println("Information-----------------------");
        System.out.println("coefficientA: " + recursiveCoeffA);
        System.out.println("argA: " + recursiveArgA);
        System.out.println("coefficientB: " + recursiveCoeffB);
        System.out.println("argB: " + recursiveArgB);
        System.out.println("exprStr: " + recursiveAlterExpr);
    }
}
