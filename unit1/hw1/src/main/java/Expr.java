import java.math.BigInteger;
import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.Iterator;

public class Expr implements Factor {
    private ArrayList<Term> terms;
    private BigInteger exponent = BigInteger.ONE;

    public Expr() {
        this.terms = new ArrayList<>();
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    public void setExponent(BigInteger exponent) {
        this.exponent = exponent;
    }

    @Override
    public Poly toPoly() {
        ArrayList<Mono> monos = new ArrayList<>();
        Poly poly = new Poly(monos);
        for (Term term : terms) {
            // System.out.println("poly term: " + term);
            poly.addPoly(term.toPoly());
        }
        if (exponent.compareTo(BigInteger.ONE) != 0) {
            poly = poly.powerPoly(exponent);
        }
        return poly.simplifyPoly();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exponent.compareTo(BigInteger.ZERO) == 0) {
            return "1";
        }
        if (exponent.compareTo(BigInteger.ONE) == 0) {
            for (Term term : terms) {
                // System.out.println("term: " + term);
                sb.append(term.toString());
            }
        } else {
            sb.append("(");
            for (Term term : terms) {
                // System.out.println("term: " + term);
                sb.append(term.toString());
            }
            sb.append(")^");
            sb.append(exponent.toString());
        }
        if (sb.length() == 0) {
            return "0";
        }
        return sb.toString();
    }
}
