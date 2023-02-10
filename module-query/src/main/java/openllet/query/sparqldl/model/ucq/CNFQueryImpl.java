package openllet.query.sparqldl.model.ucq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.DisjointSet;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;

import java.util.*;

public class CNFQueryImpl extends AbstractCompositeQuery implements CNFQuery
{
    List<DisjunctiveQuery> _queries = new ArrayList<>();

    public CNFQueryImpl(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    @Override
    protected String getCompositeDelimiter()
    {
        return "^";
    }

    @Override
    public CNFQuery copy()
    {
        CNFQuery copy = new CNFQueryImpl(getKB(), isDistinct());
        for (DisjunctiveQuery q : _queries)
            copy.addQuery(q.copy());
        copy.setDistVars(getDistVarsWithVarType());
        copy.setResultVars(getResultVars());
        return copy;
    }

    @Override
    public List<Query> split()
    {
        final DisjointSet<ATermAppl> disjointSet = new DisjointSet<>();
        for (final Query conjunct : getQueries())
        {
            ATermAppl toMerge = null;
            for (final Query disjunct : ((DisjunctiveQuery) conjunct).getQueries())
                for (final QueryAtom atom : ((ConjunctiveQuery) disjunct).getAtoms())
                    for (final ATermAppl arg : atom.getArguments())
                    {
                        if (!(ATermUtils.isVar(arg)))
                            continue;

                        disjointSet.add(arg);
                        if (toMerge != null) // after 1st iteration, add by union to previously added
                            disjointSet.union(toMerge, arg);
                        toMerge = arg;
                    }
        }
        final Collection<Set<ATermAppl>> equivalenceSets = disjointSet.getEquivalanceSets();

        if (equivalenceSets.size() == 1)
            return Collections.singletonList(this);

        List<List<Query>> resultToMerge = new ArrayList<>();
        List<Query> queriesToAdd = new ArrayList<>(getQueries());
        for (Set<ATermAppl> jointVars : equivalenceSets)
        {
            List<Query> queryList = new ArrayList<>();
            Set<Query> toRemove = new HashSet<>();
            for (Query q : queriesToAdd)
                for (QueryAtom atom : ((DisjunctiveQuery) q).getAtoms())
                    if (!Collections.disjoint(atom.getArguments(), jointVars) && !queryList.contains(q))
                    {
                        queryList.add(q.copy());
                        toRemove.add(q);
                    }
            queriesToAdd.removeAll(toRemove);
            resultToMerge.add(queryList);
        }
        // Disjunctive queries that do not belong to some equivalence set are ground -> put into their own class
        if (queriesToAdd.size() > 0)
            resultToMerge.add(queriesToAdd);

        List<Query> result = new ArrayList<>();
        for (List<Query> disjointCNF : resultToMerge)
        {
            CNFQuery disjointCNFQuery = new CNFQueryImpl(_kb, _distinct);
            disjointCNFQuery.setQueries(disjointCNF);
            result.add(disjointCNFQuery);
        }

        return result;
    }

    @Override
    public boolean hasCycle()
    {
        boolean hasCycle = false;
        for (DisjunctiveQuery q : _queries)
            hasCycle |= q.hasCycle();
        return hasCycle;
    }
}
