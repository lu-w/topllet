// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.boxes.tbox.impl;

import static openllet.core.utils.ATermUtils.isAnd;
import static openllet.core.utils.ATermUtils.isHasValue;
import static openllet.core.utils.ATermUtils.isNegatedPrimitive;
import static openllet.core.utils.ATermUtils.isNominal;
import static openllet.core.utils.ATermUtils.isNot;
import static openllet.core.utils.ATermUtils.isOneOf;
import static openllet.core.utils.ATermUtils.isOr;
import static openllet.core.utils.ATermUtils.isSomeValues;
import static openllet.core.utils.ATermUtils.negate;
import static openllet.core.utils.ATermUtils.nnf;
import static openllet.core.utils.TermFactory.BOTTOM;
import static openllet.core.utils.TermFactory.TOP;
import static openllet.core.utils.TermFactory.and;
import static openllet.core.utils.TermFactory.inv;
import static openllet.core.utils.TermFactory.not;
import static openllet.core.utils.TermFactory.some;
import static openllet.core.utils.TermFactory.term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import openllet.aterm.AFun;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.rbox.Role;
import openllet.core.boxes.tbox.TBox;
import openllet.core.datatypes.Facet;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.rules.model.AtomDConstant;
import openllet.core.rules.model.AtomDObject;
import openllet.core.rules.model.AtomDVariable;
import openllet.core.rules.model.AtomIConstant;
import openllet.core.rules.model.AtomIObject;
import openllet.core.rules.model.AtomIVariable;
import openllet.core.rules.model.BuiltInAtom;
import openllet.core.rules.model.ClassAtom;
import openllet.core.rules.model.DataRangeAtom;
import openllet.core.rules.model.DatavaluedPropertyAtom;
import openllet.core.rules.model.IndividualPropertyAtom;
import openllet.core.rules.model.Rule;
import openllet.core.rules.model.RuleAtom;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.BinarySet;
import openllet.core.utils.CollectionUtils;
import openllet.core.utils.MultiMapUtils;
import openllet.core.utils.Namespaces;
import openllet.core.utils.SetUtils;
import openllet.core.utils.iterator.IteratorUtils;
import openllet.core.utils.iterator.MultiIterator;
import openllet.core.utils.iterator.MultiListIterator;
import openllet.shared.tools.Log;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * TBox implementation that keeps track of explanations and allows flexible absorption algorithms.
 *
 * @author Evren Sirin
 */
public class TBoxImpl implements TBox
{
	public static final Logger _logger = Log.getLogger(TBoxImpl.class);

	private static final Map<ATermAppl, String> FACETS;
	static
	{
		FACETS = new HashMap<>();
		FACETS.put(Facet.XSD.MIN_INCLUSIVE.getName(), Namespaces.SWRLB + "greaterThanOrEqual");
		FACETS.put(Facet.XSD.MIN_EXCLUSIVE.getName(), Namespaces.SWRLB + "greaterThan");
		FACETS.put(Facet.XSD.MAX_INCLUSIVE.getName(), Namespaces.SWRLB + "lessThanOrEqual");
		FACETS.put(Facet.XSD.MAX_EXCLUSIVE.getName(), Namespaces.SWRLB + "lessThan");
	}

	private static final Set<Set<ATermAppl>> SINGLE_EMPTY_SET = Collections.singleton(Collections.<ATermAppl> emptySet());

	private final KnowledgeBase _kb;

	private final Set<ATermAppl> _classes = CollectionUtils.makeIdentitySet();
	private final Set<ATermAppl> _allClasses = SetUtils.create();

	/**
	 * MultiValueMap where key is an axiom and the values are the explanations of the key
	 */
	private final Map<ATermAppl, Set<Set<ATermAppl>>> _tboxAxioms = CollectionUtils.makeIdentityMap();
	/**
	 * MultiValueMap where key is an axiom and the values are axioms for which the key is a part of an clashExplanation
	 */
	private final Map<ATermAppl, Set<ATermAppl>> _reverseExplain = CollectionUtils.makeIdentityMap();

	private final Set<ATermAppl> _tboxAssertedAxioms = CollectionUtils.makeIdentitySet();

	private final Set<ATermAppl> _absorbedAxioms = SetUtils.create();

	private final PrimitiveTBox _primitiveTbox = new PrimitiveTBox();
	private final UnaryTBox _unaryTbox = new UnaryTBox();
	private final BinaryTBox _binaryTbox = new BinaryTBox();

	public TBoxImpl(final KnowledgeBase kb)
	{
		_kb = kb;
	}

	public KnowledgeBase getKB()
	{
		return _kb;
	}

	@Override
	public Set<ATermAppl> getAllClasses()
	{
		if (_allClasses.isEmpty())
		{
			_allClasses.addAll(_classes);
			_allClasses.add(ATermUtils.TOP);
			_allClasses.add(ATermUtils.BOTTOM);
		}
		return _allClasses;
	}

	@Override
	public Set<Set<ATermAppl>> getAxiomExplanations(final ATermAppl axiom)
	{
		return _tboxAxioms.get(axiom);
	}

	@Override
	public Set<ATermAppl> getAxiomExplanation(final ATermAppl axiom)
	{
		final Set<Set<ATermAppl>> explains = _tboxAxioms.get(axiom);

		if (explains == null || explains.isEmpty())
		{
			_logger.warning("No clashExplanation for " + axiom);
			return Collections.emptySet();
		}

		// we won't be generating multiple explanations using axiom
		// tracing so we just pick one clashExplanation. the other option
		// would be to return the union of all explanations which
		// would cause Pellet to return non-minimal explanations sets
		for (final Set<ATermAppl> explain : explains)
			return explain;
		return Collections.emptySet();
	}

	/**
	 * Add a new clashExplanation for the given axiom. If a previous clashExplanation exists this will be stored as another clashExplanation.
	 *
	 * @param axiom
	 * @param explain
	 * @return
	 */
	private boolean addAxiomExplanation(final ATermAppl axiom, final Set<ATermAppl> explain)
	{
		_logger.fine(() -> "Add Axiom: " + ATermUtils.toString(axiom) + " Explanation: " + explain);

		boolean added = false;
		if (!OpenlletOptions.USE_TRACING)
			added = _tboxAxioms.put(axiom, SINGLE_EMPTY_SET) == null;
		else
			added = MultiMapUtils.add(_tboxAxioms, axiom, explain);

		if (added)
			for (final ATermAppl explainAxiom : explain)
				if (!axiom.equals(explainAxiom))
					MultiMapUtils.add(_reverseExplain, explainAxiom, axiom);

		return added;
	}

	private static List<ATermAppl> normalizeDisjointAxiom(final ATermAppl... concepts)
	{
		final List<ATermAppl> axioms = CollectionUtils.makeList();

		for (int i = 0; i < concepts.length - 1; i++)
		{
			final ATermAppl c1 = concepts[i];
			final ATermAppl notC1 = ATermUtils.makeNot(c1);
			for (int j = i + 1; j < concepts.length; j++)
			{
				final ATermAppl c2 = concepts[j];
				final ATermAppl notC2 = ATermUtils.makeNot(c2);

				axioms.add(ATermUtils.makeSub(c1, notC2));
				if (ATermUtils.isPrimitive(c2))
					axioms.add(ATermUtils.makeSub(c2, notC1));
			}
		}

		return axioms;
	}

	@Override
	public boolean addAxiom(final ATermAppl axiom)
	{
		_tboxAssertedAxioms.add(axiom);

		List<ATermAppl> axioms = null;

		final Set<ATermAppl> explain = OpenlletOptions.USE_TRACING ? Collections.singleton(axiom) : Collections.<ATermAppl> emptySet();

		if (axiom.getAFun().equals(ATermUtils.EQCLASSFUN))
			axioms = Collections.singletonList(axiom);
		else
			if (axiom.getAFun().equals(ATermUtils.SUBFUN))
				axioms = Collections.singletonList(axiom);
			else
				if (axiom.getAFun().equals(ATermUtils.DISJOINTFUN))
				{
					final ATermAppl c1 = (ATermAppl) axiom.getArgument(0);
					final ATermAppl c2 = (ATermAppl) axiom.getArgument(1);

					axioms = normalizeDisjointAxiom(c1, c2);
				}
				else
					if (axiom.getAFun().equals(ATermUtils.DISJOINTSFUN))
					{
						final ATermList list = (ATermList) axiom.getArgument(0);
						final ATermAppl[] concepts = ATermUtils.toArray(list);

						axioms = normalizeDisjointAxiom(concepts);
					}
					else
					{
						_logger.warning("Not a valid TBox axiom: " + axiom);
						return false;
					}

		boolean added = false;
		for (final ATermAppl a : axioms)
			added |= addAxiom(a, explain, false);

		return added;
	}

	private boolean addAxiom(final ATermAppl axiom, final Set<ATermAppl> explanation, final boolean forceAddition)
	{
		final boolean added = addAxiomExplanation(axiom, explanation);

		if (added || forceAddition)
			if (axiom.getAFun().equals(ATermUtils.EQCLASSFUN))
			{
				final ATermAppl c1 = (ATermAppl) axiom.getArgument(0);
				final ATermAppl c2 = (ATermAppl) axiom.getArgument(1);

				boolean def = false;
				if (ATermUtils.isPrimitive(c1) && !_unaryTbox.unfold(c1).hasNext() && !_binaryTbox.unfold(c1).hasNext())
					def = _primitiveTbox.add(c1, c2, explanation);

				if (!def && ATermUtils.isPrimitive(c2) && !_unaryTbox.unfold(c2).hasNext() && !_binaryTbox.unfold(c2).hasNext())
					def = _primitiveTbox.add(c2, c1, explanation);

				if (!def)
				{
					absorbSubClass(c1, c2, explanation);
					absorbSubClass(c2, c1, explanation);
				}
			}
			else
				if (axiom.getAFun().equals(ATermUtils.SUBFUN))
				{
					final ATermAppl sub = (ATermAppl) axiom.getArgument(0);
					final ATermAppl sup = (ATermAppl) axiom.getArgument(1);

					absorbSubClass(sub, sup, explanation);
				}

		return added;
	}

	private void absorbSubClass(final ATermAppl sub, final ATermAppl sup, final Set<ATermAppl> explanation)
	{
		_logger.fine(() -> "Absorb: subClassOf(" + ATermUtils.toString(sub) + ", " + ATermUtils.toString(sup) + ")");

		final Set<ATermAppl> terms = SetUtils.create();
		terms.add(nnf(sub));
		terms.add(nnf(negate(sup)));

		absorbAxiom(terms, SetUtils.create(explanation));
	}

	private final Absorption[] absorptions = { new BinaryAbsorption(true), new DeterministicUnaryAbsorption(), new SimplifyAbsorption(), new OneOfAbsorption(), new HasValueAbsorption(), new RuleAbsorption(), new BinaryAbsorption(false), new ExistentialAbsorption(), new UnaryAbsorption(), new UnfoldAbsorption(), new DomainAbsorption(), new GeneralAbsorption() };

	private void absorbAxiom(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
	{
		if (terms.size() == 1)
		{
			_unaryTbox.add(TOP, not(terms.iterator().next()), explanation);
			return;
		}

		for (final Absorption absorption : absorptions)
			if (absorption.absorb(terms, explanation))
				return;

		throw new InternalReasonerException("Absorption failed");
	}

	private static ATermAppl disjunction(final Set<ATermAppl> terms)
	{
		return not(and(terms.toArray(new ATermAppl[terms.size()])));
	}

	private interface Absorption
	{
		boolean absorb(Set<ATermAppl> terms, Set<ATermAppl> explanation);
	}

	private class SimplifyAbsorption implements Absorption
	{
		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			final Iterator<ATermAppl> i = terms.iterator();
			while (i.hasNext())
			{
				final ATermAppl term = i.next();
				//				if( isNot( term ) ) {
				//					ATermAppl nnf = nnf( term );
				//					if( term.equals( nnf ) )
				//						continue;
				//					i.remove();
				//					terms.add( nnf );
				//					absorbAxiom( terms, clashExplanation );
				//					return true;
				//				}

				if (isAnd(term))
				{
					i.remove();
					for (ATermList list = (ATermList) term.getArgument(0); !list.isEmpty(); list = list.getNext())
						terms.add((ATermAppl) list.getFirst());
					absorbAxiom(terms, explanation);
					return true;
				}

				if (isOr(term))
				{
					i.remove();
					for (ATermList list = (ATermList) term.getArgument(0); !list.isEmpty(); list = list.getNext())
					{
						final Set<ATermAppl> newTerms = SetUtils.create(terms);
						newTerms.add((ATermAppl) list.getFirst());
						absorbAxiom(newTerms, explanation);
					}
					return true;
				}
			}
			return false;
		}
	}

	private class OneOfAbsorption implements Absorption
	{
		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			if (!OpenlletOptions.USE_NOMINAL_ABSORPTION)
				return false;

			for (final ATermAppl term : terms)
			{
				final Iterator<ATermAppl> nominals = getNominals(term);

				if (nominals.hasNext())
				{
					terms.remove(term);

					final ATermAppl c = disjunction(terms);

					absorbOneOf(nominals, c, explanation);

					return true;
				}
			}
			return false;
		}

		public Iterator<ATermAppl> getNominals(final ATermAppl term)
		{
			if (isOneOf(term))
			{
				final ATermList list = (ATermList) term.getArgument(0);
				return new MultiListIterator(list);
			}
			else
				if (isNominal(term))
					return IteratorUtils.singletonIterator(term);

			return IteratorUtils.emptyIterator();
		}

		private void absorbOneOf(final Iterator<ATermAppl> list, final ATermAppl c, final Set<ATermAppl> explain)
		{
			if (OpenlletOptions.USE_PSEUDO_NOMINALS)
			{
				_logger.warning(() -> "Ignoring axiom involving nominals: " + explain);
				return;
			}

			_absorbedAxioms.addAll(explain);

			final DependencySet ds = new DependencySet(explain);
			while (list.hasNext())
			{
				final ATermAppl nominal = list.next();
				final ATermAppl ind = (ATermAppl) nominal.getArgument(0);

				_logger.fine(() -> "Absorb nominals: " + ATermUtils.toString(c) + " " + ind);

				_kb.addIndividual(ind);
				_kb.addType(ind, c, ds);
			}
		}
	}

	private class HasValueAbsorption implements Absorption
	{
		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			if (!OpenlletOptions.USE_HASVALUE_ABSORPTION)
				return false;

			for (final Iterator<ATermAppl> i = terms.iterator(); i.hasNext();)
			{
				final ATermAppl term = i.next();
				if (isHasValue(term))
				{
					final ATermAppl p = (ATermAppl) term.getArgument(0);
					if (!_kb.isObjectProperty(p))
						continue;

					i.remove();
					final ATermAppl c = disjunction(terms);

					final ATermAppl nominal = (ATermAppl) term.getArgument(1);
					final ATermAppl ind = (ATermAppl) nominal.getArgument(0);

					final ATermAppl invP = _kb.getProperty(p).getInverse().getName();
					final ATermAppl allInvPC = ATermUtils.makeAllValues(invP, c);

					_logger.finer(() -> "Absorb into " + ATermUtils.toString(ind) + " with inverse of " + ATermUtils.toString(p) + " for " + ATermUtils.toString(c));

					_absorbedAxioms.addAll(explanation);

					_kb.addIndividual(ind);
					_kb.addType(ind, allInvPC, new DependencySet(explanation));

					return true;
				}
			}

			return false;
		}
	}

	private class RuleAbsorption implements Absorption
	{
		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			if (!OpenlletOptions.USE_RULE_ABSORPTION)
				return false;

			int propertyAtoms = 0;
			int primitiveClassAtoms = 0;
			ATermAppl head = null;
			for (final ATermAppl term : terms)
				if (ATermUtils.isPrimitive(term) && !_primitiveTbox.contains(term))
					primitiveClassAtoms++;
				else
					if (ATermUtils.isSomeValues(term))
						propertyAtoms++;
					else
						if (ATermUtils.isNot(term))
							head = term;

			if (head == null || propertyAtoms == 0 && primitiveClassAtoms < 2)
				return false;

			terms.remove(head);

			final AtomIObject var = new AtomIVariable("var");
			int varCount = 0;
			final List<RuleAtom> bodyAtoms = new ArrayList<>();
			for (final ATermAppl term : terms)
				varCount = processClass(var, term, bodyAtoms, varCount);

			final List<RuleAtom> headAtoms = new ArrayList<>();
			processClass(var, ATermUtils.negate(head), headAtoms, 1);

			final Rule rule = new Rule(headAtoms, bodyAtoms);
			_kb.addRule(rule);

			_logger.fine(() -> "Add rule: " + rule);

			return true;
		}

		private int processClass(final AtomIObject var, final ATermAppl c, final List<RuleAtom> atoms, final int varCountInit)
		{
			int varCount = varCountInit;

			final AFun afun = c.getAFun();
			if (afun.equals(ATermUtils.ANDFUN))
				for (final ATerm term : (ATermList) c.getArgument(0))
				{
					final ATermAppl conjunct = (ATermAppl) term;
					varCount = processClass(var, conjunct, atoms, varCount);
				}
			else
				if (afun.equals(ATermUtils.SOMEFUN))
				{
					final ATermAppl p = (ATermAppl) c.getArgument(0);
					final ATermAppl filler = (ATermAppl) c.getArgument(1);

					if (filler.getAFun().equals(ATermUtils.VALUEFUN))
					{
						final ATermAppl nominal = (ATermAppl) filler.getArgument(0);
						if (_kb.isDatatypeProperty(p))
						{
							final AtomDConstant arg = new AtomDConstant(nominal);
							final RuleAtom atom = new DatavaluedPropertyAtom(p, var, arg);
							atoms.add(atom);
						}
						else
						{
							final AtomIConstant arg = new AtomIConstant(nominal);
							final RuleAtom atom = new IndividualPropertyAtom(p, var, arg);
							atoms.add(atom);
						}
					}
					else
					{
						varCount++;
						if (_kb.isDatatypeProperty(p))
						{
							final AtomDObject newVar = new AtomDVariable("var" + varCount);
							final RuleAtom atom = new DatavaluedPropertyAtom(p, var, newVar);
							atoms.add(atom);
							processDatatype(newVar, filler, atoms);
						}
						else
						{
							final AtomIObject newVar = new AtomIVariable("var" + varCount);
							final RuleAtom atom = new IndividualPropertyAtom(p, var, newVar);
							atoms.add(atom);
							varCount = processClass(newVar, filler, atoms, varCount);
						}
					}
				}
				else
					if (!c.equals(ATermUtils.TOP))
						atoms.add(new ClassAtom(c, var));

			return varCount;
		}

		private void processDatatype(final AtomDObject var, final ATermAppl c, final List<RuleAtom> atoms)
		{
			final AFun afun = c.getAFun();
			if (afun.equals(ATermUtils.ANDFUN))
				for (ATermList list = (ATermList) c.getArgument(0); !list.isEmpty(); list = list.getNext())
				{
					final ATermAppl conjunct = (ATermAppl) list.getFirst();
					processDatatype(var, conjunct, atoms);
				}
			else
				if (afun.equals(ATermUtils.RESTRDATATYPEFUN))
				{
					final ATermAppl baseDatatype = (ATermAppl) c.getArgument(0);

					atoms.add(new DataRangeAtom(baseDatatype, var));

					for (ATermList list = (ATermList) c.getArgument(1); !list.isEmpty(); list = list.getNext())
					{
						final ATermAppl facetRestriction = (ATermAppl) list.getFirst();
						final ATermAppl facet = (ATermAppl) facetRestriction.getArgument(0);
						final String builtin = FACETS.get(facet);
						if (builtin != null)
						{
							final ATermAppl value = (ATermAppl) facetRestriction.getArgument(1);
							atoms.add(new BuiltInAtom(builtin, var, new AtomDConstant(value)));
						}
						else
						{
							atoms.add(new DataRangeAtom(c, var));
							return;
						}
					}
				}
				else
					atoms.add(new DataRangeAtom(c, var));
		}
	}

	private class BinaryAbsorption implements Absorption
	{
		private boolean _deterministic = false;

		BinaryAbsorption(final boolean deterministic)
		{
			_deterministic = deterministic;
		}

		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			if (!OpenlletOptions.USE_BINARY_ABSORPTION)
				return false;

			if (_deterministic && terms.size() > 3)
				return false;

			final Set<ATermAppl> candidates1 = CollectionUtils.makeIdentitySet();
			final Set<ATermAppl> candidates2 = CollectionUtils.makeIdentitySet();
			for (final ATermAppl term : terms)
			{
				if (!isPrimitive(term))
					continue;
				if (!_primitiveTbox.contains(term))
				{
					candidates1.add(term);
					candidates2.add(term);
				}
				if (_binaryTbox.contains(term))
					candidates1.add(term);
			}

			if (candidates1.isEmpty())
				return false;

			final ATermAppl a1 = candidates1.iterator().next();
			candidates2.remove(a1);

			if (candidates2.isEmpty())
				return false;

			final ATermAppl a2 = candidates2.iterator().next();

			final BinarySet<ATermAppl> set = BinarySet.create(a1, a2);
			final Unfolding unfolding = _binaryTbox.unfold(set);

			terms.remove(a1);
			terms.remove(a2);

			if (terms.size() == 0)
				_binaryTbox.add(set, BOTTOM, explanation);
			else
				if (terms.size() == 1)
					_binaryTbox.add(set, negate(terms.iterator().next()), explanation);
				else
				{
					ATermAppl a = null;
					if (unfolding == null)
					{
						a = freshConcept();
						_binaryTbox.add(set, a, explanation);
					}
					else
						a = unfolding.getResult();

					terms.add(a);

					absorbAxiom(terms, explanation);
				}

			return true;
		}
	}

	private class ExistentialAbsorption implements Absorption
	{
		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			for (final ATermAppl term : terms)
			{
				if (!isSomeValues(term))
					continue;

				final ATermAppl p = (ATermAppl) term.getArgument(0);
				final ATermAppl c = (ATermAppl) term.getArgument(1);

				if (!_kb.isObjectProperty(p))
					continue;

				terms.remove(term);

				if (terms.size() == 1 && isNegatedPrimitive(c) && isNegatedPrimitive(terms.iterator().next()))
				{
					terms.add(term);
					return false;
				}

				final ATermAppl a = freshConcept();
				terms.add(a);
				absorbAxiom(terms, explanation);

				final Set<ATermAppl> newTerms = CollectionUtils.makeIdentitySet();
				newTerms.add(nnf(c));
				newTerms.add(some(inv(p), not(a)));
				absorbAxiom(newTerms, explanation);

				return true;
			}

			return false;
		}
	}

	private class UnfoldAbsorption implements Absorption
	{
		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			for (final ATermAppl c : terms)
			{
				final Unfolding unf = _primitiveTbox.getDefinition(c);

				if (unf != null)
				{
					final ATermAppl def = unf.getResult();

					terms.remove(c);
					terms.add(nnf(def));

					absorbAxiom(terms, explanation);

					return true;
				}
			}

			return false;
		}
	}

	private abstract class AbstractUnaryAbsorption implements Absorption
	{
		protected boolean absorbIntoTerm(final ATermAppl term, final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			if (isPrimitive(term) && !_primitiveTbox.contains(term))
			{
				terms.remove(term);

				final ATermAppl disjunction = disjunction(terms);
				_unaryTbox.add(term, disjunction, explanation);

				return true;
			}

			return false;
		}
	}

	private class DeterministicUnaryAbsorption extends AbstractUnaryAbsorption
	{
		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			if (terms.size() != 2)
				return false;

			final Iterator<ATermAppl> i = terms.iterator();

			final ATermAppl first = i.next();
			if (absorbIntoTerm(first, terms, explanation))
				return true;

			final ATermAppl second = i.next();
			return absorbIntoTerm(second, terms, explanation);
		}
	}

	private class UnaryAbsorption extends AbstractUnaryAbsorption
	{
		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			for (final ATermAppl term : terms)
				if (absorbIntoTerm(term, terms, explanation))
					return true;

			return false;
		}
	}

	private class DomainAbsorption implements Absorption
	{
		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			for (final ATermAppl term : terms)
			{
				if (!isSomeValues(term))
					continue;

				final ATermAppl p = (ATermAppl) term.getArgument(0);

				final Role role = _kb.getRole(p);
				if (role == null || role.hasComplexSubRole())
					continue;

				final ATermAppl disjunction = disjunction(terms);
				_kb.addDomain(p, disjunction, explanation);

				_logger.fine(() -> "Add dom: " + ATermUtils.toString(p) + " " + ATermUtils.toString(disjunction));

				_absorbedAxioms.addAll(explanation);
				return true;
			}

			return false;
		}
	}

	private class GeneralAbsorption implements Absorption
	{
		@Override
		public boolean absorb(final Set<ATermAppl> terms, final Set<ATermAppl> explanation)
		{
			final ATermAppl disjunction = disjunction(terms);
			_unaryTbox.add(TOP, disjunction, explanation);
			return true;
		}
	}

	private int freshConceptCount = 0;

	private ATermAppl freshConcept()
	{
		return term("_A" + (++freshConceptCount) + "_");
	}

	@Override
	public boolean removeAxiom(final ATermAppl axiom)
	{
		return removeAxiom(axiom, axiom);
	}

	@Override
	public boolean removeAxiom(final ATermAppl dependantAxiom, final ATermAppl explanationAxiom)
	{

		if (!OpenlletOptions.USE_TRACING)
		{
			_logger.fine("Cannot remove axioms when PelletOptions.USE_TRACING is false");
			return false;
		}

		if (_absorbedAxioms.contains(dependantAxiom))
		{
			_logger.fine("Cannot remove axioms that have been absorbed outside TBox");
			return false;
		}

		_tboxAssertedAxioms.remove(dependantAxiom);

		final Set<ATermAppl> sideEffects = new HashSet<>();
		final boolean removed = removeExplanation(dependantAxiom, explanationAxiom, sideEffects);

		// an axiom might be effectively removed as a side-effect of another
		// removal. For example see TBoxTests.removedByAbsorbReaddedOnChange
		for (final ATermAppl readdAxiom : sideEffects)
		{
			final Set<Set<ATermAppl>> explanations = _tboxAxioms.get(readdAxiom);
			// if the axiom is really removed (and not just side-effected)
			// then there wouldn't be any clashExplanation and we shouldn't readd
			if (explanations != null)
			{
				final Iterator<Set<ATermAppl>> i = explanations.iterator();
				addAxiom(readdAxiom, i.next(), true);
				while (i.hasNext())
					addAxiomExplanation(readdAxiom, i.next());
			}
		}

		return removed;
	}

	private boolean removeExplanation(final ATermAppl dependantAxiom, final ATermAppl explanationAxiom, final Set<ATermAppl> sideEffects)
	{
		boolean removed = false;

		if (!OpenlletOptions.USE_TRACING)
		{
			_logger.fine("Cannot remove axioms when PelletOptions.USE_TRACING is false");
			return false;
		}

		_logger.fine(() -> "Removing " + explanationAxiom);

		// this axiom is being removed so it cannot support any other axiom
		MultiMapUtils.remove(_reverseExplain, explanationAxiom, dependantAxiom);

		final Set<Set<ATermAppl>> explains = _tboxAxioms.get(dependantAxiom);
		final Set<Set<ATermAppl>> newExplains = new HashSet<>();

		if (explains != null)
			for (final Set<ATermAppl> explain : explains)
				if (!explain.contains(explanationAxiom))
					newExplains.add(explain);
				else
				{
					sideEffects.addAll(explain);
					sideEffects.remove(explanationAxiom);
				}

		if (!newExplains.isEmpty())
		{
			// there are still other axioms supporting this axiom so it won't be
			// removed but we still need to update the explanations
			_tboxAxioms.put(dependantAxiom, newExplains);

			// also make sure the concept on the left hand side is normalized
			//			_Tu.updateDef( dependantAxiom );

			// there is no need for a reload
			return true;
		}

		// there is no other clashExplanation for this dependant axiom so
		// we can safely remove it
		removed |= _tboxAxioms.remove(dependantAxiom) != null;

		final AFun fun = dependantAxiom.getAFun();
		if (fun.equals(ATermUtils.SUBFUN) || fun.equals(ATermUtils.EQCLASSFUN))
		{
			// remove the axiom fom _Tu and _Tg
			//			removed |= _Tu.removeDef( dependantAxiom );
			//			removed |= _Tg.removeDef( dependantAxiom );
		}

		// find if this axiom supports any other axiom
		final Set<ATermAppl> otherDependants = _reverseExplain.remove(dependantAxiom);
		if (otherDependants != null)
			for (final ATermAppl otherDependant : otherDependants)
			{
				// remove this axiom from any clashExplanation it contributes to

				if (otherDependant.equals(dependantAxiom))
					continue;

				removed |= removeExplanation(otherDependant, dependantAxiom, sideEffects);
			}

		return removed;
	}

	@Override
	public Collection<ATermAppl> getAxioms()
	{
		return _tboxAxioms.keySet();
	}

	@Override
	public Collection<ATermAppl> getAssertedAxioms()
	{
		return _tboxAssertedAxioms;
	}

	//	public boolean containsAxiom(final ATermAppl axiom)
	//	{
	//		return _tboxAxioms.containsKey(axiom);
	//	}

	//	public void print()
	//	{
	//		try
	//		{
	//			print(System.out);
	//		}
	//		catch (final IOException e)
	//		{
	//			e.printStackTrace();
	//		}
	//	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		try
		{
			print(sb);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}

	public void print(final Appendable str) throws IOException
	{
		//		generalTbox.print(str);
		_primitiveTbox.print(str);
		_unaryTbox.print(str);
		_binaryTbox.print(str);
		str.append("Explain: [\n");
		for (final ATermAppl axiom : _tboxAxioms.keySet())
		{
			str.append(ATermUtils.toString(axiom));
			str.append(" -> ");
			str.append(_tboxAxioms.get(axiom).toString());
			str.append("\n");
		}
		str.append("]\nReverseExplain: [\n");
		for (final ATermAppl axiom : _reverseExplain.keySet())
		{
			str.append(ATermUtils.toString(axiom));
			str.append(" -> ");
			str.append(_reverseExplain.get(axiom).toString());
			str.append("\n");
		}
		str.append("]\n");
	}

	@Override
	public boolean addClass(final ATermAppl term)
	{
		final boolean added = _classes.add(term);

		if (added)
			_allClasses.clear();

		return added;
	}

	@Override
	public Set<ATermAppl> getClasses()
	{
		return _classes;
	}

	@Override
	public Collection<ATermAppl> getAxioms(final ATermAppl term)
	{
		final List<ATermAppl> axioms = new ArrayList<>();
		//		TermDefinition def = _Tg.getTD( term );
		//		if( def != null ) {
		//			axioms.addAll( def.getSubClassAxioms() );
		//			axioms.addAll( def.getEqClassAxioms() );
		//		}
		//		def = _Tu.getTD( term );
		//		if( def != null ) {
		//			axioms.addAll( def.getSubClassAxioms() );
		//			axioms.addAll( def.getEqClassAxioms() );
		//		}

		return axioms;
	}

	@Override
	public Iterator<Unfolding> unfold(final ATermAppl c)
	{
		if (ATermUtils.isPrimitive(c))
		{
			final MultiIterator<Unfolding> result = new MultiIterator<>(_primitiveTbox.unfold(c));
			result.append(_unaryTbox.unfold(c));
			result.append(_binaryTbox.unfold(c));
			return result;
		}
		else
			if (isNot(c))
				return _primitiveTbox.unfold(c);
			else
				return IteratorUtils.emptyIterator();
	}

	@Override
	public boolean isPrimitive(final ATermAppl c)
	{
		return ATermUtils.isPrimitive(c) && !_primitiveTbox.contains(c);
	}

	@Override
	public void prepare()
	{
		// nothing to do
	}

	@Override
	public DefaultDirectedGraph<ATerm, DefaultEdge> computeAxiomGraph()
	{
		return TBoxExpImpl.computeAxiomGraph(_tboxAssertedAxioms);
	}
}
