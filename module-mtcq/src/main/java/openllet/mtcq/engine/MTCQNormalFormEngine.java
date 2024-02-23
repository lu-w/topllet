package openllet.mtcq.engine;

import openllet.mtcq.engine.rewriting.DXNFTransformer;
import openllet.mtcq.engine.rewriting.DXNFVerifier;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.model.results.QueryResult;

public class MTCQNormalFormEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        MetricTemporalConjunctiveQuery transformed = DXNFTransformer.transform(q);
        DXNFVerifier verifier = new DXNFVerifier();
        if (!verifier.verify(transformed))
            throw new RuntimeException("Unexpected: After transformation, MTCQ does not adhere to normal form. Reason is: " + verifier.getReason());
        QueryResult result;

        

        return result;
    }
}
