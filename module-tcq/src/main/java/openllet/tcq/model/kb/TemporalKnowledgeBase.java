package openllet.tcq.model.kb;

import openllet.core.KnowledgeBase;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.Iterator;

public interface TemporalKnowledgeBase extends Iterator<KnowledgeBase>
{
    enum LoadingMode
    {
        DEFAULT, INCREMENTAL;
    }

    KnowledgeBase first();
}
