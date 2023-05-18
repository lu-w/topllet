package openllet.tcq.model.kb.loader;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.modularity.OntologyDiff;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.shared.tools.Log;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

public class IncrementalKnowledgeBaseLoader extends KnowledgeBaseLoader
{
    public static final Logger _logger = Log.getLogger(IncrementalKnowledgeBaseLoader.class);

    private OpenlletReasoner _reasoner = null;
    private KnowledgeBase _lastLoaded;

    @Nullable
    @Override
    public KnowledgeBase load(String fileName)
    {
        Timer timer = new Timer("incrementalLoader");
        timer.start();
        int numChanges = -1;
        OWLOntology ont = KnowledgeBaseLoader.loadOWLOntology(fileName);
        if (_reasoner == null)
        {
            _reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ont);
        }
        if (_lastLoaded != null)
        {
            OWLOntology oldOnt = _reasoner.getOntology();
            OWLOntology newOnt = KnowledgeBaseLoader.loadOWLOntology(fileName);
            final OntologyDiff ontologyDiff = OntologyDiff.diffAxioms(oldOnt.getAxioms(), newOnt.getAxioms());
            if (ontologyDiff.getDiffCount() > 0)
            {
                Collection<OWLOntologyChange> changes = ontologyDiff.getChanges(newOnt);
                numChanges = changes.size();
                _reasoner.ontologiesChanged(new LinkedList<>(changes));
            }
        }
        timer.stop();
        _lastLoaded = _reasoner.getKB();
        _logger.info("Incrementally loaded " + fileName + (numChanges >= 0 ? " (" + numChanges + " changes were propagated)" : " (full load)"));
        _logger.info("Loading took " + timer.getTotal() + " ms");
        return _lastLoaded;
    }
}
