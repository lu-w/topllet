// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.pellint.lintpattern.axiom;

import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;

import openllet.owlapi.OWL;
import openllet.pellint.format.CompactClassLintFormat;
import openllet.pellint.format.LintFormat;
import openllet.pellint.model.Lint;
import openllet.pellint.model.LintFixer;

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
public class EquivalentToMaxCardinalityPattern extends AxiomLintPattern
{
	private static final LintFormat DEFAULT_LINT_FORMAT = new CompactClassLintFormat();

	@Override
	public String getName()
	{
		return getClass().getSimpleName();
	}

	@Override
	public String getDescription()
	{
		return "A named concept is equivalent to a max cardinality restriction";
	}

	@Override
	public boolean isFixable()
	{
		return true;
	}

	@Override
	public LintFormat getDefaultLintFormat()
	{
		return DEFAULT_LINT_FORMAT;
	}

	@Override
	public void visit(final OWLEquivalentClassesAxiom axiom)
	{
		final Set<OWLClassExpression> owlDescs = axiom.classExpressions().collect(Collectors.toSet());
		if (owlDescs.size() != 2)
			return;

		OWLClass namedClass = null;
		OWLClassExpression cardinalityRestriction = null;
		for (final OWLClassExpression owlDesc : owlDescs)
			if (!owlDesc.isAnonymous())
				namedClass = owlDesc.asOWLClass();
			else
				if (owlDesc instanceof OWLObjectMaxCardinality || owlDesc instanceof OWLDataMaxCardinality)
					cardinalityRestriction = owlDesc;

		if (namedClass != null && cardinalityRestriction != null)
		{
			final Lint lint = makeLint();
			lint.addParticipatingClass(namedClass);
			lint.addParticipatingAxiom(axiom);
			final OWLAxiom newAxiom = OWL.subClassOf(namedClass, cardinalityRestriction);
			final LintFixer fixer = new LintFixer(axiom, newAxiom);
			lint.setLintFixer(fixer);
			setLint(lint);
		}
	}
}
