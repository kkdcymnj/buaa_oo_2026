package components;

import tools.FuncManager;
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

    public FuncFactor(String funcName, ArrayList<Expr> realArgs, BigInteger exponent) {
        this.funcName = funcName;
        this.funcDef = FuncManager.getFuncDef(funcName);
        this.funcArgs = FuncManager.getFuncArgs(funcName);
        this.realArgs = realArgs;
        this.realArgStrings = new ArrayList<>();
        for (Expr arg : realArgs) {
            this.realArgStrings.add(arg.toPoly().toString());
        }
        this.exponent = exponent;
        this.convertedExpr = this.convert();
    }

    //TODO: 目前只能完成单字母参数的变换，后续要改进

    public Expr convert() {
        String temp = funcDef;
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
        return convertedExpr.toPoly();
    }

    @Override
    public String toString() {
        return convertedExpr.toString();
    }
}
