// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.clarkparsia.pellet.sparqldl.engine;

import com.clarkparsia.pellet.sparqldl.model.Query;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.katk.tools.Log;
import org.mindswap.pellet.PelletOptions;

/**
 * <p>
 * Title: Optimizer of the query. Provides query atoms for the engine in particular ordering.
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
public class QueryOptimizer
{

	private static final Logger _logger = Log.getLogger(QueryOptimizer.class);

	public QueryPlan getExecutionPlan(final Query query)
	{
		if (PelletOptions.SAMPLING_RATIO == 0)
			return new NoReorderingQueryPlan(query);

		if (query.getAtoms().size() > PelletOptions.STATIC_REORDERING_LIMIT)
		{
			if (_logger.isLoggable(Level.FINE))
				_logger.fine("Using incremental query plan.");
			return new IncrementalQueryPlan(query);
		}
		else
		{
			if (_logger.isLoggable(Level.FINE))
				_logger.fine("Using full query plan.");
			return new CostBasedQueryPlanNew(query);
		}

	}
}
