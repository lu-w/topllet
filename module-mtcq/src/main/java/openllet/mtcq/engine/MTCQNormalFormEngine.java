package openllet.mtcq.engine;

import openllet.mtcq.engine.rewriting.CNFTransformer;
import openllet.mtcq.engine.rewriting.DXNFTransformer;
import openllet.mtcq.engine.rewriting.DXNFVerifier;
import openllet.mtcq.model.query.*;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;

import java.util.ArrayList;
import java.util.List;

public class MTCQNormalFormEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        System.out.println("======= ANSWERING MTCQ " + q + " =================");
        return answerTime(q, 0, null);
    }

    private QueryResult answerTime(MetricTemporalConjunctiveQuery q, int timePoint, QueryResult candidates)
    {
        q = DXNFTransformer.transform(q);
        DXNFVerifier verifier = new DXNFVerifier();
        if (!verifier.verify(q))
            throw new RuntimeException("Unexpected: After transformation, MTCQ is not in normal form. Reason is: " +
                    verifier.getReason());
        QueryResult result;
        if (candidates != null)
        {
            candidates = candidates.copy();
            q.setResultVars(candidates.getResultVars().stream().toList());
        }

        if (q instanceof StrongNextFormula qNext)
        {
            if (timePoint < q.getTemporalKB().size())
                result = answerTime(qNext.getSubFormula(), timePoint + 1, candidates);
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
                result = answerAtemporal(qOrAtemporal, timePoint, candidates);
                QueryResult nextResult = answerTime(qOrNext, timePoint, candidates);
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
            QueryResult leftResult = answerTime(qAnd.getLeftSubFormula(), timePoint, candidates);
            if (candidates != null)
                candidates.retainAll(leftResult);
            else
                candidates = leftResult;
            result = answerTime(qAnd.getRightSubFormula(), timePoint, candidates);
        }
        else
        {
            result = answerAtemporal(q, timePoint, candidates);
        }

        result.expandToAllVariables(q.getResultVars());
        result.explicate();
        result.retainAll(candidates);

        System.out.println("Answering " + q + " at time " + timePoint + " for candidates " + candidates);
        System.out.println("res = " + result);

        return result;
    }

    private QueryResult answerAtemporal(MetricTemporalConjunctiveQuery q, int timePoint, QueryResult candidates)
    {
        if (q.isTemporal())
            throw new RuntimeException("Got temporal operator for answering an atemporal query: " + q);
        List<MetricTemporalConjunctiveQuery> cnf = CNFTransformer.transformToListOfConjuncts(q);

        // TODO sort conjuncts to front that consist of only one non-negated CQ
        QueryResult result = null;

        for (MetricTemporalConjunctiveQuery query : cnf)
        {
            QueryResult queryResult = answerUCQWithNegations(query, timePoint, candidates);
            if (result == null)
                result = queryResult;
            else
                result.retainAll(queryResult);
            result.expandToAllVariables(q.getResultVars());
        }

        return result;
    }

    private QueryResult answerUCQWithNegations(MetricTemporalConjunctiveQuery q, int timePoint, QueryResult candidates)
    {

        // NOTE: in cq1 v cq2 ... v cqn, some cqi must not be an actual CQ but can be one of:
        //  EndFormula, LastFormula, PropositionallyTrueFormula, PropositionallyFalseFormula, LogicalTrueFormula,
        //  LogicallyFalseFormula, EmptyFormula
        // -> before feeding a UCQ to the UCQ entailment checker, check whether one of these is present, and remove all
        //    of them. Either return all candidates (e.g. if last is actually true), or just run UCQ engine on clean up UCQ
        QueryResult result = new MTCQEngine().exec(q, null, candidates);

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

        return result;
    }
}
