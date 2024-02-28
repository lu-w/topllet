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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static openllet.mtcq.engine.rewriting.MTCQSimplifier.flattenOr;
import static openllet.mtcq.engine.rewriting.MTCQSimplifier.makeOr;

public class MTCQNormalFormEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    private final Map<Pair<MetricTemporalConjunctiveQuery, Pair<Integer, QueryResult>>, QueryResult> _cachedResults = new HashMap<>();

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

        result = new QueryResultImpl(q);

        if (candidates == null || !candidates.isEmpty())
        {
            if (candidates != null)
                candidates = candidates.copy();
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
                    QueryResult atemporalResult = answerAtemporal(qOrAtemporal, timePoint, candidates);
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
                    result = answerAtemporal(q, timePoint, candidates);
                }
            }
            else if (q instanceof AndFormula qAnd)
            {
                // TODO sort (atemporal first, conjuncts to front that consist of only one non-negated CQ)
                QueryResult leftResult = answerTime(qAnd.getLeftSubFormula(), timePoint, candidates, variables);
                if (candidates != null)
                    candidates.retainAll(leftResult);
                else
                    candidates = leftResult;
                result = answerTime(qAnd.getRightSubFormula(), timePoint, candidates, variables);
            }
            else
            {
                result = answerAtemporal(q, timePoint, candidates);
            }
        }
        //System.out.println("Answering at time " + timePoint + " query " + q + ": " + result);

        result.expandToAllVariables(q.getResultVars());
        result.explicate();
        result.retainAll(candidates);

        return result;
    }

    private QueryResult answerAtemporal(MetricTemporalConjunctiveQuery q, int timePoint, QueryResult candidates)
    {
        if (q.isTemporal())
            throw new RuntimeException("Got temporal operator for answering an atemporal query: " + q);

        QueryResult result = null;
        Pair<MetricTemporalConjunctiveQuery, Pair<Integer, QueryResult>> queryAtTime =
                new Pair<>(q, new Pair<>(timePoint, candidates));

        if (!_cachedResults.containsKey(queryAtTime))
        {
            KnowledgeBase kb = q.getTemporalKB().get(timePoint);
            List<MetricTemporalConjunctiveQuery> cnf = CNFTransformer.transformToListOfConjuncts(q);

            // TODO sort conjuncts to front that consist of only one non-negated CQ (or of UCQs that can be checked independently)
            //  or those that are of the form (A | last/true).

            System.out.println("Answering at time " + timePoint + " for atemporal query " + q);

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
                result.expandToAllVariables(q.getResultVars());
                result.explicate();
                result.retainAll(candidates);
            }
            System.out.println("Answering at time " + timePoint + " for atemporal query " + q + ". Result is: " + result);

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
        System.out.println("Answering UCQ w/ negations: " + q);
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
                if (disjunct.getTemporalKB().size() >= timePoint)
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
        System.out.println("Assembled clean disjuncts " + cleanDisjuncts);
        if (cleanDisjuncts.size() > 1)
        {
            OrFormula orFormula = makeOr(cleanDisjuncts);
            result = new BDQEngine().exec(orFormula);
        }
        else if (cleanDisjuncts.size() == 1)
        {
            MetricTemporalConjunctiveQuery one = cleanDisjuncts.get(0);
            if (one instanceof LogicalFalseFormula || one instanceof PropositionalFalseFormula ||
                    one instanceof EmptyFormula)
                result = new QueryResultImpl(one);
            else
                result = new BDQEngine().exec(one);
        }
        else
            // we have a formula of the form "last v end v last v false v false ..." and are not at last or end point.
            //   -> nothing can entail this formula
            result = new QueryResultImpl(q);
        return result;
    }
}
