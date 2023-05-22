package openllet.tcq;

import java.util.Arrays;

import openllet.query.sparqldl.model.results.QueryResult;
import openllet.tcq.engine.BooleanTCQEngine;
import openllet.tcq.model.kb.TemporalKnowledgeBase;
import openllet.tcq.model.kb.TemporalKnowledgeBaseImpl;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.parser.TemporalConjunctiveQueryParser;

public class TCQChecker
{
    public static void main(final String[] args) {
        if (args.length > 1)
        {
            // Import TBox & ABoxes
            TemporalKnowledgeBase kb = new TemporalKnowledgeBaseImpl(
                    Arrays.stream(args).toList().subList(1, args.length));

            // Parse TCQ
            TemporalConjunctiveQuery tcq = TemporalConjunctiveQueryParser.parse(args[0], kb);

            // Run BooleanTCQEngine
            BooleanTCQEngine engine = new BooleanTCQEngine();
            QueryResult res = engine.exec(tcq);

            // Output
            System.out.println("Result: " + !res.isEmpty());
        }
    }
}
