// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.results;

import java.util.*;
import java.util.Map.Entry;

import openllet.aterm.ATermAppl;
import openllet.core.utils.Bool;
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
	private Collection<ResultBinding> _bindings;

	private List<ATermAppl> _resultVars;
	private final Query<?> _query;
	private final QueryParameters _parameters;
	private boolean _isInverted = false;
	private Collection<ATermAppl> _isExpandedWrt = null;

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
		expandToAllVariables(binding.getAllVariables());
		Collection<ResultBinding> bindings = explicate(binding);
		if (!_isInverted)
		{
			for (ResultBinding add : bindings)
				if (!_bindings.contains(add))
					_bindings.add(process(add));
		}
		else
			for (ResultBinding rem : bindings)
				_bindings.remove(process(rem));
	}

	@Override
	public void remove(ResultBinding binding)
	{
		expandToAllVariables(binding.getAllVariables());
		Collection<ResultBinding> bindings = explicate(binding);
		if (!_isInverted)
			for (ResultBinding rem : bindings)
				_bindings.remove(process(rem));
		else
			for (ResultBinding add : bindings)
				if (!_bindings.contains(add))
					_bindings.add(process(add));
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
		if (_isInverted == other._isInverted)
		{
			if (_bindings == null)
			{
				if (other._bindings != null)
					return false;
			}
			else if (!_bindings.equals(other._bindings))
				return false;
			if (_resultVars == null)
				return other._resultVars == null;
			else
				return _resultVars.equals(other._resultVars);
		}
		else
		{
			Set<ResultBinding> allBindings = new HashSet<>(_bindings);
			allBindings.addAll(other._bindings);
			return _resultVars.equals(other._resultVars) && (allBindings.size() == getMaxSize());
		}
	}

	@Override
	public void expandToAllVariables(Collection<ATermAppl> variables)
	{
		if (_isExpandedWrt == null || !_isExpandedWrt.equals(variables))
		{
			for (ATermAppl variable : variables)
				if (!_resultVars.contains(variable))
					_resultVars.add(variable);
			explicate();
			_isExpandedWrt = new HashSet<>(variables);
		}
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
		result = PRIME * result + (_isInverted ? 0 : 1);
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
	public int getMaxSize()
	{
		return (int) Math.pow(getIndividualCount(), getResultVars().size());
	}

	protected int getIndividualCount()
	{
		return _query.getKB().getIndividuals().stream().toList().size();
	}

	@Override
	public Query<?> getQuery()
	{
		return _query;
	}

	@Override
	public Iterator<ResultBinding> iterator()
	{
		if (_isInverted)
			performInversion();
		return _bindings.iterator();
	}

	public void explicate()
	{
		// Explicates all partial bindings for explicit iteration.
		Set<ResultBinding> toRemove = new HashSet<>();
		Set<ResultBinding> toAdd = new HashSet<>();
		for (ResultBinding binding : _bindings)
			if (isPartialBinding(binding))
			{
				toRemove.add(binding);
				toAdd.addAll(explicate(binding));
			}
		_bindings.removeAll(toRemove);
		_bindings.addAll(toAdd);
	}

	protected Collection<ResultBinding> explicate(ResultBinding binding)
	{
		Set<ResultBinding> explicatedBindings = new HashSet<>();
		if (!isPartialBinding(binding))
			explicatedBindings.add(binding);
		else
			for (ResultBinding newBinding : QueryResult.allBindings(
					getUnspecifiedVariablesInBinding(binding, getResultVars()),
					_query.getKB().getIndividuals().stream().toList()))
			{
				newBinding.merge(binding);
				explicatedBindings.add(newBinding);
			}
		return explicatedBindings;
	}

	@Override
	public int size()
	{
		if (!_isInverted)
			return _bindings.size();
		else
			return getMaxSize() - _bindings.size();
	}

	protected int getNumberOfOccurrences(ResultBinding partialBinding)
	{
		int numberOfOccurrences = 0;
		for (ResultBinding binding : _bindings)
			if (binding.contains(partialBinding))
				numberOfOccurrences++;
		return numberOfOccurrences;
	}

	protected List<ATermAppl> getUnspecifiedVariablesInBinding(ResultBinding binding, List<ATermAppl> variables)
	{
		List<ATermAppl> variablesNotInBinding = new ArrayList<>(variables);
		variablesNotInBinding.removeAll(binding.getAllVariables());
		return variablesNotInBinding;
	}

	@Override
	public QueryResult restrictToVariables(List<ATermAppl> vars)
	{
		if (vars.containsAll(_resultVars))
			return copy();
		else
		{
			QueryResultImpl restrictedResult = new QueryResultImpl(_query);
			restrictedResult._resultVars = vars;
			List<ATermAppl> ignoredVars = new ArrayList<>(_resultVars);
			ignoredVars.removeAll(vars);
			int numberOfIgnoredVars = ignoredVars.size();
			// Finds all bindings that are only partially represented w.r.t. the new _resultVars.
			for (ResultBinding binding : _bindings)
			{
				ResultBinding reducedBinding = new ResultBindingImpl();
				for (ATermAppl var : vars)
					reducedBinding.setValue(var, binding.getValue(var));
				if (!restrictedResult.contains(reducedBinding) &&
						getNumberOfOccurrences(reducedBinding) >= Math.pow(getIndividualCount(), numberOfIgnoredVars))
					restrictedResult.add(reducedBinding);
			}
			return restrictedResult;
		}
	}

	@Override
	public QueryResult invert()
	{
		QueryResultImpl inv = new QueryResultImpl(_query);
		for (ResultBinding binding : _bindings)
			inv.add(binding);
		inv._isInverted = !this._isInverted;
		return inv;
	}

	/**
	 * Inverts this query result (i.e., flips all bindings).
	 * Modifies in-place and sets the _isInverted flag accordingly.
	 */
	private void performInversion()
	{
		Collection<ResultBinding> invBindings;
		if (_query.isDistinct())
			invBindings = new HashSet<>();
		else
			invBindings = new ArrayList<>();
		List<ATermAppl> inds = _query.getKB().getIndividuals().stream().toList();
		// If we have a Boolean result, and no binding in the result (i.e., the result is False), we just return True
		if (getResultVars().size() == 0 && _bindings.size() == 0)
			invBindings.add(new ResultBindingImpl());
		// If this result contains all possible bindings anyhow, we skip this and just return the empty result
		else if (size() > 0)
		{
			invBindings = QueryResult.allBindings(_resultVars, inds);
			for (ResultBinding binding : _bindings)
				invBindings.remove(binding);
		}
		_bindings = invBindings;
		_isInverted = !_isInverted;
	}

	@Override
	public boolean contains(ResultBinding binding)
	{
		Collection<ResultBinding> explicated = explicate(binding);
		boolean contains = true;
		for (ResultBinding explBinding : explicated)
			contains &= _bindings.contains(explBinding);
		if (_isInverted)
			contains = !contains;
		return contains;
	}

	@Override
	public boolean isPartialBinding(ResultBinding binding)
	{
		return isPartialBinding(binding, _resultVars);
	}

	protected boolean isPartialBinding(ResultBinding binding, Collection<ATermAppl> variables)
	{
		return binding.getAllVariables().size() < variables.size() &&
				variables.containsAll(binding.getAllVariables());
	}

	@Override
	public boolean isComplete()
	{
		return size() >= getMaxSize();
	}

	@Override
	public void addAll(QueryResult toAdd)
	{
		addAll(toAdd, null);
	}

	@Override
	public void addAll(QueryResult toAdd, QueryResult restrictToBindings)
	{
		if (toAdd != null)
		{
			QueryResult copy = toAdd.copy();
			copy.expandToAllVariables(getResultVars());
			if (restrictToBindings == null)
			{
				for (ResultBinding binding : copy)
					if (!contains(binding))
						add(binding);
			}
			else
			{
				for (ResultBinding restrictToBinding : restrictToBindings)
					for (ResultBinding binding : copy)
						if (restrictToBinding.contains(binding) && !contains(binding))
							add(restrictToBinding);
			}
		}
	}

	@Override
	public void removeAll(QueryResult toRemove)
	{
		if (toRemove != null)
		{
			QueryResult copy = toRemove.copy();
			copy.expandToAllVariables(getResultVars());
			for (ResultBinding binding : copy)
				remove(binding);
		}
	}

	/**
	 * Empties this queries result completely.
	 */
	private void empty()
	{
		if (isDistinct())
			_bindings = new HashSet<>();
		else
			_bindings = new ArrayList<>();
		_isInverted = false;
	}

	@Override
	public void retainAll(QueryResult toRetain)
	{
		if (toRetain != null)
		{
			if (!toRetain.getResultVars().equals(getResultVars()))
				for (ATermAppl var : toRetain.getResultVars())
					if (!_resultVars.contains(var))
						_resultVars.add(var);
			if (toRetain.size() == 0)
				empty();
			else if (!equals(toRetain) && !toRetain.isComplete())
			{
				Set<ResultBinding> toRemove = new HashSet<>();
				QueryResult copy = toRetain.copy();
				copy.expandToAllVariables(getResultVars());
				for (ResultBinding thisBinding : this)
					if (!toRetain.contains(thisBinding))
						toRemove.add(thisBinding);
				for (ResultBinding binding : toRemove)
					remove(binding);
			}
		}
	}

	@Override
	public QueryResult copy()
	{
		QueryResultImpl copy = new QueryResultImpl(_query);
		copy._resultVars = _resultVars;
		if (isDistinct())
			copy._bindings = new HashSet<>(_bindings);
		else
			copy._bindings = new ArrayList<>(_bindings);
		copy._isInverted = _isInverted;
		return copy;
	}

	@Override
	public String toString()
	{
		return (_isInverted ? "Inverted: " : "") + _bindings.toString();
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
