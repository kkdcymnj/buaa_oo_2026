package components;

import java.math.BigInteger;

public class PowerFactor implements Factor {
    private String varName;
    private BigInteger exponent;

    public PowerFactor(String varName, BigInteger exponent) {
        this.varName = varName;
        this.exponent = exponent;
    }

    public PowerFactor deepCopy() {
        return new PowerFactor(varName, exponent);
    }

    @Override
    public Poly toPoly() {
        Mono mono;
        if (varName.equals("x")) {
            mono = new Mono(BigInteger.ONE, this.deepCopy());
        } else {
            mono = new Mono(BigInteger.ONE, new PowerFactor("x", BigInteger.ZERO), this.deepCopy());
        }
        Poly poly = new Poly();
        poly.addMono(mono);
        return poly;
    }

    @Override
    public String toString() {
        if (exponent.compareTo(BigInteger.ZERO) == 0) {
            return "1";
        } else if (exponent.compareTo(BigInteger.ONE) == 0) {
            return varName;
        } else {
            return varName + "^" + exponent;
        }
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public String getVarName() {
        return varName;
    }

    public String toFullString() {
        return varName + "^" + exponent;
    }

    // 求导
    public Poly derive(String varName) {
        if (this.exponent.equals(BigInteger.ZERO)) {
            return Poly.intToPoly(BigInteger.ZERO);
        }

        if (this.varName.equals(varName)) {
            Mono mono;
            if (varName.equals("x")) {
                mono = new Mono(exponent,
                        new PowerFactor(varName, exponent.subtract(BigInteger.ONE)));
            } else {
                mono = new Mono(exponent, new PowerFactor("x", BigInteger.ZERO),
                        new PowerFactor(varName, exponent.subtract(BigInteger.ONE)));
            }

            Poly poly = new Poly();
            poly.addMono(mono);
            return poly;
        } else {
            return Poly.intToPoly(BigInteger.ZERO);
        }
    }
}
