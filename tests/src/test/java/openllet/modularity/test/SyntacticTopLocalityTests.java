// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.modularity.test;

import static openllet.modularity.test.TestUtils.set;
import static openllet.owlapi.OWL.Class;
import static openllet.owlapi.OWL.Nothing;
import static openllet.owlapi.OWL.ObjectProperty;
import static openllet.owlapi.OWL.Thing;
import static openllet.owlapi.OWL.all;
import static openllet.owlapi.OWL.max;
import static openllet.owlapi.OWL.not;
import static openllet.owlapi.OWL.some;
import static openllet.owlapi.OWL.subClassOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

public class SyntacticTopLocalityTests
{

	private final SyntacticLocalityEvaluator evaluator = new SyntacticLocalityEvaluator(LocalityClass.TOP_BOTTOM);

	private void assertLocal(final OWLAxiom a, final OWLEntity... signature)
	{
		assertTrue(evaluator.isLocal(a, set(signature)));
	}

	private void assertNonLocal(final OWLAxiom a, final OWLEntity... signature)
	{
		assertFalse(evaluator.isLocal(a, set(signature)));
	}

	/**
	 * Test that complemented sub class descriptions are handled correctly.
	 */
	@Test
	public void objectComplementSubCls()
	{
		assertLocal(subClassOf(not(Thing), Class("A")), Class("A"));
		assertLocal(subClassOf(not(Class("B")), Class("A")), Class("A"));
		assertNonLocal(subClassOf(not(Class("B")), Class("A")), Class("A"), Class("B"));
	}

	/**
	 * Test that filler classes used in existentials as superclass are handled correctly.
	 */
	@Test
	public void objectExistentialFillerSuperCls()
	{
		assertNonLocal(subClassOf(Class("A"), some(ObjectProperty("p"), Class("B"))), Class("A"));
		assertNonLocal(subClassOf(Class("A"), some(ObjectProperty("p"), Class("B"))), Class("A"), ObjectProperty("p"));
	}

	@Test
	public void objectMaxSubCls()
	{
		assertNonLocal(subClassOf(max(ObjectProperty("p"), 2, Thing), Class("A")), Class("A"));
		assertNonLocal(subClassOf(max(ObjectProperty("p"), 2, Thing), Class("A")), Class("A"), ObjectProperty("p"));
		assertNonLocal(subClassOf(max(ObjectProperty("p"), 2, Nothing), Class("A")), Class("A"));
		assertNonLocal(subClassOf(max(ObjectProperty("p"), 2, Nothing), Class("A")), Class("A"), ObjectProperty("p"));
		assertNonLocal(subClassOf(max(ObjectProperty("p"), 2, Class("B")), Class("A")), Class("A"));
		assertNonLocal(subClassOf(max(ObjectProperty("p"), 2, Class("B")), Class("A")), Class("A"), ObjectProperty("p"));
		assertNonLocal(subClassOf(max(ObjectProperty("p"), 2, Class("B")), Class("A")), Class("A"), ObjectProperty("p"), Class("B"));
	}

	/**
	 * Test that named classes as super in subClass axioms are handled correctly.
	 */
	@Test
	public void objectSuperCls()
	{
		assertLocal(subClassOf(Class("A"), Class("B")), Class("A"));
		assertNonLocal(subClassOf(Class("A"), Class("B")), Class("B"));

	}

	/**
	 * Test that universal object restriction subclasses are handled correctly
	 */
	@Test
	public void objectUniversalSubCls()
	{
		assertNonLocal(subClassOf(all(ObjectProperty("p"), Class("B")), Class("A")), Class("A"));
		assertNonLocal(subClassOf(all(ObjectProperty("p"), Class("B")), Class("A")), Class("A"), Class("B"));
		assertNonLocal(subClassOf(all(ObjectProperty("p"), Class("B")), Class("A")), Class("A"), ObjectProperty("p"));
	}
}
