import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        return this;
    }

    public Poly multPoly(Poly poly) {
        Poly result = new Poly();
        ArrayList<Mono> hisMonos = poly.getMonos();
        for (Mono monoA : monos) {
            for (Mono monoB : hisMonos) {
                // 目前暂不考虑是否可相乘
                BigInteger newCoefficient =
                        monoA.getCoefficient().multiply(monoB.getCoefficient());
                BigInteger newExponent =
                        monoA.getExponent().add(monoB.getExponent());
                PowerFactor newPowerFactor =
                        new PowerFactor("x", newExponent);
                Mono newMono =
                        new Mono(newCoefficient, newPowerFactor);
                result.addMono(newMono);
            }
        }
        return result.simplifyPoly();
    }

    public Poly powerPoly(BigInteger exponent) {
        if (exponent.compareTo(BigInteger.ZERO) == 0) {
            PowerFactor newPowerFactor =
                    new PowerFactor("x", BigInteger.ZERO);
            Mono newMono = new Mono(BigInteger.ONE, newPowerFactor);
            ArrayList<Mono> newMonos = new ArrayList<>();
            newMonos.add(newMono);
            return new Poly(newMonos);
        }
        Poly result = new Poly();
        for (Mono mono : this.monos) {
            result.addMono(mono);
        }
        int intExponent = exponent.intValue();
        for (int i = 1; i < intExponent; i++) {
            result = result.multPoly(this);
        }
        return result;
    }

    public Poly simplifyPoly() {
        // 使用字符串作为key："变量^指数"
        Map<String, BigInteger> termMap = new HashMap<>();

        for (Mono mono : monos) {
            PowerFactor pf = mono.getPowerFactor();
            String key = pf.toFullString();
            BigInteger coefficient = mono.getCoefficient();
            termMap.merge(key, coefficient, BigInteger::add);
        }

        ArrayList<Mono> mergedMonos = new ArrayList<>();
        for (Map.Entry<String, BigInteger> entry : termMap.entrySet()) {
            BigInteger coefficient = entry.getValue();
            if (coefficient.compareTo(BigInteger.ZERO) != 0) {
                String[] parts = entry.getKey().split("\\^");
                BigInteger exp = new BigInteger(parts[1]);
                PowerFactor pf = new PowerFactor(parts[0], exp);
                mergedMonos.add(new Mono(coefficient, pf));
            }
        }
        return new Poly(mergedMonos);
    }

    public void polyNegate() {
        for (Mono mono : monos) {
            mono.setCoefficient(mono.getCoefficient().negate());
        }
    }

    @Override
    public String toString() {
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
