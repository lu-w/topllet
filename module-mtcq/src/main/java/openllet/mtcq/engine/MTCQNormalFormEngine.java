package openllet.mtcq.engine;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.Pair;
import openllet.mtcq.engine.atemporal.BDQEngine;
import openllet.mtcq.engine.rewriting.CNFTransformer;
import openllet.mtcq.engine.rewriting.DXNFTransformer;
import openllet.mtcq.engine.rewriting.DXNFVerifier;
import openllet.mtcq.model.query.*;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;

import java.util.*;

import static openllet.mtcq.engine.rewriting.MTCQSimplifier.flattenOr;
import static openllet.mtcq.engine.rewriting.MTCQSimplifier.makeOr;

public class MTCQNormalFormEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    private final Map<Pair<MetricTemporalConjunctiveQuery, Pair<Integer, QueryResult>>, QueryResult> _cachedResults = new HashMap<>();
    private final BDQEngine _bdqEngine = new BDQEngine();

    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        System.out.println("======= ANSWERING MTCQ " + q + " =================");
        return answerTime(q, 0, null, q.getResultVars());
    }

    private QueryResult answerTime(MetricTemporalConjunctiveQuery query, int timePoint, QueryResult candidates, List<ATermAppl> variables)
    {
        QueryResult result;

        MetricTemporalConjunctiveQuery q = DXNFTransformer.transform(query);
        DXNFVerifier verifier = new DXNFVerifier();
        if (!verifier.verify(q))
            throw new RuntimeException("Unexpected: After transformation, MTCQ is not in normal form. Reason is: " +
                    verifier.getReason());

        System.out.println("Answering at time " + timePoint + " query size " + query.toPropositionalAbstractionString().length());
        result = new QueryResultImpl(q);

        if (candidates == null || !candidates.isEmpty())
        {
            if (candidates != null)
            {
                candidates = candidates.copy();
                candidates.setQuery(q);
                candidates.explicate();
            }
            if (q instanceof StrongNextFormula qNext)
            {
                if (timePoint < q.getTemporalKB().size() - 1)
                    result = answerTime(qNext.getSubFormula(), timePoint + 1, candidates, variables);
                else
                    // empty query result if strong next exceeds trace length
                    result = new QueryResultImpl(q);
            }
            else if (q instanceof OrFormula qOr)
            {
                if (qOr.getLeftSubFormula() instanceof StrongNextFormula ||
                        qOr.getRightSubFormula() instanceof StrongNextFormula)
                {
                    MetricTemporalConjunctiveQuery qOrAtemporal, qOrNext;
                    if (qOr.getLeftSubFormula() instanceof StrongNextFormula)
                    {
                        qOrNext = qOr.getLeftSubFormula();
                        qOrAtemporal = qOr.getRightSubFormula();
                    }
                    else
                    {
                        qOrAtemporal = qOr.getLeftSubFormula();
                        qOrNext = qOr.getRightSubFormula();
                    }
                    QueryResult atemporalResult = answerAtemporal(qOrAtemporal, timePoint, candidates, variables);
                    QueryResult qOrNextCandidates;
                    if (candidates != null && !candidates.isEmpty())
                    {
                        qOrNextCandidates = candidates.copy();
                        qOrNextCandidates.removeAll(atemporalResult);
                    }
                    else
                        qOrNextCandidates = candidates;
                    QueryResult nextResult = answerTime(qOrNext, timePoint, qOrNextCandidates, variables);
                    result.addAll(atemporalResult);
                    result.addAll(nextResult);
                }
                else
                {
                    result = answerAtemporal(q, timePoint, candidates, variables);
                }
            }
            else if (q instanceof AndFormula qAnd)
            {
                // examines atemporal formula first
                MetricTemporalConjunctiveQuery first = qAnd.getLeftSubFormula();
                MetricTemporalConjunctiveQuery second = qAnd.getRightSubFormula();
                if (qAnd.getLeftSubFormula().isTemporal())
                {
                    first = qAnd.getRightSubFormula();
                    second = qAnd.getLeftSubFormula();
                }
                if (qAnd.isTemporal())
                {
                    QueryResult firstResult = answerTime(first, timePoint, candidates, variables);
                    if (candidates != null)
                        candidates.retainAll(firstResult);
                    else
                        candidates = firstResult;
                    result = answerTime(second, timePoint, candidates, variables);
                }
                else
                {
                    result = answerAtemporal(qAnd, timePoint, candidates, variables);
                }
            }
            else
            {
                result = answerAtemporal(q, timePoint, candidates, variables);
            }
        }
        //System.out.println("Answering at time " + timePoint + " query " + q + ": " + result);

        result.expandToAllVariables(q.getResultVars());
        result.explicate();
        result.retainAll(candidates);

        return result;
    }
    // TODO sort conjunRescts to front that consist of only one non-negated CQ (or of UCQs that can be checked independently)
    //  or those that are of the form (A | last/true).

    private QueryResult answerAtemporal(MetricTemporalConjunctiveQuery q, int timePoint, QueryResult candidates,
                                        List<ATermAppl> variables)
    {
        if (q.isTemporal())
            throw new RuntimeException("Got temporal operator for answering an atemporal query: " + q);

        QueryResult result = null;
        Pair<MetricTemporalConjunctiveQuery, Pair<Integer, QueryResult>> queryAtTime =
                new Pair<>(q, new Pair<>(timePoint, candidates));

        if (!_cachedResults.containsKey(queryAtTime))
        {
            KnowledgeBase kb = q.getTemporalKB().get(timePoint);
            q.setKB(kb);
            List<MetricTemporalConjunctiveQuery> cnf = CNFTransformer.transformToListOfConjuncts(q);

            if (candidates != null)
            {
                candidates = candidates.copy();
                candidates.setQuery(q);
                candidates.explicate();
            }


            System.out.println("Answering at time " + timePoint + " for atemporal query " + q.hashCode() + " and candidates " + (candidates != null ? ((float)candidates.size()/candidates.getMaxSize()) * 100 : 100) + " %");

            for (MetricTemporalConjunctiveQuery query : cnf)
            {
                query.setKB(kb);
                QueryResult queryResult = answerUCQWithNegations(query, timePoint, candidates);
                if (result == null)
                    result = queryResult;
                else
                    result.retainAll(queryResult);
                if (result instanceof MultiQueryResults mResult)
                    result = mResult.toQueryResultImpl(query);
                result.expandToAllVariables(variables);
                result.explicate();
                result.retainAll(candidates);
            }

            _cachedResults.put(queryAtTime, result);
        }
        else
        {
            result = _cachedResults.get(queryAtTime);
            //System.out.println("Using cached result at " + timePoint + " for atemporal query " + q + ": " + result);
        }

        return result;
    }

    private QueryResult answerUCQWithNegations(MetricTemporalConjunctiveQuery q, int timePoint, QueryResult candidates)
    {
        QueryResult result;
        KnowledgeBase kb = q.getTemporalKB().get(timePoint);
        List<MetricTemporalConjunctiveQuery> cleanDisjuncts = new ArrayList<>();
        for (MetricTemporalConjunctiveQuery disjunct : flattenOr(q))
        {
            if (disjunct instanceof LastFormula)
            {
                if (disjunct.getTemporalKB().size() - 1 == timePoint)
                {
                    if (candidates != null)
                        return candidates.copy();
                    else
                        return new QueryResultImpl(disjunct).invert();
                }
            }
            else if (disjunct instanceof EndFormula)
            {
                if (timePoint >= disjunct.getTemporalKB().size())
                {
                    if (candidates != null)
                        return candidates.copy();
                    else
                        return new QueryResultImpl(disjunct).invert();
                }
            }
            else if (disjunct instanceof PropositionalTrueFormula || disjunct instanceof LogicalTrueFormula)
            {
                if (candidates != null)
                    return candidates.copy();
                else
                    return new QueryResultImpl(disjunct).invert();
            }
            else if (disjunct instanceof ConjunctiveQueryFormula ||
                    (disjunct instanceof NotFormula not && not.getSubFormula() instanceof ConjunctiveQueryFormula))
            {
                cleanDisjuncts.add(disjunct);
                disjunct.setKB(kb);
            }
            else if (!(disjunct instanceof LogicalFalseFormula || disjunct instanceof EmptyFormula ||
                    disjunct instanceof PropositionalFalseFormula))
                throw new RuntimeException("Invalid query for checking UCQs with negation: " + q);
        }
        if (cleanDisjuncts.size() > 1)
        {
            OrFormula orFormula = makeOr(cleanDisjuncts);
            orFormula.setKB(kb);  // TODO fix correct setting of KB in makeOr()
            result = _bdqEngine.exec(orFormula);
        }
        else if (cleanDisjuncts.size() == 1)
        {
            MetricTemporalConjunctiveQuery one = cleanDisjuncts.get(0);
            if (one instanceof LogicalFalseFormula || one instanceof PropositionalFalseFormula ||
                    one instanceof EmptyFormula)
                result = new QueryResultImpl(one);
            else
                result = _bdqEngine.exec(one);
        }
        else
            // we have a formula of the form "last v end v last v false v false ..." and are not at last or end point.
            //   -> nothing can entail this formula
            result = new QueryResultImpl(q);
        return result;
    }
}
