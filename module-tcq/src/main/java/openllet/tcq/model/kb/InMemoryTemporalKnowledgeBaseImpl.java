package openllet.tcq.model.kb;

import openllet.core.KnowledgeBase;
import openllet.modularity.OntologyDiff;
import openllet.shared.tools.Log;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class InMemoryTemporalKnowledgeBaseImpl extends ArrayList<KnowledgeBase> implements TemporalKnowledgeBase
{
    public static final Logger _logger = Log.getLogger(InMemoryTemporalKnowledgeBaseImpl.class);

    @Nullable
    @Override
    public OntologyDiff getDiffToLastKB()
    {
        _logger.fine("In memory temporal knowledge base does not support difference operation");
        return null;
    }

    @Override
    public void resetLoader()
    {
        _logger.fine("In memory temporal knowledge base does not have to re-load");
    }
}
