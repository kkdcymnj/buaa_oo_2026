package components;

// import com.sun.org.apache.xpath.internal.operations.Bool;

import java.math.BigInteger;
import java.util.ArrayList;

public class ExpFactor implements Factor {
    private Factor expression;
    private BigInteger exponent;
    private Poly poly;

    public ExpFactor(Factor expression, BigInteger exponent) {
        this.expression = expression;
        this.exponent = exponent;
        this.poly = expression.toPoly();
    }

    public ExpFactor(Factor expression, BigInteger exponent,
                     Poly poly) {
        this.expression = expression;
        this.exponent = exponent;
        this.poly = poly;
    }

    public ExpFactor(Poly poly, BigInteger exponent) {
        this.poly = poly;
        this.exponent = exponent;
    }

    public Factor getExpression() {
        return this.expression;
    }

    public BigInteger getExponent() {
        return this.exponent;
    }

    public void setExponent(BigInteger exponent) {
        this.exponent = exponent;
    }

    public Poly getPoly() {
        return this.poly;
    }

    //同类项（不考虑次数）
    public boolean judgeSameExpFactor(ExpFactor expFactor) {
        Poly poly1 = this.poly;
        Poly poly2 = expFactor.getPoly();
        return Poly.judgeEqualPoly(poly1, poly2);
    }

    @Override
    public String toString() {
        boolean flag = poly.judgeSingularPoly();
        if (poly.judgeEqualToZero()) {
            return "1";
        }

        String expressionString = poly.toString();
        if (exponent.compareTo(BigInteger.ZERO) == 0) {
            return "1";
        } else if (exponent.compareTo(BigInteger.ONE) == 0) {
            if (flag) {
                return "exp(" + expressionString + ")";
            }
            else {
                return "exp((" + expressionString + "))";
            }
        } else {
            if (flag) {
                return "exp(" + expressionString + ")^" + exponent;
            } else {
                return "exp((" + expressionString + "))^" + exponent;
            }
        }
    }

    @Override
    public Poly toPoly() {
        PowerFactor powerFactor = new PowerFactor("x", BigInteger.ZERO);
        BigInteger coefficient = BigInteger.ONE;
        ArrayList<ExpFactor> expFactors = new ArrayList<>();
        expFactors.add(
                new ExpFactor(this.expression, this.exponent, this.poly)
        );
        Mono mono = new Mono(coefficient, powerFactor, expFactors);
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(mono);
        return new Poly(monos);
    }
}
