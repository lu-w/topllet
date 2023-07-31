package openllet.tcq.model.kb;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.utils.Timer;
import openllet.core.utils.iterator.IteratorUtils;
import openllet.modularity.OntologyDiff;
import openllet.shared.tools.Log;
import openllet.tcq.model.kb.loader.IncrementalKnowledgeBaseLoader;
import openllet.tcq.model.kb.loader.KnowledgeBaseLoader;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Logger;

public class FileBasedTemporalKnowledgeBaseImpl extends ArrayList<KnowledgeBase> implements TemporalKnowledgeBase
{
    public static final Logger _logger = Log.getLogger(FileBasedTemporalKnowledgeBaseImpl.class);

    private final List<String> _files;
    private KnowledgeBaseLoader _loader;
    private KnowledgeBase _curKb;
    private int _curKbIndex = -1;
    private final String _catalogFile;
    private final Timer _timer;
    private DefaultUndirectedGraph<ATerm, DefaultEdge> _axiomGraph;

    public FileBasedTemporalKnowledgeBaseImpl(Iterable<String> files)
    {
        this(files, null);
    }

    public FileBasedTemporalKnowledgeBaseImpl(Iterable<String> files, String catalogFile)
    {
        this(files, catalogFile, null);
    }

    public FileBasedTemporalKnowledgeBaseImpl(Iterable<String> files, String catalogFile, Timer timer)
    {
        _catalogFile = catalogFile;
        _timer = timer;
        _files = IteratorUtils.toList(files.iterator());
        resetLoader();
    }

    @Override
    public KnowledgeBase get(int index)
    {
        if (index != _curKbIndex || _curKb == null)
            try
            {
                _curKb = _loader.load(_files.get(index));
                _curKbIndex = index;
            }
            catch (OWLOntologyCreationException | FileNotFoundException e)
            {
                throw new IndexOutOfBoundsException(e.toString());
            }
        return _curKb;
    }

    @Override
    public int size()
    {
        return _files.size();
    }

    @Override
    public boolean isEmpty()
    {
        return _files.isEmpty();
    }

    @Override
    public KnowledgeBase set(int i, KnowledgeBase knowledgeBase)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public void add(int i, KnowledgeBase knowledgeBase)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public KnowledgeBase remove(int i)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public boolean add(KnowledgeBase knowledgeBase)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public boolean remove(Object o)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public boolean addAll(Collection<? extends KnowledgeBase> collection)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public boolean addAll(int i, Collection<? extends KnowledgeBase> collection)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public boolean removeAll(Collection<?> collection)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public boolean retainAll(Collection<?> collection)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public void clear()
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Nullable
    @Override
    public OntologyDiff getDiffToLastKB()
    {
        return _loader.getDiffToLastKB();
    }

    @Override
    public void resetLoader()
    {
        if (OpenlletOptions.TCQ_ENGINE_USE_INCREMENTAL_LOADING)
            _loader = new IncrementalKnowledgeBaseLoader(_timer);
        else
            _loader = new KnowledgeBaseLoader(_timer);
        if (_catalogFile != null)
            _loader.addCatalog(_catalogFile);
    }

    @Nonnull
    @Override
    public DefaultUndirectedGraph<ATerm, DefaultEdge> computeAxiomGraph()
    {
        if (_axiomGraph == null)
        {
            if (size() > 0)
                _axiomGraph = get(0).getTBox().computeAxiomGraph();
            else
                _axiomGraph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        }
        return _axiomGraph;
    }

    @Override
    public Collection<ATerm> getConnectedClassesAndRolesInAxiomGraph(Collection<ATerm> classesAndRoles)
    {
        ConnectivityInspector<ATerm, DefaultEdge> conInspector = new ConnectivityInspector<>(computeAxiomGraph());
        Collection<ATerm> connected = new HashSet<>();
        for (ATerm classOrRole : classesAndRoles)
            // Classes or roles not contain in the graph are not used in any axiom, therefore we just ignore them.
            if (_axiomGraph.containsVertex(classOrRole))
                connected.addAll(conInspector.connectedSetOf(classOrRole));
        return connected;
    }
}
