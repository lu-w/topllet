// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC.
// <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms
// of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test;

import static openllet.core.utils.TermFactory.term;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.KnowledgeBaseImpl;

public class AbstractKBTests
{
	public static String base = "file:" + PelletTestSuite.base + "misc/";

	protected static final ATermAppl _A = term("A");
	protected static final ATermAppl _B = term("B");
	protected static final ATermAppl _C = term("C");
	protected static final ATermAppl _D = term("D");
	protected static final ATermAppl _E = term("E");
	protected static final ATermAppl _F = term("F");
	protected static final ATermAppl _G = term("G");
	protected static final ATermAppl _H = term("H");

	protected static final ATermAppl _p = term("p");
	protected static final ATermAppl _q = term("q");
	protected static final ATermAppl _r = term("r");
	protected static final ATermAppl _s = term("s");
	protected static final ATermAppl _o = term("o");
	protected static final ATermAppl _u = term("u");
	protected static final ATermAppl _t = term("t");
	protected static final ATermAppl _f = term("f");

	protected static final ATermAppl _a = term("a");
	protected static final ATermAppl _b = term("b");
	protected static final ATermAppl _c = term("c");
	protected static final ATermAppl _d = term("d");
	protected static final ATermAppl _e = term("e");

	protected KnowledgeBase _kb;

	@Before
	public void initializeKB()
	{
		_kb = new KnowledgeBaseImpl();
	}

	@After
	public void disposeKB()
	{
		_kb = null;
	}

	protected void classes(final ATermAppl... classes)
	{
		for (final ATermAppl cls : classes)
			_kb.addClass(cls);
	}

	protected void objectProperties(final ATermAppl... props)
	{
		for (final ATermAppl p : props)
			_kb.addObjectProperty(p);
	}

	protected void dataProperties(final ATermAppl... props)
	{
		for (final ATermAppl p : props)
			_kb.addDatatypeProperty(p);
	}

	protected void annotationProperties(final ATermAppl... props)
	{
		for (final ATermAppl p : props)
			_kb.addAnnotationProperty(p);
	}

	protected void individuals(final ATermAppl... inds)
	{
		for (final ATermAppl ind : inds)
			_kb.addIndividual(ind);
	}

	@SafeVarargs
	public static <T> Set<Set<T>> singletonSets(final T... es)
	{
		final Set<Set<T>> set = new HashSet<>();
		for (final T e : es)
			set.add(Collections.singleton(e));
		return set;
	}
}
