package openllet.test.tcq;

import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.test.query.AbstractQueryTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static openllet.query.sparqldl.model.cq.QueryAtomFactory.TypeAtom;

public class QueryResultTest extends AbstractQueryTest
{
    ConjunctiveQuery _q;

    @Before
    public void addIndividuals()
    {
        _kb.addIndividual(_a);
        _kb.addIndividual(_b);
        _kb.addIndividual(_c);
        _q = query(TypeAtom(x, _A), TypeAtom(y, _A));
        _q.setResultVars(List.of(x, y));
    }

    @Test
    public void addToInvertedPartialResult()
    {
        QueryResult res = new QueryResultImpl(_q);
        ResultBinding b1 = new ResultBindingImpl();
        ResultBinding b2 = new ResultBindingImpl();
        b1.setValue(x, _a);
        b2.setValue(y, _b);
        res.add(b1);
        res.add(b2);
        Assert.assertEquals(res.size(), 5);

        res = res.invert();
        Assert.assertEquals(res.size(), 4);

        ResultBinding b3 = new ResultBindingImpl();
        b3.setValue(x, _a);
        b3.setValue(y, _b);
        res.add(b3);
        Assert.assertEquals(res.size(), 5);
    }
}
