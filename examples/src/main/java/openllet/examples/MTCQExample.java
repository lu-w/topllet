package openllet.examples;
import openllet.core.KnowledgeBase;
import openllet.core.KnowledgeBaseImpl;
import openllet.mtcq.engine.engine_rewriting.MTCQNormalFormEngine;
import openllet.mtcq.model.kb.InMemoryTemporalKnowledgeBaseImpl;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.parser.MetricTemporalConjunctiveQueryParser;
import openllet.mtcq.parser.ParseException;
import openllet.query.sparqldl.model.results.QueryResult;

import java.io.IOException;

import static openllet.core.utils.TermFactory.term;

public class MTCQExample
{
    public static void main(String[] args) throws ParseException, IOException, InterruptedException
    {
        // TKB
        TemporalKnowledgeBase tkb = new InMemoryTemporalKnowledgeBaseImpl();
        tkb.add(new KnowledgeBaseImpl());
        tkb.add(new KnowledgeBaseImpl());
        // TBox
        for (KnowledgeBase kb : tkb)
        {
            kb.addClass(term("A"));
            kb.addClass(term("B"));
            kb.addSubClass(term("B"), term("A"));
        }
        // ABox
        for (KnowledgeBase kb : tkb)
        {
            kb.addIndividual(term("a"));
            kb.addType(term("a"), term("B"));
        }

        // MTCQ
        String formula = "G(A(?x))";
        MetricTemporalConjunctiveQuery mtcq;
        mtcq = MetricTemporalConjunctiveQueryParser.parse(formula, tkb);

        // Answering
        System.out.println("Answering MTCQ " + formula);
        MTCQNormalFormEngine eng = new MTCQNormalFormEngine();
        QueryResult res = eng.exec(mtcq);
        System.out.println(res);
    }
}