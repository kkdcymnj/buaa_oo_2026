import java.math.BigInteger;
// import java.util.ArrayList;

public class Mono {
    private BigInteger coefficient;
    private PowerFactor powerFactor;

    public Mono(BigInteger coefficient, PowerFactor powerFactor) {
        this.coefficient = coefficient;
        this.powerFactor = powerFactor;
    }

    public boolean isAddable(Mono mono) {
        // return this.equals(mono);
        return this.powerFactor.equals(mono.powerFactor);
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

    @Override
    public String toString() {
        if (coefficient.compareTo(BigInteger.ZERO) == 0) {
            return "0";
        } else if (coefficient.compareTo(BigInteger.ONE) == 0) {
            return "+" + powerFactor.toString();
        } else if (coefficient.compareTo(new BigInteger("-1")) == 0) {
            return "-" + powerFactor.toString();
        } else {
            if (powerFactor.getExponent().compareTo(BigInteger.ZERO) == 0) {
                return "+" + coefficient.toString();
            }
            return "+" + coefficient.toString() + "*" + powerFactor.toString();
        }
    }
}
