package openllet.query.sparqldl.engine.ucq;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.rbox.Role;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Pair;
import openllet.query.sparqldl.engine.cq.CombinedQueryEngine;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQueryImpl;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.ucq.DisjunctiveQuery;
import openllet.query.sparqldl.model.ucq.CNFQuery;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.shared.tools.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static openllet.core.utils.TermFactory.TOP_OBJECT_PROPERTY;
import static openllet.query.sparqldl.model.cq.QueryPredicate.*;

public class BooleanUnionQueryEngineSimple extends AbstractBooleanUnionQueryEngine
{
    public static final Logger _logger = Log.getLogger(BooleanUnionQueryEngineSimple.class);

    public static long calls = 0;

    @Override
    protected boolean execBooleanABoxQuery(CNFQuery q, ABox abox)
    {
        _abox = abox;

        // 1. PRELIMINARY CONSISTENCY CHECK
        q.getKB().ensureConsistency();

        // 2. TRY TO USE STANDARD CQ ENGINE
        if (OpenlletOptions.UCQ_ENGINE_USE_STANDARD_CQ_ENGINE_IF_POSSIBLE)
        {
            boolean allConjunctsSizeOne = true;
            for (DisjunctiveQuery subQuery : q.getQueries())
                if (subQuery.getAtoms().size() > 1)
                {
                    allConjunctsSizeOne = false;
                    break;
                }
            if (allConjunctsSizeOne)
            {
                ConjunctiveQuery cq = new ConjunctiveQueryImpl(q.getKB(), q.isDistinct());
                for (DisjunctiveQuery subQuery : q.getQueries())
                    if (subQuery.getAtoms().size() == 1)
                        cq.add(subQuery.getAtoms().get(0));
                return !(new CombinedQueryEngine().exec(cq).isEmpty());
            }
        }

        // 3. CHECK ENTAILMENT FOR EACH CONJUNCT
        return isEntailed(q, q.getKB());
    }

    @Override
    protected boolean execBooleanABoxQuery(UnionQuery q)
    {
        // 1. TRY TO USE STANDARD CQ ENGINE
        if (OpenlletOptions.UCQ_ENGINE_USE_STANDARD_CQ_ENGINE_IF_POSSIBLE && q.getQueries().size() == 1)
            return execUnderapproximatingSemanticsBoolean(q);

        // 2. PRELIMINARY CONSISTENCY CHECK
        q.getKB().ensureConsistency();

        // 3. TRY UNDER-APPROXIMATING SEMANTICS
        if (OpenlletOptions.UCQ_ENGINE_USE_UNDERAPPROXIMATING_SEMANTICS)
        {
            boolean someDisjunctEntailed = execUnderapproximatingSemanticsBoolean(q);
            if (someDisjunctEntailed)
            {
                if (_logger.isLoggable(Level.FINE))
                    _logger.fine("Under-approximation returned true, hence some query disjunct is entailed.");
                return true;
            }
        }

        // 4. ROLL-UP UCQ
        UnionQuery rolledUpUnionQuery = q.rollUp();
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Rolled-up union query: " + rolledUpUnionQuery);

        // 5. CONVERT TO CNF
        CNFQuery cnfQuery = rolledUpUnionQuery.toCNF();
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Rolled-up union query in CNF is: " + cnfQuery);

        // 6. CHECK ENTAILMENT FOR EACH CONJUNCT
        return isEntailed(cnfQuery, q.getKB());
    }

    /**
     * Core of the UCQ Engine. Checks whether a given query in conjunctive normal form (CNF) is entailed.
     * @param cnfQuery The query in CNF to check. The list represents the conjunction, each element is a disjunction
     * @return True iff. the given query is entailed by the knowledge base
     */
    private boolean isEntailed(CNFQuery cnfQuery, KnowledgeBase kb)
    {
        calls++;
        boolean isEntailed = true;
        // Query is entailed iff. all conjuncts are entailed
        for (DisjunctiveQuery disjunctiveQuery : cnfQuery.getQueries())
        {
            if (_logger.isLoggable(Level.FINER))
                _logger.finer("Checking disjunctive query: " + disjunctiveQuery);
            // Creates a list with the axioms to check. We have the following cases:
            // - C(a) for individual a and concept C -> disjunctionInd
            // - C(x) for undistinguished variable x and concept C -> disjunctionVar
            // - r(a,b) for individuals a, b and role r -> disjunctionInd (plus rolling up into (∃r.{b})(a))
            // - r(a,x) for individual a and variable x -> disjunctionInd (plus rolling up into (∃r.T)(a))
            // - r(x,b) for individual b and variable x -> disjunctionVar (plus rolling up into (∃r.{b}))
            // - r(x,y) for variables x, y -> disjunctionVar (plus rolling up into (∃r.T))
            List<Pair<ATermAppl, ATermAppl>> disjunctionInd = new ArrayList<>();
            List<ATermAppl> disjunctionVar = new ArrayList<>();
            for (QueryAtom atom : disjunctiveQuery.getAtoms())
            {
                ATermAppl lhs = atom.getArguments().get(0);
                if (atom.getPredicate() == Type)
                {
                    if (kb.isIndividual(lhs))
                        disjunctionInd.add(new Pair<>(lhs, atom.getArguments().get(1)));
                    else
                        disjunctionVar.add(atom.getArguments().get(1));
                }
                else if (atom.getPredicate() == PropertyValue)
                {
                    ConjunctiveQuery tmpQuery = new ConjunctiveQueryImpl(disjunctiveQuery);
                    tmpQuery.add(atom);
                    for (ATermAppl var : disjunctiveQuery.getResultVars())
                        tmpQuery.addResultVar(var);
                    tmpQuery.setDistVars(new EnumMap<>(disjunctiveQuery.getDistVarsWithVarType()));
                    ATermAppl rolledUpAtom = tmpQuery.rollUpTo(lhs, List.of(), false);
                    if (kb.isIndividual(lhs))
                        disjunctionInd.add(new Pair<>(lhs, rolledUpAtom));
                    else // lhs is undist. var
                        disjunctionVar.add(rolledUpAtom);
                }
                else
                    _logger.warning("Disjunctive entailment check can not yet handle " + atom.getPredicate());
            }
            // Case 1: No undistinguished variables in disjunction
            if (disjunctionVar.isEmpty())
            {
                if (_logger.isLoggable(Level.FINER))
                    _logger.finer("No variables in disjunctive query -> checking type of disjunction in A-Box");
                isEntailed = kb.isType(disjunctionInd);
            }
            else
            {
                if (_logger.isLoggable(Level.FINER))
                    _logger.finer("Variables in disjunctive query found");
                final ABox copy = _abox.copy();
                final Role topObjectRole = kb.getRole(TOP_OBJECT_PROPERTY);
                List<ATermAppl> newUCs = new ArrayList<>();
                for (ATermAppl testClass : disjunctionVar)
                {
                    final ATermAppl newUC = ATermUtils.normalize(ATermUtils.makeNot(testClass));
                    final boolean added = topObjectRole.addDomain(newUC, DependencySet.INDEPENDENT);
                    if (added)
                        newUCs.add(newUC);
                    if (_logger.isLoggable(Level.FINER))
                        _logger.finer("Added axiom '" + newUC + " ⊑ T' to T-Box");
                }
                copy.setInitialized(false);
                boolean isConsistent = copy.isConsistent();
                // Case 2: No individuals in disjunction
                if (disjunctionInd.isEmpty())
                {
                    if (_logger.isLoggable(Level.FINER))
                        _logger.finer("No individuals in disjunctive query -> consistency of the extended T-Box");
                    isEntailed = !isConsistent;
                }
                // Case 3: Both individuals and undistinguished variables in disjunction
                else
                {
                    if (_logger.isLoggable(Level.FINER))
                        _logger.finer("Also found individuals in disjunctive query -> type of disjunction in " +
                                "A-Box wrt. extended T-Box");
                    // we only need to check for the type if the T-Box axioms do not lead to inconsistency because if
                    // they do, we have found some axiom in the disjunction that is entailed and can continue the loop
                    if (isConsistent)
                    {
                        if (_logger.isLoggable(Level.FINER))
                            _logger.finer("T-Box is consistent -> forced to check type " + disjunctionInd + " in " +
                                    "A-Box.");
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

    /**
     * Checks whether we find one of the queries of the disjunction is separately entailed - if so, the whole
     * disjunction is entailed.
     * @param q The query to check
     * @return True only if the query is entailed
     */
    private boolean execUnderapproximatingSemanticsBoolean(UnionQuery q)
    {
        return !execUnderapproximatingSemantics(q).isEmpty();
    }

    /**
     * Searches for answers to the disjunctive query by combining all separate answers from its disjuncts. This is an
     * under-approximating semantics.
     * @param q The query to check
     * @return The answers for all its disjuncts
     */
    private QueryResult execUnderapproximatingSemantics(UnionQuery q)
    {
        QueryResult result = new QueryResultImpl(q);
        UnionQuery qCopy = q.copy();
        for (ConjunctiveQuery conjunctiveQuery : qCopy.getQueries())
        {
            QueryResult conjunctiveQueryResult = QueryEngine.execQuery(conjunctiveQuery);
            for (ResultBinding binding : conjunctiveQueryResult)
                result.add(binding);
        }
        return result;
    }

    @Override
    public QueryResult exec(UnionQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        return exec(q);
    }
}
