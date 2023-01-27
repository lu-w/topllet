// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.expressivity;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.el.ELExpressivityChecker;
import openllet.core.utils.ATermUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Harris Lin
 */
public class ExpressivityChecker
{
	private final KnowledgeBase _KB;
	private final ELExpressivityChecker _ELChecker;
	private final DLExpressivityChecker _DLChecker;
	private volatile Expressivity _expressivity;

	public ExpressivityChecker(final KnowledgeBase kb)
	{
		this(kb, new Expressivity());
	}

	public ExpressivityChecker(final KnowledgeBase kb, final Expressivity expr)
	{
		_KB = kb;
		_ELChecker = new ELExpressivityChecker(_KB);
		_DLChecker = new DLExpressivityChecker(_KB);
		_expressivity = expr;
	}

	public void prepare()
	{
		_expressivity = new Expressivity();
		if (_ELChecker.compute(_expressivity))
			return;

		_expressivity = new Expressivity();
		// force expressivity to be non-EL
		_expressivity.setHasAllValues(true);
		_DLChecker.compute(_expressivity);
	}

	public Expressivity getExpressivity()
	{
		return _expressivity;
	}

	public Expressivity getExpressivityWith(final ATermAppl c)
	{
		return getExpressivityWith(c == null ? new ArrayList<>() : List.of(c));
	}

	public Expressivity getExpressivityWith(final Collection<ATermAppl> cs)
	{
		if (cs == null || cs.size() == 0)
			return _expressivity;

		final Expressivity newExp = new Expressivity(_expressivity);
		for (ATermAppl c : cs)
		{
			if (c != null)
				_DLChecker.updateWith(newExp, c);
		}

		return newExp;
	}

	/**
	 * Added for incremental reasoning. Given an openllet.aterm corresponding to an _individual and concept, the expressivity is updated accordingly.
	 *
	 * @param i
	 * @param concept
	 */
	public void updateWithIndividual(final ATermAppl i, final ATermAppl concept)
	{
		final ATermAppl nominal = ATermUtils.makeValue(i);

		if (concept.equals(nominal))
			return;

		_DLChecker.updateWith(_expressivity, concept);
	}
}
