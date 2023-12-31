package openllet.test.query;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;

import openllet.core.KnowledgeBase;
import openllet.jena.PelletInfGraph;
import openllet.jena.PelletReasonerFactory;
import openllet.query.sparqldl.engine.cq.QuerySubsumption;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.parser.QueryEngineBuilder;
import openllet.query.sparqldl.parser.QueryParser;

/**
 * Test cases for the class QuerySubsumption
 *
 * @author Hector Perez-Urbina
 */

public class TestQuerySubsumption
{
	private final String _ont = "file:test/data/misc/family.owl";
	private final String _family = "http://www.example.org/family#";
	private final String _prefix = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" //
			+ "PREFIX family: <" + _family + ">\r\n" + "SELECT * { ";
	private final String suffix = " }";
	private KnowledgeBase _kb;
	private QueryParser _parser;

	@Before
	public void setUp()
	{
		final OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		model.read(_ont);
		model.prepare();

		_kb = ((PelletInfGraph) model.getGraph()).getKB();
		_parser = QueryEngineBuilder.getParser();
	}

	private ConjunctiveQuery query(final String queryStr)
	{
		return _parser.parse(_prefix + queryStr + suffix, _kb);
	}

	@Test
	public void testIsEquivalentTo()
	{
		ConjunctiveQuery[] queries = new ConjunctiveQuery[2];

		queries = example1();
		assertFalse(QuerySubsumption.isEquivalentTo(queries[0], queries[1]));

		queries = example2();
		assertFalse(QuerySubsumption.isEquivalentTo(queries[1], queries[0]));

		queries = example3();
		assertTrue(QuerySubsumption.isEquivalentTo(queries[1], queries[0]));

		queries = example4();
		assertFalse(QuerySubsumption.isEquivalentTo(queries[1], queries[0]));
	}

	@Test
	public void testIsSubsumedBy()
	{

		ConjunctiveQuery[] queries = new ConjunctiveQuery[2];

		queries = example1();
		assertTrue(QuerySubsumption.isSubsumedBy(queries[0], queries[1]));
		assertFalse(QuerySubsumption.isSubsumedBy(queries[1], queries[0]));

		queries = example2();
		assertTrue(QuerySubsumption.isSubsumedBy(queries[0], queries[1]));
		assertFalse(QuerySubsumption.isSubsumedBy(queries[1], queries[0]));

		queries = example3();
		assertTrue(QuerySubsumption.isSubsumedBy(queries[0], queries[1]));
		assertTrue(QuerySubsumption.isSubsumedBy(queries[1], queries[0]));

		queries = example4();
		assertTrue(QuerySubsumption.isSubsumedBy(queries[0], queries[1]));
		assertFalse(QuerySubsumption.isSubsumedBy(queries[1], queries[0]));
	}

	/**
	 * Simple query subsumption similar to standard concept subsumption. Every Male is a Person so query 1 is subsumed by query 2. The converse is obviously not
	 * true.
	 */
	private ConjunctiveQuery[] example1()
	{
		final ConjunctiveQuery[] queries = new ConjunctiveQuery[2];

		queries[0] = query("?x a family:Male .");
		queries[1] = query("?x a family:Person .");

		return queries;
	}

	/**
	 * Another example of subsumption. First query asks for all people married to Male individuals which is subsumed by the second query which asks for all
	 * Females.
	 *
	 * @return examples queries
	 */
	public ConjunctiveQuery[] example2()
	{
		final ConjunctiveQuery[] queries = new ConjunctiveQuery[2];

		queries[0] = query("?x family:isMarriedTo ?y . ?y rdf:type family:Male");
		queries[1] = query("?x a family:Female .");

		return queries;
	}

	/**
	 * Example showing query equivalence. The subproperty relation between hasFather and hasParent properties would normally establish subsumption in one way
	 * but due to cardinality restrictions defined in the ontology two queries _end up being equivalent,
	 *
	 * @return examples queries
	 */
	public ConjunctiveQuery[] example3()
	{
		final ConjunctiveQuery[] queries = new ConjunctiveQuery[2];

		queries[0] = query("?x family:hasFather ?y . ");
		queries[1] = query("?x family:hasParent ?y . ?y a family:Male .");

		return queries;
	}

	/**
	 * The subsumption in this example holds because of the subproperty relation between hasBrother and hasSibling.
	 * 
	 * @return examples queries
	 */
	public ConjunctiveQuery[] example4()
	{

		final ConjunctiveQuery[] queries = new ConjunctiveQuery[2];

		queries[0] = query("?x a family:Female; family:hasBrother ?y . ");
		queries[1] = query("?x a family:Female; family:hasSibling ?z .");

		return queries;
	}
}
