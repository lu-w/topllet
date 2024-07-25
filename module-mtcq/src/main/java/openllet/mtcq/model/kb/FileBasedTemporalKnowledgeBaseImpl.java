package openllet.mtcq.model.kb;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.utils.Timer;
import openllet.core.utils.iterator.IteratorUtils;
import openllet.modularity.OntologyDiff;
import openllet.shared.tools.Log;
import openllet.mtcq.model.kb.loader.IncrementalKnowledgeBaseLoader;
import openllet.mtcq.model.kb.loader.KnowledgeBaseLoader;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.base.Sys;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * An implementation of the TemporalKnowledgeBase based on files. This means that on-demand the current KB is loaded
 * from a file. Once a new KB is loaded, the old one is discarded, keeping only on KB at a time in memory.
 * This also implies that some functionality is not supported by this kind of implementation.
 * A file based TKB uses a loader to load the KBs from the files.
 */
public class FileBasedTemporalKnowledgeBaseImpl extends ArrayList<KnowledgeBase> implements TemporalKnowledgeBase
{
    public static final Logger _logger = Log.getLogger(FileBasedTemporalKnowledgeBaseImpl.class);

    private final List<String> _files;
    private KnowledgeBaseLoader _loader;
    private KnowledgeBase _curKb;
    private int _curKbIndex = -1;
    private final String _catalogFile;
    private final Timer _timer;
    private DefaultDirectedGraph<ATerm, DefaultEdge> _axiomGraph;
    private Set<ATermAppl> _prevInds = null;

    static public List<String> parseKBSFile(String kbsFile) throws FileNotFoundException
    {
        if (new File(kbsFile).exists())
        {
            List<String> inputFiles = new ArrayList<>();
            for (String line : IO.readWholeFileAsUTF8(kbsFile).lines().toList())
                if (!line.startsWith("#") && !line.isEmpty())
                {
                    String inputFile = line.strip();
                    if (!new File(inputFile).exists())
                    {
                        Path p = Paths.get(kbsFile);
                        if (p.getParent() != null)
                            // tries folder of .kbs file first
                            inputFile = Paths.get(p.getParent().toString(), inputFile).toString();
                    }
                    File file = new File(inputFile);
                    if (file.exists())
                        inputFiles.add(inputFile);
                    else
                        throw new FileNotFoundException(inputFile);
                }
            return inputFiles;
        }
        else
            throw new FileNotFoundException(kbsFile);
    }

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
                if (_curKb != null && _prevInds == null)
                    _prevInds = _curKb.getIndividuals();
                if (_curKb != null && _curKb.getExpressivity().hasNominal())
                {
                    throw new RuntimeException("Nominals are not allowed in components TKBs.");
                }
            }
            catch (OWLOntologyCreationException | FileNotFoundException e)
            {
                throw new RuntimeException(e.toString());
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
    public boolean addAll(@Nonnull Collection<? extends KnowledgeBase> collection)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public boolean addAll(int i, @Nonnull Collection<? extends KnowledgeBase> collection)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> collection)
    {
        throw new RuntimeException("Manipulation of file based temporal knowledge base not possible");
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> collection)
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
        if (OpenlletOptions.MTCQ_ENGINE_USE_INCREMENTAL_LOADING)
            _loader = new IncrementalKnowledgeBaseLoader(_timer);
        else
            _loader = new KnowledgeBaseLoader(_timer);
        if (_catalogFile != null)
            _loader.addCatalog(_catalogFile);
    }

    @Nonnull
    @Override
    public DefaultDirectedGraph<ATerm, DefaultEdge> computeAxiomGraph()
    {
        if (_axiomGraph == null)
        {
            if (size() > 0)
                _axiomGraph = get(0).getTBox().computeAxiomGraph();
            else
                _axiomGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        }
        return _axiomGraph;
    }

    @Override
    public Collection<ATerm> getConnectedClassesAndRolesInAxiomGraph(Collection<ATerm> classesAndRoles)
    {
        computeAxiomGraph();
        Collection<ATerm> connected = new HashSet<>();
        for (ATerm classOrRole : classesAndRoles)
            // Classes or roles not contain in the graph are not used in any axiom, therefore we just ignore them.
            if (_axiomGraph.containsVertex(classOrRole))
                for (ATerm vertex : _axiomGraph.vertexSet())
                {
                    Collection<ATerm> reach = computeReachableSetDirected(_axiomGraph, vertex);
                    if (reach.contains(classOrRole))
                        connected.add(vertex);
                }
        return connected;
    }

    protected static Collection<ATerm> computeReachableSetDirected(DefaultDirectedGraph<ATerm, DefaultEdge> graph, ATerm node)
    {
        Collection<ATerm> reach = new HashSet<>();
        BreadthFirstIterator<ATerm, DefaultEdge> it = new BreadthFirstIterator<>(graph, node);
        reach.add(node);
        while (it.hasNext())
            reach.add(it.next());
        return reach;
    }
}
