package openllet.query.sparqldl.engine.ucq;

import openllet.query.sparqldl.engine.AbstractBooleanQueryEngine;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.ucq.CNFQuery;
import openllet.shared.tools.Log;

import java.util.logging.Logger;

abstract public class AbstractBooleanUnionQueryEngine extends AbstractBooleanQueryEngine<UnionQuery>
{
    public static final Logger _logger = Log.getLogger(BooleanUnionQueryEngineSimple.class);

    /**
     * Executes the entailment check of the given Boolean query in CNF. Complementary to execBooleanABoxQuery for
     * UnionQuery, but directly runs on the CNF.
     * @param q The CNF query to execute
     * @return True iff. the Boolean CNF query is entailed in its knowledge base
     */
    abstract protected boolean execBooleanABoxQuery(CNFQuery q);
}
