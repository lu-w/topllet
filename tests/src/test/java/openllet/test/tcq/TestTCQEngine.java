package openllet.test.tcq;

import openllet.aterm.ATermAppl;
import org.junit.Test;

import java.util.List;

public class TestTCQEngine extends AbstractTCQTest
{
    @Test
    public void testSimpleQuery1()
    {
        simpleTKB();
        testQuery("G(A(?x))", new ATermAppl[][] { { _a } });
        testQuery("G(A(?x) ^ B(?y)) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
        testQuery("G((A(?x)) & (B(?y))) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
        testQuery("G(A(?x) ^ B(?y)) & F!(r(?x,?y))");
        testQuery("G(A(?x) ^ B(?y)) & G!(r(?x,?y))");
        testQuery("G((A(?x)) & (B(?y))) & G!(r(?x,?y))");
        testQuery("G(A(?x)) & G(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
        testQuery("G(A(?x)) & F(p(?x,?y))");
        testQuery("G(A(?x)) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testSimpleQuery2()
    {
        simpleTKB();
        testQuery("(A(?x)) U (B(?y))", new ATermAppl[][] { { _a, _b }, { _b, _b }, { _c, _b } });
        testQuery("((A(?x)) U (B(?y))) | F(C(?y))", new ATermAppl[][] { { _a, _b }, { _b, _b }, { _c, _b } });
        testQuery("((A(?x)) U (B(?y))) | X(C(?y))", new ATermAppl[][] { { _a, _b }, { _b, _b }, { _c, _b } });
    }

    @Test
    public void testSimpleQuery3()
    {
        simpleTKB2();
        testQuery("(A(?x)) U (B(?y))", new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testSimpleQuery4()
    {
        simpleTKB2();
        testQuery("((A(?x)) U (B(?y))) & (A(?x))", new ATermAppl[][] { { _a, _b } });
        testQuery("X(A(?x)) U (B(?y)) & (A(?z))", new ATermAppl[][] { { _a, _b, _a } });
    }

    @Test
    public void testSimpleQuery5()
    {
        simpleTKB2();
        testQuery("(F(A(?x)) & G (r(?y,?z)))", new ATermAppl[][] { { _a, _a, _b } });
        testQuery("F(A(?x)) | G (r(?y,?z))", new ATermAppl[][] { { _a, _a, _b }, { _b, _a, _b }, { _c, _a, _b },
                { _a, _a, _a }, { _a, _a, _c }, { _a, _b, _a }, { _a, _b, _b }, { _a, _b, _c }, { _a, _c, _a },
                { _a, _c, _b }, { _a, _c, _c } });
    }

    @Test
    public void testSimpleQuery6()
    {
        simpleTKB();
        testQuery("!G(A(?x))");
    }

    @Test
    public void testSimpleQuery7()
    {
        simpleTKB();
        testQuery("F(A(?x))",  new ATermAppl[][] { { _a } });
    }

    @Test
    public void testStrongAndWeakNextOverWordLength()
    {
        fillSimpleTKB(2);
        _tkb.get(1).addType(_a, _A);
        testQuery("(A(?x))");
        testQuery("X[!](A(?x))",  new ATermAppl[][] { { _a } });
        testQuery("X[!]X[!](A(a))");
        testQuery("X(A(?x))",  new ATermAppl[][] { { _a } });
        testQuery("XX(A(?x))",  new ATermAppl[][] { { _a }, { _b }, { _c } });
    }

    @Test
    public void testEmptyKBAndEmptyQuery()
    {
        fillSimpleTKB(2);
        testQuery("(A(?x))");
        testQuery("", new ATermAppl[][] { {} });
    }

    @Test
    public void testNotEntailedAndNegationNotEntailedQuery()
    {
        fillSimpleTKB(2);
        final String query = "X[!](A(?x))";
        testQuery(query);
        testQuery("!" + query);
    }

    @Test
    public void testNonTrivialSemanticsOfEdgeConstraints()
    {
        complexTKB();
        // a query engine with a naive implementation of semantics will return no binding.
        testQuery("!G(C(?x) ^ r(?x,?y))", allResults(List.of(_a, _b, _c), 2));
        // this is due to the axiom "C subclass of not(some(r, TOP))". B does not have such a constraint, therefore:
        testQuery("!G(B(?x) ^ r(?x,?y))");
    }

    @Test
    public void testInferenceRequiredForTCQEntailment()
    {
        complexTKB();
        testQuery("!(D(?x))", new ATermAppl[][] { { _a } } );
        testQuery("G(D(?x))", new ATermAppl[][] { { _b } } );
        testQuery("G(!(D(?x)) -> X(D(?y)))", new ATermAppl[][] { { _a, _b }, { _b, _a }, { _b, _b }, { _b, _c }, { _c, _b } });
        testQuery("G((D(?x)) -> X(D(?y)))", new ATermAppl[][] { { _a, _a }, { _a, _b }, { _a, _c }, { _b, _b }, { _c, _a }, { _c, _b }, { _c, _c }  });
        testQuery("G((A(?x)) -> X(r(?x,?y)))", new ATermAppl[][] { { _b, _a }, { _b, _b },  { _b, _c } });
        testQuery("G(!(D(?x)) -> X(p(?y,z)))", new ATermAppl[][] { { _b, _a }, { _b, _b }, { _b, _c }, { _a, _c }, { _c, _c }  });
        testQuery("G(!(D(?x)) U (E(?y)))", new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testMetricsOperators()
    {
        complexTKB();
        testQuery("G_[0,9] (!(D(?x)) | (E(?y)))", new ATermAppl[][] { { _a, _b } });
        testQuery("G_[0,10] (!(D(?x)) | (E(?y)))");
        testQuery("F_<=3 !(C(?x) ^ r(?x,?y))", allResults(List.of(_a, _b, _c), 2));
        testQuery("(!(D(?w)) | (E(?x))) U_[0,9] (q(?y, ?z))", new ATermAppl[][] { { _a, _b, _c, _b },
                { _a, _b, _b, _a } });
        testQuery("(!(D(?w)) | (E(?x))) U_[0,8] (q(?y, ?z))", new ATermAppl[][] { { _a, _b, _b, _a } });
        testQuery("(!(D(?w)) | (E(?x))) U_[0,7] (q(?y, ?z))");
    }

    //@Test
    // TODO runs too long - minimize abox
    public void testIllegCrossing()
    {
        useCaseTKBIllegCrossing();
        String tcqString = """
        # A=l4d:Bicyclist, B=l4c:Traffic_Participant, C=l4c:Traffic_Participant, D=l1c:Driveable_Lane, E=l1d:Pedestrian_Crossing
        # r=geo:sfIntersects, q=phy:has_intersecting_path
        G (A(?bi) ^ B(?t) ^ C(?w) ^ D(?l) ^ E(?cr) ^ r(?cr,?l))
            &
        ((r(?bi,?w))
            U_<=10 # somewhere in the first second, we require to take right of way
        (!F_<=5 !(r(?bi,?cr) ^ r(?t,?l) ^ q(?t,?bi)))) # illegitimately taking right of way has to be sustained for some time to be significant
        """;
        testQuery(tcqString, new ATermAppl[][] { { _a, _b, _c, _d, _e } });
    }

    @Test
    public void testIntersectingVRU()
    {
        useCaseTKBIntersectingVRU();
        String tcqString = """
        # A=Vehicle, B=VRU, r=has_intersecting_path
        G (A(?x) ^ B(?v))
            &
        F (r(?x,?v))""";
        testQuery(tcqString, new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testLaneChange()
    {
        useCaseTKBLaneChange(false);
        String tcqString = """
        # A=Vehicle, B=Lane, D=Left_Turn_Signal, q=sfWithin, p=sfIntersects, r=com:delivers_signal
        G (A(?x) ^ B(?l1) ^ B(?l2))
            &
        F
        (
            (q(?x,?l1))
                &
            X
            (
                (!(r(?x,s) ^ D(s)))
                    U
                (p(?x,?l2))
            )
        )""";
        testQuery(tcqString, new ATermAppl[][] { { _a, _b, _c } });
    }

    //@Test
    // TODO runs too long - minimize abox
    public void testLeftTurnOnc()
    {
        useCaseTKBLeftTurnOnc();
        String tcqString = """
        # A=Vehicle, B=Lane, r=has_successor_lane, p=is_lane_left_of, q=is_lane_parallel_to, s=sfIntersects,
        # t=is_in_front_of, u=sfDisjoint, o=is_behind
        G (A(?x) ^ A(?y) ^ B(?l1) ^ B(?l2) ^ B(?l3) ^ r(?l1,?l2) ^ p(?l2,?l1) ^ q(?l3,?l1))
            &
        (s(?x,?l1) ^ s(?y,?l3) ^ t(?y,?x))
            U
        (
            (u(?x,?l2))
                U
            (
                (o(?y,?x))
                    &
                (s(?x,?l2))
            )
        )""";
        testQuery(tcqString, new ATermAppl[][] { { _a, _b, _c, _d, _e } });
    }

    @Test
    public void testOvertaking()
    {
        useCaseTKBOvertaking();
        String tcqString = """
        # A=Dynamical_Object, r=is_in_proximity, q=is_to_the_side_of, t=is_to_the_front_of, s=is_behind
        G(A(?x) ^ A(?y))
            &
        F
        (
            ((r(?x,?y) ^ q(?y,?x)) | (r(?x,?y) ^ t(?y,?x)))
                &
            F (r(?x,?y) ^ s(?y,?x))
        )""";
        testQuery(tcqString, new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testRightTurn()
    {
        useCaseTKBRightTurn();
        String tcqString = """
        # A=Vehicle, B=Lane, r=is_lane_right_of, q=sfIntersects
        G (A(?x) ^ B(?l1) ^ B(?l2) ^ r(?l2, ?l1))
            &
        F ((q(?l1, ?x)) & F(q(?l2, ?x)))""";
        testQuery(tcqString, new ATermAppl[][] { { _a, _b, _c } });
    }

    @Test
    public void testPassingParkingVehicles()
    {
        useCaseTKBPassingParkingVehicles();
        String tcqString = """
        # A=Vehicle, B=2_Lane_Road, C=Parking_Vehicle, r=is_in_front_of, q=sfIntersects, s=is_to_the_side_of,
        # t=is_in_proximity, u=phy:is_behind
        G (A(?x) ^ B(z) ^ q(z,?x))
            &
        F
        (
            (r(?y,?x))
                &
            X
            (
                (C(?y) ^ t(?x,?y) ^ s(?y,?x))
                    U
                (u(?y,?x))
            )
        )
        """;
        testQuery(tcqString, new ATermAppl[][] { { _a, _c } });
    }

    // TODO also make 1 version of the useCaseTKBs where they are not entailed (just add boolean parameter and 'break' them somewhere)
}
