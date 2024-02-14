package openllet.mtcq.engine.ubcq;

import openllet.aterm.ATermAppl;
import openllet.core.utils.Pair;
import openllet.mtcq.engine.MTCQEngine;
import openllet.mtcq.model.kb.InMemoryTemporalKnowledgeBaseImpl;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.mtcq.model.query.*;
import openllet.mtcq.parser.MTCQBuilder;
import openllet.mtcq.parser.MetricTemporalConjunctiveQueryParser;
import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.ubcq.UBCQQuery;

import java.util.ArrayList;
import java.util.List;

public class UBCQQueryEngineByMTCQ extends AbstractUBCQQueryEngine
{
    @Override
    protected QueryResult execABoxQuery(UBCQQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        if (q.getQueries().isEmpty())
            System.out.println("Got empty UBCQ. No optimization required.");
        else if (q.getQueries().size() == 1)
        {
            boolean optimizable = true;
            for (ConjunctiveQuery cq : q.getQueries().get(0).getNegativeQueries())
                if (!cq.getUndistVars().isEmpty())
                {
                    optimizable = false;
                    break;
                }
            if (optimizable)
                System.out.println("UBCQ " + q + " has only one BCQ and is optimiziable! Yay!");
            else
                System.out.println("UBCQ " + q + " has only one BCQ but is not optimiziable due to undist vars in negated CQs");
        }
        else
        {
            System.out.println("Got complex UBCQ " + q + ". Meh, not optimizable.");
        }
        TemporalKnowledgeBase tkb = new InMemoryTemporalKnowledgeBaseImpl();
        tkb.add(q.getKB());
        StringBuilder mtcqString = new StringBuilder();
        PropositionFactory pf = new PropositionFactory();
        List<Pair<ConjunctiveQuery, Pair<Proposition, String>>> cqsToPropsAndString = new ArrayList<>();
        for (int i = 0; i < q.getQueries().size(); i++)
        {
            mtcqString.append("(");
            BCQQuery bcq = q.getQueries().get(i);
            for (int j = 0; j < bcq.getQueries().size(); j++)
            {
                ConjunctiveQuery cq = bcq.getQueries().get(j);
                StringBuilder cqString = new StringBuilder();
                if (cq.isNegated())
                    mtcqString.append("!");
                mtcqString.append("(");
                for (int k = 0; k < cq.getAtoms().size(); k++)
                {
                    QueryAtom atom = cq.getAtoms().get(k);
                    List<ATermAppl> args = atom.getArguments();
                    switch (atom.getPredicate())
                    {
                        case Type:
                            cqString.append(args.get(1)).append("(").append(args.get(0).getChildAt(0)).append(")");
                            break;
                        case ObjectProperty:
                            cqString.append(args.get(2)).append("(").append(args.get(0).getChildAt(0)).append(",").append(args.get(1).getChildAt(0)).append(")");
                            break;
                    }
                    if (k < cq.getAtoms().size() - 1)
                        cqString.append(" & ");
                }
                Proposition propString = pf.create(cq);
                mtcqString.append(cqString);
                mtcqString.append(")");
                cqsToPropsAndString.add(new Pair<>(cq, new Pair<>(propString, cqString.toString())));
                if (j < bcq.getQueries().size() - 1)
                    mtcqString.append(" & ");
            }
            mtcqString.append(")");
            if (i < q.getQueries().size() - 1)
                mtcqString.append(" | ");
        }

        /**
         * TODO adapt for new MTCQFormula class (or just rewrite completely)
        MetricTemporalConjunctiveQuery mtcq = new MetricTemporalConjunctiveQueryImpl(mtcqString.toString(), tkb, q.isDistinct());
        for (Pair<ConjunctiveQuery, Pair<Proposition, String>> cqToPropsAndString : cqsToPropsAndString)
        {
            ConjunctiveQuery cq = cqToPropsAndString.first;
            Proposition prop = cqToPropsAndString.second.first;
            String cqString = cqToPropsAndString.second.second;
            mtcq.addConjunctiveQuery(prop, cq, cqString);
        }
         **/

        MTCQFormula mtcq = MetricTemporalConjunctiveQueryParser.parse(mtcqString.toString(), tkb, q.isDistinct());
        QueryResult res = new MTCQEngine().exec(mtcq);
        res.explicate();

        System.out.println("Result = " + res);

        return res;
    }
}
