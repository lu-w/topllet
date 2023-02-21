package openllet.query.sparqldl.engine;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.Bool;
import openllet.core.utils.Pair;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class QueryBindingCandidateGenerator implements Iterable<ResultBinding>
{
    protected Query<?> _query;
    protected Set<Pair<ResultBinding, Bool>> _triedBindingsWithResultInformation;
    protected ResultBinding prevBinding = new ResultBindingImpl();

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
        informAboutResultForBinding(result, prevBinding);
    }

    @Override
    abstract public Iterator<ResultBinding> iterator();
}
