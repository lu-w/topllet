package openllet.mtcq.model.kb;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.modularity.OntologyDiff;
import openllet.shared.tools.Log;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A simple implementation of the TemporalKnowledgeBase interface. Mainly used for testing. Note that loading all
 * KBs in memory can be highly inefficient for large use cases.
 */
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

    @Override
    public boolean add(KnowledgeBase kb)
    {
        if (this.size() > 0 && kb != null)
        {
            KnowledgeBase prevKb = this.get(this.size() - 1);
            if (kb.getExpressivity().hasNominal())
            {
                throw new RuntimeException("Nominals are not allowed in components TKBs.");
            }
        }
        return super.add(kb);
    }
}
