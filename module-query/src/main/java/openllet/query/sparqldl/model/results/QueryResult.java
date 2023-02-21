// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.Query;

/**
 * <p>
 * Title: Query Result Interface
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
public interface QueryResult extends Iterable<ResultBinding>
{

	/**
	 * Adds a new binding to the query result.
	 *
	 * @param binding to be added
	 */
	void add(final ResultBinding binding);

	/**
	 * Removes the binding (if existent) from this query result.
	 * @param binding to be removed
	 */
	void remove(final ResultBinding binding);

	/**
	 * Returns result variables.
	 *
	 * @return variables that appear in the result
	 */
	List<ATermAppl> getResultVars();

	boolean isDistinct();

	/**
	 * Tests whether the result is empty or not.
	 *
	 * @return true if the result contains not bindings
	 */
	boolean isEmpty();

	/**
	 * Returns number of bindings in the result.
	 *
	 * @return number of bindings
	 */
	int size();

	/**
	 * Returns a copy of this query result where all bindings are inverted, i.e., the copy contains var -> ind iff.
	 * this result does not contain var -> ind.
	 * @return a copy of this query result where all bindings are inverted
	 */
	QueryResult invert();

	static QueryResult allBindings(Query<?> q, List<ATermAppl> variables, List<ATermAppl> individuals)
	{
		// https://stackoverflow.com/a/40101377/4145563
		int resultSize = variables.size();
		QueryResult res = new QueryResultImpl(q);
		int[] indexes = new int[Math.max(individuals.size(), resultSize)];
		for (int j = (int) Math.pow(individuals.size(), resultSize); j > 0; j--)
		{
			ResultBinding binding = new ResultBindingImpl();
			for (int i = 0; i < resultSize; i++)
				binding.setValue(variables.get(i), individuals.get(indexes[i]));
			res.add(binding);
			for (int i = 0; i < resultSize; i++)
				if (indexes[i] >= individuals.size() - 1)
					indexes[i] = 0;
				else
				{
					indexes[i]++;
					break;
				}
		}
		return res;
	}
}
