package openllet.query.sparqldl.engine;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.rbox.Role;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Pair;
import openllet.query.sparqldl.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static openllet.core.utils.TermFactory.TOP_OBJECT_PROPERTY;
import static openllet.query.sparqldl.engine.QueryEngine.split;

public class UnionQueryEngine extends AbstractABoxEngineWrapper
{

    @Override
    public QueryResult execABoxQuery(UnionQuery q)
    {
        QueryResult result = new QueryResultImpl(q);
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Exec union ABox query: " + q);

        // 1. ROLL-UP
        UnionQuery rolledUpUnionQuery = new UnionQueryImpl(q);
        for (Query conjunctiveQuery : q.getQueries())
        {
            _logger.finer("Rolling up for conjunctive query: " + conjunctiveQuery);
            // 1. step: Find disjoint parts of the query
            List<Query> splitQueries = split(conjunctiveQuery, true);

            // 2. step: Roll each part up
            Query rolledUpQuery = new QueryImpl(q.getKB(), q.isDistinct());
            for (Query connectedQuery : splitQueries)
            {
                _logger.finer("Connected sub-query of this conjunctive query is: " + connectedQuery);
                final ATermAppl testIndOrVar;
                if (!connectedQuery.getConstants().isEmpty())
                    testIndOrVar = connectedQuery.getConstants().iterator().next();
                else
                    testIndOrVar = connectedQuery.getUndistVars().iterator().next();
                _logger.finer("Test individual/variable is: " + testIndOrVar);
                final ATermAppl testClass = connectedQuery.rollUpTo(testIndOrVar, Collections.emptySet(), false);
                if (_logger.isLoggable(Level.FINEST))
                    _logger.finer("Boolean query: " + testIndOrVar + " -> " + testClass);
                QueryAtom rolledUpAtom = new QueryAtomImpl(QueryPredicate.Type, testIndOrVar, testClass);
                rolledUpQuery.add(rolledUpAtom);
            }
            rolledUpUnionQuery.addQuery(rolledUpQuery);
        }
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Rolled-up union query: " + rolledUpUnionQuery);

        // 2. CNF
        List<DisjunctiveQuery> cnfQuery = rolledUpUnionQuery.toCNF();
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Rolled-up union query in CNF is: " + cnfQuery);

        // 3. TYPE CHECK EACH CONJUNCT
        boolean isEntailed = true;
        for (DisjunctiveQuery disjunctiveQuery : cnfQuery)
        {
            List<Pair<ATermAppl, ATermAppl>> disjunctionInd = new ArrayList<>();
            List<ATermAppl> disjunctionVar = new ArrayList<>();
            for (Query atomicQuery : disjunctiveQuery.getQueries())
                if (atomicQuery.getAtoms().size() == 1)
                    if (q.getKB().getABox().getIndividual(atomicQuery.getAtoms().get(0).getArguments().get(0)) != null)
                        disjunctionInd.add(new Pair<>(atomicQuery.getAtoms().get(0).getArguments().get(0),
                                atomicQuery.getAtoms().get(0).getArguments().get(1)));
                    else
                        // TODO think: can we make the assumption that no two classes refer to the same undist. var?
                        // probably yes, because we rolled everything up before, right?
                        disjunctionVar.add(atomicQuery.getAtoms().get(0).getArguments().get(1));
            // Case 1: no undist. vars in disjunction
            if (disjunctionVar.isEmpty())
                isEntailed &= rolledUpUnionQuery.getKB().isType(disjunctionInd);
            else
            {
                final ABox copy = q.getKB().getABox().copy();
                for (ATermAppl testClass : disjunctionVar)
                {
                    final ATermAppl newUC = ATermUtils.normalize(ATermUtils.makeNot(testClass));
                    final Role topObjectRole = q.getKB().getRole(TOP_OBJECT_PROPERTY);
                    topObjectRole.addDomain(newUC, DependencySet.INDEPENDENT);
                }
                copy.setInitialized(false);
                // Case 2: no individuals in disjunction
                if (disjunctionInd.isEmpty())
                    isEntailed &= !copy.isConsistent();
                // Case 3: both individuals and undist. vars in disjunction
                else
                    isEntailed &= rolledUpUnionQuery.getKB().isType(disjunctionInd);
            }
        }

        if (isEntailed)
            result.add(new ResultBindingImpl());

        return result;
    }

    @Override
    public boolean supports(UnionQuery q)
    {
        return false;
    }
}
