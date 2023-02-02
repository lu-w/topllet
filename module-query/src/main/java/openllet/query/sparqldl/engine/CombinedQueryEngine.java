// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.engine;

import static openllet.core.utils.TermFactory.hasValue;
import static openllet.core.utils.TermFactory.inv;
import static openllet.core.utils.TermFactory.not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import openllet.aterm.ATermAppl;
import openllet.atom.OpenError;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.exceptions.UnsupportedQueryException;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.taxonomy.TaxonomyNode;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.CandidateSet;
import openllet.core.utils.DisjointSet;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.model.*;
import openllet.query.sparqldl.model.UnionQuery.VarType;
import openllet.shared.tools.Log;

/**
 * <p>
 * Title: Engine for queries with only distinguished variables.
 * </p>
 * <p>
 * Description: All variable name spaces are disjoint.
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
public class CombinedQueryEngine implements QueryExec
{
	public static final Logger _logger = Log.getLogger(CombinedQueryEngine.class);

	public static final QueryOptimizer _optimizer = new QueryOptimizer();

	private KnowledgeBase _kb;

	protected QueryPlan _plan;

	protected Query _oldQuery;

	protected Query _query;

	private QueryResult _result;

	private Set<ATermAppl> _downMonotonic;

	private void prepare(final Query query)
	{
		if (_logger.isLoggable(Level.FINE))
			_logger.fine("Preparing plan ...");

		_kb = query.getKB();
		if (_kb == null)
			throw new OpenError("No input data set is given for query!");

		_result = new QueryResultImpl(query);

		_oldQuery = query;
		_query = setupCores(query);

		if (_logger.isLoggable(Level.FINE))
			_logger.fine("After setting-up cores : " + _query);

		_plan = _optimizer.getExecutionPlan(_query);
		_plan.reset();

		// warm up the reasoner by computing the satisfiability of classes
		// used in the query so that cached models can be used for instance
		// checking - TODO also non-named classes
		if (OpenlletOptions.USE_CACHING && !_kb.isClassified())
			for (final QueryAtom a : _oldQuery.getAtoms())
				for (final ATermAppl arg : a.getArguments())
					if (_kb.isClass(arg))
					{
						_kb.isSatisfiable(arg);
						_kb.isSatisfiable(ATermUtils.makeNot(arg));
					}

		if (OpenlletOptions.OPTIMIZE_DOWN_MONOTONIC)
		{
			// TODO use down monotonic variables for implementation of
			// DirectType atom
			_downMonotonic = new HashSet<>();
			setupDownMonotonicVariables(_query);
			if (_logger.isLoggable(Level.FINE))
				_logger.fine("Variables to be optimized : " + _downMonotonic);
		}
	}

	// computes cores of undistinguished variables
	private Query setupCores(final Query query)
	{
		final Iterator<ATermAppl> undistVarIterator = query.getUndistVars().iterator();
		if (!undistVarIterator.hasNext())
			return query;
		final DisjointSet<Object> coreVertices = new DisjointSet<>();

		final List<QueryAtom> toRemove = new ArrayList<>();

		while (undistVarIterator.hasNext())
		{
			final ATermAppl a = undistVarIterator.next();

			coreVertices.add(a);

			for (final QueryAtom atom : query.findAtoms(QueryPredicate.PropertyValue, a, null, null))
			{
				coreVertices.add(atom);
				coreVertices.union(a, atom);

				final ATermAppl a2 = atom.getArguments().get(2);
				if (query.getUndistVars().contains(a2))
				{
					coreVertices.add(a2);
					coreVertices.union(a, a2);
				}
				toRemove.add(atom);
			}
			for (final QueryAtom atom : query.findAtoms(QueryPredicate.PropertyValue, null, null, a))
			{
				coreVertices.add(atom);
				coreVertices.union(a, atom);

				final ATermAppl a2 = atom.getArguments().get(0);
				if (query.getUndistVars().contains(a2))
				{
					coreVertices.add(a2);
					coreVertices.union(a, a2);
				}
				toRemove.add(atom);
			}

			for (final QueryAtom atom : query.findAtoms(QueryPredicate.Type, a, null))
			{
				coreVertices.add(atom);
				coreVertices.union(a, atom);
				toRemove.add(atom);
			}
		}

		final Query transformedQuery = query.apply(new ResultBindingImpl());

		for (final Set<Object> set : coreVertices.getEquivalanceSets())
		{
			final Collection<QueryAtom> atoms = new ArrayList<>();

			for (final Object a : set)
				if (a instanceof QueryAtom)
					atoms.add((QueryAtom) a);

			final CoreNewImpl c = (CoreNewImpl) QueryAtomFactory.Core(atoms, query.getUndistVars(), _kb);

			transformedQuery.add(c);

			_logger.fine(() -> c.getUndistVars() + " : " + c.getDistVars() + " : " + c.getQuery().getAtoms());
		}

		for (final QueryAtom atom : toRemove)
			transformedQuery.remove(atom);

		return transformedQuery;
	}

	// down-monotonic variables = Class variables in Type atoms and Property
	// variables in PropertyValue atoms
	private void setupDownMonotonicVariables(final Query query)
	{
		for (final QueryAtom atom : query.getAtoms())
		{
			ATermAppl arg;

			switch (atom.getPredicate())
			{
				case PropertyValue:
				case Type:
					arg = atom.getArguments().get(1);
					if (ATermUtils.isVar(arg))
						_downMonotonic.add(arg);
					break;
				default:
					arg = null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(final Query q)
	{
		// TODO fully undist.vars queries are not supported !!!
		return q.hasCycle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryResult exec(final Query query)
	{
		_logger.fine(() -> "Executing query " + query);

		final Timer timer = new Timer("CombinedQueryEngine");
		timer.start();
		prepare(query);
		branches = 0;
		exec(new ResultBindingImpl());
		timer.stop();

		_logger.fine(() -> "#B=" + branches + ", time=" + timer.getLast() + " ms.");

		return _result;
	}

	private long branches;

	private void exec(final ResultBinding bindingParam)
	{
		ResultBinding binding = bindingParam;

		if (_logger.isLoggable(Level.FINE))
			branches++;

		if (!_plan.hasNext())
		{
			// TODO if _result vars are not same as dist vars.
			if (!binding.isEmpty() || _result.isEmpty())
			{
				if (_logger.isLoggable(Level.FINE))
					_logger.fine("Found binding: " + binding);

				if (!_result.getResultVars().containsAll(binding.getAllVariables()))
				{
					final ResultBinding newBinding = new ResultBindingImpl();
					for (final ATermAppl var : _result.getResultVars())
					{
						final ATermAppl value = binding.getValue(var);

						newBinding.setValue(var, value);
					}
					binding = newBinding;
				}

				_result.add(binding);
			}

			if (_logger.isLoggable(Level.FINE))
				_logger.finer("Returning ... binding=" + binding);
			return;
		}

		final QueryAtom current = _plan.next(binding);

		_logger.finer(() -> "Evaluating " + current);

		if (current.isGround() && !current.getPredicate().equals(QueryPredicate.UndistVarCore))
		{
			if (QueryEngine.checkGround(current, _kb))
				exec(binding);
		}
		else
			exec(current, binding);

		if (_logger.isLoggable(Level.FINE))
			_logger.finer("Returning ... " + binding);
		_plan.back();
	}

	private void exec(final QueryAtom current, final ResultBinding binding)
	{
		final List<ATermAppl> arguments = current.getArguments();

		boolean direct = false;
		boolean strict = false;

		switch (current.getPredicate())
		{

			case DirectType:
				direct = true;
				//$FALL-THROUGH$
			case Type: // TODO implementation of _downMonotonic vars
				final ATermAppl tI = arguments.get(0);
				final ATermAppl tC = arguments.get(1);

				Set<ATermAppl> instanceCandidates = null;
				if (tI.equals(tC))
				{
					instanceCandidates = _kb.getIndividualsCount() < _kb.getClasses().size() ? _kb.getIndividuals() : _kb.getClasses();
					for (final ATermAppl ic : instanceCandidates)
						if (direct ? _kb.getInstances(ic, direct).contains(ic) : _kb.isType(ic, ic))
						{
							final ResultBinding candidateBinding = binding.duplicate();

							if (ATermUtils.isVar(tI))
								candidateBinding.setValue(tI, ic);

							exec(candidateBinding);
						}
				}
				else
				{
					final Set<ATermAppl> classCandidates;

					if (!ATermUtils.isVar(tC))
					{
						classCandidates = Collections.singleton(tC);
						instanceCandidates = _kb.getInstances(tC, direct);
					}
					else
						if (!ATermUtils.isVar(tI))
						{
							// classCandidates = flatten(TaxonomyUtils.getTypes(_kb.getTaxonomy(), tI, direct)); // TODO
							classCandidates = flatten(_kb.getTypes(tI, direct)); // TODO
							instanceCandidates = Collections.singleton(tI);
						}
						else
							classCandidates = _kb.getAllClasses();

					// explore all possible bindings
					final boolean loadInstances = instanceCandidates == null;
					for (final ATermAppl cls : classCandidates)
					{
						if (loadInstances)
							instanceCandidates = _kb.getInstances(cls, direct);
						if (instanceCandidates != null)
							for (final ATermAppl inst : instanceCandidates)
								runNext(binding, arguments, inst, cls);
					} // finish explore bindings
				}
				break;

			case PropertyValue: // TODO implementation of _downMonotonic vars
				final ATermAppl pvI = arguments.get(0);
				final ATermAppl pvP = arguments.get(1);
				final ATermAppl pvIL = arguments.get(2);

				Collection<ATermAppl> propertyCandidates = null;
				Collection<ATermAppl> subjectCandidates = null;
				Collection<ATermAppl> objectCandidates = null;

				boolean loadProperty = false;
				boolean loadSubjects = false;
				boolean loadObjects = false;

				if (!ATermUtils.isVar(pvP))
				{
					propertyCandidates = Collections.singleton(pvP);
					if (!ATermUtils.isVar(pvI))
					{
						subjectCandidates = Collections.singleton(pvI);
						objectCandidates = _kb.getPropertyValues(pvP, pvI);
					}
					else
						if (!ATermUtils.isVar(pvIL))
						{
							objectCandidates = Collections.singleton(pvIL);
							subjectCandidates = _kb.getIndividualsWithProperty(pvP, pvIL);
						}
					loadProperty = false;
				}
				else
				{
					if (!ATermUtils.isVar(pvI))
						subjectCandidates = Collections.singleton(pvI);

					if (!ATermUtils.isVar(pvIL))
						objectCandidates = Collections.singleton(pvIL);
					else
						if (!_plan.getQuery().getDistVarsForType(VarType.LITERAL).contains(pvIL))
							propertyCandidates = _kb.getObjectProperties();

					if (propertyCandidates == null)
						propertyCandidates = _kb.getProperties();
					loadProperty = true;
				}

				loadSubjects = subjectCandidates == null;
				loadObjects = objectCandidates == null;

				for (final ATermAppl property : propertyCandidates)
					// TODO replace this nasty if-cascade with some map for var bindings.
					if (loadObjects && loadSubjects)
					{
						if (pvI.equals(pvIL))
						{
							if (pvI.equals(pvP))
							{
								if (!_kb.hasPropertyValue(property, property, property))
									continue;
								runNext(binding, arguments, property, property, property);
							}
							else
								for (final ATermAppl i : _kb.getIndividuals())
								{
									if (!_kb.hasPropertyValue(i, property, i))
										continue;
									runNext(binding, arguments, i, property, i);
								}
						}
						else
							if (pvI.equals(pvP))
								for (final ATermAppl i : _kb.getIndividuals())
								{
									if (!_kb.hasPropertyValue(property, property, i))
										continue;
									runNext(binding, arguments, property, property, i);
								}
							else
								if (pvIL.equals(pvP))
									for (final ATermAppl i : _kb.getIndividuals())
									{
										if (!_kb.hasPropertyValue(i, property, property))
											continue;
										runNext(binding, arguments, i, property, property);
									}
								else
									for (final ATermAppl subject : _kb.getIndividuals())
										for (final ATermAppl object : _kb.getPropertyValues(property, subject))
											runNext(binding, arguments, subject, property, object);
					}
					else
						if (loadObjects)
						{
							// subject is known.
							if (pvP.equals(pvIL))
								if (subjectCandidates != null && !_kb.hasPropertyValue(subjectCandidates.iterator().next(), property, property))
									// terminate
									subjectCandidates = Collections.emptySet();

							if (subjectCandidates != null)
								for (final ATermAppl subject : subjectCandidates)
									for (final ATermAppl object : _kb.getPropertyValues(property, subject))
										runNext(binding, arguments, subject, property, object);
						}
						else
							// object is known.
							if (objectCandidates != null)
								for (final ATermAppl object : objectCandidates)
								{
									if (loadSubjects)
										if (pvI.equals(pvP))
										{
											if (_kb.hasPropertyValue(property, property, object))
												subjectCandidates = Collections.singleton(property);
											else
												// terminate
												subjectCandidates = Collections.emptySet();
										}
										else
											subjectCandidates = new HashSet<>(_kb.getIndividualsWithProperty(property, object));

									if (subjectCandidates != null)
										for (final ATermAppl subject : subjectCandidates)
										{
											if (loadProperty && !_kb.hasPropertyValue(subject, property, object))
												continue;

											runNext(binding, arguments, subject, property, object);
										}
								}
				break;

			case SameAs:
				// optimize - merge _nodes
				final ATermAppl saI1 = arguments.get(0);
				final ATermAppl saI2 = arguments.get(1);

				for (final ATermAppl known : getSymmetricCandidates(VarType.INDIVIDUAL, saI1, saI2))
				{

					final Set<ATermAppl> dependents;

					if (saI1.equals(saI2))
						dependents = Collections.singleton(known);
					else
						dependents = _kb.getAllSames(known);

					for (final ATermAppl dependent : dependents)
						runSymetricCheck(current, saI1, known, saI2, dependent, binding);
				}
				break;

			case DifferentFrom:
				// optimize - different from map
				final ATermAppl dfI1 = arguments.get(0);
				final ATermAppl dfI2 = arguments.get(1);

				if (!dfI1.equals(dfI2))
					for (final ATermAppl known : getSymmetricCandidates(VarType.INDIVIDUAL, dfI1, dfI2))
						for (final ATermAppl dependent : _kb.getDifferents(known))
							runSymetricCheck(current, dfI1, known, dfI2, dependent, binding);
				else
					if (_logger.isLoggable(Level.FINER))
						_logger.finer("Atom " + current + "cannot be satisfied in any consistent ontology.");
				// TODO What about undist vars ?
				// Query : PropertyValue(?x,p,_:x), Type(_:x, C),
				// DifferentFrom( _:x, x) .
				// Data : p(a,x) . p(b,y) . C(x) . C(y) .
				// Result: {b}
				//
				// Data : p(a,x) . (exists p (C and {y}))(b) . C(x) .
				// Result: {y}
				//
				// rolling-up to ?x : (exists p (C and not {x}))(?x) .
				//
				// More complex problems :
				// Query : PropertyValue(?x,p,_:x), Type(_:x, C),
				// DifferentFrom( _:x, _:y) . Type(_:y, T) .
				// Data : p(a,x) . C(x) .
				// Result: {a}
				//
				// Query : PropertyValue(?x,p,_:x), Type(_:x, C),
				// DifferentFrom( _:x, _:y) . Type(_:y, T) .
				// Data : p(x,x) . C(x) .
				// Result: {}
				//
				// Query : PropertyValue(?x,p,_:x), Type(_:x, C),
				// DifferentFrom( _:x, _:y) . Type(_:y, D) .
				// Data : p(a,x) . C(x) . D(a) .
				// Result: {a}
				//
				// rolling-up to ?x : (exists p (C and (not D)))(?x) .
				//
				// rolling-up to _:x of DifferentFrom(_:x,_:y) :
				// roll-up(_:x) and (not roll-up(_:y)).
				// but it is not complete if the rolling-up to _:y is not
				// complete, but just a preprocessing (for example _:y is in
				// a cycle).
				break;

			case Annotation:
				final ATermAppl aI = arguments.get(0);
				final ATermAppl aP = arguments.get(1);
				final ATermAppl aIL = arguments.get(2);

				subjectCandidates = null;
				objectCandidates = null;
				propertyCandidates = null;

				//if aI is a variable, get all the annotation subjects
				if (ATermUtils.isVar(aI))
					subjectCandidates = _kb.getAnnotationSubjects();
				else
					subjectCandidates = Collections.singleton(aI);

				//if aP is a variable, get all the annotation properties
				if (ATermUtils.isVar(aP))
					propertyCandidates = _kb.getAnnotationProperties();
				else
					propertyCandidates = Collections.singleton(aP);

				//if aIL is a variable, get all the annotation objects for the subject and the property candidates
				if (ATermUtils.isVar(aIL))
					for (final ATermAppl subject : subjectCandidates)
						for (final ATermAppl property : propertyCandidates)
							for (final ATermAppl object : _kb.getAnnotations(subject, property))
								runNext(binding, arguments, subject, property, object);
				else
					for (final ATermAppl subject : subjectCandidates)
						for (final ATermAppl property : propertyCandidates)
							if (_kb.isAnnotation(subject, property, aIL))
								runNext(binding, arguments, subject, property, aIL);

				break;
			// throw new IllegalArgumentException("The annotation atom "
			// + _current + " should be ground, but is not.");

			// TBOX ATOMS
			case DirectSubClassOf:
				direct = true;
				//$FALL-THROUGH$
			case StrictSubClassOf:
				strict = true;
				//$FALL-THROUGH$
			case SubClassOf:
				final ATermAppl scLHS = arguments.get(0);
				final ATermAppl scRHS = arguments.get(1);

				if (scLHS.equals(scRHS))
					// TODO optimization for _downMonotonic variables
					for (final ATermAppl ic : _kb.getClasses())
						runNext(binding, arguments, ic, ic);
				else
				{
					final boolean lhsDM = isDownMonotonic(scLHS);
					final boolean rhsDM = isDownMonotonic(scRHS);

					if (lhsDM || rhsDM)
						downMonotonic(_kb.getTaxonomy(), _kb.getClasses(), lhsDM, scLHS, scRHS, binding, direct, strict);
					else
					{
						final Set<ATermAppl> lhsCandidates;
						Set<ATermAppl> rhsCandidates = null;

						if (!ATermUtils.isVar(scLHS))
						{
							lhsCandidates = Collections.singleton(scLHS);
							rhsCandidates = flatten(_kb.getSuperClasses(scLHS, direct));

							rhsCandidates.addAll(_kb.getEquivalentClasses(scLHS));

							if (strict)
								rhsCandidates.removeAll(_kb.getEquivalentClasses(scLHS));
							else
								if (!ATermUtils.isComplexClass(scLHS))
									rhsCandidates.add(scLHS);
						}
						else
							if (!ATermUtils.isVar(scRHS))
							{
								rhsCandidates = Collections.singleton(scRHS);
								if (scRHS.equals(ATermUtils.TOP))
									lhsCandidates = new HashSet<>(_kb.getAllClasses());
								else
								{
									lhsCandidates = flatten(_kb.getSubClasses(scRHS, direct));

									lhsCandidates.addAll(_kb.getAllEquivalentClasses(scRHS));
								}

								if (strict)
									lhsCandidates.removeAll(_kb.getAllEquivalentClasses(scRHS));
							}
							else
								lhsCandidates = _kb.getClasses();

						final boolean reload = rhsCandidates == null;
						for (final ATermAppl subject : lhsCandidates)
						{
							if (reload)
							{
								rhsCandidates = flatten(_kb.getSuperClasses(subject, direct));
								if (strict)
									rhsCandidates.removeAll(_kb.getEquivalentClasses(subject));
								else
									if (!ATermUtils.isComplexClass(subject))
										rhsCandidates.add(subject);
							}
							if (rhsCandidates != null)
								for (final ATermAppl object : rhsCandidates)
									runNext(binding, arguments, subject, object);
						}
					}
				}
				break;

			case EquivalentClass: // TODO implementation of _downMonotonic vars
				final ATermAppl eqcLHS = arguments.get(0);
				final ATermAppl eqcRHS = arguments.get(1);

				for (final ATermAppl known : getSymmetricCandidates(VarType.CLASS, eqcLHS, eqcRHS))
				{
					// TODO optimize - try just one - if success then take
					// all
					// found bindings and extend them for other equivalent
					// classes as well.
					// meanwhile just a simple check below

					final Set<ATermAppl> dependents;

					if (eqcLHS.equals(eqcRHS))
						dependents = Collections.singleton(known);
					else
						dependents = _kb.getEquivalentClasses(known);

					for (final ATermAppl dependent : dependents)
					{
						final int size = _result.size();

						runSymetricCheck(current, eqcLHS, known, eqcRHS, dependent, binding);

						if (_result.size() == size)
							// no binding found, so that there is no need to
							// explore other equivalent classes - they fail
							// as
							// well.
							break;
					}
				}
				break;

			case DisjointWith: // TODO implementation of _downMonotonic vars
				final ATermAppl dwLHS = arguments.get(0);
				final ATermAppl dwRHS = arguments.get(1);

				if (!dwLHS.equals(dwRHS))
					// TODO optimizeTBox
					for (final ATermAppl known : getSymmetricCandidates(VarType.CLASS, dwLHS, dwRHS))
						for (final Set<ATermAppl> dependents : _kb.getDisjointClasses(known))
							for (final ATermAppl dependent : dependents)
								runSymetricCheck(current, dwLHS, known, dwRHS, dependent, binding);
				else
					_logger.finer("Atom " + current + "cannot be satisfied in any consistent ontology.");
				break;

			case ComplementOf: // TODO implementation of _downMonotonic vars
				final ATermAppl coLHS = arguments.get(0);
				final ATermAppl coRHS = arguments.get(1);

				if (!coLHS.equals(coRHS))
					// TODO optimizeTBox
					for (final ATermAppl known : getSymmetricCandidates(VarType.CLASS, coLHS, coRHS))
						for (final ATermAppl dependent : _kb.getComplements(known))
							runSymetricCheck(current, coLHS, known, coRHS, dependent, binding);
				else
					_logger.finer("Atom " + current + "cannot be satisfied in any consistent ontology.");
				break;

			// RBOX ATOMS
			case DirectSubPropertyOf:
				direct = true;
				//$FALL-THROUGH$
			case StrictSubPropertyOf:
				strict = true;
				//$FALL-THROUGH$
			case SubPropertyOf:
				final ATermAppl spLHS = arguments.get(0);
				final ATermAppl spRHS = arguments.get(1);

				if (spLHS.equals(spRHS))
					// TODO optimization for _downMonotonic variables
					for (final ATermAppl ic : _kb.getProperties())
						runNext(binding, arguments, ic, ic);
				else
				{
					final boolean lhsDM = isDownMonotonic(spLHS);
					final boolean rhsDM = isDownMonotonic(spRHS);

					if (lhsDM || rhsDM)
						downMonotonic(_kb.getRoleTaxonomy(true), _kb.getProperties(), lhsDM, spLHS, spRHS, binding, direct, strict);
					else
					{
						final Set<ATermAppl> spLhsCandidates;
						Set<ATermAppl> spRhsCandidates = null;

						if (!ATermUtils.isVar(spLHS))
						{
							spLhsCandidates = Collections.singleton(spLHS);
							spRhsCandidates = flatten(_kb.getSuperProperties(spLHS, direct));
							if (strict)
								spRhsCandidates.removeAll(_kb.getEquivalentProperties(spLHS));
							else
								spRhsCandidates.add(spLHS);
						}
						else
							if (!ATermUtils.isVar(spRHS))
							{
								spRhsCandidates = Collections.singleton(spRHS);
								spLhsCandidates = flatten(_kb.getSubProperties(spRHS, direct));
								if (strict)
									spLhsCandidates.removeAll(_kb.getEquivalentProperties(spRHS));
								else
									spLhsCandidates.add(spRHS);
							}
							else
								spLhsCandidates = _kb.getProperties();
						final boolean reload = spRhsCandidates == null;
						for (final ATermAppl subject : spLhsCandidates)
						{
							if (reload)
							{
								spRhsCandidates = flatten(_kb.getSuperProperties(subject, direct));
								if (strict)
									spRhsCandidates.removeAll(_kb.getEquivalentProperties(subject));
								else
									spRhsCandidates.add(subject);
							}

							if (spRhsCandidates != null)
								for (final ATermAppl object : spRhsCandidates)
									runNext(binding, arguments, subject, object);
						}
					}
				}
				break;

			case EquivalentProperty: // TODO implementation of _downMonotonic
				// vars
				final ATermAppl eqpLHS = arguments.get(0);
				final ATermAppl eqpRHS = arguments.get(1);

				// TODO optimize - try just one - if success then take all
				// found
				// bindings and extend them for other equivalent classes as
				// well.
				// meanwhile just a simple check below
				for (final ATermAppl known : getSymmetricCandidates(VarType.PROPERTY, eqpLHS, eqpRHS))
				{
					final Set<ATermAppl> dependents;

					if (eqpLHS.equals(eqpRHS))
						dependents = Collections.singleton(known);
					else
						dependents = _kb.getEquivalentProperties(known);

					for (final ATermAppl dependent : dependents)
					{
						final int size = _result.size();
						runSymetricCheck(current, eqpLHS, known, eqpRHS, dependent, binding);
						if (_result.size() == size)
							// no binding found, so that there is no need to
							// explore other equivalent classes - they fail
							// as
							// well.
							break;

					}
				}
				break;

			case Domain:
				final ATermAppl domLHS = arguments.get(0);
				final ATermAppl domRHS = arguments.get(1);

				Collection<ATermAppl> domLhsCandidates;
				Collection<ATermAppl> domRhsCandidates;

				if (!ATermUtils.isVar(domLHS))
					domLhsCandidates = Collections.singleton(domLHS);
				else
					domLhsCandidates = _kb.getProperties();

				if (!ATermUtils.isVar(domRHS))
					domRhsCandidates = Collections.singleton(domRHS);
				else
					domRhsCandidates = _kb.getAllClasses();

				for (final ATermAppl prop : domLhsCandidates)
					for (final ATermAppl cls : domRhsCandidates)
						if ((_kb.isDatatypeProperty(prop) || _kb.isObjectProperty(prop)) && _kb.hasDomain(prop, cls))
							runNext(binding, arguments, prop, cls);

				break;

			case Range:
				final ATermAppl rangeLHS = arguments.get(0);
				final ATermAppl rangeRHS = arguments.get(1);

				Collection<ATermAppl> rangeLhsCandidates;
				Collection<ATermAppl> rangeRhsClassCandidates;
				Collection<ATermAppl> rangeRhsDTypeCandidates;

				if (!ATermUtils.isVar(rangeLHS))
					rangeLhsCandidates = Collections.singleton(rangeLHS);
				else
					rangeLhsCandidates = _kb.getProperties();

				if (!ATermUtils.isVar(rangeRHS))
				{

					//System.out.println( "Bound range: " + rangeRHS );
					if (_kb.isDatatype(rangeRHS))
					{
						rangeRhsClassCandidates = Collections.emptySet();
						rangeRhsDTypeCandidates = Collections.singleton(rangeRHS);
					}
					else
					{
						rangeRhsClassCandidates = Collections.singleton(rangeRHS);
						rangeRhsDTypeCandidates = Collections.emptySet();
					}

				}
				else
				{
					rangeRhsClassCandidates = _kb.getAllClasses();
					// TODO : change the datatype reasoner to keep track of associated aterms.
					rangeRhsDTypeCandidates = new HashSet<>();
					for (final ATermAppl dtype : _kb.getDatatypeReasoner().listDataRanges())
						rangeRhsDTypeCandidates.add(dtype);
				}

				for (final ATermAppl prop : rangeLhsCandidates)
					if (_kb.isObjectProperty(prop))
					{
						for (final ATermAppl cls : rangeRhsClassCandidates)
							if (_kb.hasRange(prop, cls))
								runNext(binding, arguments, prop, cls);
					}
					else
						if (_kb.isDatatypeProperty(prop))
							for (final ATermAppl dtype : rangeRhsDTypeCandidates)
								if (_kb.hasRange(prop, dtype))
									runNext(binding, arguments, prop, dtype);

				break;

			case InverseOf: // TODO implementation of _downMonotonic vars
				final ATermAppl ioLHS = arguments.get(0);
				final ATermAppl ioRHS = arguments.get(1);

				if (ioLHS.equals(ioRHS))
					runAllPropertyChecks(current, arguments.get(0), _kb.getSymmetricProperties(), binding);
				else
					for (final ATermAppl known : getSymmetricCandidates(VarType.PROPERTY, ioLHS, ioRHS))
						// meanwhile workaround
						for (final ATermAppl dependent : _kb.getInverses(known))
							runSymetricCheck(current, ioLHS, known, ioRHS, dependent, binding);
				break;

			case Symmetric:
				runAllPropertyChecks(current, arguments.get(0), _kb.getSymmetricProperties(), binding);
				break;

			case Asymmetric:
				runAllPropertyChecks(current, arguments.get(0), _kb.getAsymmetricProperties(), binding);
				break;

			case Reflexive:
				runAllPropertyChecks(current, arguments.get(0), _kb.getReflexiveProperties(), binding);
				break;

			case Irreflexive:
				runAllPropertyChecks(current, arguments.get(0), _kb.getIrreflexiveProperties(), binding);
				break;

			case ObjectProperty:
				runAllPropertyChecks(current, arguments.get(0), _kb.getObjectProperties(), binding);
				break;

			case DatatypeProperty:
				runAllPropertyChecks(current, arguments.get(0), _kb.getDataProperties(), binding);
				break;

			case Functional:
				runAllPropertyChecks(current, arguments.get(0), _kb.getFunctionalProperties(), binding);
				break;

			case InverseFunctional:
				runAllPropertyChecks(current, arguments.get(0), _kb.getInverseFunctionalProperties(), binding);
				break;

			case Transitive:
				runAllPropertyChecks(current, arguments.get(0), _kb.getTransitiveProperties(), binding);
				break;

			case UndistVarCore:
				// TODO Core IF
				final CoreNewImpl core = (CoreNewImpl) current.apply(binding);

				final Collection<ATermAppl> distVars = core.getDistVars();

				if (distVars.isEmpty())
				{
					final Collection<ATermAppl> constants = core.getConstants();
					if (constants.isEmpty())
					{
						if (QueryEngine.execBooleanABoxQuery(core.getQuery()))
							_result.add(binding);
						// throw new OpenError("The query contains neither dist vars, nor constants, yet evaluated by the CombinedQueryEngine !!! ");
					}
					else
					{
						final ATermAppl c = constants.iterator().next();
						final ATermAppl clazz = core.getQuery().rollUpTo(c, Collections.<ATermAppl> emptySet(), STOP_ROLLING_ON_CONSTANTS);

						if (_kb.isType(c, clazz))
							exec(binding);
					}
				}
				else
					if (distVars.size() == 1)
					{
						final ATermAppl var = distVars.iterator().next();
						final ATermAppl c = core.getQuery().rollUpTo(var, Collections.<ATermAppl> emptySet(), STOP_ROLLING_ON_CONSTANTS);
						final Collection<ATermAppl> instances = _kb.getInstances(c);

						for (final ATermAppl a : instances)
						{
							final ResultBinding candidateBinding = binding.duplicate();
							candidateBinding.setValue(var, a);
							exec(candidateBinding);
						}
					}
					else
					{
						// TODO
						// if (distVars.size() == 2
						// && core.getUndistVars().size() == 1
						// && !_kb.getExpressivity().hasNominal()
						// && !_kb.getExpressivity().hasTransitivity()) {
						// // TODO 1. undist. var. in distinguished manner
						// // TODO 2. identify both DV's
						// }

						final CoreStrategy s = QueryEngine.getStrategy(current);

						switch (s)
						{
							case SIMPLE:
								execSimpleCore(_oldQuery, binding, distVars);
								break;
							case ALLFAST:
								execAllFastCore(_oldQuery, binding, distVars, core.getUndistVars());
								break;
							default:
								throw new InternalReasonerException("Unknown core _strategy.");
						}
					}

				break;

			case NegativePropertyValue:
			{
				final ATermAppl s = arguments.get(0);
				final ATermAppl p = arguments.get(1);
				final ATermAppl o = arguments.get(2);

				if (ATermUtils.isVar(p))
					throw new UnsupportedQueryException("NegativePropertyValue atom with a variable property not supported");
				if (ATermUtils.isVar(o) && _kb.isDatatypeProperty(p))
					throw new UnsupportedQueryException("NegativePropertyValue atom with a datatype property and variable object not supported");

				if (ATermUtils.isVar(s))
				{
					final Set<ATermAppl> oValues = ATermUtils.isVar(o) ? _kb.getIndividuals() : Collections.singleton(o);

					for (final ATermAppl oValue : oValues)
					{
						final Set<ATermAppl> sValues = _kb.getInstances(not(hasValue(p, oValue)));
						for (final ATermAppl sValue : sValues)
							runNext(binding, arguments, sValue, p, oValue);
					}
				}
				else
					if (ATermUtils.isVar(o))
					{
						final Set<ATermAppl> oValues = _kb.getInstances(not(hasValue(inv(p), o)));
						for (final ATermAppl oValue : oValues)
							runNext(binding, arguments, s, p, oValue);
					}
					else
						if (_kb.isType(s, hasValue(p, o)))
							exec(binding);

				break;
			}

			case NotKnown:
			{
				final Query newQuery = new QueryImpl(_kb, true);
				for (final QueryAtom atom : ((NotKnownQueryAtom) current).getAtoms())
					newQuery.add(atom.apply(binding));

				for (final ATermAppl var : newQuery.getUndistVars())
					newQuery.addDistVar(var, VarType.INDIVIDUAL);

				final QueryExec newEngine = new CombinedQueryEngine();

				final boolean isNegationTrue = newEngine.exec(newQuery).isEmpty();

				if (isNegationTrue)
					exec(binding);

				break;
			}

			case Union:
			{
				for (final List<QueryAtom> atoms : ((UnionQueryAtom) current).getUnion())
				{
					final Query newQuery = new QueryImpl(_kb, true);
					for (final QueryAtom atom : atoms)
						newQuery.add(atom.apply(binding));
					for (final ATermAppl var : newQuery.getUndistVars())
					{
						newQuery.addDistVar(var, VarType.INDIVIDUAL);
						newQuery.addResultVar(var);
					}

					final QueryExec newEngine = new CombinedQueryEngine();

					final QueryResult newResult = newEngine.exec(newQuery);
					for (final ResultBinding newBinding : newResult)
					{
						newBinding.setValues(binding);
						exec(newBinding);
					}
				}
				break;
			}

			case Datatype:
				throw new UnsupportedQueryException("Datatype atom not ground: " + current);

			case propertyDisjointWith:
				final ATermAppl dwLHSp = arguments.get(0);
				final ATermAppl dwRHSp = arguments.get(1);

				if (!dwLHSp.equals(dwRHSp))
					// TODO optimizeTBox
					for (final ATermAppl known : getSymmetricCandidates(VarType.PROPERTY, dwLHSp, dwRHSp))
						for (final Set<ATermAppl> dependents : _kb.getDisjointProperties(known))
							for (final ATermAppl dependent : dependents)
								runSymetricCheck(current, dwLHSp, known, dwRHSp, dependent, binding);
				else
					_logger.finer("Atom " + current + "cannot be satisfied in any consistent ontology.");
				break;
			default:
				throw new UnsupportedQueryException("Unknown atom type '" + current.getPredicate() + "'.");

		}
	}

	private final boolean STOP_ROLLING_ON_CONSTANTS = false;

	private void execSimpleCore(final Query q, final ResultBinding binding, final Collection<ATermAppl> distVars)
	{
		final Map<ATermAppl, Set<ATermAppl>> varBindings = new HashMap<>();

		final KnowledgeBase kb = q.getKB();

		for (final ATermAppl currVar : distVars)
		{
			final ATermAppl rolledUpClass = q.rollUpTo(currVar, Collections.<ATermAppl> emptySet(), STOP_ROLLING_ON_CONSTANTS);

			if (_logger.isLoggable(Level.FINER))
				_logger.finer(currVar + " rolled to " + rolledUpClass);

			final Set<ATermAppl> inst = kb.getInstances(rolledUpClass);
			varBindings.put(currVar, inst);
		}

		if (_logger.isLoggable(Level.FINER))
			_logger.finer("Var bindings: " + varBindings);

		final Set<ATermAppl> literalVars = q.getDistVarsForType(VarType.LITERAL);
		final Set<ATermAppl> individualVars = q.getDistVarsForType(VarType.INDIVIDUAL);

		final boolean hasLiterals = !individualVars.containsAll(literalVars);

		for (final Iterator<ResultBinding> i = new BindingIterator(varBindings); i.hasNext();)
		{
			final ResultBinding candidate = i.next().duplicate();
			candidate.setValues(binding);
			if (hasLiterals)
				for (final Iterator<ResultBinding> l = new LiteralIterator(q, candidate); l.hasNext();)
				{
					final ResultBinding mappy = binding.duplicate();
					mappy.setValues(l.next());
					if (QueryEngine.execBooleanABoxQuery(q.apply(mappy)))
						exec(mappy);
				}
			else
				if (QueryEngine.execBooleanABoxQuery(q.apply(candidate)))
					exec(candidate);
		}
	}

	private Map<ATermAppl, Boolean> fastPrune(final Query q, final ATermAppl var)
	{
		final ATermAppl c = q.rollUpTo(var, Collections.<ATermAppl> emptySet(), STOP_ROLLING_ON_CONSTANTS);
		if (_logger.isLoggable(Level.FINER))
			_logger.finer(var + " rolled to " + c);

		final CandidateSet<ATermAppl> set = _kb.getABox().getObviousInstances(c);

		final Map<ATermAppl, Boolean> map = new HashMap<>();

		for (final Object o : set.getKnowns())
			map.put((ATermAppl) o, true);

		for (final Object o : set.getUnknowns())
			map.put((ATermAppl) o, false);

		return map;
	}

	private void execAllFastCore(final Query q, final ResultBinding binding, final Collection<ATermAppl> distVars, final Collection<ATermAppl> undistVars)
	{
		if (distVars.isEmpty())
			exec(binding);
		else
		{
			final ATermAppl var = distVars.iterator().next();
			distVars.remove(var);

			final Map<ATermAppl, Boolean> instances = fastPrune(q, var);

			for (final Entry<ATermAppl, Boolean> entry : instances.entrySet())
			{
				final ATermAppl b = entry.getKey();
				final ResultBinding newBinding = binding.duplicate();

				newBinding.setValue(var, b);
				final Query q2 = q.apply(newBinding);

				if (entry.getValue() || QueryEngine.execBooleanABoxQuery(q2))
					execAllFastCore(q2, newBinding, distVars, undistVars);
			}

			distVars.add(var);
		}
	}

	private void downMonotonic(final Taxonomy<ATermAppl> taxonomy, final Collection<ATermAppl> all, final boolean lhsDM, final ATermAppl lhs, final ATermAppl rhs, final ResultBinding binding, final boolean direct, final boolean strict)
	{
		final ATermAppl downMonotonic = lhsDM ? lhs : rhs;
		final ATermAppl theOther = lhsDM ? rhs : lhs;
		Collection<ATermAppl> candidates;

		if (ATermUtils.isVar(theOther))
			candidates = all;
		// TODO more refined evaluation in case that both
		// variables are down-monotonic
		else
		{
			final ATermAppl top = lhsDM ? rhs : taxonomy.getTop().getName();

			if (ATermUtils.isComplexClass(top))
			{
				candidates = _kb.getEquivalentClasses(top);

				if (!strict && candidates.isEmpty())
					candidates = flatten(_kb.getSubClasses(top, true));
			}
			else
				candidates = Collections.singleton(top);
		}

		for (final ATermAppl candidate : candidates)
		{
			final ResultBinding newBinding = binding.duplicate();

			if (ATermUtils.isVar(theOther))
				newBinding.setValue(theOther, candidate);

			// final Set<ATermAppl> toDo = lhsDM ? taxonomy.getFlattenedSubs(
			// ATermUtils.normalize(candidate), direct) :
			// taxonomy.getFlattenedSupers(ATermUtils.normalize(candidate),
			// direct);

			final Set<ATermAppl> toDo = lhsDM ? flatten(taxonomy.getSubs(candidate, direct)) : flatten(taxonomy.getSupers(candidate, direct));

			if (strict)
				toDo.removeAll(taxonomy.getEquivalents(candidate));
			else
				toDo.add(candidate);

			runRecursively(taxonomy, downMonotonic, candidate, newBinding, new HashSet<>(toDo), direct, strict);
		}
	}

	private boolean isDownMonotonic(final ATermAppl scLHS)
	{
		// TODO more refined _condition to allow optimization for other atoms as
		// well - Type and
		// PropertyValue as well.

		return OpenlletOptions.OPTIMIZE_DOWN_MONOTONIC && _downMonotonic.contains(scLHS);
	}

	private void runNext(final ResultBinding binding, final List<ATermAppl> arguments, final ATermAppl... values)
	{

		final ResultBinding candidateBinding = binding.duplicate();

		for (int i = 0; i < arguments.size(); i++)
			if (ATermUtils.isVar(arguments.get(i)))
				candidateBinding.setValue(arguments.get(i), values[i]);

		exec(candidateBinding);
	}

	private Set<ATermAppl> getSymmetricCandidates(final VarType forType, final ATermAppl cA, final ATermAppl cB)
	{
		final Set<ATermAppl> candidates;

		if (!ATermUtils.isVar(cA))
			candidates = Collections.singleton(cA);
		else
			if (!ATermUtils.isVar(cB))
				candidates = Collections.singleton(cB);
			else
				switch (forType)
				{
					case CLASS:
						candidates = _kb.getClasses();
						break;
					case PROPERTY:
						candidates = _kb.getProperties();
						break;
					case INDIVIDUAL:
						candidates = _kb.getIndividuals();
						break;
					default:
						throw new OpenError("Uknown variable type : " + forType);
				}

		return candidates;
	}

	private void runRecursively(final Taxonomy<ATermAppl> t, final ATermAppl downMonotonic, final ATermAppl rootCandidate, final ResultBinding binding, final Set<ATermAppl> toDo, final boolean direct, final boolean strict)
	{
		final int size = _result.size();

		_logger.fine(() -> "Trying : " + rootCandidate + ", done=" + toDo);

		if (!strict)
		{
			toDo.remove(rootCandidate);
			runNext(binding, Collections.singletonList(downMonotonic), rootCandidate);
		}

		if (strict || _result.size() > size)
		{
			// final Set<ATermAppl> subs = t.getSFlattenedSubs(rootCandidate,
			// direct);
			final Set<ATermAppl> subs = flatten(t.getSubs(rootCandidate, direct));

			for (final ATermAppl subject : subs)
			{
				if (!toDo.contains(subject))
					continue;
				runRecursively(t, downMonotonic, subject, binding, toDo, false, false);
			}
		}
		else
		{
			_logger.fine(() -> "Skipping subs of " + rootCandidate);
			// toDo.removeAll(t.getFlattenedSubs(rootCandidate, false));
			toDo.removeAll(flatten(t.getSubs(rootCandidate, false)));
		}
	}

	private void runSymetricCheck(@SuppressWarnings("unused") final QueryAtom current, final ATermAppl cA, final ATermAppl known, final ATermAppl cB, final ATermAppl dependent, final ResultBinding binding)
	{
		final ResultBinding candidateBinding = binding.duplicate();

		if (!ATermUtils.isVar(cA))
			candidateBinding.setValue(cB, dependent);
		else
			if (!ATermUtils.isVar(cB))
				candidateBinding.setValue(cA, dependent);
			else
			{
				candidateBinding.setValue(cA, known);
				candidateBinding.setValue(cB, dependent);
			}

		exec(candidateBinding);
	}

	private void runAllPropertyChecks(@SuppressWarnings("unused") final QueryAtom current, final ATermAppl var, final Set<ATermAppl> candidates, final ResultBinding binding)
	{
		if (isDownMonotonic(var))
			for (final TaxonomyNode<ATermAppl> topNode : _kb.getRoleTaxonomy(true).getTop().getSubs())
			{

				final ATermAppl top = topNode.getName();

				if (candidates.contains(top))
					runRecursively(_kb.getRoleTaxonomy(true), var, topNode.getName(), binding, new HashSet<>(candidates), false, false);
			}
		else
			for (final ATermAppl candidate : candidates)
			{
				final ResultBinding candidateBinding = binding.duplicate();

				candidateBinding.setValue(var, candidate);

				exec(candidateBinding);
			}
	}

	private static Set<ATermAppl> flatten(final Set<Set<ATermAppl>> set)
	{
		final Set<ATermAppl> result = new HashSet<>();

		for (final Set<ATermAppl> set2 : set)
			for (final ATermAppl a : set2)
				result.add(a);

		return result;
	}
}
