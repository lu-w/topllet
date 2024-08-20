package openllet.mtcq.model.kb;

import openllet.aterm.ATerm;
import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.modularity.OntologyDiff;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * A temporal knowledge base is just a list of atemporal knowledge bases that follow some assumptions:
 * 1. each KB contains exactly the same individuals with the same names (and ontology IRI for those),
 * 2. all of these individuals are already present in the first KB, and
 * 3. all KBs in the list import exactly the same ontologies (especially, the shared TBox).
 * If these assumptions are not met, correctness can not be guaranteed.
 */
public interface TemporalKnowledgeBase extends List<KnowledgeBase>
{
    /**
     * @return The difference from the last accessed KB to the KB accessed before that.
     */
    @Nullable
    OntologyDiff getDiffToLastKB();

    /**
     * Resets the loader of this temporal knowledge base (which may trigger a re-load).
     */
    void resetLoader();

    @Nullable
    DefaultDirectedGraph<ATerm, DefaultEdge> computeAxiomGraph();

    @Nullable
    Collection<ATerm> getConnectedClassesAndRolesInAxiomGraph(Collection<ATerm> classesAndRoles);

    /**
     * @return The knowledge base loaded previously (via get()). null if no loading was done.
     */
    KnowledgeBase getLastLoadedKB();

    /**
     * @return The timer used for timing the loading of knowledge bases. Might be null if no timer was given.
     */
    Timer getTimer();
}
