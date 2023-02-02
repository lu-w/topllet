package openllet.query.sparqldl.engine;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.rbox.Role;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Pair;
import openllet.query.sparqldl.model.*;
import openllet.shared.tools.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static openllet.core.utils.TermFactory.TOP_OBJECT_PROPERTY;
import static openllet.query.sparqldl.engine.QueryEngine.split;

// TODO Lukas: do we need an interface for this? It does not make sense to inherit from AbstractABoxEngineWrapper, right?
public class UnionQueryEngine
{
    public static final Logger _logger = Log.getLogger(UnionQueryEngine.class);

    public QueryResult execABoxQuery(UnionQuery q)
    {
        // TODO Lukas
        return new QueryResultImpl(q.getQueries().get(0));
    }

    public boolean execBooleanABoxQuery(UnionQuery q)
    {
        // TODO Lukas: check for cycles in each disjunct (i.e., implement supports function)
        // TODO Lukas: check assumption that disjuncts do not refer to the same undistinguished variables.
        // TODO Lukas: move the 4 steps to single functions
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Exec ABox query: " + q);

        // PRELIMINARY CONSISTENCY CHECK
        q.getKB().ensureConsistency();

        // 1. ROLL-UP
        UnionQuery rolledUpUnionQuery = new UnionQueryImpl(q);
        for (Query conjunctiveQuery : q.getQueries())
        {
            if (_logger.isLoggable(Level.FINER))
                _logger.finer("Rolling up for conjunctive query: " + conjunctiveQuery);
            // 1. step: Find disjoint parts of the query
            List<Query> splitQueries = split(conjunctiveQuery, true);
            if (_logger.isLoggable(Level.FINER))
            {
                _logger.finer("Split query: " + splitQueries);
                _logger.finer("Now rolling up each separate element.");
            }

            // 2. step: Roll each part up
            Query rolledUpQuery = new QueryImpl(q.getKB(), q.isDistinct());
            for (Query connectedQuery : splitQueries)
            {
                final ATermAppl testIndOrVar;
                if (!connectedQuery.getConstants().isEmpty())
                    testIndOrVar = connectedQuery.getConstants().iterator().next();
                else
                    testIndOrVar = connectedQuery.getUndistVars().iterator().next();
                final ATermAppl testClass = connectedQuery.rollUpTo(testIndOrVar, Collections.emptySet(), false);
                if (_logger.isLoggable(Level.FINER))
                    _logger.finer("Rolled-up Boolean query: " + testIndOrVar + " -> " + testClass);
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
        // Query is entailed iff. all conjuncts are entailed
        for (DisjunctiveQuery disjunctiveQuery : cnfQuery)
        {
            if (_logger.isLoggable(Level.FINER))
                _logger.finer("Checking disjunctive query: " + disjunctiveQuery);
            // Creates a list with the axioms to check (since they are already rolled-up, we only have concepts):
            // - C(a) for individuals a and concepts C -> disjunctionInd
            // - C(x) for undistinguished variables Cx and concepts C -> disjunctionVar
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
            // Case 1: No undistinguished variables in disjunction
            if (disjunctionVar.isEmpty())
            {
                _logger.finer("No variables in disjunctive query -> checking type of disjunction in A-Box");
                isEntailed = rolledUpUnionQuery.getKB().isType(disjunctionInd);
            }
            else
            {
                _logger.finer("Variables in disjunctive query found");
                final ABox copy = q.getKB().getABox().copy();
                final Role topObjectRole = q.getKB().getRole(TOP_OBJECT_PROPERTY);
                List<ATermAppl> newUCs = new ArrayList<>();
                for (ATermAppl testClass : disjunctionVar)
                {
                    final ATermAppl newUC = ATermUtils.normalize(ATermUtils.makeNot(testClass));
                    final boolean added = topObjectRole.addDomain(newUC, DependencySet.INDEPENDENT);
                    if (added)
                        newUCs.add(newUC);
                    _logger.finer("Added axiom '" + newUC + " ⊑ T' to T-Box");
                }
                copy.setInitialized(false); // TODO Lukas: what does this do?
                boolean isConsistent = copy.isConsistent();
                // Case 2: No individuals in disjunction
                if (disjunctionInd.isEmpty())
                {
                    _logger.finer("No individuals in disjunctive query -> consistency of the extended T-Box");
                    isEntailed = !isConsistent;
                }
                // Case 3: Both individuals and undistinguished variables in disjunction
                else
                {
                    _logger.finer("Also found individuals in disjunctive query -> type of disjunction in " +
                            "A-Box wrt. extended T-Box");
                    // we only need to check for the type if the T-Box axioms do not lead to inconsistency because if
                    // they do, we have found some axiom in the disjunction that is entailed and can continue the loop
                    if (isConsistent)
                    {
                        _logger.finest("T-Box is consistent -> forced to check type " + disjunctionInd + " in A-Box.");
                        isEntailed = copy.isType(disjunctionInd);
                    }
                }
                // Re-stores prior state of T-Box
                for (ATermAppl newUC : newUCs)
                    topObjectRole.removeDomain(newUC, DependencySet.INDEPENDENT);
            }
            if (!isEntailed) // Early break if we find that the query can not be entailed anymore.
                break;
        }

        if (isEntailed)
            _logger.fine("Query is entailed.");
        else
            _logger.fine("Query is not entailed.");

        return isEntailed;
    }

    public boolean supports(UnionQuery q)
    {
        return true; // TODO
    }
}
