package openllet.mtcq.model.kb.loader;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.modularity.OntologyDiff;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.shared.tools.Log;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Incrementally load the knowledge base based on the previously loaded knowledge base. This is based on the
 * OntologyDiff.diffOntologies of OWLAPI and the standard KnowledgeBaseLoader.
 */
public class IncrementalKnowledgeBaseLoader extends KnowledgeBaseLoader
{
    public static final Logger _logger = Log.getLogger(IncrementalKnowledgeBaseLoader.class);

    private OpenlletReasoner _reasoner;
    private OWLOntology _prevOnt;
    private OntologyDiff _diffToPrevOnt;

    public IncrementalKnowledgeBaseLoader()
    {
        super();
    }

    public IncrementalKnowledgeBaseLoader(Timer timer)
    {
        super(timer);
    }

    @Nullable
    @Override
    public OntologyDiff getDiffToLastKB()
    {
        return _diffToPrevOnt;
    }

    @Nullable
    @Override
    public KnowledgeBase load(String fileName) throws OWLOntologyCreationException, FileNotFoundException
    {
        _timer.start();
        int numChanges = -1;
        if (_reasoner == null)
        {
            _reasoner = OpenlletReasonerFactory.getInstance().createReasoner(super.loadOntology(fileName));
            _prevOnt = _reasoner.getOntology();
        }
        else
        {
            OWLOntology newOnt = super.loadOntology(fileName);
            _diffToPrevOnt = OntologyDiff.diffOntologies(_prevOnt, newOnt);
            _prevOnt = newOnt;
            if (_diffToPrevOnt.getDiffCount() >= 0)
            {
                Collection<OWLOntologyChange> changes = _diffToPrevOnt.getChanges(newOnt);
                numChanges = changes.size();
                boolean success = _reasoner.processChanges(new LinkedList<>(changes));
                if (!success)
                    _logger.warning("Some changes in incrementally loaded ontology could not be processed");
            }
        }
        KnowledgeBase loadedKB = _reasoner.getKB();
        _timer.stop();
        _logger.fine("Incrementally loaded " + fileName + (numChanges >= 0 ? " (" + numChanges +
                " changes were propagated)" : " (full load)"));
        return loadedKB;
    }
}
