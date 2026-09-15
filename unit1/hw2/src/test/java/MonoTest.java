/*
import components.ExpFactor;
import components.Mono;
import org.junit.Before;
import org.junit.Test;
import tools.StrPolyExchanger;

import java.math.BigInteger;
import java.util.ArrayList;

public class MonoTest {
    private Mono mono;
    private ArrayList<ExpFactor> expFactors = new ArrayList<>();

    @Before
    public void setUp() {
        expFactors.add(
                new ExpFactor(StrPolyExchanger.strToExpr("x*x"), BigInteger.ONE)
        );
        expFactors.add(
                new ExpFactor(StrPolyExchanger.strToExpr("x^2"), BigInteger.valueOf(2))
        );
        expFactors.add(
                new ExpFactor(StrPolyExchanger.strToExpr("x*9"), BigInteger.valueOf(3))
        );
        expFactors.add(
                new ExpFactor(StrPolyExchanger.strToExpr("x-(x+1)"), BigInteger.valueOf(3))
        );
        expFactors.add(
                new ExpFactor(StrPolyExchanger.strToExpr("x"), BigInteger.ZERO)
        );
        mono = new components.Mono(
                new BigInteger("2"),
                new components.PowerFactor("x", new BigInteger("3")),
                expFactors
        );
    }

    @Test
    public void expFactorsToString() {
        System.out.println(mono.expFactorsToString(BigInteger.ONE));
        System.out.println(mono.toString());
        System.out.println(mono.mergeExpFactors());
    }

    @Test
    public void expSimplifyTest() {
        System.out.println(mono.expFactorsToString(BigInteger.ONE));
        mono.mergeExpFactors().shortExpFactors();
        System.out.println(mono);
    }
}
*/