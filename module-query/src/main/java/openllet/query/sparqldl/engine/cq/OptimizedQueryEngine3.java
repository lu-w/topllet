// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.engine.cq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.abox.ABox;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.query.sparqldl.model.Query.VarType;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
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
public class OptimizedQueryEngine3 extends AbstractABoxEngineWrapper
{
	@SuppressWarnings("hiding")
	public static final Logger _logger = Log.getLogger(QueryEngine.class);

	@Override
	public boolean supports(ConjunctiveQuery q)
	{
		return !q.isNegated() && !q.hasCycle();
	}

	@Override
	public QueryResult execABoxQuery(final ConjunctiveQuery q)
	{
		final QueryResult results = new QueryResultImpl(q);
		final KnowledgeBase kb = q.getKB();

		final long satCount = kb.getABox().getStats()._satisfiabilityCount;
		final long consCount = kb.getABox().getStats()._consistencyCount;

		if (q.getDistVars().isEmpty())
		{
			if (QueryEngine.execBooleanABoxQuery(q))
				results.add(new ResultBindingImpl());
		}
		else
		{
			final Map<ATermAppl, Set<ATermAppl>> varBindings = new HashMap<>();

			for (final ATermAppl currVar : q.getDistVarsForType(VarType.INDIVIDUAL))
			{
				final ATermAppl rolledUpClass = q.rollUpTo(currVar, Collections.emptySet(), false);

				if (_logger.isLoggable(Level.FINER))
					_logger.finer("Rolled up class " + rolledUpClass);
				varBindings.put(currVar, kb.getInstances(rolledUpClass));
			}

			if (_logger.isLoggable(Level.FINER))
				_logger.finer("Var bindings: " + varBindings);

			final List<ATermAppl> varList = new ArrayList<>(varBindings.keySet()); // TODO

			final Map<ATermAppl, Collection<ResultBinding>> goodLists = new HashMap<>();

			final ATermAppl first = varList.get(0);
			final Collection<ResultBinding> c = new HashSet<>();

			for (final ATermAppl a : varBindings.get(first))
			{
				final ResultBinding bind = new ResultBindingImpl();
				bind.setValue(first, a);
				c.add(bind);
			}

			goodLists.put(first, c);

			Collection<ResultBinding> previous = goodLists.get(first);
			for (int i = 1; i < varList.size(); i++)
			{
				final ATermAppl next = varList.get(i);

				final Collection<ResultBinding> newBindings = new HashSet<>();

				for (final ResultBinding binding : previous)
					for (final ATermAppl testBind : varBindings.get(next))
					{
						final ResultBinding bindingCandidate = binding.duplicate();

						bindingCandidate.setValue(next, testBind);

						final boolean queryTrue = QueryEngine.execBooleanABoxQuery(q.apply(bindingCandidate));
						if (queryTrue)
						{
							newBindings.add(bindingCandidate);
							if (_logger.isLoggable(Level.FINER))
								_logger.finer("Accepted binding: " + bindingCandidate);
						}
						else
							if (_logger.isLoggable(Level.FINER))
								_logger.finer("Rejected binding: " + bindingCandidate);
					}

				previous = newBindings;
			}

			// no var. should be marked as both INDIVIDUAL and LITERAL in an
			// ABox query.
			final boolean hasLiterals = !q.getDistVarsForType(VarType.LITERAL).isEmpty();

			if (hasLiterals)
				for (final ResultBinding b : previous)
					for (final Iterator<ResultBinding> i = new LiteralIterator(q, b); i.hasNext();)
						results.add(i.next());
			else
				for (final ResultBinding b : previous)
					results.add(b);
			if (_logger.isLoggable(Level.FINE))
			{
				_logger.fine("Results: " + results);
				_logger.fine("Total satisfiability operations: " + (kb.getABox().getStats()._satisfiabilityCount - satCount));
				_logger.fine("Total consistency operations: " + (kb.getABox().getStats()._consistencyCount - consCount));
			}
		}
		return results;
	}
}
