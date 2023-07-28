package openllet.query.sparqldl.engine;

import openllet.core.utils.Bool;
import openllet.core.utils.Pair;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class QueryBindingCandidateGenerator implements Iterable<ResultBinding>
{
    protected Query<?> _query;
    protected Set<Pair<ResultBinding, Bool>> _triedBindingsWithResultInformation;
    protected ResultBinding _prevBinding = new ResultBindingImpl();
    protected @Nullable QueryResult _excludeBindings;
    protected @Nullable QueryResult _restrictToBindings;

    public QueryBindingCandidateGenerator()
    {
        _triedBindingsWithResultInformation = new HashSet<>();
    }

    public QueryBindingCandidateGenerator(Query<?> query)
    {
        this();
        _query = query;
    }

    public void informAboutResultForBinding(Bool result, ResultBinding binding)
    {
        _triedBindingsWithResultInformation.add(new Pair<>(binding, result));
    }

    public void informAboutResultForBinding(Bool result)
    {
        informAboutResultForBinding(result, _prevBinding);
    }

    public void excludeBindings(QueryResult bindings)
    {
        if (bindings != null)
            // We maybe manipulate bindings later; copy to avoid side effects.
            _excludeBindings = bindings.copy();
    }

    public void restrictToBindings(QueryResult bindings)
    {
        if (bindings != null)
        {
            if (_restrictToBindings == null)
                // We maybe manipulate bindings later; copy to avoid side effects.
                _restrictToBindings = bindings.copy();
            else
                _restrictToBindings.retainAll(bindings);
        }
    }

    public void doNotExcludeBindings()
    {
        _excludeBindings = null;
    }

    public void doNotRestrictToBindings()
    {
        _restrictToBindings = null;
    }

    @Override
    abstract public Iterator<ResultBinding> iterator();
}
