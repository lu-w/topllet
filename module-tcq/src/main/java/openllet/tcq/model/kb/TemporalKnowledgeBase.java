package openllet.tcq.model.kb;

import openllet.core.KnowledgeBase;
import openllet.modularity.OntologyDiff;

import javax.annotation.Nullable;
import java.util.List;

public interface TemporalKnowledgeBase extends List<KnowledgeBase>
{
    // Remark: right now, we require all individuals to be present already in the first knowledge base (otherwise, the
    // query may wrongly interpret atom arguments as answer variables and not as individuals.
    // TODO may add a getIndividuals() here

    @Nullable
    OntologyDiff getDiffToLastKB();

    /**
     * Resets the loader of this temporal knowledge base (which may trigger a re-load).
     */
    void resetLoader();
}
