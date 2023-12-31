// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.query;

import java.util.Set;

/**
 * <p>
 * Title: Engine for processing DAWG test manifests
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Petr Kremen
 * @author Markus Stocker
 */
public interface SparqlDawgTester
{

	void setQueryURI(final String queryURI);

	void setDatasetURIs(final Set<String> graphURIs, final Set<String> namedGraphURIs);

	void setResult(final String queryURI);

	/**
	 * Checks whether the given query can be parsed.
	 *
	 * @return true if the query can be parsed.
	 */
	boolean isParsable();

	/**
	 * Checks whether the query is correctly evaluated with respect to given graph (dataURI), named graphs (namedDataURIs) and _expected results (result URI)
	 *
	 * @param dataURI URI of the graph to be the query executed against
	 * @param namedDataURIs URIs of the named graphs
	 * @param resultURI URI of the results
	 * @return true if the _expected and actual results match
	 */
	boolean isCorrectlyEvaluated();

	/**
	 * Determines whether the given test name is applicable for the given tester. Allows for using avoid lists.
	 *
	 * @param testURI name of the test
	 * @return true, if the tester can be run on the test.
	 */
	boolean isApplicable(final String testURI);

	/**
	 * Returns a user-friendly name for this tester
	 *
	 * @return the name of this tester.
	 */
	String getName();
}
