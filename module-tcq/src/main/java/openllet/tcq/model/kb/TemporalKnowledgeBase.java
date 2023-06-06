package openllet.tcq.model.kb;

import openllet.core.KnowledgeBase;

import java.util.List;

public interface TemporalKnowledgeBase extends List<KnowledgeBase>
{
    // Remark: right now, we require all individuals to be present already in the first knowledge base (otherwise, the
    // query may wrongly interpret atom arguments as answer variables and not as individuals.
    // TODO may add a getIndividuals() here

}
