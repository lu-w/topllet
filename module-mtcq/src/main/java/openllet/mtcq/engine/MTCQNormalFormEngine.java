package openllet.mtcq.engine;

import openllet.aterm.ATermAppl;
import openllet.core.utils.Pair;
import openllet.mtcq.engine.rewriting.CNFTransformer;
import openllet.mtcq.engine.rewriting.DXNFTransformer;
import openllet.mtcq.engine.rewriting.DXNFVerifier;
import openllet.mtcq.model.kb.InMemoryTemporalKnowledgeBaseImpl;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.mtcq.model.query.*;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                result = answerAtemporal(q, timePoint, candidates, variables);
            }
        }
        System.out.println("Answering at time " + timePoint + " query " + q + ": " + result);

        result.expandToAllVariables(q.getResultVars());
        result.explicate();
        result.retainAll(candidates);

        return result;
    }

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

            List<MetricTemporalConjunctiveQuery> cnf = CNFTransformer.transformToListOfConjuncts(q);

            // TODO sort conjuncts to front that consist of only one non-negated CQ

            for (MetricTemporalConjunctiveQuery query : cnf)
            {
                QueryResult queryResult = answerUCQWithNegations(query, timePoint, candidates, variables);
                if (result == null)
                    result = queryResult;
                else
                    result.retainAll(queryResult);
                result.expandToAllVariables(q.getResultVars());
                result.explicate();
                result.retainAll(candidates);
            }

            //System.out.println("Answering at time " + timePoint + " for atemporal query " + q + ": " + result);

            _cachedResults.put(queryAtTime, result);
        }
        else
        {
            result = _cachedResults.get(queryAtTime);
            //System.out.println("Using cached result at " + timePoint + " for atemporal query " + q + ": " + result);
        }

        return result;
    }

    private QueryResult answerUCQWithNegations(MetricTemporalConjunctiveQuery q, int timePoint, QueryResult candidates,
                                               List<ATermAppl> variables)
    {
        // NOTE: in cq1 v cq2 ... v cqn, some cqi must not be an actual CQ but can be one of:
        //  EndFormula, LastFormula, PropositionallyTrueFormula, PropositionallyFalseFormula, LogicalTrueFormula,
        //  LogicallyFalseFormula, EmptyFormula
        // -> before feeding a UCQ to the UCQ entailment checker, check whether one of these is present, and remove all
        //    of them. Either return all candidates (e.g. if last is actually true), or just run UCQ engine on clean up UCQ
        if (q instanceof LastFormula)
        {
            if (q.getTemporalKB().size() - 1 == timePoint)
            {
                if (candidates != null)
                    return candidates.copy();
                else
                    return new QueryResultImpl(q).invert();
            }
            else
                return new QueryResultImpl(q);
        }
        else
        {
            MetricTemporalConjunctiveQuery atemporalQuery = q.copy();
            atemporalQuery.setResultVars(variables);
            atemporalQuery.setKB(q.getTemporalKB().get(timePoint));
            TemporalKnowledgeBase tkb = new InMemoryTemporalKnowledgeBaseImpl();
            tkb.add(atemporalQuery.getKB());
            if (timePoint < q.getTemporalKB().size() - 1)
                tkb.add(q.getTemporalKB().get(timePoint + 1));
            atemporalQuery.setTemporalKB(tkb);
            QueryResult result = new MTCQEngine().exec(atemporalQuery, null, candidates);
            result.explicate();
            return result;
        }

        /**
        if (q instanceof ConjunctiveQueryFormula cq)
        {
            result = new CombinedQueryEngine().exec(cq.getConjunctiveQuery());
        }
        else if (q instanceof LastFormula)
        {
            if (timePoint == q.getTemporalKB().size())
                result = candidates;
            else
                result = new QueryResultImpl(q);
        }
        else if (q instanceof EndFormula)
        {
            if (timePoint >= q.getTemporalKB().size())
                result = candidates;
            else
                result = new QueryResultImpl(q);
        }
        else if (q instanceof PropositionalTrueFormula)
        {
            result = candidates;
        }
        else if (q instanceof PropositionalFalseFormula)
        {
            result = new QueryResultImpl(q);
        }
        else if (q instanceof LogicalTrueFormula)
        {
            result = candidates;
        }
        else if (q instanceof LogicalFalseFormula)
        {
            result = new QueryResultImpl(q);
        }
        else if (q instanceof EmptyFormula)
        {
            result = new QueryResultImpl(q);
        }
        else if (q instanceof OrFormula or)
        {

        }
        else
            throw new RuntimeException("Found unexpected query: " + q);
         **/
    }
}
