package openllet.test.mtcq;

import openllet.mtcq.engine.rewriting.DXNFTransformer;
import openllet.mtcq.engine.rewriting.DXNFVerifier;
import openllet.mtcq.model.query.MTCQFormula;
import org.junit.Test;

import static org.junit.Assert.*;

public class MTCQTransformerTest extends AbstractMTCQTest
{
    protected void testTransformation(String input, String expected)
    {
        MTCQFormula actual = DXNFTransformer.transform(temporalQuery(input));
        DXNFVerifier verifier = new DXNFVerifier();
        if (!verifier.verify(actual))
        {
            fail("Transformed formula is " + actual + ", but this is not in DXNF due to: " +
                    verifier.getReason().toString());
        }
        assertEquals(expected, actual.toString());
    }

    @Test
    public void testNegation()
    {
        simpleTKB();
        testTransformation(
                "!X[!](A(a) | ((B(b)) & (C(c))))",
                "(last) | (X[!] ((!(A(a))) & ((!(B(b))) | (!(C(c))))))"
        );
    }

    @Test
    public void testNextInUntil()
    {
        simpleTKB();
        testTransformation(
                "(A(a)) U (X[!] (D(a)))",
                "((X[!] (D(a))) | (A(a))) & (X[!] ((D(a)) | ((A(a)) U (X[!] (D(a))))))"
        );
    }

    @Test
    public void testNextNestedInOr()
    {
        simpleTKB();
        testTransformation(
                "(X[!] (A(a))) | ((B(b)) | ((C(c)) | (X[!] (D(d)))))",
                "(X[!] ((D(d)) | (A(a)))) | ((B(b)) | (C(c)))"
        );
    }

    @Test
    public void testBubblingUpNext1()
    {
        simpleTKB();
        testTransformation(
                "X[!] (A(a)) | (X[!] B(b))",
                "X[!] ((A(a)) | (B(b)))"
        );
    }

    @Test
    public void testBubblingUpNext2()
    {
        simpleTKB();
        testTransformation(
                "X[!] (A(a)) | (X[!] (B(b)) | (X[!] (C(c))))",
                "X[!] (((B(b)) | (A(a))) | (C(c)))"
        );
    }

    @Test
    public void testBubblingUpNext3()
    {
        simpleTKB();
        testTransformation(
                "X[!] (A(a)) | ((X[!] B(b)) | (((X[!] C(c)) | (X[!] D(d)))))",
                "X[!] (((C(c)) | ((B(b)) | (A(a)))) | (D(d)))"
        );
    }

    @Test
    public void testBubblingUpNext4()
    {
        simpleTKB();
        testTransformation(
                "X[!] (A(a)) | (X[!] (B(b)) | (X[!] ( C(c) | X[!] D(d))))",
                "X[!] ((X[!] (D(d))) | (((B(b)) | (A(a))) | (C(c))))"
        );
    }

    @Test
    public void testNextInEventually()
    {
        simpleTKB();
        testTransformation(
                "F (B(?x) & X[!] (C(?x)))",
                "((X[!] (F ((B(?x)) & (X[!] (C(?x)))))) | (B(?x))) & (X[!] ((F ((B(?x)) & (X[!] (C(?x))))) | (C(?x))))"
        );
    }

    @Test
    public void testParkingVehicles()
    {
        useCaseTKBPassingParkingVehicles(false);
        testTransformation("""
                G (A(?x))
                    &
                F
                (
                    (B(?x))
                        &
                    X[!]
                    (
                        (C(?x))
                            U
                        (D(?x))
                    )
                )""",
                "((A(?x)) & ((last) | (X[!] (G (A(?x)))))) & (((X[!] (F ((B(?x)) & (X[!] ((C(?x)) U (D(?x))))))) | (B(?x))) & (X[!] ((F ((B(?x)) & (X[!] ((C(?x)) U (D(?x)))))) | ((C(?x)) U (D(?x))))))");
    }

    @Test
    public void testNestedEventually()
    {
        useCaseTKBPassingParkingVehicles(false);
        testTransformation(
                "F ((D(?x)) & F (E(?x)))",
                "((X[!] (F ((D(?x)) & (F (E(?x)))))) | (D(?x))) & ((X[!] ((F (E(?x))) | (F ((D(?x)) & (F (E(?x))))))) | (E(?x)))"
        );
    }

    @Test
    public void testNestedOrNext()
    {
        useCaseTKBPassingParkingVehicles(false);
        testTransformation(
                "((s(?x,?r1)) | (s(?y,c))) | (((last) | (!(s(?x,?r1)))) | (X[!] (s(?x,?r2))))",
                "(X[!] (s(?x,?r2))) | (((s(?x,?r1)) | (s(?y,c))) | ((last) | (!(s(?x,?r1)))))"
        );
    }
}
