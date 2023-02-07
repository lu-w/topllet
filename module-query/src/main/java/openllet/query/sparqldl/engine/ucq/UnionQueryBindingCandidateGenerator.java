package openllet.query.sparqldl.engine.ucq;

import openllet.core.utils.Bool;
import openllet.core.utils.Pair;
import openllet.query.sparqldl.model.ResultBinding;
import openllet.query.sparqldl.model.ResultBindingImpl;
import openllet.query.sparqldl.model.ucq.UnionQuery;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class UnionQueryBindingCandidateGenerator implements Iterable<ResultBinding>
{
    protected UnionQuery _query;
    protected Set<Pair<ResultBinding, Bool>> _triedBindingsWithResultInformation;
    protected ResultBinding prevBinding = new ResultBindingImpl();

    UnionQueryBindingCandidateGenerator(UnionQuery query)
    {
        _query = query;
        _triedBindingsWithResultInformation = new HashSet<>();
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