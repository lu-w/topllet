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

public class CNFQueryImpl extends AbstractCompositeQuery<DisjunctiveQuery, CNFQuery> implements CNFQuery
{
    public CNFQueryImpl(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    public CNFQueryImpl(Query<?> q)
    {
        super(q.getKB(), q.isDistinct());
    }

    @Override
    protected String getCompositeDelimiter()
    {
        return "&";
    }

    @Override
    public List<CNFQuery> split()
    {
        final DisjointSet<ATermAppl> disjointSet = new DisjointSet<>();
        for (final DisjunctiveQuery conjunct : getQueries())
        {
            ATermAppl toMerge = null;
            for (final QueryAtom atom : conjunct.getAtoms())
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

        List<List<DisjunctiveQuery>> resultToMerge = new ArrayList<>();
        List<DisjunctiveQuery> queriesToAdd = new ArrayList<>(getQueries());
        for (Set<ATermAppl> jointVars : equivalenceSets)
        {
            List<DisjunctiveQuery> queryList = new ArrayList<>();
            Set<DisjunctiveQuery> toRemove = new HashSet<>();
            for (DisjunctiveQuery q : queriesToAdd)
                for (QueryAtom atom : q.getAtoms())
                    if (!Collections.disjoint(atom.getArguments(), jointVars) && !queryList.contains(q))
                    {
                        queryList.add(q.copy());
                        toRemove.add(q);
                    }
            queriesToAdd.removeAll(toRemove);
            resultToMerge.add(queryList);
        }
        // Disjunctive queries that do not belong to some equivalence set are ground -> put into their own class
        if (!queriesToAdd.isEmpty())
            resultToMerge.add(queriesToAdd);

        List<CNFQuery> result = new ArrayList<>();
        for (List<DisjunctiveQuery> disjointCNF : resultToMerge)
        {
            CNFQuery disjointCNFQuery = new CNFQueryImpl(_kb, _distinct);
            disjointCNFQuery.setQueries(disjointCNF);
            result.add(disjointCNFQuery);
        }

        return result;
    }

    public CNFQuery createQuery(KnowledgeBase kb, boolean isDistinct)
    {
        return new CNFQueryImpl(kb, isDistinct);
    }
}
