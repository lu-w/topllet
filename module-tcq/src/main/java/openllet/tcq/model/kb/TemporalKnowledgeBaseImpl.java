package openllet.tcq.model.kb;

import openllet.core.KnowledgeBase;
import openllet.core.utils.iterator.IteratorUtils;
import openllet.shared.tools.Log;
import openllet.tcq.model.kb.loader.IncrementalKnowledgeBaseLoader;
import openllet.tcq.model.kb.loader.KnowledgeBaseLoader;
import openllet.tcq.model.kb.loader.ReloadKnowledgeBaseLoader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Logger;

public class TemporalKnowledgeBaseImpl implements TemporalKnowledgeBase
{
    // Remark: right now, we require all individuals to be present already in the first knowledge base (otherwise, the
    // query may wrongly interpret atom arguments as answer variables and not as individuals.

    public static final Logger _logger = Log.getLogger(TemporalKnowledgeBaseImpl.class);

    private int _curKBIndex = -1;
    private KnowledgeBase _curKB;
    private KnowledgeBaseLoader _loader;
    private final List<String> _files;
    private boolean _firstCalled = false;

    public TemporalKnowledgeBaseImpl(Iterable<String> files)
    {
        this(files, LoadingMode.INCREMENTAL);
    }

    public TemporalKnowledgeBaseImpl(Iterable<String> files, LoadingMode loadingMode)
    {
        _files = IteratorUtils.toList(files.iterator());
        // TODO we can cleverly decide which loader to use based on the number of CNCQs estimated and the size of the
        //  ABox... high # CNCQs & small ABox -> inc loader
        switch (loadingMode)
        {
            case DEFAULT -> _loader = new ReloadKnowledgeBaseLoader();
            case INCREMENTAL -> _loader = new IncrementalKnowledgeBaseLoader();
        }
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
            _curKB = _loader.load(_files.get(_curKBIndex));
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
            _curKB = _loader.load(_files.get(0));
        }
        return _curKB;
    }
}
