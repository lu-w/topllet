package openllet.tcq.model.kb.loader;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.modularity.OntologyDiff;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.shared.tools.Log;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

public class IncrementalKnowledgeBaseLoader extends KnowledgeBaseLoader
{
    public static final Logger _logger = Log.getLogger(IncrementalKnowledgeBaseLoader.class);

    private OpenlletReasoner _reasoner = null;
    private OWLOntology _prevOnt;

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
            final OntologyDiff ontologyDiff = OntologyDiff.diffOntologies(_prevOnt, newOnt);
            _prevOnt = newOnt;
            if (ontologyDiff.getDiffCount() >= 0)
            {
                Collection<OWLOntologyChange> changes = ontologyDiff.getChanges(newOnt);
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
