/*
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

public class PolyTest {
    private Mono monoA;
    private Mono monoB;
    private Mono monoC;
    private Mono monoD;
    private Poly poly;

    @Before
    public void setUp() {
        monoA = new Mono(
                new BigInteger("2"),
                new PowerFactor("x", new BigInteger("0"))
        );
        monoB = new Mono(
                new BigInteger("3"),
                new PowerFactor("x", new BigInteger("1"))
        );
        monoC = new Mono(
                new BigInteger("-2"),
                new PowerFactor("x", new BigInteger("0"))
        );
        monoD = new Mono(
                new BigInteger("5"),
                new PowerFactor("x", new BigInteger("1"))
        );
        poly = new Poly();
        poly.addMono(monoA);
        poly.addMono(monoB);
        poly.addMono(monoC);
        poly.addMono(monoD);
    }

    @Test
    public void testPolySimplify() {
        Poly poly1 = new Poly();
        Poly poly2 = new Poly();
        poly1.addMono(monoA);
        poly2.addMono(monoC);
        poly = poly1.addPoly(poly2);
        System.out.println(poly.simplifyPoly().toString());
    }

    @Test
    public void testPolyMultiply() {
        Poly polyA = new Poly();    //2x^2+3x^3
        Poly polyB = new Poly();    //4x^2+5x
        Poly result = new Poly();
        polyA.addMono(monoA);
        polyA.addMono(monoB);
        polyB.addMono(monoC);
        polyB.addMono(monoD);
        result = polyA.multPoly(polyB);
        System.out.println(result.simplifyPoly().toString());
    }
}
*/
