// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.engine.cq;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.cq.QueryPredicate;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.shared.tools.Log;

/**
 * <p>
 * Title: SimpleQueryEngine
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
public class OptimizedQueryEngine2 extends AbstractABoxEngineWrapper
{
	@SuppressWarnings("hiding")
	public static final Logger _logger = Log.getLogger(QueryEngine.class);

	private QueryResult _results;

	private KnowledgeBase _kb;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(final ConjunctiveQuery q)
	{
		return !q.getDistVars().isEmpty();
	}

	private void exec(final ConjunctiveQuery q, final ResultBinding binding, final boolean first)
	{
		if (q.getDistVars().isEmpty())
		{
			_results.add(binding);
			return;
		}

		final Iterator<ATermAppl> i = q.getDistVars().iterator();

		final ATermAppl var = i.next();

		final Collection<ATermAppl> empty = Collections.emptySet();
		final ATermAppl clazz = q.rollUpTo(var, empty, false);

		_logger.fine(() -> "Rolling up " + var + " to " + clazz);

		final Collection<ATermAppl> instances;

		if (first)
		{
			instances = new HashSet<>(_kb.getIndividuals());
			for (final QueryAtom atom : q.findAtoms(QueryPredicate.PropertyValue, var, null, null))
				instances.retainAll(_kb.retrieveIndividualsWithProperty(atom.getArguments().get(1)));

			for (final QueryAtom atom : q.findAtoms(QueryPredicate.PropertyValue, null, null, var))
				instances.retainAll(
						_kb.retrieveIndividualsWithProperty(ATermUtils.makeInv(atom.getArguments().get(1))));
		}
		else
			instances = _kb.getInstances(clazz);

		for (final ATermAppl b : instances)
		{
			_logger.fine(() -> "trying " + var + " --> " + b);
			final ResultBinding newBinding = binding.duplicate();

			newBinding.setValue(var, b);
			final ConjunctiveQuery q2 = q.apply(newBinding);
			exec(q2, newBinding, false);
		}
	}

	@Override
	public QueryResult execABoxQuery(final ConjunctiveQuery q)
	{
		_results = new QueryResultImpl(q);

		_kb = q.getKB();

		final long satCount = _kb.getABox().getStats()._satisfiabilityCount;
		final long consCount = _kb.getABox().getStats()._consistencyCount;

		exec(q, new ResultBindingImpl(), true);

		if (_logger.isLoggable(Level.FINE))
		{
			_logger.fine("Total satisfiability operations: " + (_kb.getABox().getStats()._satisfiabilityCount - satCount));
			_logger.fine("Total consistency operations: " + (_kb.getABox().getStats()._consistencyCount - consCount));
			_logger.fine("Results of ABox query : " + _results);
		}

		return _results;
	}
}
