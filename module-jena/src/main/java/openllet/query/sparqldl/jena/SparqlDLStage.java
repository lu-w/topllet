// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.jena;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.apache.jena.sparql.engine.main.StageGeneratorGenericStar;

import openllet.aterm.ATermAppl;
import openllet.core.exceptions.UnsupportedQueryException;
import openllet.core.utils.ATermUtils;
import openllet.jena.PelletInfGraph;
import openllet.jena.graph.loader.GraphLoader;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.query.sparqldl.parser.ARQParser;
import openllet.shared.tools.Log;

/**
 * <p>
 * Description: An implementation of ARQ query stage for PelletInfGraph. The {@link BasicPattern} is converted into a native Pellet SPARQL-DL _query and
 * answered by the Pellet _query engine. The conversion to Pellet query might fail if the _pattern is not a SPARQL-DL query in which case the default ARQ
 * handler is used.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
class SparqlDLStage
{
	public final static Logger _logger = Log.getLogger(SparqlDLStage.class);

	private final ARQParser _parser;

	private final BasicPattern _pattern;
	private Collection<String> _vars;

	public SparqlDLStage(final BasicPattern pattern)
	{
		this(pattern, true);
	}

	public SparqlDLStage(final BasicPattern pattern, final boolean handleVariableSPO)
	{
		_pattern = pattern;
		_parser = new ARQParser(handleVariableSPO);

		initVars();
	}

	private void initVars()
	{
		_vars = new LinkedHashSet<>();
		for (int i = 0; i < _pattern.size(); i++)
		{
			final Triple t = _pattern.get(i);

			if (ARQParser.isDistinguishedVariable(t.getSubject()))
				_vars.add(t.getSubject().getName());
			if (t.getPredicate().isVariable())
				_vars.add(t.getPredicate().getName());
			if (ARQParser.isDistinguishedVariable(t.getObject()))
				_vars.add(t.getObject().getName());
		}
	}

	public QueryIterator build(final QueryIterator input, final ExecutionContext execCxt)
	{
		final Graph graph = execCxt.getActiveGraph();
		if (!(graph instanceof PelletInfGraph))
			throw new UnsupportedOperationException("A Pellet-backed model is required");

		final PelletInfGraph pellet = (PelletInfGraph) graph;

		pellet.prepare();

		final ConjunctiveQuery query = parsePattern(pellet);

		if (query != null)
			return new PelletQueryIterator(pellet, query, input, execCxt);
		else
			return new StageGeneratorGenericStar().execute(_pattern, input, execCxt);
	}

	private ConjunctiveQuery parsePattern(final PelletInfGraph pellet)
	{
		try
		{
			return _parser.parse(_pattern, _vars, pellet.getKB(), false);
		}
		catch (final UnsupportedQueryException e)
		{
			if (_logger.isLoggable(Level.FINE))
				_logger.log(Level.FINE, "Falling back to Jena stage", e);

			return null;
		}
	}

	private static class PelletQueryIterator extends QueryIterRepeatApply
	{
		private final PelletInfGraph _pellet;
		private final ConjunctiveQuery _query;

		public PelletQueryIterator(final PelletInfGraph pellet, final ConjunctiveQuery query, final QueryIterator input, final ExecutionContext execCxt)
		{
			super(input, execCxt);

			_pellet = pellet;
			_query = query;
		}

		private ResultBinding convertBinding(final Binding binding)
		{
			final ResultBinding pelletBinding = new ResultBindingImpl();
			final GraphLoader loader = _pellet.getLoader();
			for (final Iterator<?> vars = binding.vars(); vars.hasNext();)
			{
				final Var var = (Var) vars.next();
				final Node value = binding.get(var);
				if (value != null)
				{
					final ATermAppl pelletVar = ATermUtils.makeVar(var.getVarName());
					final ATermAppl pelletValue = loader.node2term(value);
					pelletBinding.setValue(pelletVar, pelletValue);
				}
			}

			return pelletBinding;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected QueryIterator nextStage(final Binding binding)
		{
			final ConjunctiveQuery newQuery = _query.apply(convertBinding(binding));

			final QueryResult results = QueryEngine.execQuery(newQuery);

			final SparqlDLResultSet resultSet = new SparqlDLResultSet(results, null, binding);

			final QueryIteratorResultSet iter = new QueryIteratorResultSet(resultSet);

			return iter;
		}
	}
}
