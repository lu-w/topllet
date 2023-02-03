package openllet.query.sparqldl.engine;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
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
        assert(supports(q));

        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Exec ABox query: " + q);

        if (_logger.isLoggable(Level.INFO) && q.disjunctsShareUndistVars())
            _logger.info("Union query " + q + " contains disjuncts that share undistinguished variables. Will treat " +
                    "them as different variables.");

        // 1. PRELIMINARY CONSISTENCY CHECK
        q.getKB().ensureConsistency();

        // 2. TRY UNDER-APPROXIMATING SEMANTICS
        boolean someDisjunctEntailed = execUnderapproximatingSemanticsBoolean(q);
        if (someDisjunctEntailed)
        {
            if (_logger.isLoggable(Level.FINE))
                _logger.fine("Under-approximation returned true, hence some query disjunct is entailed.");
            return true;
        }

        // 3. ROLL-UP UCQ
        UnionQuery rolledUpUnionQuery = q.rollUp();
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Rolled-up union query: " + rolledUpUnionQuery);

        // 4. CONVERT TO CNF
        List<DisjunctiveQuery> cnfQuery = rolledUpUnionQuery.toCNF();
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Rolled-up union query in CNF is: " + cnfQuery);

        // 5. CHECK ENTAILMENT FOR EACH CONJUNCT
        boolean isEntailed = isEntailed(cnfQuery, q.getKB());
        if (_logger.isLoggable(Level.FINE))
            _logger.fine("Query is " + (!isEntailed ? "not" : "") + " entailed.");

        return isEntailed;
    }

    /**
     * Core of the UCQ Engine. Checks whether a given query in conjunctive normal form (CNF) is entailed.
     * @param cnfQuery The query in CNF to check. The list represents the conjunction, each element is a disjunction
     * @return True iff. the given query is entailed by the knowledge base
     */
    private boolean isEntailed(List<DisjunctiveQuery> cnfQuery, KnowledgeBase kb)
    {
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
                    if (kb.getABox().getIndividual(atomicQuery.getAtoms().get(0).getArguments().get(0)) != null)
                        disjunctionInd.add(new Pair<>(atomicQuery.getAtoms().get(0).getArguments().get(0),
                                atomicQuery.getAtoms().get(0).getArguments().get(1)));
                    else
                        // TODO Lukas: can we make the assumption that no two classes refer to the same undist. var?
                        // probably yes, because we rolled everything up before, right?
                        disjunctionVar.add(atomicQuery.getAtoms().get(0).getArguments().get(1));
            // Case 1: No undistinguished variables in disjunction
            if (disjunctionVar.isEmpty())
            {
                _logger.finer("No variables in disjunctive query -> checking type of disjunction in A-Box");
                isEntailed = kb.isType(disjunctionInd);
            }
            else
            {
                _logger.finer("Variables in disjunctive query found");
                final ABox copy = kb.getABox().copy();
                final Role topObjectRole = kb.getRole(TOP_OBJECT_PROPERTY);
                List<ATermAppl> newUCs = new ArrayList<>();
                for (ATermAppl testClass : disjunctionVar)
                {
                    final ATermAppl newUC = ATermUtils.normalize(ATermUtils.makeNot(testClass));
                    final boolean added = topObjectRole.addDomain(newUC, DependencySet.INDEPENDENT);
                    if (added)
                        newUCs.add(newUC);
                    _logger.finer("Added axiom '" + newUC + " ⊑ T' to T-Box");
                }
                copy.setInitialized(false);
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
        return isEntailed;
    }

    public boolean supports(UnionQuery q)
    {
        return !q.hasCycle();
    }

    private boolean execUnderapproximatingSemanticsBoolean(UnionQuery q)
    {
        return !execUnderapproximatingSemantics(q).isEmpty();
    }

    private QueryResult execUnderapproximatingSemantics(UnionQuery q)
    {
        QueryResult result = new QueryResultImpl(q);
        UnionQuery qCopy = q.copy();
        for (Query conjunctiveQuery : qCopy.getQueries())
        {
            QueryResult conjunctiveQueryResult = QueryEngine.exec(conjunctiveQuery);
            for (ResultBinding binding : conjunctiveQueryResult)
                result.add(binding);
        }
        return result;
    }
}
