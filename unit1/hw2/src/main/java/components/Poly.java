package components;

// import tools.StrPolyExchanger;

import java.math.BigInteger;
import java.util.ArrayList;

public class Poly {
    private ArrayList<Mono> monos;

    public Poly(ArrayList<Mono> monos) {
        this.monos = monos;
    }

    public Poly() {
        this.monos = new ArrayList<>();
    }

    public ArrayList<Mono> getMonos() {
        return monos;
    }

    public void addMono(Mono m) {
        monos.add(m);
    }

    /*public static boolean judgeEqualExpr(String expr1, String expr2) {
        Poly poly1 = StrPolyExchanger.exchange(expr1, false);
        Poly poly2 = StrPolyExchanger.exchange(expr2, true);
        return poly1.addPoly(poly2).judgeEqualToZero();
    }*/

    public static boolean judgeEqualPoly(Poly p1, Poly p2) {
        // 复制一个p1
        ArrayList<Mono> monosCopyA = new ArrayList<>();
        for (Mono m : p1.getMonos()) {
            monosCopyA.add(m.deepCopy());
        }
        Poly p1Copy = new Poly(monosCopyA);

        // 复制一个p2
        ArrayList<Mono> monosCopyB = new ArrayList<>();
        for (Mono m : p2.getMonos()) {
            monosCopyB.add(m.deepCopy());
        }
        Poly p2Copy = new Poly(monosCopyB);
        p2Copy.polyNegate();
        Poly result = p1Copy.addPoly(p2Copy);
        return result.getMonos().isEmpty();
    }

    public Poly addPoly(Poly poly) {
        ArrayList<Mono> hisMonos = poly.getMonos();
        for (Mono hisMono : hisMonos) {
            // 合并同类项
            boolean flag = false;
            for (Mono myMono : monos) {
                if (hisMono.isAddable(myMono)) {
                    flag = true;
                    myMono.setCoefficient(
                            myMono.getCoefficient().add(hisMono.getCoefficient())
                    );
                    break;
                }
            }
            if (!flag) {
                monos.add(hisMono);
            }
        }
        this.removeZeroCoeffMonos();
        return this;
    }

    public Poly multPoly(Poly poly) {
        Poly result = new Poly(new ArrayList<>());
        ArrayList<Mono> hisMonos = poly.getMonos();

        this.removeZeroCoeffMonos();
        poly.removeZeroCoeffMonos();

        for (Mono monoA : monos) {
            for (Mono monoB : hisMonos) {
                // 幂函数部分的相乘
                BigInteger newCoefficient =
                        monoA.getCoefficient().multiply(monoB.getCoefficient());

                if (newCoefficient.compareTo(BigInteger.ZERO) == 0) {
                    continue;
                }

                BigInteger newExponent =
                        monoA.getExponent().add(monoB.getExponent());
                PowerFactor newPowerFactor =
                        new PowerFactor("x", newExponent);

                // 重要：创建新的 ArrayList，避免修改原始对象的列表
                ArrayList<ExpFactor> newExpFactors = new ArrayList<>();

                // 深拷贝 monoA 的指数因子
                for (ExpFactor ef : monoA.getExpFactors()) {
                    newExpFactors.add(
                            new ExpFactor(ef.getExpression(), ef.getExponent(),
                                    ef.getPoly())
                    );
                }
                // 深拷贝 monoB 的指数因子
                for (ExpFactor ef : monoB.getExpFactors()) {
                    newExpFactors.add(
                            new ExpFactor(ef.getExpression(), ef.getExponent(),
                                    ef.getPoly())
                    );
                }

                // 添加非零新项
                Mono newMono = new Mono(newCoefficient, newPowerFactor, newExpFactors);
                ArrayList<Mono> newMonos = new ArrayList<>();
                newMonos.add(newMono.mergeExpFactors());
                result.addPoly(new Poly(newMonos));
                /*result.addMono(newMono);*/
            }
        }
        return result;
    }

    public Poly powerPoly(BigInteger exponent) {
        if (exponent.compareTo(BigInteger.ZERO) == 0) {
            PowerFactor newPowerFactor = new PowerFactor("x", BigInteger.ZERO);
            Mono newMono = new Mono(BigInteger.ONE, newPowerFactor);
            ArrayList<Mono> newMonos = new ArrayList<>();
            newMonos.add(newMono);
            return new Poly(newMonos);
        }

        // 创建原始多项式的深拷贝
        Poly base = new Poly();
        for (Mono mono : this.monos) {
            // 创建新的Mono对象，避免共享引用
            Mono newMono = new Mono(
                    mono.getCoefficient(),
                    new PowerFactor("x", mono.getExponent()),
                    new ArrayList<>(mono.getExpFactors())
            );
            base.addMono(newMono);
        }

        Poly result = new Poly();
        for (Mono mono : base.getMonos()) {
            Mono newMono = new Mono(
                    mono.getCoefficient(),
                    new PowerFactor("x", mono.getExponent()),
                    new ArrayList<>(mono.getExpFactors())
            );
            result.addMono(newMono);
        }

        int intExponent = exponent.intValue();
        for (int i = 1; i < intExponent; i++) {
            result = result.multPoly(base);  // 使用base（原始多项式的副本）进行乘法
        }
        return result;
    }

    public void removeZeroCoeffMonos() {
        monos.removeIf(mono ->
                mono.getCoefficient().compareTo(BigInteger.ZERO) == 0);
    }

    public Poly simplifyPoly() {
        // 合并同类项
        ArrayList<Mono> mergedMonos = new ArrayList<>();
        this.removeZeroCoeffMonos();
        for (Mono currentMono : monos) {
            boolean flag = false;
            for (Mono mergedMono : mergedMonos) {
                if (currentMono.isAddable(mergedMono)) {
                    BigInteger newCoefficient = mergedMono.getCoefficient()
                            .add(currentMono.getCoefficient());
                    mergedMono.setCoefficient(newCoefficient);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                Mono newMono = new Mono(
                        currentMono.getCoefficient(),
                        currentMono.getPowerFactor(),
                        new ArrayList<>(currentMono.getExpFactors())
                );
                mergedMonos.add(newMono);
            }
        }
        this.monos = mergedMonos;
        return this;
    }

    public void polyNegate() {
        for (Mono mono : monos) {
            mono.setCoefficient(mono.getCoefficient().negate());
        }
    }

    public boolean judgeEqualToZero() {
        this.simplifyPoly();
        return monos.isEmpty();
    }

    //判定这个多项式是不是只含有一项mono，且这个mono只含常数/1*幂函数/1*指数函数 -> 优化exp输出中多的一层括号
    public boolean judgeSingularPoly() {
        boolean flag = false;
        this.simplifyPoly();

        if (monos.isEmpty()) {
            flag = true;
        }
        if (monos.size() == 1) {
            for (Mono mono : monos) {
                //只包含常数
                if (mono.hasOnlyCoeff()) {
                    flag = true;
                    break;
                }

                //系数为1
                if (mono.getCoefficient().compareTo(BigInteger.ONE) == 0) {
                    // 如果含有幂函数项
                    if (mono.getExponent().compareTo(BigInteger.ZERO) != 0) {
                        flag = (mono.getExpFactors().isEmpty());
                        break;
                    } else {
                        flag = (mono.getExpFactors().size() == 1);
                        break;
                    }
                }
            }
        }
        return flag;
    }

    public BigInteger getMonoGcd() {
        // 特判：exp内只有数字，那就一定是最简的
        if (monos.size() == 1 && monos.get(0).hasOnlyCoeff()) {
            return BigInteger.ONE;
        }

        BigInteger result = monos.get(0).getCoefficient().abs();

        for (int i = 1; i < monos.size(); i++) {
            result = result.gcd(monos.get(i).getCoefficient());
        }

        return result;
    }

    // 正项提前 - 按照系数值排序多项式项
    public void positiveToFront() {
        // 将多项式项分为正系数和负系数两组
        ArrayList<Mono> positiveMonos = new ArrayList<>();
        ArrayList<Mono> negativeMonos = new ArrayList<>();

        for (Mono mono : monos) {
            if (mono.getCoefficient().compareTo(BigInteger.ZERO) > 0) {
                positiveMonos.add(mono);
            } else {
                negativeMonos.add(mono);
            }
        }

        // 重新组合，正系数项在前，负系数项在后
        monos.clear();
        monos.addAll(positiveMonos);
        monos.addAll(negativeMonos);
    }

    @Override
    public String toString() {
        positiveToFront();

        StringBuilder sb = new StringBuilder();
        for (Mono mono : monos) {
            sb.append(mono.toString());
        }
        if (sb.length() == 0) {
            return "0";
        }
        return sb.toString();
    }
}
