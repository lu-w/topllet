package openllet.mtcq.ui;

import openllet.core.KnowledgeBase;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;

public class SimplePrintUI implements MTCQEngineUI {

    @Override
    public void setup(MetricTemporalConjunctiveQuery query)
    {
        System.out.println("Evaluating MTCQ " + query);
    }

    @Override
    public void clear()
    {

    }

    @Override
    public void tearDown()
    {

    }

    @Override
    public void informAboutStartOfIteration(int timePoint)
    {
        System.out.println("[t = " + timePoint + "] Starting iteration");
    }

    @Override
    public void informAboutEndOfIteration(int timePoint)
    {
        System.out.println("[t = " + timePoint + "] Ending iteration");
    }

    @Override
    public void informAboutResults(int timePoint, KnowledgeBase kb, Query<?> query, QueryResult result)
    {
        if (result != null)
            System.out.println("[t = " + timePoint + "] Computed " + result.size() + " result(s) for " + query);
    }
}
