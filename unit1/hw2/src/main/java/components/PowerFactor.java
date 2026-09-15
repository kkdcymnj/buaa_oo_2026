package components;

import java.math.BigInteger;

public class PowerFactor implements Factor {
    private String varName;
    private BigInteger exponent;

    public PowerFactor(String varName, BigInteger exponent) {
        this.varName = varName;
        this.exponent = exponent;
    }

    @Override
    public Poly toPoly() {
        Mono mono = new Mono(BigInteger.ONE, this);
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
}
