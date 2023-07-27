// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.results;

import java.util.*;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.Query;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>
 * Title: Result combining more disjoint query results to prevent generating cross-products.
 * </p>
 * <p>
 * Description:
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
public class MultiQueryResults implements QueryResult
{

	private final List<ATermAppl> _resultVars;

	private final List<QueryResult> _queryResults;

	private int _size;

	public MultiQueryResults(final List<ATermAppl> resultVars, final List<QueryResult> queryResults)
	{
		_resultVars = resultVars;
		_queryResults = queryResults;

		_size = 1;
		for (final QueryResult result : queryResults)
			_size *= result.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(final ResultBinding binding)
	{
		throw new UnsupportedOperationException("MultiQueryResults do not support addition!");
	}

	@Override
	public void remove(ResultBinding binding)
	{
		throw new UnsupportedOperationException("MultiQueryResults do not support removal!");
	}

	@Override
	public boolean containsPartialBindings()
	{
		boolean result = true;
		for (QueryResult q : _queryResults)
			result &= q.containsPartialBindings();
		return result;
	}

	@Override
	public void expandToAllVariables(Collection<ATermAppl> variables)
	{
		for (ATermAppl variable : variables)
			if (!_resultVars.contains(variable))
				_resultVars.add(variable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ATermAppl> getResultVars()
	{
		return _resultVars;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDistinct()
	{
		for (final QueryResult result : _queryResults)
			if (!result.isDistinct())
				return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty()
	{
		return _size == 0;
	}

	@Override
	public int getMaxSize()
	{
		if (_queryResults.size() > 0)
			return (int) Math.pow(_queryResults.get(0).getQuery().getKB().getIndividuals().stream().toList().size(),
					getResultVars().size());
		else
			return 0;
	}

	@Override
	public Query<?> getQuery()
	{
		if (_queryResults.size() > 0)
			return _queryResults.get(0).getQuery();
		else
			return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<ResultBinding> iterator()
	{
		return new Iterator<>()
		{
			private final List<Iterator<ResultBinding>> iterators = new ArrayList<>();

			private final List<ResultBinding> bindings = new ArrayList<>();

			private boolean hasNext = init();

			private boolean init()
			{
				for (final QueryResult result : _queryResults)
				{
					final Iterator<ResultBinding> iterator = result.iterator();

					if (!iterator.hasNext())
						return false;

					iterators.add(iterator);
					bindings.add(iterator.next());
				}

				return true;
			}

			private void findNext()
			{
				final ListIterator<Iterator<ResultBinding>> i = iterators.listIterator();

				for (int index = 0; index < iterators.size(); index++)
				{
					Iterator<ResultBinding> iterator = i.next();
					if (iterator.hasNext())
					{
						bindings.set(index, iterator.next());
						return;
					}
					else
						if (index == iterators.size() - 1)
						{
							hasNext = false;
							return;
						}
						else
						{
							iterator = _queryResults.get(index).iterator();
							i.set(iterator);
							bindings.set(index, iterator.next());
						}
				}
			}

			@Override
			public boolean hasNext()
			{
				return hasNext;
			}

			@Override
			public ResultBinding next()
			{
				if (!hasNext())
					throw new NoSuchElementException();

				final ResultBinding result = new ResultBindingImpl();
				for (final ResultBinding binding : bindings)
					result.setValues(binding);

				findNext();

				return result;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return _size;
	}

	@Override
	public QueryResult restrictToVariables(List<ATermAppl> vars)
	{
		List<QueryResult> restrictedResults = new ArrayList<>();
		for (QueryResult res : _queryResults)
			restrictedResults.add(res.restrictToVariables(vars));
		return new MultiQueryResults(vars, restrictedResults);
	}

	@Override
	public QueryResult invert()
	{
		List<QueryResult> results = new ArrayList<>();
		for (QueryResult res : _queryResults)
			results.add(res.invert());
		return new MultiQueryResults(getResultVars(), results);
	}

	@Override
	public boolean contains(ResultBinding binding)
	{
		for (final QueryResult result : _queryResults)
			if (result.contains(binding))
				return true;

		return false;
	}

	@Override
	public boolean isPartialBinding(ResultBinding binding)
	{
		return binding.getAllVariables().size() < getResultVars().size() &&
				new HashSet<>(getResultVars()).containsAll(binding.getAllVariables());
	}

	@Override
	public boolean isComplete()
	{
		throw new UnsupportedOperationException("MultiQueryResults do not support completeness!");
	}

	@Override
	public void addAll(QueryResult toAdd)
	{
		addAll(toAdd, null);
	}

	@Override
	public void addAll(QueryResult toAdd, QueryResult restrictToBindings)
	{
		throw new UnsupportedOperationException("MultiQueryResults do not support addition!");
	}

	@Override
	public void removeAll(QueryResult toRemove)
	{
		throw new UnsupportedOperationException("MultiQueryResults do not support removal!");
	}

	@Override
	public void retainAll(QueryResult toRetain)
	{
		throw new UnsupportedOperationException("MultiQueryResults do not support retaining!");
	}

	@Override
	public QueryResult copy()
	{
		final List<QueryResult> copies = new ArrayList<>();
		for (QueryResult orig : _queryResults)
			copies.add(orig.copy());
		return new MultiQueryResults(new ArrayList<>(_resultVars), copies);
	}

	@Override
	public void explicate()
	{
		for (QueryResult res : _queryResults)
			res.explicate();
	}

	public QueryResultImpl toQueryResultImpl(Query<?> originalQuery)
	{
		QueryResultImpl qr = new QueryResultImpl(originalQuery);
		for (ResultBinding binding : this)
			qr.add(binding);
		return qr;
	}
}
