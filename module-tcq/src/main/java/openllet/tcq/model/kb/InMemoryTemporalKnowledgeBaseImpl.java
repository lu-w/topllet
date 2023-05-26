package openllet.tcq.model.kb;

import openllet.core.KnowledgeBase;

import java.util.ArrayList;
import java.util.List;

public class InMemoryTemporalKnowledgeBaseImpl implements TemporalKnowledgeBase
{
    private final List<KnowledgeBase> _kbs;
    private int _curKBIndex = 0;

    public InMemoryTemporalKnowledgeBaseImpl()
    {
        _kbs = new ArrayList<>();
    }

    InMemoryTemporalKnowledgeBaseImpl(int size)
    {
        _kbs = new ArrayList<>(size);
    }

    @Override
    public KnowledgeBase first()
    {
        KnowledgeBase kb = null;
        if (_kbs.size() > 0)
            kb = _kbs.get(0);
        _curKBIndex = 0;
        return kb;
    }

    @Override
    public void add(KnowledgeBase kb)
    {
        _kbs.add(kb);
    }

    @Override
    public void reset()
    {
        _curKBIndex = 0;
    }

    @Override
    public boolean hasNext()
    {
        return _curKBIndex < _kbs.size() - 1;
    }

    @Override
    public KnowledgeBase next()
    {
        KnowledgeBase kb = null;
        if (hasNext())
        {
            kb = _kbs.get(_curKBIndex);
            _curKBIndex++;
        }
        return kb;
    }
}
