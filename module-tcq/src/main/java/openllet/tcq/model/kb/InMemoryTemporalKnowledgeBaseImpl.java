package openllet.tcq.model.kb;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.modularity.OntologyDiff;
import openllet.shared.tools.Log;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class InMemoryTemporalKnowledgeBaseImpl extends ArrayList<KnowledgeBase> implements TemporalKnowledgeBase
{
    public static final Logger _logger = Log.getLogger(InMemoryTemporalKnowledgeBaseImpl.class);

    private DefaultUndirectedGraph<ATermAppl, DefaultEdge> _axiomGraph;

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

    @Nullable
    @Override
    public DefaultDirectedGraph<ATerm, DefaultEdge> computeAxiomGraph()
    {
        _logger.fine("In memory temporal knowledge does not support incremental temporal querying");
        return null;
    }

    @Nullable
    @Override
    public Collection<ATerm> getConnectedClassesAndRolesInAxiomGraph(Collection<ATerm> classesAndRoles)
    {
        _logger.fine("In memory temporal knowledge does not support incremental temporal querying");
        return null;
    }
}
