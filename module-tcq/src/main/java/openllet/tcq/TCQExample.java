package openllet.tcq;

import java.util.Arrays;

import openllet.tcq.engine.BooleanTCQEngine;
import openllet.tcq.engine.BooleanTCQEngineImpl;
import openllet.tcq.model.kb.TemporalKnowledgeBase;
import openllet.tcq.model.kb.TemporalKnowledgeBaseImpl;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.model.query.TemporalConjunctiveQueryImpl;

public class TCQExample {

    public static void main(final String[] args) {
        // Import TBox & ABoxes
        TemporalKnowledgeBase kb = new TemporalKnowledgeBaseImpl(Arrays.stream(args).toList().subList(1, args.length));

        // Parse TCQ
        TemporalConjunctiveQuery tcq = new TemporalConjunctiveQueryImpl(args[0], kb, false);

        // Run BooleanTCQEngine

        BooleanTCQEngine engine = new BooleanTCQEngineImpl();
        boolean res = engine.exec(tcq);

        // Output
        System.out.println(res);
    }
}
