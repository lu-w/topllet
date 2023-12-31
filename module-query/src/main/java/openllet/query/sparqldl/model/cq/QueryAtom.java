// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.cq;

import java.util.List;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.results.ResultBinding;

/**
 * <p>
 * Title: Atom of a SPARQL-DL query.
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
public interface QueryAtom
{
	/**
	 * Returns predicate of the query atom.
	 *
	 * @return predicate of the query atom
	 */
	QueryPredicate getPredicate();

	/**
	 * Returns arguments of the atom.
	 *
	 * @return arguments of the atom
	 */
	List<ATermAppl> getArguments();

	/**
	 * @return true if the atom is ground, i.e. does not use variables, either distinguished or undistinguished ones.
	 */
	boolean isGround();

	/**
	 * Applies variable binding to the _current atom and returns the result which might be same as the _current atom. Current atom is not affected.
	 *
	 * @param binding QueryBinding to apply
	 * @return a query atom with applied query binding
	 */
	QueryAtom apply(final ResultBinding binding);

	/**
	 * Returns a (deep) copy of this query atom.
	 * @return a copy of this query atom
	 */
	QueryAtom copy();
}
