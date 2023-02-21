// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.results;

import java.util.*;
import java.util.Map.Entry;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.QueryParameters;

/**
 * <p>
 * Title: Default implementation of {@link QueryResult}
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Petr Kremen
 */
public class QueryResultImpl implements QueryResult
{
	private final Collection<ResultBinding> _bindings;

	private final List<ATermAppl> _resultVars;
	private final Query<?> _query;
	private final QueryParameters _parameters;

	public QueryResultImpl(final Query<?> query)
	{
		_query = query;
		_parameters = query.getQueryParameters();
		_resultVars = new ArrayList<>(query.getResultVars());

		if (query.isDistinct())
			_bindings = new HashSet<>();
		else
			_bindings = new ArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(final ResultBinding binding)
	{
		_bindings.add(process(binding));
	}

	@Override
	public void remove(ResultBinding binding)
	{
		_bindings.remove(process(binding));
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final QueryResultImpl other = (QueryResultImpl) obj;
		if (_bindings == null)
		{
			if (other._bindings != null)
				return false;
		}
		else
			if (!_bindings.equals(other._bindings))
				return false;
		if (_resultVars == null)
			return other._resultVars == null;
		else
			return _resultVars.equals(other._resultVars);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ATermAppl> getResultVars()
	{
		return Collections.unmodifiableList(_resultVars);
	}

	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (_bindings == null ? 0 : _bindings.hashCode());
		result = PRIME * result + (_resultVars == null ? 0 : _resultVars.hashCode());
		return result;
	}

	@Override
	public boolean isDistinct()
	{
		return _bindings instanceof Set;
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public Iterator<ResultBinding> iterator()
	{
		return _bindings.iterator();
	}

	@Override
	public int size()
	{
		return _bindings.size();
	}

	@Override
	public QueryResult invert()
	{
		QueryResult inv = new QueryResultImpl(_query);
		List<ATermAppl> inds = _query.getKB().getIndividuals().stream().toList();
		// If we have a Boolean result, and no binding in the result (i.e., the result is False), we just return True
		if (getResultVars().size() == 0 && _bindings.size() == 0)
			inv.add(new ResultBindingImpl());
		// If this result contains all possible bindings anyhow, we skip this and just return the empty result
		else if (size() < Math.pow(getResultVars().size(), inds.size()))
		{
			inv = QueryResult.allBindings(_query, getResultVars(), inds);
			for (ResultBinding binding : _bindings)
				inv.remove(binding);
		}
		return inv;
	}

	@Override
	public String toString()
	{
		return _bindings.toString();
	}

	private ResultBinding process(final ResultBinding binding)
	{
		if (_parameters == null)
			return binding;

		final int numOfVars = _query.getResultVars().size();

		// Add the _query _parameters to the binding if the variable is in the
		// _query projection
		for (final Entry<ATermAppl, ATermAppl> entry : _parameters.entrySet())
		{
			final ATermAppl var = entry.getKey();
			final ATermAppl value = entry.getValue();

			if (numOfVars == 0 || _query.getResultVars().contains(var))
				binding.setValue(var, value);
		}

		return binding;
	}
}
