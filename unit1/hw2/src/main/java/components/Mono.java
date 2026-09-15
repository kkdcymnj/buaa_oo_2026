package components;

// import tools.StrPolyExchanger;

import java.math.BigInteger;
import java.util.ArrayList;

public class Mono {
    private BigInteger coefficient;
    private PowerFactor powerFactor;
    private ArrayList<ExpFactor> expFactors;

    public Mono(BigInteger coefficient, PowerFactor powerFactor) {
        this.coefficient = coefficient;
        this.powerFactor = powerFactor;
        this.expFactors = new ArrayList<>();
    }

    public Mono(BigInteger coefficient, PowerFactor powerFactor,
                ArrayList<ExpFactor> expFactors) {
        this.coefficient = coefficient;
        this.powerFactor = powerFactor;
        this.expFactors = expFactors;
    }

    public Mono deepCopy() {
        if (this == null) {
            return null;
        }

        BigInteger coefficientCopy = this.coefficient;
        PowerFactor powerFactorCopy =
                new PowerFactor(this.powerFactor.getVarName(),
                        this.powerFactor.getExponent());
        ArrayList<ExpFactor> expFactorsCopy = new ArrayList<>();
        for (ExpFactor expFactor : this.expFactors) {
            expFactorsCopy.add(
                    new ExpFactor(
                            expFactor.getExpression(),
                            expFactor.getExponent(),
                            expFactor.getPoly()
                    )
            );
        }
        return new Mono(coefficientCopy, powerFactorCopy, expFactorsCopy);
    }

    public Poly expFactorsToPoly(BigInteger divisor) {
        Mono mono = new Mono(BigInteger.ZERO, new PowerFactor("x", BigInteger.ZERO));
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(mono);
        Poly result = new Poly(monos);
        Poly temp = new Poly();

        for (ExpFactor expFactor : expFactors) {
            //拿到指数项
            BigInteger exponent = expFactor.getExponent().divide(divisor);
            Mono expMono = new Mono(exponent, new PowerFactor("x", BigInteger.ZERO));
            ArrayList<Mono> expMonos = new ArrayList<>();
            expMonos.add(expMono);
            Poly expTemp = new Poly(expMonos);

            temp = expFactor.getPoly().multPoly(expTemp);
            result.addPoly(temp);
        }

        return result;
    }

    public void shortExpFactors() {
        if (expFactors.isEmpty()) {
            return;
        }

        Poly exprPoly = this.expFactorsToPoly(BigInteger.ONE);
        if (exprPoly.judgeEqualToZero()) {
            this.expFactors = new ArrayList<>();
            return;
        }

        BigInteger gcd = exprPoly.getMonoGcd();
        ArrayList<Mono> monos = exprPoly.getMonos();
        for (Mono mono : monos) {
            BigInteger coeffTemp = mono.getCoefficient();
            mono.setCoefficient(coeffTemp.divide(gcd));
        }

        ArrayList<ExpFactor> newExpFactors = new ArrayList<>();
        newExpFactors.add(new ExpFactor(exprPoly, gcd));
        this.expFactors = newExpFactors;
    }

    public boolean isAddable(Mono mono) {
        // 先对两个对象进行深克隆
        Mono thisCopy = this.deepCopy();
        Mono monoCopy = mono.deepCopy();

        //防止类似exp(0)==1被误判
        thisCopy.shortExpFactors();
        monoCopy.shortExpFactors();

        if (thisCopy.getExpFactors().isEmpty()) {
            if (monoCopy.getExpFactors().isEmpty()) {
                return (thisCopy.getExponent().compareTo(monoCopy.getExponent()) == 0);
            } else {
                return false;
            }
        } else {
            if (monoCopy.getExpFactors().isEmpty()) {
                return false;
            }
        }

        // 是同类项的条件1：幂函数次数相等
        boolean flag1 = (thisCopy.getExponent().compareTo(monoCopy.getExponent()) == 0);
        if (!flag1) {
            return false;
        }

        // 是同类项的条件2：提取全部指数中的“表达式”，计算来看二者是一样的
        Poly exp1Poly = thisCopy.expFactorsToPoly(BigInteger.ONE);
        Poly exp2Poly = monoCopy.expFactorsToPoly(BigInteger.ONE);
        // System.out.println("[poly1, poly2] : " + exp1Poly + "," + exp2Poly);

        return Poly.judgeEqualPoly(exp1Poly, exp2Poly);
    }

    public void setCoefficient(BigInteger coefficient) {
        this.coefficient = coefficient;
    }

    public BigInteger getCoefficient() {
        return this.coefficient;
    }

    public BigInteger getExponent() {
        return this.powerFactor.getExponent();
    }

    public PowerFactor getPowerFactor() {
        return this.powerFactor;
    }

    public ArrayList<ExpFactor> getExpFactors() {
        return this.expFactors;
    }

    public Mono mergeExpFactors() {
        if (expFactors.isEmpty()) {
            return this;
        }
        // 合并同类项
        ArrayList<ExpFactor> simplified = new ArrayList<>();
        for (ExpFactor current : expFactors) {
            // System.out.println(current.getPoly());
            boolean flag = false;
            for (ExpFactor existing : simplified) {
                // System.out.println("existing: " + existing.getPoly());
                if (current.judgeSameExpFactor(existing)) {
                    existing.setExponent(existing.getExponent().add(current.getExponent()));
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                ExpFactor newFactor =
                        new ExpFactor(
                                current.getExpression(), current.getExponent(),
                                current.getPoly()
                        );
                simplified.add(newFactor);
            }
            // System.out.println(simplified);
        }
        simplified.
                removeIf(expFactor ->
                        expFactor.getExponent().compareTo(BigInteger.ZERO) == 0);

        this.expFactors = simplified;
        return this;
    }

    @Override
    public String toString() {
        // 系数为0不用输出
        if (coefficient.compareTo(BigInteger.ZERO) == 0) {
            return "";
        }
        if (!expFactors.isEmpty()) {
            this.shortExpFactors();
        }

        final boolean allNotExist = this.hasOnlyCoeff();
        final BigInteger powerExp = powerFactor.getExponent();

        StringBuilder sb = new StringBuilder();

        final String powerStr = powerFactor.toString();
        StringBuilder expStr = new StringBuilder();

        for (ExpFactor expFactor : expFactors) {
            expStr.append(expFactor.toString()).append("*");
        }

        // 处理正系数
        if (coefficient.compareTo(BigInteger.ZERO) > 0) {
            // 正系数为1时省略1
            if (coefficient.compareTo(BigInteger.ONE) == 0) {
                if (allNotExist) {
                    return "+1";
                }
                sb.append("+");
            } else {
                sb.append("+").append(coefficient).append("*");
            }
        }
        // 处理负系数
        else {
            // 负系数为-1时只输出负号
            if (coefficient.compareTo(BigInteger.valueOf(-1)) == 0) {
                if (allNotExist) {
                    return "-1";
                }
                sb.append("-");
            } else {
                sb.append(coefficient).append("*");
            }
        }

        // 构建剩余部分
        if (!allNotExist) {
            if (powerExp.compareTo(BigInteger.ZERO) != 0) {
                sb.append(powerStr).append("*").append(expStr);
            } else {
                sb.append(expStr);
            }
        }

        return sb.substring(0, sb.length() - 1);
    }

    // 只包含常数项的判定，这样可以简化输出
    public boolean hasOnlyCoeff() {
        BigInteger powerExp = powerFactor.getExponent();
        return powerExp.compareTo(BigInteger.ZERO) == 0 && expFactors.isEmpty();
    }

    public void removeZeroExpFactors() {
        if (expFactors.isEmpty()) {
            return;
        }

        ArrayList<ExpFactor> newExpFactors = new ArrayList<>();
        for (ExpFactor expFactor : expFactors) {
            if (!expFactor.getPoly().judgeEqualToZero()) {
                newExpFactors.add(expFactor);
            }
        }
        expFactors = newExpFactors;
    }
}
