// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.cq;

import java.util.Collection;
import java.util.List;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.AtomQuery;
import openllet.query.sparqldl.model.Query;

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
public interface ConjunctiveQuery extends AtomQuery<ConjunctiveQuery>
{
	/**
	 * Whether the query is negated.
	 * @return true iff. the query is negated
	 */
	boolean isNegated();

	/**
	 * Sets the negation of the query.
	 * @param isNegated false iff. the whole query shall be interpreted as negated
	 */
	void setNegation(boolean isNegated);

	/**
	 * Rolls up the query to the given variable
	 * @param distVar the variable to roll up to
	 * @param avoidList a collection of terms to avoid
	 * @param stopOnConstants whether to stop rolling up on constants
	 * @return A new copy of the rolled up query.
	 */
	ATermAppl rollUpTo(final ATermAppl distVar, final Collection<ATermAppl> avoidList, final boolean stopOnConstants);

	/**
	 * Splits the query and subsequently rolls each subquery into some available variable (starting with
	 * distinguished, if available, otherwise individuals, and finally undistinguished variables).
	 * @param splitOnDistVars whether to split the query also on distinguished variables
	 * @return A new copy of the rolled up query.
	 */
	ConjunctiveQuery splitAndRollUp(boolean splitOnDistVars);

	/**
	 * Extension of the standard split() functionality of queries that can also split on individuals.
	 * @param splitOnIndividuals whether to split on individuals (i.e. C(a), C(b) become two separate queries)
	 * @param splitOnDistVars If true, each split query contains at most one distinguished variable
	 * @return A list of split queries
	 */
	List<ConjunctiveQuery> split(boolean splitOnIndividuals, boolean splitOnDistVars);

	/**
	 * @param other The object to compare equality against.
	 * @return True iff. the given object equals this query, but ignores whether this or the other query is negated.
	 */
	boolean equalsExceptNegation(Object other);
}
