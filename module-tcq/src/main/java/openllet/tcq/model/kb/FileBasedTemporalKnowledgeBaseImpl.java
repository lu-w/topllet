package openllet.tcq.model.kb;

import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.utils.Timer;
import openllet.core.utils.iterator.IteratorUtils;
import openllet.shared.tools.Log;
import openllet.tcq.model.kb.loader.IncrementalKnowledgeBaseLoader;
import openllet.tcq.model.kb.loader.KnowledgeBaseLoader;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Logger;

public class FileBasedTemporalKnowledgeBaseImpl implements TemporalKnowledgeBase
{
    // Remark: right now, we require all individuals to be present already in the first knowledge base (otherwise, the
    // query may wrongly interpret atom arguments as answer variables and not as individuals.

    public static final Logger _logger = Log.getLogger(FileBasedTemporalKnowledgeBaseImpl.class);

    private int _curKBIndex = -1;
    private KnowledgeBase _curKB;
    private final KnowledgeBaseLoader _loader;
    private final List<String> _files;
    private boolean _firstCalled = false;

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
        _files = IteratorUtils.toList(files.iterator());
        // TODO we can cleverly decide which loader to use based on the number of CNCQs estimated and the size of the
        //  ABox... high # CNCQs & small ABox -> inc loader
        if (OpenlletOptions.TCQ_ENGINE_USE_INCREMENTAL_LOADING)
            _loader = new IncrementalKnowledgeBaseLoader(timer);
        else
            _loader = new KnowledgeBaseLoader(timer);
        if (catalogFile != null)
            _loader.addCatalog(catalogFile);
    }

    @Override
    public boolean hasNext()
    {
        return (_curKBIndex < _files.size() - 1) || (_firstCalled && !_files.isEmpty());
    }

    @Override
    public @Nullable KnowledgeBase next()
    {
        // first() may have already loaded the current KB - then do nothing
        if (hasNext() && !(_firstCalled && _curKB != null))
        {
            _curKBIndex++;
            try
            {
                _curKB = _loader.load(_files.get(_curKBIndex));
            }
            catch (OWLOntologyCreationException | FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }
        else if (!hasNext())
        {
            _curKB = null;
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
            try
            {
                _curKB = _loader.load(_files.get(0));
            }
            catch (OWLOntologyCreationException | FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }
        return _curKB;
    }

    @Override
    public void add(KnowledgeBase kb)
    {
        // TODO Lukas: implement
        throw new NoSuchMethodError("Adding not yet possible for file based temporal knowledge bases");
    }

    @Override
    public void reset()
    {
        _curKBIndex = -1;
        _curKB = null;
    }
}
