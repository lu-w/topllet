package openllet.query.sparqldl.model;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.TermFactory;
import openllet.query.sparqldl.model.cq.*;
import openllet.shared.tools.Log;

import java.util.*;
import java.util.logging.Logger;

public abstract class AbstractQuery<QueryType extends Query<QueryType>> implements Query<QueryType>
{
    public static final Logger _logger = Log.getLogger(AbstractQuery.class);

    protected static final ATermAppl DEFAULT_NAME = TermFactory.term("query");

    // COMMON PART
    protected ATermAppl _name = DEFAULT_NAME;

    protected KnowledgeBase _kb;

    protected Filter _filter;

    protected List<ATermAppl> _resultVars;

    protected Set<ATermAppl> _allVars;

    // VARIABLES
    protected EnumMap<VarType, Set<ATermAppl>> _distVars;

    protected Set<ATermAppl> _individualsAndLiterals;

    protected boolean _ground;

    protected final boolean _distinct;

    protected QueryParameters _parameters;

    public AbstractQuery(final KnowledgeBase kb, final boolean distinct)
    {
        _kb = kb;
        _ground = true;
        _resultVars = new ArrayList<>();
        _allVars = new HashSet<>();
        _individualsAndLiterals = new HashSet<>();
        _distVars = new EnumMap<>(VarType.class);

        for (final VarType type : VarType.values())
            _distVars.put(type, new HashSet<>());

        _distinct = distinct;
    }

    public AbstractQuery(final QueryType query)
    {
        this(query.getKB(), query.isDistinct());
        _name = query.getName();
        _parameters = query.getQueryParameters();
    }

    @Override
    public Set<ATermAppl> getDistVarsForType(final VarType type)
    {
        return _distVars.get(type);
    }

    @Override
    public void addDistVar(final ATermAppl a, final VarType type)
    {
        final Set<ATermAppl> set = _distVars.get(type);
        set.add(a);
    }

    @Override
    public void addResultVar(final ATermAppl a)
    {
        _resultVars.add(a);
    }

    @Override
    public void removeResultVar(final ATermAppl a)
    {
        _resultVars.remove(a);
    }

    @Override
    public void setResultVars(final List<ATermAppl> resultVars)
    {
        _resultVars = resultVars;
    }

    @Override
    public void setDistVars(final EnumMap<VarType, Set<ATermAppl>> distVars)
    {
        _distVars = distVars;
    }

    @Override
    public Set<ATermAppl> getConstants()
    {
        return Collections.unmodifiableSet(_individualsAndLiterals);
    }

    @Override
    public Set<ATermAppl> getDistVars()
    {
        final Set<ATermAppl> result = new HashSet<>();
        for (final VarType t : VarType.values())
            result.addAll(_distVars.get(t));
        return result;
    }

    @Override
    public Map<VarType, Set<ATermAppl>> getDistVarsWithVarType()
    {
        return Collections.unmodifiableMap(_distVars);
    }

    @Override
    public Set<ATermAppl> getUndistVars()
    {
        final Set<ATermAppl> result = new HashSet<>(_allVars);
        result.removeAll(getDistVars());
        return result;
    }

    @Override
    public List<ATermAppl> getResultVars()
    {
        return Collections.unmodifiableList(_resultVars);
    }

    @Override
    public Set<ATermAppl> getVars()
    {
        return Collections.unmodifiableSet(_allVars);
    }

    @Override
    public boolean isGround()
    {
        return _ground;
    }

    @Override
    public KnowledgeBase getKB()
    {
        return _kb;
    }

    @Override
    public void setKB(final KnowledgeBase kb)
    {
        _kb = kb;
    }

    @Override
    public boolean isDistinct()
    {
        return _distinct;
    }

    @Override
    public Filter getFilter()
    {
        return _filter;
    }

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
    public QueryType reorder(int[] queries)
    {
        _logger.warning("Reordering not yet implemented for " + this.getClass());
        return copy();
    }

    @Override
    public QueryParameters getQueryParameters()
    {
        return _parameters;
    }

    public QueryType copy()
    {
        QueryType copy = this.createQuery(getKB(), isDistinct());
        copy.setDistVars(new EnumMap<>(getDistVarsWithVarType()));
        copy.setResultVars(new ArrayList<>(getResultVars()));
        return copy;
    }
}
