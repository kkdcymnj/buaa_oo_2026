package components;

import tools.FuncManager;
import tools.Processor;
import tools.StrPolyExchanger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class FuncFactor implements Factor {
    private String funcName;
    private String funcDef;
    private ArrayList<String> funcArgs;
    private ArrayList<Expr> realArgs;
    private ArrayList<String> realArgStrings;
    private Expr convertedExpr;

    private BigInteger exponent;

    private Poly result;

    // 新增：嵌套函数层数，如果为-1，代表不是嵌套函数
    private int layer;

    public FuncFactor(String funcName, ArrayList<Expr> realArgs, BigInteger exponent) {
        this.funcName = funcName;
        this.funcDef = FuncManager.getFuncDef(funcName);
        this.funcArgs = FuncManager.getFuncArgs(funcName);
        this.realArgs = realArgs;
        this.exponent = exponent;
        this.layer = -1;
    }

    public FuncFactor(String funcDef, ArrayList<String> realArgStrings) {
        this.funcDef = funcDef;
        this.funcArgs = new ArrayList<>();
        this.funcArgs.add("x");     // 题目规定形参只可以是x
        this.realArgStrings = realArgStrings;
        this.exponent = BigInteger.ONE;
        this.layer = -1;
    }

    public FuncFactor(String funcName, int layer, ArrayList<Expr> realArgs) {
        this.funcName = funcName;
        this.layer = layer;
        this.funcArgs = new ArrayList<>();
        this.funcArgs.add("x");     // 题目规定形参只可以是x
        this.realArgs = realArgs;
        this.exponent = BigInteger.ONE;
    }

    //TODO: 目前只能完成单字母形式参数的变换，后续要改进

    public Expr convert() {
        String temp;
        if (layer == -1) {
            temp = funcDef;
        } else {
            temp = FuncManager.calcRecursiveFunc(funcName, layer);
        }

        // System.out.println("real args: " + realArgStrings);

        for (int i = 0; i < funcArgs.size(); i++) {
            String thisArg = funcArgs.get(i);
            String thisRealArg = realArgStrings.get(i);
            String pattern = "(?<![a-zA-Z])" + Pattern.quote(thisArg) + "(?![a-zA-Z])";
            temp = temp.replaceAll(pattern, "(" + thisRealArg + ")");
        }

        temp = "(" + temp + ")^" + exponent;

        return StrPolyExchanger.strToExpr(temp);
    }

    @Override
    public Poly toPoly() {
        if (realArgStrings == null) {
            realArgStrings = new ArrayList<>();
            for (Expr arg : realArgs) {
                realArgStrings.add(Processor.process(arg.toPoly().toString()));
            }
        }
        convertedExpr = convert();
        result = convertedExpr.toPoly();
        return result;
    }
}
