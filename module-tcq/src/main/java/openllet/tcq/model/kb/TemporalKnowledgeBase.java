package openllet.tcq.model.kb;

import openllet.aterm.ATerm;
import openllet.core.KnowledgeBase;
import openllet.modularity.OntologyDiff;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface TemporalKnowledgeBase extends List<KnowledgeBase>
{
    // Remark: right now, we require all individuals to be present already in the first knowledge base (otherwise, the
    // query may wrongly interpret atom arguments as answer variables and not as individuals.

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
}
