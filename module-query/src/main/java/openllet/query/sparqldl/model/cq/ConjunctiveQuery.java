// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.cq;

import java.util.Collection;
import java.util.List;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.ResultBinding;

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
public interface ConjunctiveQuery extends Query<ConjunctiveQuery>
{
	/**
	 * Whether the query is negated.
	 * @return true iff. the query is negated
	 */
	boolean isNegated();

	/**
	 * Adds a query atom to the query.
	 *
	 * @param atom the atom to add
	 */
	void add(final QueryAtom atom);

	/**
	 * Removes a query atom from the query.
	 * @param atom the atom to remove
	 */
	void remove(final QueryAtom atom);

	/**
	 * @return all the atoms in the query.
	 */
	List<QueryAtom> getAtoms();

	/**
	 * Rolls up the query to the given variable
	 * @param distVar the set of variables to not roll up (as they are dist. variables)
	 * @param avoidList a collection of terms to avoid
	 * @param stopOnConstants whether to stop rolling up on constants
	 * @return A new copy of the rolled up query.
	 */
	ATermAppl rollUpTo(final ATermAppl distVar, final Collection<ATermAppl> avoidList, final boolean stopOnConstants);


	/**
	 * Searches for given atom pattern. This also might be used for different types of rolling-up, involving various sets of allowed atom types.
	 *
	 * @param predicate the predicate
	 * @param arguments arguments
	 * @return query atoms in the order as they appear in the query
	 */
	List<QueryAtom> findAtoms(final QueryPredicate predicate, final ATermAppl... arguments);

	/**
	 * Extension of the standard split() functionality of queries that can also split on individuals.
	 * @param splitOnIndividuals whether to split on individuals (i.e. C(a), C(b) become two separate queries)
	 * @param splitOnDistVars If true, each split query contains at most one distinguished variable
	 * @return A list of split queries
	 */
	List<ConjunctiveQuery> split(boolean splitOnIndividuals, boolean splitOnDistVars);
}
