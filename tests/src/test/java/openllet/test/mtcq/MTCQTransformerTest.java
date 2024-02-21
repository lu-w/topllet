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
    public void testParkingVehicles()
    {
        // TODO verify that result is correct (manually)
        useCaseTKBPassingParkingVehicles(false);
        testTransformation("""
                # A=Vehicle, B=2_Lane_Road, C=Parking_Vehicle, r=is_in_front_of, q=sfIntersects, s=is_to_the_side_of,
                # t=is_in_proximity, u=phy:is_behind
                G (A(?x) & B(z) & q(z,?x) & C(?y))
                    &
                F
                (
                    (r(?y,?x))
                        &
                    X[!]
                    (
                        (t(?x,?y) & s(?y,?x))
                            U
                        (u(?y,?x))
                    )
                )""",
                "");
    }

    @Test
    public void testRightOfWay()
    {
        // TODO verify that result is correct (manually)
        useCaseTKBPassingParkingVehicles(false);
        testTransformation("""
                G
                (
                    (A(?x) & B(?y) & C(?r2) & C(?r1) & r(?r2, ?r1))
                        &
                    ((D(?y)) | (E(?y)))
                )
                    &
                (
                    F
                    (
                        (s(?x,?r1) & t(?x,?y) & u(?x,?y))
                            &
                        F (s(?x,?r2))
                    )
                        ->
                    (
                        (s(?x,?r1))
                            U
                        ((s(?y,c) & H(c) & o(c,?r1) & o(c,?r2)))
                    )
                )""",
                ""
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
