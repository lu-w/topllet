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

import static openllet.mtcq.engine.rewriting.MTCQSimplifier.*;

public class MTCQNormalFormEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    private final Map<Pair<MetricTemporalConjunctiveQuery, Pair<Integer, QueryResult>>, QueryResult> _cachedResults = new HashMap<>();
    private final BDQEngine _bdqEngine = new BDQEngine();

    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        System.out.println("======= ANSWERING MTCQ " + q + " =================");
        return answerTime(q);
    }

    private QueryResult answerTime(MetricTemporalConjunctiveQuery query)
    {
        Set<ATermAppl> vars = query.getVars();
        TemporalQueryResult temporalResultAt0 = new TemporalQueryResult();
        // elements are of the form: query, candidates to check against, temporal result to write to.
        List<Pair<MetricTemporalConjunctiveQuery, Pair<QueryResult, TemporalQueryResult>>> todoList = new ArrayList<>();
        todoList.add(new Pair<>(query, new Pair<>(null, temporalResultAt0)));

        for (int t = 0; t < query.getTemporalKB().size(); t++)
        {
            List<Pair<MetricTemporalConjunctiveQuery, Pair<QueryResult, TemporalQueryResult>>> nextTodoList = new ArrayList<>();
            for (Pair<MetricTemporalConjunctiveQuery, Pair<QueryResult, TemporalQueryResult>> todo : todoList)
            {
                MetricTemporalConjunctiveQuery q = todo.first;
                QueryResult candidates = todo.second.first;
                TemporalQueryResult result = todo.second.second;
                MetricTemporalConjunctiveQuery transformed = DXNFTransformer.transform(q);
                DXNFVerifier verifier = new DXNFVerifier();
                if (!verifier.verify(transformed))
                    throw new RuntimeException("Unexpected: After transformation, MTCQ is not in normal form. Reason is: " +
                            verifier.getReason());

                System.out.println("Answering at time " + t + " query: " + transformed);
                List<MetricTemporalConjunctiveQuery> flattenedCNF = sort(flattenAnd(transformed));

                for (MetricTemporalConjunctiveQuery conjunct : flattenedCNF)
                {
                    if (!conjunct.isTemporal())
                    {
                        QueryResult atempResult = answerUCQWithNegations(conjunct, t, candidates, vars);
                        result.addNewConjunct(atempResult);
                        if (candidates != null)
                            candidates.retainAll(atempResult);
                        else
                            candidates = atempResult.copy();
                    }
                    else
                    {
                        MetricTemporalConjunctiveQuery tempPart;
                        QueryResult atempOrResult = null;
                        if (conjunct instanceof OrFormula or)
                        {
                            MetricTemporalConjunctiveQuery atempoOrPart;
                            if (!or.getLeftSubFormula().isTemporal())
                            {
                                atempoOrPart = or.getLeftSubFormula();
                                tempPart = or.getRightSubFormula();
                            }
                            else
                            {
                                tempPart = or.getLeftSubFormula();
                                atempoOrPart = or.getRightSubFormula();
                            }
                            atempOrResult = answerUCQWithNegations(atempoOrPart, t, candidates, vars);
                        }
                        else  // must be of StrongNextFormula
                            tempPart = conjunct;
                        if (tempPart instanceof StrongNextFormula XtempPart)
                        {
                            TemporalQueryResult newTemporalResult = new TemporalQueryResult();
                            result.addNewConjunct(atempOrResult, newTemporalResult);
                            QueryResult nextCandidates = null;
                            if (candidates != null)
                            {
                                nextCandidates = candidates.copy();
                                if (atempOrResult != null)
                                    nextCandidates.removeAll(atempOrResult); // already found answer - no need to check anymore
                            }
                            todoList.add(new Pair<>(XtempPart.getSubFormula(),
                                    new Pair<>(nextCandidates, newTemporalResult)));
                        }
                        else
                            throw new RuntimeException("Unexpected temporal operator: " + tempPart.getClass());
                    }
                }
            }
            todoList = nextTodoList;
        }

        // adds empty query result for all things still in to-do list (they exceeded the trace length)
        for (Pair<MetricTemporalConjunctiveQuery, Pair<QueryResult, TemporalQueryResult>> todo : todoList)
            todo.second.second.addNewConjunct(new QueryResultImpl(todo.first));

        return temporalResultAt0.collapse();
    }

    private List<MetricTemporalConjunctiveQuery> sort(List<MetricTemporalConjunctiveQuery> cnf)
    {
        List<MetricTemporalConjunctiveQuery> sorted = new ArrayList<>();
        for (MetricTemporalConjunctiveQuery conjunct : cnf)
            if (conjunct.isTemporal())
                sorted.add(conjunct);
            else if (answerableByCQ(conjunct))
                sorted.add(0, conjunct);
            else if (!sorted.isEmpty())
                sorted.add(1, conjunct);
            else
                sorted.add(0, conjunct);
        return sorted;
    }

    private boolean answerableByCQ(MetricTemporalConjunctiveQuery mtcq)
    {
        if (mtcq instanceof ConjunctiveQueryFormula)
            return true;
        else if (mtcq instanceof OrFormula or)
        {
            int numberOfCQs = 0;
            for (MetricTemporalConjunctiveQuery disjunct : flattenOr(or))
                if (disjunct instanceof ConjunctiveQueryFormula)
                    numberOfCQs++;
            return numberOfCQs <= 1;
        }
        else
            return true;
    }

    private QueryResult answerUCQWithNegations(MetricTemporalConjunctiveQuery q, int timePoint, QueryResult candidates,
                                               Set<ATermAppl> variables)
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
        // expands to all variables (can probably be done more efficiently) - suffices for now
        if (!variables.equals(result.getResultVars()))
        {
            result.expandToAllVariables(variables);
            result.explicate();
        }
        return result;
    }
}
