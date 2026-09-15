package components;

// import tools.StrPolyExchanger;

import java.math.BigInteger;
import java.util.ArrayList;

public class Mono {
    private BigInteger coefficient;
    private PowerFactor powerFactorX;
    private PowerFactor powerFactorY;
    private ArrayList<ExpFactor> expFactors;
    private Poly expPoly;
    private boolean expFactorSimplified = false;

    public Mono(BigInteger coefficient,
                PowerFactor powerFactorX) {
        this.coefficient = coefficient;
        this.powerFactorX = powerFactorX;
        this.powerFactorY = new PowerFactor("y", BigInteger.ZERO);
        this.expFactors = new ArrayList<>();
    }

    public Mono(BigInteger coefficient,
                PowerFactor powerFactorX,
                PowerFactor powerFactorY) {
        this.coefficient = coefficient;
        this.powerFactorX = powerFactorX;
        this.powerFactorY = powerFactorY;
        this.expFactors = new ArrayList<>();
    }

    public Mono(BigInteger coefficient,
                PowerFactor powerFactorX,
                ArrayList<ExpFactor> expFactors) {
        this.coefficient = coefficient;
        this.powerFactorX = powerFactorX;
        this.powerFactorY = new PowerFactor("y", BigInteger.ZERO);
        this.expFactors = expFactors;
    }

    public Mono(BigInteger coefficient,
                PowerFactor powerFactorX,
                PowerFactor powerFactorY,
                ArrayList<ExpFactor> expFactors) {
        this.coefficient = coefficient;
        this.powerFactorX = powerFactorX;
        this.powerFactorY = powerFactorY;
        this.expFactors = expFactors;
    }

    public Mono deepCopy() {
        if (this == null) {
            return null;
        }

        BigInteger coefficientCopy = this.coefficient;
        PowerFactor powerFactorCopyX =
                new PowerFactor(this.powerFactorX.getVarName(),
                        this.powerFactorX.getExponent());
        PowerFactor powerFactorCopyY =
                new PowerFactor(this.powerFactorY.getVarName(),
                        this.powerFactorY.getExponent());
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
        return new Mono(coefficientCopy, powerFactorCopyX,
                powerFactorCopyY, expFactorsCopy);
    }

    public Poly expFactorsToPoly(BigInteger divisor) {
        Poly result = Poly.intToPoly(BigInteger.ZERO);
        for (ExpFactor expFactor : expFactors) {
            //拿到指数项
            BigInteger exponent = expFactor.getExponent().divide(divisor);
            if (exponent.compareTo(BigInteger.ZERO) == 0) {
                continue;
            }
            if (exponent.compareTo(BigInteger.ONE) != 0) {
                result.addPoly(expFactor.getPoly().multPoly(Poly.intToPoly(exponent)));
            } else {
                result.addPoly(expFactor.getPoly());
            }
        }
        return result;
    }

    public void shortExpFactors() {
        if (expFactorSimplified) {
            return;
        }

        expFactorSimplified = true;

        if (expFactors.isEmpty()) {
            this.expPoly = Poly.intToPoly(BigInteger.ZERO);
            return;
        }

        if (this.expPoly == null) {
            this.expPoly = this.expFactorsToPoly(BigInteger.ONE);
        }

        if (expPoly.judgeEqualToZero()) {
            this.expFactors = new ArrayList<>();
            return;
        }

        BigInteger gcd = expPoly.getMonoGcd();
        if (gcd.compareTo(BigInteger.ONE) == 0) {
            ArrayList<ExpFactor> newExpFactors = new ArrayList<>();
            newExpFactors.add(new ExpFactor(expPoly, BigInteger.ONE));
            this.expFactors = newExpFactors;
        } else {
            ArrayList<Mono> monos = expPoly.getMonos();
            Poly newPoly = new Poly();
            for (Mono mono : monos) {
                Mono newMono = mono.deepCopy();
                newMono.setCoefficient(newMono.getCoefficient().divide(gcd));
                newPoly.addMono(newMono);
            }
            ArrayList<ExpFactor> newExpFactors = new ArrayList<>();
            newExpFactors.add(new ExpFactor(newPoly, gcd));
            this.expFactors = newExpFactors;
        }
    }

    public boolean equalInExponents(Mono mono) {
        return this.getExponentX().equals(mono.getExponentX()) &&
                this.getExponentY().equals(mono.getExponentY());
    }

    public boolean noPowerFactors() {
        return this.powerFactorX.getExponent().equals(BigInteger.ZERO) &&
                this.powerFactorY.getExponent().equals(BigInteger.ZERO);
    }

    public boolean isAddable(Mono mono) {
        // 是同类项的条件1：幂函数次数相等，如果不相等，没有判定exp项的必要
        boolean flag1 = this.equalInExponents(mono);
        if (!flag1) {
            return false;
        }

        //防止类似exp(0)==1被误判
        this.shortExpFactors();
        mono.shortExpFactors();

        if (this.getExpFactors().isEmpty()) {
            return mono.getExpFactors().isEmpty();
        } else {
            if (mono.getExpFactors().isEmpty()) {
                return false;
            }
        }

        // 是同类项的条件2：提取全部指数中的“表达式”，计算来看二者是一样的
        return Poly.judgeEqualPoly(this.getExpPoly(), mono.getExpPoly());
    }

    public void setCoefficient(BigInteger coefficient) {
        this.coefficient = coefficient;
    }

    public BigInteger getCoefficient() {
        return this.coefficient;
    }

    public BigInteger getExponentX() {
        return this.powerFactorX.getExponent();
    }

    public BigInteger getExponentY() {
        return this.powerFactorY.getExponent();
    }

    public PowerFactor getPowerFactorX() {
        return this.powerFactorX;
    }

    public PowerFactor getPowerFactorY() {
        return powerFactorY;
    }

    public ArrayList<ExpFactor> getExpFactors() {
        return this.expFactors;
    }

    public Poly getExpPoly() {
        return expPoly;
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

        // 只有系数，没有幂函数和exp
        final boolean allNotExist = this.hasOnlyCoeff();

        // exp项的字符串
        StringBuilder expStr = new StringBuilder();
        for (ExpFactor expFactor : expFactors) {
            expStr.append(expFactor.toString()).append("*");
        }

        // pow项的字符串
        StringBuilder powStr = new StringBuilder();
        if (!this.noPowerFactors()) {
            if (this.getExponentX().compareTo(BigInteger.ZERO) != 0) {
                powStr.append(powerFactorX.toString());
            }
            if (this.getExponentY().compareTo(BigInteger.ZERO) != 0) {
                if (powStr.length() > 0) {
                    powStr.append("*");
                }
                powStr.append(powerFactorY.toString());
            }
        }

        StringBuilder sb = new StringBuilder();

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
            if (powStr.length() > 0) {
                sb.append(powStr).append("*").append(expStr);
            } else {
                sb.append(expStr);
            }
        }

        return sb.substring(0, sb.length() - 1);
    }

    // 只包含常数项的判定，这样可以简化输出
    public boolean hasOnlyCoeff() {
        return noPowerFactors() && expFactors.isEmpty();
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

    public Poly derive(String varName) {
        final Poly coeffPoly = Poly.intToPoly(coefficient);
        final Poly powXPoly = powerFactorX.toPoly();
        final Poly powYPoly = powerFactorY.toPoly();
        this.shortExpFactors();
        Poly expPoly = Poly.intToPoly(BigInteger.ONE);
        ExpFactor expFactor = new ExpFactor(Poly.intToPoly(BigInteger.ZERO), BigInteger.ZERO);
        if (!expFactors.isEmpty()) {
            expFactor = expFactors.get(0);
            expPoly = expFactor.toPoly();
        }

        Poly innerPolyA;
        Poly innerPolyB;
        Poly innerPolyC;
        if (expFactors.isEmpty()) {
            innerPolyA = powerFactorX.derive(varName).multPoly(powYPoly);
            innerPolyB = powXPoly.multPoly(powerFactorY.derive(varName));
            innerPolyC = Poly.intToPoly(BigInteger.ZERO);
        } else {
            innerPolyA = powerFactorX.derive(varName).multPoly(powYPoly).multPoly(expPoly);
            innerPolyB = powXPoly.multPoly(powerFactorY.derive(varName)).multPoly(expPoly);
            innerPolyC = powXPoly.multPoly(powYPoly).multPoly(expFactor.derive(varName));
        }

        // System.out.println("innerPolyA: " + innerPolyA);
        // System.out.println("innerPolyB: " + innerPolyB);
        // System.out.println("innerPolyC: " + innerPolyC);

        return coeffPoly.multPoly(innerPolyA.addPoly(innerPolyB).addPoly(innerPolyC));
    }
}
