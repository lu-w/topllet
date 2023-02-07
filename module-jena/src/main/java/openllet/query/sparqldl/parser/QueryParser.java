// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.parser;

import java.io.InputStream;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

/**
 * <p>
 * Title: SPARQL-DL Query Parser Interface
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
public interface QueryParser
{

	ConjunctiveQuery parse(final String queryString, KnowledgeBase kb);

	ConjunctiveQuery parse(final InputStream stream, KnowledgeBase kb);

	ConjunctiveQuery parse(final org.apache.jena.query.Query stream, KnowledgeBase kb);
}
