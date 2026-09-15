package components;

import java.math.BigInteger;

public class Num implements Factor {
    private BigInteger number;

    public Num(BigInteger number) {
        this.number = number;
    }

    public BigInteger getNumber() {
        return number;
    }

    @Override
    public Poly toPoly() {
        PowerFactor powerFactor = new PowerFactor("x", BigInteger.ZERO);
        Mono mono = new Mono(number, powerFactor);
        Poly poly = new Poly();
        poly.addMono(mono);
        return poly;
    }

    @Override
    public String toString() {
        return this.number.toString();
    }
}
