package openllet.tcq.model.kb;

import openllet.core.KnowledgeBase;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.ArrayList;
import java.util.List;

public class TemporalKnowledgeBaseImpl implements TemporalKnowledgeBase
{
    private List<KnowledgeBase> _kbs = new ArrayList<>();

    public TemporalKnowledgeBaseImpl(Iterable<String> files)
    {
        for (String arg : files)
        {
            System.out.println("DEBUG: Loading " + arg);
            //KBLoader loader = new OWLAPILoader();
            //loader.parse(arg);
            //loader.load();
            // TODO add XMLcatalog from protegeproject to parse and manage local catalogs
            // TODO outsource into a Loader class
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            try
            {
                OWLOntology ont = man.loadOntology(IRI.create("file://" + arg));
                OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ont);
                _kbs.add(reasoner.getKB());
            }
            catch (final Exception e)
            {
                System.out.println(e);
            }
        }
    }

    // this implementation shall deliver the new ABox "on the fly" as requested, i.e. it computes the next ABox on demand
    // by using the addType, removeType, etc. operations on the current KnowledgeBase.
    @Override
    public boolean hasNext() {
        return _kbs.iterator().hasNext();
    }

    @Override
    public KnowledgeBase next() {
        return _kbs.iterator().next();
    }
}
