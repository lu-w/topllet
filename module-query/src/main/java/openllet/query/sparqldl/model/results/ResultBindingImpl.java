// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.results;

import java.util.*;

import openllet.aterm.ATermAppl;

/**
 * <p>
 * Title: Default implementation of the result binding.
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
public class ResultBindingImpl implements ResultBinding
{
	private int _hashCode;
	private int _isDistinct = 0;
	private final Map<ATermAppl, ATermAppl> _bindings = new HashMap<>();

	public ResultBindingImpl()
	{
	}

	private ResultBindingImpl(final Map<ATermAppl, ATermAppl> bindings)
	{
		_bindings.putAll(bindings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(final ATermAppl var, final ATermAppl binding)
	{
		_bindings.put(var, binding);
		_hashCode = 0;
		_isDistinct = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValues(final ResultBinding binding)
	{
		if (binding instanceof ResultBindingImpl)
			_bindings.putAll(((ResultBindingImpl) binding)._bindings);
		else
			for (final ATermAppl var : binding.getAllVariables())
				setValue(var, binding.getValue(var));
		_hashCode = 0;
		_isDistinct = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ATermAppl getValue(final ATermAppl var)
	{
		return _bindings.get(var);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBound(final ATermAppl var)
	{
		return _bindings.containsKey(var);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<ATermAppl> getAllVariables()
	{
		return _bindings.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultBinding duplicate()
	{
		return new ResultBindingImpl(_bindings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void merge(ResultBinding binding)
	{
		for (ATermAppl var : binding.getAllVariables())
			if (!getAllVariables().contains(var))
				setValue(var, binding.getValue(var));
	}

	@Override
	public boolean contains(ResultBinding binding)
	{
		boolean isContained = true;
		if (!equals(binding))
		{
			if (getAllVariables().containsAll(binding.getAllVariables()))
			{
				for (ATermAppl var : binding.getAllVariables())
					if (binding.getValue(var) != getValue(var))
					{
						isContained = false;
						break;
					}
			}
			else
				isContained = false;
		}
		return isContained;
	}

	@Override
	public boolean isDistinct()
	{
		if (_isDistinct == 0)
		{
			Set<ATermAppl> vals = new HashSet<>();
			for (ATermAppl val : _bindings.values())
				if (!vals.add(val))
				{
					_isDistinct = -1;
					return false;
				}
			_isDistinct = 1;
			return true;
		}
		else
			return _isDistinct > 0;
	}

	@Override
	public String toString()
	{
		return _bindings.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty()
	{
		return _bindings.isEmpty();
	}

	@Override
	public int hashCode()
	{
		int result = _hashCode;
		if (result == 0)
		{
			result = 1;
			result = 31 * result + _bindings.hashCode();
			_hashCode = result;
		}
		return result;
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
		final ResultBindingImpl other = (ResultBindingImpl) obj;
		if (_bindings == null)
		{
			if (other._bindings != null)
				return false;
		}
		else
			if (!_bindings.equals(other._bindings))
				return false;
		return true;
	}
}
