package openllet.query.sparqldl.engine;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class QueryCandidateGeneratorNaive extends QueryBindingCandidateGenerator
{
    List<ATermAppl> _vars;
    List<ATermAppl> _inds; // we need a fixed order for enumeration

    public QueryCandidateGeneratorNaive(Query<?> query)
    {
        super(query);
        _vars = query.getResultVars();
        _inds = query.getKB().getIndividuals().stream().toList();
        assert(_inds.size() > 0);
    }

    public QueryCandidateGeneratorNaive(List<ATermAppl> individuals, List<ATermAppl> resultVars)
    {
        super();
        _vars = resultVars;
        _inds = individuals;
    }

    @Override
    public void excludeBindings(QueryResult bindings)
    {
        if (bindings != null)
            _excludeBindings = bindings.restrictToVariables(_vars);
    }

    @Override
    public Iterator<ResultBinding> iterator()
    {
        if (_restrictToBindings == null)
        {
            return new Iterator<>()
            {
                private int curPos = (int) Math.pow(_inds.size(), _vars.size());
                private final int[] indexes = new int[Math.max(_inds.size(), _vars.size())];

                @Override
                public boolean hasNext()
                {
                    return curPos > (_excludeBindings == null ? 0 : _excludeBindings.size());
                }

                @Override
                public ResultBinding next()
                {
                    // https://stackoverflow.com/a/40101377/4145563
                    ResultBinding newBinding = new ResultBindingImpl();

                    for (int i = 0; i < _vars.size(); i++)
                        newBinding.setValue(_vars.get(i), _inds.get(indexes[i]));

                    for (int i = 0; i < _vars.size(); i++)
                    {
                        if (indexes[i] >= _inds.size() - 1)
                            indexes[i] = 0;
                        else
                        {
                            indexes[i]++;
                            break;
                        }
                    }

                    _prevBinding = newBinding;
                    if (_excludeBindings != null && _excludeBindings.contains(newBinding))
                    {
                        if (hasNext())
                            return next();
                        else
                            return null;
                    }
                    else
                    {
                        curPos--;
                        return newBinding;
                    }
                }
            };
        }
        else
        {
            List<ResultBinding> bindings = StreamSupport.stream(
                    _restrictToBindings.spliterator(), false).collect(Collectors.toList());
            final Iterator<ResultBinding> bindingsIterator;
            if (_excludeBindings != null)
            {
                final Set<ResultBinding> excludeBindingSet = StreamSupport.stream(
                        _excludeBindings.spliterator(), false).collect(Collectors.toSet());
                bindings.removeAll(excludeBindingSet);
                bindingsIterator = bindings.iterator();
            }
            else
                bindingsIterator = _restrictToBindings.iterator();

            return new Iterator<>()
            {
                @Override
                public boolean hasNext()
                {
                    return bindingsIterator.hasNext();
                }

                @Override
                public ResultBinding next()
                {
                    ResultBinding newBinding = bindingsIterator.next();
                    _prevBinding = newBinding;
                    return newBinding;
                }
            };
        }
    }
}
