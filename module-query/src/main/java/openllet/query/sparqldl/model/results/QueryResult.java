// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.results;

import java.util.*;

import openllet.aterm.ATermAppl;
import openllet.core.rules.VariableBinding;
import openllet.query.sparqldl.model.Query;
import org.checkerframework.checker.nullness.qual.Nullable;

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
	 * @return True iff. this result contains a partial (not fully explicated) binding.
	 */
	boolean containsPartialBindings();

	void expandToAllVariables(Collection<ATermAppl> variables);

	/**
	 * Returns result variables.
	 *
	 * @return variables that appear in the result
	 */
	Collection<ATermAppl> getResultVars();

	boolean isDistinct();

	/**
	 * Tests whether the result is empty or not.
	 *
	 * @return true if the result contains not bindings
	 */
	boolean isEmpty();

	/**
	 * @return The maximum possible size of this query result.
	 */
	int getMaxSize();

	/**
	 * @return The query that this query result belongs to.
	 */
	Query<?> getQuery();

	/**
	 * Returns number of bindings in the result.
	 *
	 * @return number of bindings
	 */
	int size();

	// TODO
	QueryResult restrictToVariables(List<ATermAppl> vars);

	/**
	 * Returns a copy of this query result where all bindings are inverted, i.e., the copy contains var -> ind iff.
	 * this result does not contain var -> ind.
	 * @return a copy of this query result where all bindings are inverted
	 */
	QueryResult invert();

	/**
	 * @param binding The binding to find.
	 * @return True iff. the query result contains the given binding
	 */
	boolean contains(ResultBinding binding);

	/**
	 * @param binding Binding to check whether it is partial or fully explicated.
	 * @return True iff. there are result variables of this query result that are not mapped within the given binding
	 */
	boolean isPartialBinding(ResultBinding binding);

	/**
	 * @return True iff. the query result contains all possible bindings of its query
	 */
	boolean isComplete();

	/**
	 * Adds all bindings from the given query result to this query result.
	 * @param toAdd Query result to add
	 */
	void addAll(QueryResult toAdd);

	/**
	 * Adds all bindings from the given query result to this query result.
	 * @param toAdd Query result to add
	 * @param restrictToBindings Only adds those bindings from toAdd also present in restrictToBindings
	 */
	void addAll(QueryResult toAdd, QueryResult restrictToBindings);

	/**
	 * Removes all bindings from the given query result from this query result.
	 * @param toRemove Query result to remove
	 */
	void removeAll(QueryResult toRemove);

	/**
	 * Reduces this query result to the given set of bindings.
	 * @param toRetain Bindings to reduce to.
	 */
	void retainAll(QueryResult toRetain);

	/**
	 * @return An exact copy of this query results.
	 */
	QueryResult copy();

	void explicate();

	static Collection<ResultBinding> allBindings(List<ATermAppl> variables, List<ATermAppl> individuals)
	{
		return allBindings(variables, individuals, true);
	}

	static Collection<ResultBinding> allBindings(Collection<ATermAppl> variables, Collection<ATermAppl> individuals,
												 boolean distinct)
	{
		Collection<ResultBinding> bindings = new HashSet<>();
		// https://stackoverflow.com/a/40101377/4145563
		List<ATermAppl> varList = variables.stream().toList();
		List<ATermAppl> indList = individuals.stream().toList();
		int resultSize = varList.size();
		int[] indexes = new int[Math.max(individuals.size(), resultSize)];
		for (int j = (int) Math.pow(individuals.size(), resultSize); j > 0; j--)
		{
			ResultBinding binding = new ResultBindingImpl();
			for (int i = 0; i < resultSize; i++)
				binding.setValue(varList.get(i), indList.get(indexes[i]));
			if (!distinct || binding.isDistinct())
				bindings.add(binding);
			for (int i = 0; i < resultSize; i++)
				if (indexes[i] >= individuals.size() - 1)
					indexes[i] = 0;
				else
				{
					indexes[i]++;
					break;
				}
		}
		return bindings;
	}
}
