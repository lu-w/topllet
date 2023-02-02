package openllet.query.sparqldl.model;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.TermFactory;

import java.util.*;

public class UnionQueryImpl implements UnionQuery
{
    protected static final ATermAppl DEFAULT_NAME = TermFactory.term("query");

    // COMMON PART
    protected ATermAppl _name = DEFAULT_NAME;

    protected KnowledgeBase _kb;

    protected Filter _filter;

    protected List<ATermAppl> _resultVars;

    protected Set<ATermAppl> _allVars;

    // VARIABLES
    protected EnumMap<UnionQuery.VarType, Set<ATermAppl>> _distVars;

    protected Set<ATermAppl> _individualsAndLiterals;

    protected boolean _ground;

    protected final boolean _distinct;

    protected List<Query> _queries;

    protected QueryParameters _parameters;

    public UnionQueryImpl(final KnowledgeBase kb, final boolean distinct)
    {
        _kb = kb;

        _ground = true;
        _queries = new ArrayList<>();
        _resultVars = new ArrayList<>();
        _allVars = new HashSet<>();
        _individualsAndLiterals = new HashSet<>();
        _distVars = new EnumMap<>(UnionQuery.VarType.class);

        for (final UnionQuery.VarType type : UnionQuery.VarType.values())
            _distVars.put(type, new HashSet<ATermAppl>());

        _distinct = distinct;
    }

    public UnionQueryImpl(final UnionQuery query)
    {
        this(query.getKB(), query.isDistinct());

        _name = query.getName();
        _parameters = query.getQueryParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ATermAppl> getDistVarsForType(final UnionQuery.VarType type)
    {
        return _distVars.get(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDistVar(final ATermAppl a, final UnionQuery.VarType type)
    {
        final Set<ATermAppl> set = _distVars.get(type);

        if (!set.contains(a))
            set.add(a);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addResultVar(final ATermAppl a)
    {
        _resultVars.add(a);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ATermAppl> getConstants()
    {
        return Collections.unmodifiableSet(_individualsAndLiterals);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ATermAppl> getDistVars()
    {
        final Set<ATermAppl> result = new HashSet<>();

        for (final UnionQuery.VarType t : UnionQuery.VarType.values())
            result.addAll(_distVars.get(t));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ATermAppl> getUndistVars()
    {
        final Set<ATermAppl> result = new HashSet<>(_allVars);

        result.removeAll(getDistVars());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ATermAppl> getResultVars()
    {
        return Collections.unmodifiableList(_resultVars);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ATermAppl> getVars()
    {
        return Collections.unmodifiableSet(_allVars);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGround()
    {
        return _ground;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnowledgeBase getKB()
    {
        return _kb;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setKB(final KnowledgeBase kb)
    {
        _kb = kb;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Query> getQueries()
    {
        return _queries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQueries(List<Query> queries)
    {
        _queries = queries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnionQuery apply(final ResultBinding binding)
    {
        final UnionQueryImpl query = new UnionQueryImpl(this);

        for (final Query subquery : _queries) {
            final QueryImpl conjunct = new QueryImpl(this.getKB(), this.isDistinct());
            final List<QueryAtom> atoms = new ArrayList<>();

            for (final QueryAtom atom : subquery.getAtoms())
                atoms.add(atom.apply(binding));

            query._resultVars.addAll(_resultVars);
            conjunct.getResultVars().addAll(subquery.getResultVars());
            query._resultVars.removeAll(binding.getAllVariables());
            conjunct.getResultVars().removeAll(binding.getAllVariables());

            for (final UnionQuery.VarType type : UnionQuery.VarType.values())
                for (final ATermAppl atom : getDistVarsForType(type))
                    if (!binding.isBound(atom))
                    {
                        query.addDistVar(atom, type);
                        conjunct.addDistVar(atom, type);
                    }

            for (final QueryAtom atom : atoms)
                conjunct.add(atom);
            query.addQuery(conjunct);
        }

        return query;
    }

    /**
     * {@inheritDoc}
     */
    public void addQuery(final Query query) {
        this._queries.add(query);
    }

    /**
     * Recursive implementation of toCNF.
     * Note: This method creates new query and union query objects, but it does not create new atoms. It rather re-uses
     * the atoms from the original query.
     * TODO Lukas: check whether we need to do a deep copy of the atoms. For now, this should suffice.
     * @param query The input union query (i.e. of the form (a ^ ... ^ b) v ... v (c ^ ... ^ d)) to convert into CNF
     * @return A list of union queries, where each conjunctive query of each union query contains only one atom
     * representing the CNF of the input query
     */
    private List<DisjunctiveQuery> toCNFRec(UnionQuery query)
    {
        List<DisjunctiveQuery> newCnf = new ArrayList<>();
        // Safety check: no queries given
        if (query.getQueries().size() == 0)
            return newCnf;
        // Base case: CNF(a_1 ^ ... ^ a_n) = a_1 ^ ... ^ a_n
        // Return a list of atoms (wrapped in a Query and again in a UnionQuery) to represent this conjunction.
        else if (query.getQueries().size() == 1) {
            for (QueryAtom a : query.getQueries().get(0).getAtoms())
            {
                DisjunctiveQuery singleUnion = new DisjunctiveQueryImpl(query.getKB(), query.isDistinct());
                Query singleQuery = new QueryImpl(query.getQueries().get(0));
                singleQuery.add(a);
                singleUnion.addQuery(singleQuery);
                newCnf.add(singleUnion);
            }
            return newCnf;
        }
        // Recursive case: CNF((a_1 ^ ... ^ a_n) v q) = \bigwedge_{x \in a_1 ... a_n} \bigwedge_{c \in CNF(q)} (x v c)
        else
        {
            // Fetch CNF(q)
            UnionQuery subQuery = new UnionQueryImpl(query.getKB(), query.isDistinct());
            for (Query q : query.getQueries().subList(1, query.getQueries().size()))
                subQuery.addQuery(q);
            List<DisjunctiveQuery> cnf = toCNFRec(subQuery);
            // Create (x v c) for all atoms x and conjuncts c
            for (QueryAtom atom : query.getQueries().get(0).getAtoms())
                for (DisjunctiveQuery conjunct : cnf)
                {
                    DisjunctiveQuery c = new DisjunctiveQueryImpl(query.getKB(), query.isDistinct());
                    for (Query sq : conjunct.getQueries())
                        c.addQuery(sq);
                    Query q = new QueryImpl(query.getKB(), query.isDistinct());
                    q.add(atom);
                    c.addQuery(q);
                    newCnf.add(c);
                }
        }
        return newCnf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DisjunctiveQuery> toCNF()
    {
        return toCNFRec(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnionQuery reorder(int[] queries)
    {
        // TODO Lukas: implement query reordering for UCQs.
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean disjunctsShareUndistVars()
    {
        boolean shared = false;
        if (_queries.size() > 1)
        {
            Set<ATermAppl> undistVars = _queries.get(0).getUndistVars();
            for (Query query : _queries.subList(1, _queries.size()))
                undistVars.retainAll(query.getUndistVars());
            shared = !undistVars.isEmpty();
        }
        return shared;
    }

    @Override
    public String toString()
    {
        return toString(false);
    }

    public String toString(final boolean multiLine)
    {
        final String indent = multiLine ? "     " : " ";
        final StringBuilder sb = new StringBuilder();

        sb.append(ATermUtils.toString(_name)).append("(");
        for (int i = 0; i < _resultVars.size(); i++)
        {
            final ATermAppl var = _resultVars.get(i);
            if (i > 0)
                sb.append(", ");
            sb.append(ATermUtils.toString(var));
        }
        sb.append(")");

        sb.append(" :-");

        List<Query> queries = _queries;
        if (_queries.size() == 0)
            queries = List.of((Query) this);
        for (int i = 0; i < queries.size(); i++)
        {
            final Query query = queries.get(i);
            if (i > 0)
            {
                sb.append(" v");
                if (multiLine)
                    sb.append("\n");
            }
            if (query.getAtoms().size() > 0)
            {
                if (multiLine)
                    sb.append("\n");
                for (int j = 0; j < query.getAtoms().size(); j++) {
                    final QueryAtom a = query.getAtoms().get(j);
                    if (j > 0) {
                        sb.append(",");
                        if (multiLine)
                            sb.append("\n");
                    }

                    sb.append(indent);
                    sb.append(a.toString()); // TODO qNameProvider
                }
            }
        }

        sb.append(".");
        if (multiLine)
            sb.append("\n");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDistinct()
    {
        return _distinct;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Filter getFilter()
    {
        return _filter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFilter(final Filter filter)
    {
        _filter = filter;
    }

    @Override
    public ATermAppl getName()
    {
        return _name;
    }

    @Override
    public void setName(final ATermAppl name)
    {
        _name = name;
    }

    @Override
    public void setQueryParameters(final QueryParameters parameters)
    {
        _parameters = parameters;
    }

    @Override
    public QueryParameters getQueryParameters()
    {
        return _parameters;
    }

}
