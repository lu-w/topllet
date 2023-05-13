package openllet.tcq.model.kb;

import openllet.core.KnowledgeBase;

import java.util.Iterator;

public interface TemporalKnowledgeBase extends Iterator<KnowledgeBase>
{
    KnowledgeBase first();
}
