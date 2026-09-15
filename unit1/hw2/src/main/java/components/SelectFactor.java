package components;

public class SelectFactor implements Factor {
    private Factor exprA;
    private Factor exprB;
    private Factor exprC;
    private Factor exprD;

    public SelectFactor(Factor exprA, Factor exprB, Factor exprC, Factor exprD) {
        this.exprA = exprA;
        this.exprB = exprB;
        this.exprC = exprC;
        this.exprD = exprD;
        /*System.out.println("exprA: " + exprA);
        System.out.println("exprB: " + exprB);
        System.out.println("exprC: " + exprC);
        System.out.println("exprD: " + exprD);*/
    }

    public boolean judgeEqual() {
        Poly polyA = exprA.toPoly();
        Poly polyB = exprB.toPoly();
        return Poly.judgeEqualPoly(polyA, polyB);
    }

    @Override
    public String toString() {
        if (judgeEqual()) {
            return exprC.toString();
        } else {
            return exprD.toString();
        }
    }

    @Override
    public Poly toPoly() {
        if (judgeEqual()) {
            return exprC.toPoly();
        } else {
            return exprD.toPoly();
        }
    }
}
