package components;

import java.math.BigInteger;

public class DeriveFactor implements Factor {
    private Expr expression;
    private boolean deriveXFlag;
    private boolean deriveYFlag;
    private Poly result;

    public DeriveFactor(Expr expression, boolean deriveXFlag, boolean deriveYFlag) {
        this.expression = expression;
        this.deriveXFlag = deriveXFlag;
        this.deriveYFlag = deriveYFlag;
    }

    @Override
    public Poly toPoly() {
        if (result == null) {
            Poly temp = expression.toPoly();
            this.result = Poly.intToPoly(BigInteger.ZERO);
            if (deriveXFlag) {
                result = result.addPoly(temp.derive("x"));
            }
            if (deriveYFlag) {
                result = result.addPoly(temp.derive("y"));
            }
        }
        return result;
    }
}
