// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.engine.cq;

import static java.lang.String.format;
import static openllet.core.utils.TermFactory.TOP_OBJECT_PROPERTY;
import static openllet.core.utils.TermFactory.hasValue;
import static openllet.core.utils.TermFactory.not;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.rbox.Role;
import openllet.core.datatypes.DatatypeReasoner;
import openllet.core.datatypes.exceptions.DatatypeReasonerException;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.utils.*;
import openllet.query.sparqldl.engine.QueryCache;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.cq.NotKnownQueryAtom;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.cq.QueryAtomFactory;
import openllet.query.sparqldl.model.cq.QueryPredicate;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.query.sparqldl.model.cq.UnionQueryAtom;
import openllet.shared.tools.Log;

/**
 * <p>
 * Title: Query Engine for SPARQL-DL
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
public class QueryEngine implements QueryExec<ConjunctiveQuery>
{
	public static Logger _logger = Log.getLogger(QueryEngine.class);
	private final static QueryCache _cache = new QueryCache();

	public static CoreStrategy STRATEGY = CoreStrategy.ALLFAST;

	public static QueryExec<ConjunctiveQuery> getQueryExec()
	{
		return new CombinedQueryEngine();
	}

	public static boolean supports(final ConjunctiveQuery query, @SuppressWarnings("unused") final KnowledgeBase kb)
	{
		return getQueryExec().supports(query);
	}

	public boolean supports(final ConjunctiveQuery query)
	{
		return getQueryExec().supports(query);
	}

	public QueryResult exec(final ConjunctiveQuery query)
	{
		return execQuery(query);
	}

	@Override
	public QueryResult exec(ConjunctiveQuery q, ABox abox)
	{
		return exec(q);
	}

	@Override
	public QueryResult exec(ConjunctiveQuery q, ABox abox, Timer timer)
	{
		timer.start();
		QueryResult result = exec(q, abox);
		timer.stop();
		return result;
	}

	@Override
	public QueryResult exec(ConjunctiveQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
	{
		return execQuery(q, excludeBindings, restrictToBindings);
	}

	public static QueryResult execQuery(final ConjunctiveQuery query, final KnowledgeBase kb,
										QueryResult excludeBindings, QueryResult restrictToBindings)
	{
		final KnowledgeBase queryKB = query.getKB();
		query.setKB(kb);
		final QueryResult result = execQuery(query, excludeBindings, restrictToBindings);
		query.setKB(queryKB);
		return result;
	}

	public static QueryResult execQuery(final ConjunctiveQuery query)
	{
		return execQuery(query, null, null);
	}

	public static QueryResult execQuery(final ConjunctiveQuery query, QueryResult excludeBindings,
										QueryResult restrictToBindings)
	{
		Pair<QueryResult, QueryResult> cachedResults = _cache.fetch(query, restrictToBindings);
		QueryResult result = cachedResults.first;
		QueryResult candidates = cachedResults.second;

		if (!candidates.isEmpty())
		{
			if (query.getAtoms().isEmpty())
			{
				final QueryResultImpl results = new QueryResultImpl(query);
				results.add(new ResultBindingImpl());
				return results;
			}
			query.getKB().ensureConsistency();

			// PREPROCESSING
			_logger.fine(() -> "Preprocessing:\n" + query);
			final ConjunctiveQuery preprocessed = preprocess(query);

			// SIMPLIFICATION
			if (OpenlletOptions.SIMPLIFY_QUERY)
			{
				_logger.fine(() -> "Simplifying:\n" + preprocessed);

				simplify(preprocessed);
			}

			// SPLITTING
			_logger.fine(() -> "Splitting:\n" + preprocessed);

			final List<ConjunctiveQuery> queries = split(preprocessed);

			if (queries.isEmpty())
				throw new InternalReasonerException("Splitting query returned no results!");
			else
				if (queries.size() == 1)
					result = execSingleQuery(queries.get(0), excludeBindings, candidates);
				else
				{
					final List<QueryResult> results = new ArrayList<>(queries.size());
					for (final ConjunctiveQuery q : queries)
						results.add(execSingleQuery(q, excludeBindings, candidates));

					result = new MultiQueryResults(query.getResultVars(), results);
				}
			_cache.add(query, candidates, result);
		}

		return result;
	}

	public static QueryCache getCache()
	{
		return _cache;
	}

	private static boolean isObjectProperty(final ATermAppl t, final KnowledgeBase kb)
	{
		if (!ATermUtils.isVar(t) && !kb.isObjectProperty(t))
		{
			_logger.warning("Undefined object property used in query: " + t);
			return false;
		}

		return true;
	}

	private static boolean isDatatypeProperty(final ATermAppl t, final KnowledgeBase kb)
	{
		if (!ATermUtils.isVar(t) && !kb.isDatatypeProperty(t))
		{
			_logger.warning("Undefined datatype property used in query: " + t);
			return false;
		}

		return true;
	}

	private static boolean isAnnotationProperty(final ATermAppl t, final KnowledgeBase kb)
	{
		if (!ATermUtils.isVar(t) && !kb.isAnnotationProperty(t))
		{
			_logger.warning("Undefined annotation property used in query: " + t);
			return false;
		}

		return true;
	}

	private static boolean isProperty(final ATermAppl t, final KnowledgeBase kb)
	{
		if (!ATermUtils.isVar(t) && !kb.isObjectProperty(t) && !kb.isDatatypeProperty(t) && !kb.isAnnotationProperty(t))
		{
			_logger.warning("Not an object/data/annotation property: " + t);
			return false;
		}

		return true;
	}

	private static boolean isIndividual(final ATermAppl t, final KnowledgeBase kb)
	{
		if (!ATermUtils.isVar(t) && !kb.isIndividual(t))
		{
			_logger.warning("Undefined _individual used in query: " + t);
			return false;
		}

		return true;
	}

	private static boolean isClass(final ATermAppl t, final KnowledgeBase kb)
	{
		if (!ATermUtils.isVar(t) && !kb.isClass(t))
		{
			_logger.warning("Undefined class used in query: " + t);
			return false;
		}

		return true;
	}

	private static boolean isDatatype(final ATermAppl t, final KnowledgeBase kb)
	{
		if (!ATermUtils.isVar(t) && !kb.isDatatype(t))
		{
			_logger.warning("Undefined datatype used in query: " + t);
			return false;
		}

		return true;
	}

	private static boolean hasDefinedTerms(final QueryAtom atom, final KnowledgeBase kb)
	{
		final List<ATermAppl> args = atom.getArguments();

		// TODO in various parts object/data property checks should be strengthened
		switch (atom.getPredicate())
		{
			case Type, DirectType ->
			{
				return isIndividual(args.get(0), kb) && isClass(args.get(1), kb);
			}
			case PropertyValue, NegativePropertyValue ->
			{
				final ATermAppl s = args.get(0);
				final ATermAppl p = args.get(1);
				final ATermAppl o = args.get(2);
				return isIndividual(s, kb) && (ATermUtils.isVar(o) ? isProperty(p, kb) : ATermUtils.isLiteral(o) ? isDatatypeProperty(p, kb) : isObjectProperty(p, kb) && isIndividual(o, kb));
			}
			case SameAs, DifferentFrom ->
			{
				return isIndividual(args.get(0), kb) && isIndividual(args.get(1), kb);
			}
			case DatatypeProperty ->
			{
				return isDatatypeProperty(args.get(0), kb);
			}
			case ObjectProperty, Transitive, InverseFunctional, Symmetric, Asymmetric, Reflexive, Irreflexive ->
			{
				return isObjectProperty(args.get(0), kb);
			}
			case Functional ->
			{
				return isProperty(args.get(0), kb);
			}
			case InverseOf ->
			{
				return isObjectProperty(args.get(0), kb) && isObjectProperty(args.get(1), kb);
			}
			case Domain ->
			{
				return isProperty(args.get(0), kb) && isClass(args.get(1), kb);
			}
			case Range ->
			{
				return isObjectProperty(args.get(0), kb) && isClass(args.get(1), kb) || isDatatypeProperty(args.get(0), kb) && isDatatype(args.get(1), kb);
			}
			case SubPropertyOf, EquivalentProperty, StrictSubPropertyOf, DirectSubPropertyOf, propertyDisjointWith ->
			{
				return isProperty(args.get(0), kb) && isProperty(args.get(1), kb);
			}
			case SubClassOf, EquivalentClass, DisjointWith, ComplementOf, StrictSubClassOf, DirectSubClassOf ->
			{
				return isClass(args.get(0), kb) && isClass(args.get(1), kb);
			}
			case NotKnown ->
			{
				return !hasUndefinedTerm(((NotKnownQueryAtom) atom).getAtoms(), kb);
			}
			case Union ->
			{
				for (final List<QueryAtom> atoms : ((UnionQueryAtom) atom).getUnion())
					if (hasUndefinedTerm(atoms, kb))
						return false;
				return true;
			}
			case Datatype ->
			{
				return kb.isDatatype(args.get(1));
			}
			case Annotation ->
			{
				return isAnnotationProperty(args.get(1), kb);
			}
			default -> throw new AssertionError();
		}
	}

	private static boolean hasUndefinedTerm(final List<QueryAtom> atoms, final KnowledgeBase kb)
	{
		for (final QueryAtom atom : atoms)
			if (!hasDefinedTerms(atom, kb))
				return true;

		return false;
	}

	private static boolean hasUndefinedTerm(final ConjunctiveQuery query)
	{
		return hasUndefinedTerm(query.getAtoms(), query.getKB());
	}

	private static QueryResult execSingleQuery(final ConjunctiveQuery query, QueryResult excludeBindings,
											   QueryResult restrictToBindings)
	{
		if (hasUndefinedTerm(query))
			return new QueryResultImpl(query);

		return getQueryExec().exec(query, excludeBindings, restrictToBindings);
	}

	/**
	 * If a query has disconnected components such as C(x), D(y) then it should be answered as two separate queries.
	 * The answers to each query should be combined at the _end by taking Cartesian product. We combine results on a
	 * tuple basis as results are iterated. This way we avoid generating the full Cartesian product. Splitting the query
	 * ensures the correctness of the answer, e.g. rolling-up technique becomes applicable.
	 *
	 * @param query Query to be split
	 * @return List of queries (contains the initial query if the initial query is connected)
	 */
	public static List<ConjunctiveQuery> split(final ConjunctiveQuery query)
	{
		try
		{
			return new ArrayList<>(query.split());
		}
		catch (final RuntimeException e)
		{
			_logger.log(Level.WARNING, "Query split failed, continuing with query execution.", e);
			return Collections.singletonList(query);
		}
	}


	/**
	 * Simplifies the query.
	 *
	 * @param query the query to simplify
	 */
	private static void simplify(final ConjunctiveQuery query)
	{
		domainRangeSimplification(query);
	}

	private static ConjunctiveQuery preprocess(final ConjunctiveQuery query)
	{
		ConjunctiveQuery q = query;

		final Set<ATermAppl> undistVars = q.getUndistVars();

		// SAMEAS
		// replace of SameAs atoms that contain at least one undistinguished
		// or non-result variable.
		boolean boundSameAs = true;
		while (boundSameAs)
		{
			boundSameAs = false;
			for (final QueryAtom atom : q.findAtoms(QueryPredicate.SameAs, null, null))
			{
				final ATermAppl a1 = atom.getArguments().get(0);
				final ATermAppl a2 = atom.getArguments().get(1);

				boolean replaceA1 = false;
				boolean replaceA2 = false;

				if (!a1.equals(a2))
					if (undistVars.contains(a1))
						replaceA1 = true;
					else
						if (undistVars.contains(a2))
							replaceA2 = true;
						else
							if (ATermUtils.isVar(a1) && !q.getResultVars().contains(a1))
								replaceA1 = true;
							else
								if (ATermUtils.isVar(a2) && !q.getResultVars().contains(a2))
									replaceA2 = true;

				if (replaceA1 || replaceA2)
				{
					final ResultBinding b;
					if (replaceA1)
					{
						b = new ResultBindingImpl();
						b.setValue(a1, a2);
					}
					else
					{
						b = new ResultBindingImpl();
						b.setValue(a2, a1);
					}
					q = q.apply(b);
					boundSameAs = true;
					break;
				}
			}
		}

		// Remove sameAs statements where:
		// 1) Both arguments are the same
		// 2) Neither is a result variable
		// 3) Removing the atom doesn't result in an empty query
		for (final QueryAtom atom : q.findAtoms(QueryPredicate.SameAs, null, null))
		{
			final ATermAppl a1 = atom.getArguments().get(0);
			final ATermAppl a2 = atom.getArguments().get(1);

			// Could remove sameAs with result vars if we could guarantee the query still contained an
			// atom containing the variable.
			if (a1.equals(a2) && !q.getResultVars().contains(a1) && q.getAtoms().size() > 1)
				q.remove(atom);
		}

		// Undistinguished variables + CLASS and PROPERTY variables
		// TODO bug : queries Type(_:x,?x) and PropertyValue(_:x, ?x, . ) and
		// PropertyValue(., ?x, _:x) have to be enriched with one more atom
		// evaluating class/property DVs.
		for (final QueryAtom a : new HashSet<>(q.getAtoms()))
			switch (a.getPredicate())
			{
				case Type, DirectType ->
				{
					final ATermAppl clazz = a.getArguments().get(1);
					if (undistVars.contains(clazz) && undistVars.contains(a.getArguments().get(0)))
						q.add(QueryAtomFactory.SubClassOfAtom(clazz, clazz));
				}
				case PropertyValue ->
				{
					final ATermAppl property = a.getArguments().get(1);
					if (undistVars.contains(a.getArguments().get(0)) || undistVars.contains(a.getArguments().get(2)) && q.getDistVars().contains(property))
						q.add(QueryAtomFactory.SubPropertyOfAtom(property, property));
				}
				default ->
				{
				}
			}

		return q;
	}

	public static CoreStrategy getStrategy(@SuppressWarnings("unused") final QueryAtom core)
	{
		return STRATEGY;
	}

	private static void domainRangeSimplification(final ConjunctiveQuery query)
	{
		final Map<ATermAppl, Set<ATermAppl>> allInferredTypes = new HashMap<>();

		final KnowledgeBase kb = query.getKB();
		final Set<ATermAppl> vars = query.getVars(); // getObjVars

		for (final ATermAppl var : vars)
		{
			final Set<ATermAppl> inferredTypes = new HashSet<>();

			// domain simplification
			for (final QueryAtom pattern : query.findAtoms(QueryPredicate.PropertyValue, var, null, null))
				if (!ATermUtils.isVar(pattern.getArguments().get(1)))
					inferredTypes.addAll(kb.getDomains(pattern.getArguments().get(1)));

			// range simplification
			for (final QueryAtom pattern : query.findAtoms(QueryPredicate.PropertyValue, null, null, var))
				if (!ATermUtils.isVar(pattern.getArguments().get(1)))
					inferredTypes.addAll(kb.getRanges(pattern.getArguments().get(1)));

			if (!inferredTypes.isEmpty())
				allInferredTypes.put(var, inferredTypes);
		}

		for (final QueryAtom atom : new ArrayList<>(query.getAtoms()))
			if (atom.getPredicate() == QueryPredicate.Type)
			{
				final ATermAppl inst = atom.getArguments().get(0);
				final ATermAppl clazz = atom.getArguments().get(1);
				if (!ATermUtils.isVar(clazz))
				{
					final Set<ATermAppl> inferred = allInferredTypes.get(inst);
					if (inferred != null && !inferred.isEmpty())
						if (inferred.contains(clazz))
							query.remove(atom);
						else
							if (kb.isClassified())
							{
								final Set<ATermAppl> subs = kb.getTaxonomy().getFlattenedSubs(clazz, false);
								final Set<ATermAppl> eqs = kb.getAllEquivalentClasses(clazz);
								if (SetUtils.intersects(inferred, subs) || SetUtils.intersects(inferred, eqs))
									query.remove(atom);
							}
				}
			}
	}

	/**
	 * Executes all boolean ABox atoms
	 *
	 * @param query the query to check entailment for
	 * @return true if query is satisfied
	 */
	public static boolean execBooleanABoxQuery(final ConjunctiveQuery query)
	{
		// if (!query.getDistVars().isEmpty()) {
		// throw new InternalReasonerException(
		// "Executing execBoolean with nonboolean query : " + query);
		// }

		boolean querySatisfied;

		final KnowledgeBase kb = query.getKB();
		kb.ensureConsistency();

		// unless proven otherwise all (ground) triples are satisfied
		Bool allTriplesSatisfied = Bool.TRUE;

		for (final QueryAtom atom : query.getAtoms())
		{
			// by default we don't know if triple is satisfied
			Bool tripleSatisfied = Bool.UNKNOWN;
			// we can only check ground triples
			if (atom.isGround())
			{
				final List<ATermAppl> arguments = atom.getArguments();

				tripleSatisfied = switch (atom.getPredicate())
						{
							case Type -> kb.isKnownType(arguments.get(0), arguments.get(1));
							case Annotation, PropertyValue ->
									kb.hasKnownPropertyValue(arguments.get(0), arguments.get(1), arguments.get(2));
							default -> Bool.UNKNOWN;
						};
			}

			// if we cannot decide the truth value of this triple (without a
			// consistency
			// check) then over all truth value cannot be true. However, we will
			// continue
			// to see if there is a triple that is obviously false
			if (tripleSatisfied.isUnknown())
				allTriplesSatisfied = Bool.UNKNOWN;
			else
				if (tripleSatisfied.isFalse())
				{
					// if one triple is false then the whole query, which is the
					// conjunction of
					// all triples, is false. We can _stop now.
					allTriplesSatisfied = Bool.FALSE;

					if (_logger.isLoggable(Level.FINER))
						_logger.finer("Failed atom: " + atom);

					break;
				}
		}

		// if we reached a verdict, return it
		if (allTriplesSatisfied.isKnown())
			querySatisfied = allTriplesSatisfied.isTrue();
		else
			// do the unavoidable consistency check
			if (!query.getConstants().isEmpty())
			{
				final ATermAppl testInd = query.getConstants().iterator().next();
				final ATermAppl testClass = query.rollUpTo(testInd, Collections.<ATermAppl> emptySet(), false);

				if (_logger.isLoggable(Level.FINER))
					_logger.finer("Boolean query: " + testInd + " -> " + testClass);

				querySatisfied = kb.isType(testInd, testClass);
			}
			else
			{
				final ATermAppl testVar = query.getUndistVars().iterator().next();
				final ATermAppl testClass = query.rollUpTo(testVar, Collections.<ATermAppl> emptySet(), false);

				final ATermAppl newUC = ATermUtils.normalize(ATermUtils.makeNot(testClass));

				final Role topObjectRole = kb.getRole(TOP_OBJECT_PROPERTY);
				final boolean added = topObjectRole.addDomain(newUC, DependencySet.INDEPENDENT);

				final ABox copy = kb.getABox().copy();
				copy.setInitialized(false);
				querySatisfied = !copy.isConsistent();

				if (added)
					topObjectRole.removeDomain(newUC, DependencySet.INDEPENDENT);
			}

		return querySatisfied;
	}

	public static boolean checkGround(final QueryAtom atom, final KnowledgeBase kb)
	{

		final List<ATermAppl> arguments = atom.getArguments();

		switch (atom.getPredicate())
		{
			case Type ->
			{
				return kb.isType(arguments.get(0), arguments.get(1));
			}
			case DirectType ->
			{
				return kb.getInstances(arguments.get(1), true).contains(arguments.get(0));
			}
			case Annotation ->
			{
				return kb.getAnnotations(arguments.get(0), arguments.get(1)).contains(arguments.get(2));
			}
			case PropertyValue ->
			{
				return kb.hasPropertyValue(arguments.get(0), arguments.get(1), arguments.get(2));
			}
			case SameAs ->
			{
				return kb.isSameAs(arguments.get(0), arguments.get(1));
			}
			case DifferentFrom ->
			{
				return kb.isDifferentFrom(arguments.get(0), arguments.get(1));
			}
			case EquivalentClass ->
			{
				return kb.isEquivalentClass(arguments.get(0), arguments.get(1));
			}
			case SubClassOf ->
			{
				return kb.isSubClassOf(arguments.get(0), arguments.get(1));
			}
			case DirectSubClassOf ->
			{
				for (final Set<ATermAppl> a : kb.getSubClasses(arguments.get(1), true))
					if (a.contains(arguments.get(0)))
						return true;
				return false;
			}
			case StrictSubClassOf ->
			{
				return kb.isSubClassOf(arguments.get(0), arguments.get(1)) && !kb.getEquivalentClasses(arguments.get(1)).contains(arguments.get(0));
			}
			case DisjointWith ->
			{
				return kb.isDisjoint(arguments.get(0), arguments.get(1));
			}
			case ComplementOf ->
			{
				return kb.isComplement(arguments.get(0), arguments.get(1));
			}
			case EquivalentProperty ->
			{
				return kb.isEquivalentProperty(arguments.get(0), arguments.get(1));
			}
			case SubPropertyOf ->
			{
				return kb.isSubPropertyOf(arguments.get(0), arguments.get(1));
			}
			case DirectSubPropertyOf ->
			{
				for (final Set<ATermAppl> a : kb.getSubProperties(arguments.get(1), true))
					if (a.contains(arguments.get(0)))
						return true;
				return false;
			}
			case StrictSubPropertyOf ->
			{
				return kb.isSubPropertyOf(arguments.get(0), arguments.get(1)) &&
						!kb.getEquivalentProperties(arguments.get(1)).contains(arguments.get(0));
			}
			case Domain ->
			{
				return kb.hasDomain(arguments.get(0), arguments.get(1));
			}
			case Range ->
			{
				return kb.hasRange(arguments.get(0), arguments.get(1));
			}
			case InverseOf ->
			{
				return kb.isInverse(arguments.get(0), arguments.get(1));
			}
			case ObjectProperty ->
			{
				return kb.isObjectProperty(arguments.get(0));
			}
			case DatatypeProperty ->
			{
				return kb.isDatatypeProperty(arguments.get(0));
			}
			case Functional ->
			{
				return kb.isFunctionalProperty(arguments.get(0));
			}
			case InverseFunctional ->
			{
				return kb.isInverseFunctionalProperty(arguments.get(0));
			}
			case Symmetric ->
			{
				return kb.isSymmetricProperty(arguments.get(0));
			}
			case Asymmetric ->
			{
				return kb.isAsymmetricProperty(arguments.get(0));
			}
			case Reflexive ->
			{
				return kb.isReflexiveProperty(arguments.get(0));
			}
			case Irreflexive ->
			{
				return kb.isIrreflexiveProperty(arguments.get(0));
			}
			case Transitive ->
			{
				return kb.isTransitiveProperty(arguments.get(0));
			}
			case NotKnown ->
			{
				for (final QueryAtom notAtom : ((NotKnownQueryAtom) atom).getAtoms())
					if (!checkGround(notAtom, kb))
						return true;
				return false;
			}
			case NegativePropertyValue ->
			{
				return kb.isType(arguments.get(0), not(hasValue(arguments.get(1), arguments.get(2))));
			}
			case Union ->
			{
				for (final List<QueryAtom> atoms : ((UnionQueryAtom) atom).getUnion())
				{
					for (final QueryAtom unionAtom : atoms)
						if (!checkGround(unionAtom, kb))
							break; // Go to next sequence of atoms
					return true;
				}
				return false;
			}
			case Datatype ->
			{
				final ATermAppl l = arguments.get(0);
				final ATermAppl d = arguments.get(1);
				if (!ATermUtils.isLiteral(l))
					return false;
				final DatatypeReasoner dtReasoner = kb.getDatatypeReasoner();
				try
				{
					final Object value = dtReasoner.getValue(l);
					return dtReasoner.isSatisfiable(Collections.singleton(d), value);
				} catch (final DatatypeReasonerException e)
				{
					final String msg = format("Unexpected datatype reasoner exception while checking if literal (%s) is in datarange (%s): %s ", l, d, e.getMessage());
					_logger.severe(msg);
					throw new InternalReasonerException(msg, e);
				}
			}
			default -> throw new IllegalArgumentException("Unknown atom type : " + atom.getPredicate());
		}
	}

}
