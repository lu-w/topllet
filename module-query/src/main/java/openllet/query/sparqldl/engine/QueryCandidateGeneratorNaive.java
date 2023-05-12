package openllet.query.sparqldl.engine;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;

import java.util.*;

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
    public Iterator<ResultBinding> iterator()
    {
        return new Iterator<>()
        {
            private int curPos = (int) Math.pow(_inds.size(), _vars.size());
            private final int[] indexes = new int[Math.max(_inds.size(), _vars.size())];

            @Override
            public boolean hasNext()
            {
                return curPos > 0;
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

                curPos--;
                prevBinding = newBinding;
                return newBinding;
            }
        };
    }
}
