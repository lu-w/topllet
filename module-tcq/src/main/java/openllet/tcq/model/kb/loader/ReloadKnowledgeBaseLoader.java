package openllet.tcq.model.kb.loader;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.shared.tools.Log;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nullable;
import java.util.logging.Logger;

public class ReloadKnowledgeBaseLoader extends KnowledgeBaseLoader
{
    public static final Logger _logger = Log.getLogger(ReloadKnowledgeBaseLoader.class);

    @Nullable
    @Override
    public KnowledgeBase load(String fileName)
    {
        _logger.info("Loading knowledge base from " + fileName);
        Timer timer = new Timer("reloadLoader");
        timer.start();
        OWLOntology ont = KnowledgeBaseLoader.loadOWLOntology(fileName);
        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ont);
        timer.stop();
        _logger.info("Loading took " + timer.getTotal() + " ms");
        return reasoner.getKB();
    }
}
