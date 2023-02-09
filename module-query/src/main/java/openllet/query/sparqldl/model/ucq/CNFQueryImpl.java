package openllet.query.sparqldl.model.ucq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.ResultBinding;

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
        return copy;
    }

    @Override
    public List<Query> split()
    {
        // TODO Lukas
        /**final Set<ATermAppl> resultVars = new HashSet<>(getResultVars());

        final DisjointSet<ATermAppl> disjointSet = new DisjointSet<>();

        for (final DisjunctiveQuery q : getQueries())
        {
            for (final QueryAtom atom : q.getQueries().get(0).getAtoms())
            {
                ATermAppl toMerge = null;

                for (final ATermAppl arg : atom.getArguments())
                {
                    if (!(ATermUtils.isVar(arg)))
                        continue;

                    disjointSet.add(arg);
                    if (toMerge != null)
                        disjointSet.union(toMerge, arg);
                    toMerge = arg;
                }
            }
        }

        final Collection<Set<ATermAppl>> equivalenceSets = disjointSet.getEquivalanceSets();
        if (equivalenceSets.size() == 1)
            return Collections.singletonList(this);

        final Map<ATermAppl, CNFQuery> queries = new HashMap<>();
        CNFQuery groundQuery = null;
        for (final QueryAtom atom : getAtoms())
        {
            ATermAppl representative = null;
            for (final ATermAppl arg : atom.getArguments())
                if (ATermUtils.isVar(arg))
                {
                    representative = disjointSet.find(arg);
                    break;
                }

            CNFQuery newQuery;
            if (representative == null)
            {
                if (groundQuery == null)
                    groundQuery = new CNFQueryImpl(this.getKB(), this.isDistinct());
                newQuery = groundQuery;
            }
            else
            {
                newQuery = queries.get(representative);
                if (newQuery == null)
                {
                    newQuery = new CNFQueryImpl(this.getKB(), this.isDistinct());
                    queries.put(representative, newQuery);
                }
                for (final ATermAppl arg : atom.getArguments())
                {
                    if (resultVars.contains(arg))
                        newQuery.addResultVar(arg);

                    for (final VarType v : VarType.values())
                        if (getDistVarsForType(v).contains(arg))
                            newQuery.addDistVar(arg, v);
                }
            }

            newQuery.add(atom);
        }

        final List<Query> list = new ArrayList<>(queries.values());

        if (groundQuery != null)
            list.add(0, groundQuery);

        return list;**/
        _logger.warning("Splitting CNFQuery is not yet implemented.");
        return List.of(this);
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
