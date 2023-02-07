// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.cq;

import java.util.Collection;
import java.util.List;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.ucq.UnionQuery;

/**
 * <p>
 * Title: Query Interface
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
public interface ConjunctiveQuery extends UnionQuery
{
	/**
	 * Adds a query atom to the query.
	 *
	 * @param atom
	 */
	void add(final QueryAtom atom);

	/**
	 * @return all the atoms in the query.
	 */
	List<QueryAtom> getAtoms();

	/**
	 * Replace the variables in the query with the values specified in the binding and return a new query instance (without modifying this query).
	 *
	 * @param binding
	 * @return the query changed
	 */
	ConjunctiveQuery apply(ResultBinding binding);

	/**
	 * @param distVar
	 * @param avoidList
	 * @param stopOnConstants
	 * @return Rolls up the query to the given variable.
	 */
	ATermAppl rollUpTo(final ATermAppl distVar, final Collection<ATermAppl> avoidList, final boolean stopOnConstants);

	/**
	 * Creates a subquery from the given query. Atoms are listed according to the 'atoms' parameter.
	 *
	 * @param atoms selected atom indices
	 * @return subquery
	 */
	ConjunctiveQuery reorder(int[] atoms);

	void remove(final QueryAtom atom);

	/**
	 * Searches for given atom pattern. This also might be used for different types of rolling-up, involving various sets of allowed atom types.
	 *
	 * @param predicate
	 * @param arguments
	 * @return query atoms in the order as they appear in the query
	 */
	List<QueryAtom> findAtoms(final QueryPredicate predicate, final ATermAppl... arguments);
}