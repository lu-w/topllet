package openllet.test.tcq;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.tcq.engine.BooleanTCQEngine;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestBooleanTCQEngine extends AbstractTCQTest
{
    private QueryExec<TemporalConjunctiveQuery> _engine = new BooleanTCQEngine();

    public void simpleTKB()
    {
        fillSimpleTKB(5);
        _tkb.first();
        while (_tkb.hasNext())
        {
            KnowledgeBase kb = _tkb.next();
            kb.addType(_a, _A);
            kb.addPropertyValue(_r, _a, _b);
        }
    }

    public void complexTKB()
    {
        fillSimpleTKB(10);
        while (_tkb.hasNext())
        {
            KnowledgeBase kb = _tkb.next();
            kb.addType(_a, _A);
            kb.addPropertyValue(_r, _a, _b);
            // TODO some complex axioms here for depicting disjunction through CQ negation
        }
    }

    protected void fillSimpleTKB(int size)
    {
        timeSteps(size);
        classes(_A, _B, _C, _D);
        individuals(_a, _b, _c);
        objectProperties(_r, _p);
    }

    protected void fillComplexTKB(int size)
    {
        timeSteps(size);
        classes(_A, _B, _C, _D);
        individuals(_a, _b, _c);
        objectProperties(_r, _p);
        // TODO some complex axioms here for depicting disjunction through CQ negation
    }

    @Test
    public void testSimpleQuery1()
    {
        simpleTKB();
        TemporalConjunctiveQuery tcq = temporalQuery("F(A(a))");
        assertFalse(_engine.exec(tcq).isEmpty());
    }

    @Test
    public void testSimpleQuery2()
    {
        simpleTKB();
        TemporalConjunctiveQuery tcq = temporalQuery("!G(A(a))");
        assertTrue(_engine.exec(tcq).isEmpty());
    }

    // TODO some more test cases here
}
