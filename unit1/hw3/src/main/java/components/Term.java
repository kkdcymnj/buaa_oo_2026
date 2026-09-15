package components;// import javax.sound.midi.SysexMessage;

import java.math.BigInteger;
import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.Iterator;

public class Term implements Factor {
    private ArrayList<Factor> factors;
    private char sign;

    public Term(char sign) {
        this.factors = new ArrayList<>();
        this.sign = sign;
    }

    public Term() {
        this.factors = new ArrayList<>();
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sign);
        for (Factor factor : factors) {
            if (factor instanceof Expr) {
                sb.append("(").append(factor.toString()).append(")");
            } else {
                sb.append(factor.toString());
            }
            sb.append("*");
        }
        return sb.substring(0, sb.length() - 1);
    }

    @Override
    public Poly toPoly() {
        PowerFactor powerFactor = new PowerFactor("x", BigInteger.ZERO);
        Mono mono = new Mono(BigInteger.ONE, powerFactor);
        Poly temp = new Poly();
        Poly result = new Poly();
        result.addMono(mono);
        for (Factor factor : factors) {
            temp = result.multPoly(factor.toPoly());
            result = temp;
        }
        //System.out.println("result: " + result);
        if (sign == '-') {
            result.polyNegate();
            // sign = '+';  // 纯属挖坑，这样整个term的正负号都变了
        }
        //System.out.println("result after negate: " + result);
        return result;
    }

    public char getSign() {
        return sign;
    }

    public void changeSign() {
        if (sign == '+') {
            sign = '-';
        } else {
            sign = '+';
        }
    }
}
