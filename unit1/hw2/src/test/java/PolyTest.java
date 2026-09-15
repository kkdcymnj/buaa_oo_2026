/*
import components.ExpFactor;
import org.junit.Before;
import org.junit.Test;
import tools.StrPolyExchanger;

import java.math.BigInteger;
import java.util.ArrayList;

public class PolyTest {
    private components.Mono monoA;
    private components.Mono monoB;
    private components.Mono monoC;
    private components.Mono monoD;
    private components.Poly poly;

    @Before
    public void setUp() {
        ArrayList<ExpFactor> expFactors1 = new ArrayList<>();
        ArrayList<ExpFactor> expFactors2 = new ArrayList<>();
        ArrayList<ExpFactor> expFactors3 = new ArrayList<>();
        ArrayList<ExpFactor> expFactors4 = new ArrayList<>();

        expFactors1.add(
                new ExpFactor(StrPolyExchanger.strToExpr("x*x"), BigInteger.ONE)
        );

        expFactors3.add(
                new ExpFactor(StrPolyExchanger.strToExpr("x^2+1"), BigInteger.valueOf(1))
        );

        monoA = new components.Mono(
                new BigInteger("2"),
                new components.PowerFactor("x", new BigInteger("3")),
                expFactors1
        );
        monoB = new components.Mono(
                new BigInteger("3"),
                new components.PowerFactor("x", new BigInteger("1"))
        );
        monoC = new components.Mono(
                new BigInteger("-5"),
                new components.PowerFactor("x", new BigInteger("3")),
                expFactors3
        );
        monoD = new components.Mono(
                new BigInteger("7"),
                new components.PowerFactor("x", new BigInteger("1"))
        );
        poly = new components.Poly();
        poly.addMono(monoA);
        poly.addMono(monoB);
        poly.addMono(monoC);
        poly.addMono(monoD);
    }

    @Test
    public void testPolySimplify() {
        components.Poly poly1 = new components.Poly();
        components.Poly poly2 = new components.Poly();
        poly1.addMono(monoA);
        poly2.addMono(monoC);
        poly = poly1.addPoly(poly2);
        System.out.println(poly.simplifyPoly().toString());
    }

    @Test
    public void testPolyMultiply() {
        components.Poly polyA = new components.Poly();
        components.Poly polyB = new components.Poly();
        components.Poly result = new components.Poly();
        polyA.addMono(monoA);
        polyA.addMono(monoB);
        polyB.addMono(monoC);
        polyB.addMono(monoD);
        result = polyA.multPoly(polyB);
        System.out.println(result.simplifyPoly().toString());
    }
}
*/