// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.engine.cq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.abox.ABoxStats;
import openllet.query.sparqldl.model.*;
import openllet.query.sparqldl.model.ucq.UnionQuery.VarType;
import openllet.query.sparqldl.model.cq.Query;
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
public class SimpleQueryEngine extends AbstractABoxEngineWrapper
{
	@SuppressWarnings("hiding")
	public static final Logger _logger = Log.getLogger(QueryEngine.class);

	@Override
	public boolean supports(final Query q)
	{
		return true; // TODO
	}

	@Override
	public QueryResult execABoxQuery(final Query q)
	{
		final QueryResult results = new QueryResultImpl(q);
		final KnowledgeBase kb = q.getKB();
		final ABoxStats stats = kb.getABox().getStats();
		final long satCount = stats._satisfiabilityCount;
		final long consCount = stats._consistencyCount;

		if (q.getDistVars().isEmpty())
		{
			if (QueryEngine.execBooleanABoxQuery(q))
				results.add(new ResultBindingImpl());
		} else
		{
			final Map<ATermAppl, Set<ATermAppl>> varBindings = new HashMap<>();

			for (final ATermAppl currVar : q.getDistVarsForType(VarType.INDIVIDUAL))
			{
				final ATermAppl rolledUpClass = q.rollUpTo(currVar, Collections.emptySet(), false);

				_logger.finer(() -> "Rolled up class " + rolledUpClass);
				final Set<ATermAppl> inst = kb.getInstances(rolledUpClass);
				varBindings.put(currVar, inst);
			}

			_logger.finer(() -> "Var bindings: " + varBindings);

			final Iterator<ResultBinding> i = new BindingIterator(varBindings);

			final Set<ATermAppl> literalVars = q.getDistVarsForType(VarType.LITERAL);
			final Set<ATermAppl> individualVars = q.getDistVarsForType(VarType.INDIVIDUAL);

			final boolean hasLiterals = !individualVars.containsAll(literalVars);

			if (hasLiterals)
				while (i.hasNext())
				{
					final ResultBinding b = i.next();

					final Iterator<ResultBinding> l = new LiteralIterator(q, b);
					while (l.hasNext())
					{
						final ResultBinding mappy = l.next();
						final boolean queryTrue = QueryEngine.execBooleanABoxQuery(q.apply(mappy));
						if (queryTrue)
							results.add(mappy);
					}
				}
			else
				while (i.hasNext())
				{
					final ResultBinding b = i.next();
					final boolean queryTrue = q.getDistVarsForType(VarType.INDIVIDUAL).size() == 1 ||
							QueryEngine.execBooleanABoxQuery(q.apply(b));
					if (queryTrue)
						results.add(b);
				}
		}

		if (_logger.isLoggable(Level.FINE))
		{
			_logger.fine("Results: " + results);
			_logger.fine("Total satisfiability operations: " +
					(kb.getABox().getStats()._satisfiabilityCount - satCount));
			_logger.fine("Total consistency operations: " +
					(kb.getABox().getStats()._consistencyCount - consCount));
		}

		return results;
	}
}
