// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;

import openllet.aterm.ATermAppl;
import openllet.jena.JenaUtils;
import openllet.query.sparqldl.model.cq.QueryParameters;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.ResultBinding;

/**
 * <p>
 * Title: ResultSet wrapper for SPARQL-DL results.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Petr Kremen
 * @author Evren Sirin
 */
public class SparqlDLResultSet implements ResultSetRewindable
{
	private final Model _model;
	private final List<ATermAppl> _resultVars;
	private final List<String> _resultVarsString;
	private final QueryResult _queryResult;
	private int _index;
	private Iterator<ResultBinding> _bindings;
	private final Binding _parent;
	private QueryParameters _parameters;

	public SparqlDLResultSet(final QueryResult answers, final Model model)
	{
		this(answers, model, null, null);
	}

	public SparqlDLResultSet(final QueryResult answers, final Model model, final Binding parent)
	{
		this(answers, model, parent, null);
	}

	public SparqlDLResultSet(final QueryResult answers, final Model model, final QueryParameters parameters)
	{
		this(answers, model, null, parameters);
	}

	public SparqlDLResultSet(final QueryResult answers, final Model model, final Binding parent, final QueryParameters parameters)
	{
		_parent = parent;
		_queryResult = answers;
		_model = model;
		_parameters = parameters;
		_index = 0;
		_bindings = answers.iterator();

		_resultVars = new ArrayList<>();
		_resultVarsString = new ArrayList<>();

		for (final ATermAppl var : _queryResult.getResultVars())
		{
			_resultVars.add(var);
			_resultVarsString.add(getVarName(var));
		}

		// Ensure initial _bindings is not a null pointer
		if (parameters == null)
			_parameters = new QueryParameters();
	}

	protected static String getVarName(final ATermAppl term)
	{
		return ((ATermAppl) term.getArgument(0)).getName();
	}

	/**
	 * @return the underlying QueryResults object
	 */
	public QueryResult getQueryResult()
	{
		return _queryResult;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext()
	{
		return _bindings.hasNext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Binding nextBinding()
	{
		_index++;
		final ResultBinding binding = _bindings.next();

		final BindingMap result = _parent == null ? new BindingHashMap() : new BindingHashMap(_parent);

		for (final ATermAppl var : _resultVars)
			if (binding.isBound(var))
			{
				final String varName = getVarName(var);

				final ATermAppl value = binding.getValue(var);

				if (value == null)
					continue;

				JenaUtils.makeGraphNode(value)//
						.ifPresent(node -> result.add(Var.alloc(varName), node));
			}

		if (_resultVars.size() == 0)
			for (final Entry<ATermAppl, ATermAppl> entry : _parameters.entrySet())
			{
				final ATermAppl term = entry.getKey();

				final String varName = getVarName(term);
				final Var var = Var.alloc(varName);

				if (!result.contains(var))
					JenaUtils.makeGraphNode(entry.getValue())//
							.ifPresent(node -> result.add(var, node));
			}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QuerySolution nextSolution()
	{
		return new org.apache.jena.sparql.core.ResultBinding(_model, nextBinding());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QuerySolution next()
	{
		return nextSolution();
	}

	public boolean isDistinct()
	{
		return _queryResult.isDistinct();
	}

	public static boolean isOrdered()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRowNumber()
	{
		return _index;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getResultVars()
	{
		return Collections.unmodifiableList(_resultVarsString);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Remove not supported");
	}

	@Override
	public String toString()
	{
		return _queryResult.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset()
	{
		_index = 0;
		_bindings = _queryResult.iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return _queryResult.size();
	}

	@Override
	public Model getResourceModel()
	{
		return _model;
	}
}
