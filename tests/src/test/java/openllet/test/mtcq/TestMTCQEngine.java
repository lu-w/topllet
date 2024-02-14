package openllet.test.mtcq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.mtcq.engine.MTCQEngine;
import org.junit.Test;

import java.util.List;
import java.util.logging.Level;

import static openllet.core.utils.TermFactory.or;
import static openllet.core.utils.TermFactory.term;

public class TestMTCQEngine extends AbstractMTCQTest
{
    @Test
    public void testSimpleQuery1()
    {
        simpleTKB();
        testQuery("G(A(?x) & B(?y)) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
        testQuery("G(A(?x))", new ATermAppl[][] { { _a } });
        testQuery("G((A(?x)) & (B(?y))) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
        testQuery("G(A(?x) & B(?y)) & F!(r(?x,?y))");
        testQuery("G(A(?x) & B(?y)) & G!(r(?x,?y))");
        testQuery("G((A(?x)) & (B(?y))) & G!(r(?x,?y))");
        testQuery("G(A(?x)) & G(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
        testQuery("G(A(?x)) & F(p(?x,?y))");
        testQuery("G(A(?x)) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testSimpleQuery2()
    {
        simpleTKB();
        testQuery("(A(?x)) U (B(?y))", new ATermAppl[][] { { _a, _b }, { _c, _b } });
        testQuery("((A(?x)) U (B(?y))) | F(C(?y))", new ATermAppl[][] { { _a, _b }, { _c, _b } });
        testQuery("((A(?x)) U (B(?y))) | X(C(?y))", new ATermAppl[][] { { _a, _b }, { _c, _b } });
        testQuery("((!(A(?x))) & (B(?y))) | ((A(?x)) & (B(?y)))", new ATermAppl[][] { { _a, _b }, { _c, _b } });
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
        testQuery("X(A(?x)) U (B(?y)) & (A(?z))");
    }

    @Test
    public void testSimpleQuery5()
    {
        simpleTKB2();
        testQuery("(F(A(?x)) & G (r(?y,?z)))");
        testQuery("F(A(?x)) | G (r(?y,?z))", new ATermAppl[][] { { _c, _a, _b }, { _a, _b, _c }, { _a, _c, _b } });
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
    public void testDisjunction()
    {
        complexTKB();
        testQuery("((B(?x)) | (C(?x)))",  new ATermAppl[][] { { _a }, { _c } });
    }

    //@Test
    public void testSimpleQuery8()
    {
        // TODO debug (works when CQ engine is disabled)
        OpenlletOptions.MTCQ_ENGINE_USE_CQ_ENGINE = false;
        fillSimpleTKB(1);
        subClass(_B, or(_C, _D));
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            kb.addType(_b, _B);
            kb.addPropertyValue(_r, _a, _b);
        }
        testQuery("G((A(?x)) & ((C(?y)) | (D(?y))))",  new ATermAppl[][] { { _a, _b } });
        OpenlletOptions.MTCQ_ENGINE_USE_CQ_ENGINE = true;
    }

    @Test
    public void testSimpleQuery9()
    {
        timeSteps(1);

        ATermAppl cA = term("http://example#A");
        ATermAppl r = term("http://example#r");
        ATermAppl a = term("http://example/data#a");
        ATermAppl b = term("http://example/data#b");
        _tkb.get(0).addClass(cA);
        _tkb.get(0).addIndividual(a);
        _tkb.get(0).addIndividual(b);
        _tkb.get(0).addObjectProperty(r);

        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(a, cA);
            kb.addType(b, cA);
            kb.addPropertyValue(r, a, b);
        }

        testQuery("G(http://example#A(?x) & http://example#A(?y) & http://example#r(?y,?x))",
                new ATermAppl[][] { { b, a } });
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
        testQuery("X(X(A(?x)))",  new ATermAppl[][] { { _a }, { _b }, { _c } });
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
        testQuery("!G(C(?x) & r(?x,?y))", allResults(List.of(_a, _b, _c), 2, true));
        // this is due to the axiom "C subclass of not(some(r, TOP))". B does not have such a constraint, therefore:
        testQuery("!G(B(?x) & r(?x,?y))");
    }

    @Test
    public void testInferenceRequiredForMTCQEntailment()
    {
        complexTKB();
        testQuery("!(D(?x))", new ATermAppl[][] { { _a } } );
        testQuery("G(D(?x))", new ATermAppl[][] { { _b } } );
        testQuery("G(!(D(?x)) -> X(D(?y)))", new ATermAppl[][] { { _a, _b }, { _b, _a }, { _b, _c }, { _c, _b } });
        testQuery("!(D(?x)) | (D(?y))", new ATermAppl[][] { { _a, _b }, {_c, _b}, { _a, _c } });
        testQuery("G(!(D(?x)) | X(D(?y)))", new ATermAppl[][] { { _a, _b }, {_c, _b} });
        testQuery("G((A(?x)) -> X(r(?x,?y)))", new ATermAppl[][] { { _b, _a }, { _b, _c } });
        testQuery("G(!(D(?x)) -> X(p(?y,z)))", new ATermAppl[][] { { _b, _a }, { _b, _c }, { _a, _c } });
        testQuery("G(!(D(?x)) U (E(?y)))", new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testMetricsOperators()
    {
        complexTKB();
        testQuery("G_[0,9] (!(D(?x)) | (E(?y)))", new ATermAppl[][] { { _a, _b } });
        testQuery("G_[0,10] (!(D(?x)) | (E(?y)))");
        testQuery("F_<=3 !(C(?x) & r(?x,?y))", allResults(List.of(_a, _b, _c), 2, true));
        testQuery("F_<=9 (q(?y, ?z))", new ATermAppl[][] { { _b, _a }, { _c, _b } });
        testQuery("(!(D(?w)) | (E(?x))) U_[0,9] (q(?y, ?z))");
        testQuery("(!(D(?w)) | (E(?x))) U_[0,8] (q(?y, ?z))");
        testQuery("(!(D(?w)) | (E(?x))) U_[0,7] (q(?y, ?z))");
    }

    @Test
    public void testBoundedUntil()
    {
        complexTKB1();
        testQuery("((A(?x))) U_[0,3] (B(?y))", new ATermAppl[][] { { _a, _b }, { _a, _c } });
    }

    @Test
    public void testIllegCrossing()
    {
        useCaseTKBIllegCrossing(true, 5);
        String mtcqString = """
        # A=l4d:Bicyclist, B=l4c:Traffic_Participant, C=l4c:Traffic_Participant, D=l1c:Driveable_Lane, E=l1d:Pedestrian_Crossing
        # r=geo:sfIntersects, q=phy:has_intersecting_path
        G (A(?bi) & B(?t) & C(?w))
            &
        F
        (
            (
                (r(?bi,?w))
                    &
                F_[1,3] # somewhere in the first time points, we require to take right of way
                    (!F_<=5 !(D(l) & E(cr) & r(cr,l) & r(?bi,cr) & r(?t,l) & q(?t,?bi))) # illegitimately taking right of way has to be sustained for some time to be significant
            )
        )""";
        testQuery(mtcqString, new ATermAppl[][] { { _a, _b, _c } });
        initializeKB();
        useCaseTKBIllegCrossing(false, 5);
        testQuery(mtcqString);
    }

    @Test
    public void testIntersectingVRU()
    {
        useCaseTKBIntersectingVRU(true);
        String mtcqString = """
        # A=Vehicle, B=VRU, r=has_intersecting_path
        G (A(?x) & B(?v))
            &
        F (r(?x,?v))""";
        testQuery(mtcqString, new ATermAppl[][] { { _a, _b } });
        initializeKB();
        useCaseTKBIntersectingVRU(false);
        testQuery(mtcqString);
    }

    @Test
    public void testLaneChange()
    {
        useCaseTKBLaneChange(true);
        String mtcqString = """
        # A=Vehicle, B=Lane, D=Left_Turn_Signal, q=sfWithin, p=sfIntersects, r=com:delivers_signal
        G (A(?x) & B(?l1) & B(?l2))
            &
        F
        (
            (q(?x,?l1))
                &
            X
            (
                (!(r(?x,s) & D(s)))
                    U
                (p(?x,?l2))
            )
        )""";
        testQuery(mtcqString, new ATermAppl[][] { { _a, _b, _c } });
        initializeKB();
        useCaseTKBLaneChange(false);
        testQuery(mtcqString);
    }

    @Test
    public void testLeftTurnOnc()
    {
        useCaseTKBLeftTurnOnc(true);
        String mtcqString = """
        # A=Vehicle, B=Lane, r=has_successor_lane, p=is_lane_left_of, q=is_lane_parallel_to, s=sfIntersects,
        # t=is_in_front_of, u=sfDisjoint, o=is_behind
        G (A(?x) & A(?y) & B(?l1) & B(?l2) & B(?l3) & r(?l1,?l2) & p(?l2,?l1) & q(?l3,?l1))
            &
        F
        (
            (s(?x,?l1) & s(?y,?l3) & t(?y,?x))
                &
            (
                ((u(?x,?l2)) | (s(?x,?l1)))
                    U
                (
                    (o(?y,?x)) & F (s(?x,?l2))
                )
            )
        )""";
        testQuery(mtcqString, new ATermAppl[][] { { _a, _b, _c, _d, _e } });
        initializeKB();
        useCaseTKBLeftTurnOnc(false);
        testQuery(mtcqString);
    }

    @Test
    public void testOvertaking()
    {
        useCaseTKBOvertaking(true);
        String mtcqString = """
        # A=Dynamical_Object, r=is_in_proximity, q=is_to_the_side_of, t=is_in_front_of, s=is_behind
        G(A(?x) & A(?y))
            &
        F
        (
            ((r(?x,?y) & q(?y,?x)) | (r(?x,?y) & t(?y,?x)))
                &
            F (r(?x,?y) & s(?y,?x))
        )""";
        testQuery(mtcqString, new ATermAppl[][] { { _a, _b } });
        initializeKB();
        useCaseTKBOvertaking(false);
        testQuery(mtcqString);
    }

    @Test
    public void testRightTurn()
    {
        useCaseTKBRightTurn(true);
        String mtcqString = """
        # A=Vehicle, B=Lane, r=is_lane_right_of, q=sfIntersects
        G (A(?x) & B(?l1) & B(?l2) & r(?l2, ?l1))
            &
        F ((q(?l1, ?x)) & F(q(?l2, ?x)))""";
        testQuery(mtcqString, new ATermAppl[][] { { _a, _b, _c } });
        initializeKB();
        useCaseTKBRightTurn(false);
        testQuery(mtcqString);
    }

    @Test
    public void testPassingParkingVehicles()
    {
        useCaseTKBPassingParkingVehicles(true);
        String mtcqString = """
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
        )
        """;
        testQuery(mtcqString, new ATermAppl[][] { { _a, _c } });
        initializeKB();
        useCaseTKBPassingParkingVehicles(false);
        testQuery(mtcqString);
    }
}
