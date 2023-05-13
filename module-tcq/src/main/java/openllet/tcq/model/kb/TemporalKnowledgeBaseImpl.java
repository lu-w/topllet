package openllet.tcq.model.kb;

import openllet.core.KnowledgeBase;
import openllet.core.utils.iterator.IteratorUtils;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.shared.tools.Log;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class TemporalKnowledgeBaseImpl implements TemporalKnowledgeBase
{
    public static final Logger _logger = Log.getLogger(TemporalKnowledgeBaseImpl.class);

    private int _curKBIndex = -1;
    private KnowledgeBase _curKB;
    private final List<String> _files;
    private boolean _firstCalled = false;

    public TemporalKnowledgeBaseImpl(Iterable<String> files)
    {
        _files = IteratorUtils.toList(files.iterator());
    }

    @Override
    public boolean hasNext()
    {
        return _curKBIndex < _files.size() - 1;
    }

    @Override
    public @Nullable KnowledgeBase next()
    {
        // first() may have already loaded the current KB - do nothing
        if (hasNext() && !(_firstCalled && _curKB != null))
        {
            _curKBIndex++;
            _loadKB(_files.get(_curKBIndex));
        }
        _firstCalled = false;
        return _curKB;
    }

    public @Nullable KnowledgeBase first()
    {
        // only load if we have not already loaded the first one, and there is something to load
        if (_curKBIndex != 0 && _files.size() > 0)
        {
            _curKBIndex = 0;
            _firstCalled = true;
            _loadKB(_files.get(0));
        }
        return _curKB;
    }

    private void _loadKB(String file)
    {
        // TODO this implementation shall deliver the new ABox "on the fly" as requested, i.e. it computes the next
        //  ABox on demand by using the addType, removeType, etc. operations on the current KnowledgeBase.
        _logger.info("Loading ABox from " + file);
        //KBLoader loader = new OWLAPILoader();
        //loader.parse(arg);
        //loader.load();
        // TODO add XMLcatalog from protegeproject to parse and manage local catalogs
        // TODO outsource into a Loader class
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        try
        {
            OWLOntology ont = man.loadOntology(IRI.create("file://" + file));
            OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ont);
            _curKB = reasoner.getKB();
        }
        catch (final Exception e)
        {
            _logger.warning("Can not load ABox: " + e);
            _curKB = null;
        }
    }
}
