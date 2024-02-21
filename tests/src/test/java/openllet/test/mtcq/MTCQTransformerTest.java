package openllet.test.mtcq;

import openllet.mtcq.engine.rewriting.DXNFTransformer;
import openllet.mtcq.model.query.MTCQFormula;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MTCQTransformerTest extends AbstractMTCQTest
{
    @Test
    public void testNegation()
    {
        simpleTKB();
        MTCQFormula q = temporalQuery("!X[!](A(a) | ((B(b)) & (C(c))))");
        MTCQFormula tq = DXNFTransformer.transform(q);
        assertEquals("(last) | (X[!] ((!(A(a))) & ((!(B(b))) | (!(C(c))))))", tq.toString());
    }

    @Test
    public void testNextInUntil()
    {
        simpleTKB();
        MTCQFormula q = temporalQuery("(A(a)) U (X[!] (D(a)))");
        MTCQFormula tq = DXNFTransformer.transform(q);
        assertEquals("((X[!] (D(a))) | (A(a))) & (X[!] ((D(a)) | ((A(a)) U (X[!] (D(a))))))", tq.toString());
    }

    @Test
    public void testNextNestedInOr()
    {
        simpleTKB();
        MTCQFormula q = temporalQuery("(X[!] (A(a))) | ((B(b)) | ((C(c)) | (X[!] (D(d)))))");
        MTCQFormula tq = DXNFTransformer.transform(q);
        assertEquals("(X[!] ((D(d)) | (A(a)))) | ((C(c)) | (B(b)))", tq.toString());
    }

    @Test
    public void testBubblingUpNext1()
    {
        simpleTKB();
        MTCQFormula q = temporalQuery("X[!] (A(a)) | (X[!] B(b))");
        MTCQFormula tq = DXNFTransformer.transform(q);
        assertEquals("X[!] ((A(a)) | (B(b)))", tq.toString());
    }

    @Test
    public void testBubblingUpNext2()
    {
        simpleTKB();
        MTCQFormula q = temporalQuery("X[!] (A(a)) | (X[!] (B(b)) | (X[!] (C(c))))");
        MTCQFormula tq = DXNFTransformer.transform(q);
        assertEquals("X[!] ((A(a)) | ((B(b)) | (C(c))))", tq.toString());
    }

    @Test
    public void testBubblingUpNext3()
    {
        simpleTKB();
        MTCQFormula q = temporalQuery("X[!] (A(a)) | ((X[!] B(b)) | (((X[!] C(c)) | (X[!] D(d)))))");
        MTCQFormula tq = DXNFTransformer.transform(q);
        assertEquals("X[!] (((B(b)) | (A(a))) | ((C(c)) | (D(d))))", tq.toString());
    }

    @Test
    public void testBubblingUpNext4()
    {
        simpleTKB();
        MTCQFormula q = temporalQuery("X[!] (A(a)) | (X[!] (B(b)) | (X[!] ( C(c) | X[!] D(d))))");
        MTCQFormula tq = DXNFTransformer.transform(q);
        assertEquals("X[!] ((((C(c)) | (B(b))) | (A(a))) | (X[!] (D(d))))", tq.toString());
    }

    @Test
    public void testParkingVehicles()
    {
        // TODO verify that result is correct (manually)
        useCaseTKBPassingParkingVehicles(false);
        MTCQFormula q = temporalQuery("""
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
        )""");
        MTCQFormula tq = DXNFTransformer.transform(q);
        assertEquals("", tq.toString());
    }

    @Test
    public void testRightOfWay()
    {
        // TODO verify that result is correct (manually)
        useCaseTKBPassingParkingVehicles(false);
        MTCQFormula q = temporalQuery("""
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
        )""");
        MTCQFormula tq = DXNFTransformer.transform(q);
        assertEquals("(X[!] (s(?x,?r2))) | (((s(?x,?r1)) | (s(?y,c))) | ((last) | (!(s(?x,?r1)))))", tq.toString());
    }

    @Test
    public void testNestedOrNext()
    {
        useCaseTKBPassingParkingVehicles(false);
        MTCQFormula q = temporalQuery("((s(?x,?r1)) | (s(?y,c))) | (((last) | (!(s(?x,?r1)))) | (X[!] (s(?x,?r2))))");
        MTCQFormula tq = DXNFTransformer.transform(q);
        assertEquals("(X[!] (s(?x,?r2))) | (((s(?x,?r1)) | (s(?y,c))) | ((last) | (!(s(?x,?r1)))))", tq.toString());
    }
}
