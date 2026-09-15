package tools;

import java.util.ArrayList;
import java.util.HashMap;

public class FuncManager {
    private static HashMap<String, String> funcMap = new HashMap<>();
    private static HashMap<String, ArrayList<String>> argMap = new HashMap<>();

    public static void addFunc(String input) {
        String funcName = extractFuncName(input);
        String funcDef = extractFuncDef(input);
        String simpleFuncDef =
                StrPolyExchanger.exchange(funcDef, false).toString();
        funcMap.put(funcName, simpleFuncDef);
        //funcMap.put(funcName, funcDef);
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
        StringBuilder sb = new StringBuilder();
        sb.append(input.charAt(0));
        return sb.toString();
    }

    public static String extractFuncDef(String input) {
        String def = input.split("=")[1];
        def = Processor.process(def);
        return def;
    }

    public static ArrayList<String> extractFuncArgs(String input) {
        ArrayList<String> args = new ArrayList<>();
        for (int i = 2; input.charAt(i) != ')'; i++) {
            if (input.charAt(i) == 'x') {
                args.add(String.valueOf(input.charAt(i)));
            }
        }
        return args;
    }
}
