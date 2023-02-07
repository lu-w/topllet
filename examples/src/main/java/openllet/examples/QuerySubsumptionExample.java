// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.examples;

import java.util.Iterator;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.jena.PelletInfGraph;
import openllet.jena.PelletReasonerFactory;
import openllet.query.sparqldl.engine.cq.QuerySubsumption;
import openllet.query.sparqldl.model.cq.Query;
import openllet.query.sparqldl.model.QueryResult;
import openllet.query.sparqldl.model.ResultBinding;
import openllet.query.sparqldl.parser.QueryEngineBuilder;
import openllet.query.sparqldl.parser.QueryParser;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Example program to demonstrate the query subsumption (query containment) capabilities of Pellet. Query subsumption service reports if the answers to a query
 * would be contained in the answers of another query. It is similar to concept subsumption service but applies to conjunctive queries. The examples in this
 * sample program show both concept subsumption and concept equivalence services. The examples also show how to get the mapping between query variables if the
 * subsumption holds.
 *
 * @author Evren Sirin, Hector Perez-Urbina
 */
@Deprecated // The ontology from internet as disappear
public class QuerySubsumptionExample
{
	private static final String	ont		= "http://owldl.com/ontologies/family.owl";
	private static final String	family	= "http://www.example.org/family#";
	private static final String	prefix	= "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"		//
			+ "PREFIX family: <" + family + ">\r\n" + "SELECT * { ";
	private static final String	suffix	= " }";
	private final KnowledgeBase	_kb;
	private final QueryParser	_parser;

	public static void main(final String[] args)
	{
		new QuerySubsumptionExample().run();
	}

	public QuerySubsumptionExample()
	{
		final OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		model.read(ont);
		model.prepare();

		_kb = ((PelletInfGraph) model.getGraph()).getKB();
		_parser = QueryEngineBuilder.getParser();
	}

	public Query query(final String queryStr)
	{
		return _parser.parse(prefix + queryStr + suffix, _kb);
	}

	public void run()
	{
		example1();

		example2();

		example3();

		example4();
	}

	/**
	 * Simple query subsumption similar to standard concept subsumption. Every Male is a Person so query 1 is subsumed by query 2. The converse is obviously not
	 * true.
	 */
	public void example1()
	{
		final Query q1 = query("?x a family:Male .");
		final Query q2 = query("?x a family:Person .");

		System.out.println("Example 1");
		System.out.println("=========");
		System.out.println("Query 1: " + q1.toString());
		System.out.println("Query 2: " + q2.toString());
		System.out.println();
		System.out.println("Query 1 is subsumed by query 2: " + QuerySubsumption.isSubsumedBy(q1, q2));
		System.out.println("Query 2 is subsumed by query 1: " + QuerySubsumption.isSubsumedBy(q2, q1));
		System.out.println();
	}

	/**
	 * Another example of subsumption. First query asks for all people married to Male individuals which is subsumed by the second query which asks for all
	 * Females.
	 */
	public void example2()
	{
		final Query q3 = query("?x family:isMarriedTo ?y . ?y rdf:type family:Male");
		final Query q4 = query("?x a family:Female .");

		System.out.println("Example 2");
		System.out.println("=========");
		System.out.println("Query 3: " + q3.toString());
		System.out.println("Query 4: " + q4.toString());
		System.out.println();
		System.out.println("Query 3 is subsumed by query 4: " + QuerySubsumption.isSubsumedBy(q3, q4));
		System.out.println("Query 4 is subsumed by query 3: " + QuerySubsumption.isSubsumedBy(q4, q3));
		System.out.println();
	}

	/**
	 * Example showing query equivalence. The subproperty relation between hasFather and hasParent properties would normally establish subsumption in one way
	 * but due to cardinality restrictions defined in the ontology two queries _end up being equivalent,
	 */
	public void example3()
	{
		final Query q5 = query("?x family:hasFather ?y . ");
		final Query q6 = query("?x family:hasParent ?y . ?y a family:Male .");

		System.out.println("Example 3");
		System.out.println("=========");
		System.out.println("Query 5: " + q5.toString());
		System.out.println("Query 6: " + q6.toString());
		System.out.println();
		System.out.println("Query 5 is subsumed by query 6: " + QuerySubsumption.isSubsumedBy(q5, q6));
		System.out.println("Query 6 is subsumed by query 5: " + QuerySubsumption.isSubsumedBy(q5, q6));

		System.out.println("Query 5 is equivalent to query 6: " + QuerySubsumption.isEquivalentTo(q5, q6));
		System.out.println();
	}

	/**
	 * The subsumption in this example holds because of the subproperty relation between hasBrother and hasSibling. however, The second query uses the variable
	 * name ?z instead of the the variable name ?y used in the first query. The query subsumption algorithm finds the mapping between query variables.
	 */
	public void example4()
	{
		final Query q7 = query("?x a family:Female; family:hasBrother ?y . ");
		final Query q8 = query("?x a family:Female; family:hasSibling ?z .");

		System.out.println("Example 4");
		System.out.println("=========");
		System.out.println("Query 7: " + q7.toString());
		System.out.println("Query 8: " + q8.toString());
		System.out.println();
		System.out.println("Query 7 is subsumed by query 8: " + QuerySubsumption.isSubsumedBy(q7, q8));

		System.out.print("Subsumption mappings: ");
		final QueryResult mappings = QuerySubsumption.getSubsumptionMappings(q7, q8);
		for (final ResultBinding mapping : mappings)
			for (final Iterator<?> j = q8.getVars().iterator(); j.hasNext();)
			{
				final ATermAppl var = (ATermAppl) j.next();
				System.out.print(var.getArgument(0) + " -> " + mapping.getValue(var)); //I get var(x) as opposed to x
				if (j.hasNext()) System.out.print(", ");
			}
		System.out.println();
	}
}
