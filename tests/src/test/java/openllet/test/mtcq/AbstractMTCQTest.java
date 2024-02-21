package openllet.test.mtcq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.KnowledgeBaseImpl;
import openllet.mtcq.model.kb.InMemoryTemporalKnowledgeBaseImpl;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.mtcq.model.query.MTCQFormula;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.parser.ParseException;
import openllet.mtcq.parser.MetricTemporalConjunctiveQueryParser;
import openllet.test.query.AbstractQueryTest;
import org.junit.After;
import org.junit.Before;

import java.util.List;

import static openllet.core.utils.TermFactory.*;
import static org.junit.Assert.*;

public class AbstractMTCQTest extends AbstractQueryTest
{
    TemporalKnowledgeBase _tkb = null;

    @Override
    @Before
    public void initializeKB()
    {
        _tkb = new InMemoryTemporalKnowledgeBaseImpl();
    }

    @Override
    @After
    public void disposeKB()
    {
        _tkb = null;
    }

    @Override
    protected void classes(final ATermAppl... classes)
    {
        for (KnowledgeBase kb : _tkb)
        {
            _kb = kb;
            super.classes(classes);
        }
    }

    protected void subClass(final ATermAppl sub, final ATermAppl sup)
    {
        for (KnowledgeBase kb : _tkb)
            kb.addSubClass(sub, sup);
    }

    @Override
    protected void objectProperties(final ATermAppl... props)
    {
        for (KnowledgeBase kb : _tkb)
        {
            _kb = kb;
            super.objectProperties(props);
        }
    }

    @Override
    protected void dataProperties(final ATermAppl... props)
    {
        for (KnowledgeBase kb : _tkb)
        {
            _kb = kb;
            super.dataProperties(props);
        }
    }

    @Override
    protected void annotationProperties(final ATermAppl... props)
    {
        for (KnowledgeBase kb : _tkb)
        {
            _kb = kb;
            super.annotationProperties(props);
        }
    }

    @Override
    protected void individuals(final ATermAppl... inds)
    {
        for (KnowledgeBase kb : _tkb)
        {
            _kb = kb;
            super.individuals(inds);
        }
    }

    protected void timeSteps(int size)
    {
        for (int i = 0; i < size; i++)
            appendABox();
    }

    protected void appendABox()
    {
        _tkb.add(new KnowledgeBaseImpl());
    }

    protected MTCQFormula temporalQuery(String formula)
    {
        return temporalQuery(formula, true);
    }

    protected MTCQFormula temporalQuery(String formula, boolean distinct)
    {
        MTCQFormula mtcq = null;
        try
        {
            mtcq = MetricTemporalConjunctiveQueryParser.parse(formula, _tkb, distinct);
        }
        catch (ParseException e)
        {
            fail("Parsing of MTCQ failed: " + e);
        }
        return mtcq;
    }

    protected MTCQFormula uncheckedTemporalQuery(String formula) throws ParseException
    {
        return MetricTemporalConjunctiveQueryParser.parse(formula, _tkb);
    }

    public void simpleTKB()
    {
        fillSimpleTKB(5);
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            kb.addType(_b, _B);
            kb.addPropertyValue(_r, _a, _b);
        }
    }

    public void complexTKB1()
    {
        fillComplexTKB(4);
        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            if (i == 2)
            {
                kb.addType(_b, _B);
            }
            if (i == 3)
                kb.addType(_c, _B);
            i++;
        }
    }

    public void simpleTKB2()
    {
        fillSimpleTKB(5);
        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            if (i > 2)
                kb.addType(_b, _B);
            kb.addPropertyValue(_r, _a, _b);
            i++;
        }
    }

    protected void fillSimpleTKB(int size)
    {
        timeSteps(size);

        classes(_A, _B, _C, _D);
        individuals(_a, _b, _c);
        objectProperties(_r, _p);
    }

    public void complexTKB()
    {
        fillComplexTKB(10);
        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            if (i % 2 == 0)
            {
                kb.addType(_a, _A);
                kb.addPropertyValue(_r, _a, _b);
            }
            else
            {
                kb.addType(_b, _E);
            }
            kb.addType(_b, _D);
            kb.addType(_c, _B);
            if (i == 8)
                kb.addPropertyValue(_q, _b, _a);
            if (i == 9)
                kb.addPropertyValue(_q, _c, _b);
            i++;
        }
    }

    protected void fillComplexTKB(int size)
    {
        timeSteps(size);

        classes(_A, _B, _C, _D, _E);
        individuals(_a, _b, _c);
        objectProperties(_r, _p, _q);

        subClass(_A, or(_B, _C));
        subClass(_A, not(_D));
        subClass(_B, some(_p, TOP));
        subClass(_C, not(some(_r, TOP)));
    }

    protected void useCaseTKBIllegCrossing(boolean entailed, int numberAdditionalIndividuals)
    {
        fillUseCaseTKB(20, numberAdditionalIndividuals);

        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            kb.addType(_b, _B);
            kb.addType(_c, _C);
            kb.addType(_f, _C);
            kb.addType(_d, _D);
            kb.addType(_e, _E);
            kb.addPropertyValue(_r, _e, _d);
            if (entailed)
            {
                if (2 < i && i < 5)
                    kb.addPropertyValue(_r, _a, _c);
                if (i < 13 && i >= 5)
                {
                    kb.addPropertyValue(_r, _a, _e);
                    kb.addPropertyValue(_r, _b, _d);
                    kb.addPropertyValue(_q, _b, _a);
                }
            }
            else
            {
                if (2 < i && i < 5)
                    kb.addPropertyValue(_r, _a, _c);
                if (i < 19 && i >= 17)
                {
                    kb.addPropertyValue(_r, _a, _e);
                    kb.addPropertyValue(_r, _b, _d);
                    kb.addPropertyValue(_q, _b, _a);
                }
            }
            if (i >= 15)
            {
                kb.addPropertyValue(_r, _a, _f);
                kb.addPropertyValue(_q, _b, _a);
            }
            i++;
        }
    }

    protected void useCaseTKBIntersectingVRU(boolean entailed)
    {
        fillUseCaseTKB(20);

        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            kb.addType(_b, _B);
            kb.addPropertyValue(_r, _e, _d);
            if (entailed)
            {
                if (i < 16 && i > 12)
                    kb.addPropertyValue(_r, _a, _b);
            }
            else
            {
                if (i < 16 && i > 12)
                    kb.addPropertyValue(_r, _b, _a);
            }
            i++;
        }
    }

    protected void useCaseTKBLaneChange(boolean entailed)
    {
        fillUseCaseTKB(20);

        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            kb.addType(_b, _B);
            kb.addType(_c, _B);
            if (i < 8)
                kb.addPropertyValue(_q, _a, _b);
            if (!entailed && i == 10)
            {
                // one short turn signal -> is enough
                kb.addType(_d, _D);
                kb.addPropertyValue(_r, _a, _d);
            }
            else
                kb.addType(_a, not(some(_r, _D)));
            // intersecting lane 2 @t=13+
            if (i > 12)
                kb.addPropertyValue(_p, _a, _c);
            i++;
        }
    }

    protected void useCaseTKBLeftTurnOnc(boolean entailed)
    {
        fillUseCaseTKB(20);

        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            kb.addType(_b, _A);
            kb.addType(_c, _B);
            kb.addType(_d, _B);
            kb.addType(_e, _B);
            kb.addPropertyValue(_r, _c, _d);
            kb.addPropertyValue(_p, _d, _c);
            kb.addPropertyValue(_q, _e, _c);

            if (2 < i && i < 5)
            {
                kb.addPropertyValue(_s, _a, _c);
                kb.addPropertyValue(_s, _b, _e);
                kb.addPropertyValue(_t, _b, _a);
            }
            if (i == 5)
                kb.addPropertyValue(_s, _a, _c);
            if (6 <= i && i < 9)
                kb.addPropertyValue(_u, _a, _d);
            if (entailed && 9 <= i && i <= 12)
                kb.addPropertyValue(_o, _b, _a);
            if (i > 14)
            {
                kb.addPropertyValue(_o, _b, _a);
                kb.addPropertyValue(_s, _a, _d);
            }

            i++;
        }
    }

    protected void useCaseTKBOvertaking(boolean entailed)
    {
        fillUseCaseTKB(20);

        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            kb.addType(_b, _A);
            kb.addType(_c, _B);

            if (entailed)
            {
                if (i == 3)
                {
                    kb.addPropertyValue(_r, _a, _b);
                    kb.addPropertyValue(_t, _b, _a);
                }
                if (i >= 4 && i < 8)
                {
                    kb.addPropertyValue(_r, _a, _b);
                    kb.addPropertyValue(_q, _b, _a);
                }
            }
            if (i >= 8 && i < 14)
            {
                kb.addPropertyValue(_r, _a, _b);
                kb.addPropertyValue(_s, _b, _a);
            }

            i++;
        }
    }

    protected void useCaseTKBRightTurn(boolean entailed)
    {
        fillUseCaseTKB(20);

        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            kb.addType(_b, _B);
            kb.addType(_c, _B);
            kb.addPropertyValue(_r, _c, _b);

            if (i > 5 && i < 8)
                kb.addPropertyValue(_q, _b, _a);
            if (i >= 10 && i < 15)
            {
                if (entailed)
                    kb.addPropertyValue(_q, _c, _a);
                else
                    kb.addPropertyValue(_q, _b, _a);
            }

            i++;
        }
    }

    protected void useCaseTKBPassingParkingVehicles(boolean entailed)
    {
        fillUseCaseTKB(20);

        subClass(_B, and(_E, min(_p, 2, _F)));  // F: Lane, E: Road
        subClass(_B, and(_E, max(_p, 2, _F)));
        subClass(and(_E, min(_p, 2, _F)), _B);
        subClass(and(_E, max(_p, 2, _F)), _B);
        subClass(and(_M, and(_N, some(_o, _G))), _C);

        int i = 0;

        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            kb.addType(_b, _E);
            kb.addType(_d, _F);
            kb.addType(_e, _F);
            kb.addPropertyValue(_p, _b, _d);
            kb.addPropertyValue(_p, _b, _e);
            kb.addType(_f, _G);
            kb.addPropertyValue(_o, _c, _f);
            kb.addType(_c, _M);
            kb.addType(_c, _N);
            kb.addPropertyValue(_q, _b, _a);

            if (i > 7 && i < 10)
                kb.addPropertyValue(_r, _c, _a);
            if (i >= 10 && i < 13)
            {
                kb.addPropertyValue(_t, _a, _c);
                kb.addPropertyValue(_s, _c, _a);
            }
            if (entailed && i >= 13 && i < 17)
                kb.addPropertyValue(_u, _c, _a);

            i++;
        }
    }

    protected void fillUseCaseTKB(int size)
    {
        fillUseCaseTKB(size, 0);
    }

    protected void fillUseCaseTKB(int size, int numberAdditionalIndividuals)
    {
        timeSteps(size);

        classes(_A, _B, _C, _D, _E, _H);
        individuals(_a, _b, _c, _d, _e, _f);
        for (int i = 0; i < numberAdditionalIndividuals; i++)
            individuals(term("i" + i));
        objectProperties(_r, _p, _q, _t, _s, _u, _o);
    }

    protected void assertQueryEntailed(String query)
    {
        testQuery(query, true);
    }

    protected void assertQueryNotEntailed(String query)
    {
        testQuery(query, false);
    }

    protected void testQuery(String query, final boolean expected)
    {
        testQuery(query, true, expected);
    }

    protected void testQuery(String query, final boolean distinct, final boolean expected)
    {
        MetricTemporalConjunctiveQuery mtcq = temporalQuery(query, distinct);
        testQuery(mtcq, expected);
    }

    protected void testQuery(String query, final ATermAppl[]... values)
    {
        testQuery(query, true, values);
    }

    protected void testQuery(String query, final boolean distinct, final ATermAppl[]... values)
    {
        MetricTemporalConjunctiveQuery mtcq = temporalQuery(query, distinct);
        testQuery(mtcq, values);
    }

    protected void testQuery(String query, final List<List<ATermAppl>> values)
    {
        testQuery(query, true, values);
    }

    protected void testQuery(String query, final boolean distinct, final List<List<ATermAppl>> values)
    {
        MetricTemporalConjunctiveQuery mtcq = temporalQuery(query, distinct);
        testQuery(mtcq, values);
    }
}
