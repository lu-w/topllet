package openllet.test.mtcq;

import openllet.mtcq.engine.rewriting.DNFTransformer;
import openllet.mtcq.model.query.MTCQFormula;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import org.junit.Test;

public class MTCQTransformerTest extends AbstractMTCQTest
{
    @Test
    public void testNegation()
    {
        simpleTKB();
        MetricTemporalConjunctiveQuery q = temporalQuery("!X(A(a) | (B(b) & C(c)))");
        MTCQFormula tq = DNFTransformer.transform((MTCQFormula) q);  // TODO cast...
        System.out.println(tq);
    }
}
