/*
import components.ExpFactor;
import org.junit.Before;
import org.junit.Test;
import tools.StrPolyExchanger;

import java.math.BigInteger;

public class ExpFactorTest {
    private ExpFactor expFactor1;
    private ExpFactor expFactor2;

    @Before
    public void setUp() {
        expFactor1 =
                new ExpFactor(
                        StrPolyExchanger.strToExpr("x*x"),
                        BigInteger.ONE
                );
        expFactor2 =
                new ExpFactor(
                        StrPolyExchanger.strToExpr("(x+1)^2-x*2-exp((-x-2))"),
                        BigInteger.valueOf(1)
                );
    }

    @Test
    public void testSameFactor() {
        System.out.println(expFactor1.toString());
        System.out.println(expFactor2.judgeSameExpFactor(expFactor1));
    }

    @Test
    public void printTest() {
        String str = expFactor2.toString();
        System.out.println(str);
        str = expFactor2.toString();
        System.out.println(str);
    }
}
*/