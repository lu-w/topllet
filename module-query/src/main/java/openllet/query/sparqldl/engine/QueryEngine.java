// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.engine;

import static java.lang.String.format;
import static openllet.core.utils.TermFactory.TOP_OBJECT_PROPERTY;
import static openllet.core.utils.TermFactory.hasValue;
import static openllet.core.utils.TermFactory.not;

import java.util.ArrayList;
import java.util.Collection;
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
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Bool;
import openllet.core.utils.DisjointSet;
import openllet.core.utils.SetUtils;
import openllet.query.sparqldl.model.MultiQueryResults;
import openllet.query.sparqldl.model.NotKnownQueryAtom;
import openllet.query.sparqldl.model.UnionQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.UnionQuery.VarType;
import openllet.query.sparqldl.model.QueryAtom;
import openllet.query.sparqldl.model.QueryAtomFactory;
import openllet.query.sparqldl.model.QueryImpl;
import openllet.query.sparqldl.model.QueryPredicate;
import openllet.query.sparqldl.model.QueryResult;
import openllet.query.sparqldl.model.QueryResultImpl;
import openllet.query.sparqldl.model.ResultBinding;
import openllet.query.sparqldl.model.ResultBindingImpl;
import openllet.query.sparqldl.model.UnionQueryAtom;
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
public class QueryEngine
{
	public static Logger _logger = Log.getLogger(QueryEngine.class);

	public static CoreStrategy STRATEGY = CoreStrategy.ALLFAST;

	public static QueryExec getQueryExec()
	{
		return new CombinedQueryEngine();
	}

	public static boolean supports(final UnionQuery query, @SuppressWarnings("unused") final KnowledgeBase kb)
	{
		return getQueryExec().supports(query); // TODO for UnionQuery
	}

	public static QueryResult exec(final Query query, final KnowledgeBase kb)
	{
		final KnowledgeBase queryKB = query.getKB();
		query.setKB(kb);
		final QueryResult result = exec(query);
		query.setKB(queryKB);
		return result;
	}

	public static QueryResult exec(final Query query)
	{
		if ((query.getQueries().size() == 0) ||
				(query.getQueries().size() == 1 && query.getQueries().get(0).getAtoms().isEmpty()))
		{
			final QueryResultImpl results = new QueryResultImpl(query);
			results.add(new ResultBindingImpl());
			return results;
		}
		query.getKB().ensureConsistency();

		// PREPROCESSING
		_logger.fine(() -> "Preprocessing:\n" + query);
		final Query preprocessed = preprocess(query);

		// SIMPLIFICATION
		if (OpenlletOptions.SIMPLIFY_QUERY)
		{
			_logger.fine(() -> "Simplifying:\n" + preprocessed);

			simplify(preprocessed);
		}

		// SPLITTING
		_logger.fine(() -> "Splitting:\n" + preprocessed);

		final List<Query> queries = split(preprocessed);

		QueryResult r = null;
		if (queries.isEmpty())
			throw new InternalReasonerException("Splitting query returned no results!");
		else if (queries.size() == 1)
			r = execSingleQuery(queries.get(0));
		else
		{
			final List<QueryResult> results = new ArrayList<>(queries.size());
			for (final Query q : queries)
				results.add(execSingleQuery(q));

			r = new MultiQueryResults(query.getResultVars(), results);
		}

		return r;
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
			case Type:
			case DirectType:
				return isIndividual(args.get(0), kb) && isClass(args.get(1), kb);

			case PropertyValue:
			case NegativePropertyValue:
				final ATermAppl s = args.get(0);
				final ATermAppl p = args.get(1);
				final ATermAppl o = args.get(2);
				return isIndividual(s, kb) && (ATermUtils.isVar(o) ? isProperty(p, kb) : ATermUtils.isLiteral(o) ? isDatatypeProperty(p, kb) : isObjectProperty(p, kb) && isIndividual(o, kb));

			case SameAs:
			case DifferentFrom:
				return isIndividual(args.get(0), kb) && isIndividual(args.get(1), kb);

			case DatatypeProperty:
				return isDatatypeProperty(args.get(0), kb);

			case ObjectProperty:
			case Transitive:
			case InverseFunctional:
			case Symmetric:
			case Asymmetric:
			case Reflexive:
			case Irreflexive:
				return isObjectProperty(args.get(0), kb);

			case Functional:
				return isProperty(args.get(0), kb);

			case InverseOf:
				return isObjectProperty(args.get(0), kb) && isObjectProperty(args.get(1), kb);

			case Domain:
				return isProperty(args.get(0), kb) && isClass(args.get(1), kb);
			case Range:
				return isObjectProperty(args.get(0), kb) && isClass(args.get(1), kb) || isDatatypeProperty(args.get(0), kb) && isDatatype(args.get(1), kb);

			case SubPropertyOf:
			case EquivalentProperty:
			case StrictSubPropertyOf:
			case DirectSubPropertyOf:
			case propertyDisjointWith:
				return isProperty(args.get(0), kb) && isProperty(args.get(1), kb);

			case SubClassOf:
			case EquivalentClass:
			case DisjointWith:
			case ComplementOf:
			case StrictSubClassOf:
			case DirectSubClassOf:
				return isClass(args.get(0), kb) && isClass(args.get(1), kb);

			case NotKnown:
				return !hasUndefinedTerm(((NotKnownQueryAtom) atom).getAtoms(), kb);

			case Union:
				for (final List<QueryAtom> atoms : ((UnionQueryAtom) atom).getUnion())
					if (hasUndefinedTerm(atoms, kb))
						return false;
				return true;

			case Datatype:
				return kb.isDatatype(args.get(1));

			case Annotation:
				return isAnnotationProperty(args.get(1), kb);

			default:
				throw new AssertionError();
		}
	}

	private static boolean hasUndefinedTerm(final List<QueryAtom> atoms, final KnowledgeBase kb)
	{
		for (final QueryAtom atom : atoms)
			if (!hasDefinedTerms(atom, kb))
				return true;

		return false;
	}

	private static boolean hasUndefinedTerm(final Query query)
	{
		return hasUndefinedTerm(query.getAtoms(), query.getKB());
	}

	private static QueryResult execSingleQuery(final Query query)
	{
		if (hasUndefinedTerm(query))
			return new QueryResultImpl(query);

		return getQueryExec().exec(query);
	}

	/**
	 * If a query has disconnected components such as C(x), D(y) then it should be answered as two separate queries. The answers to each query should be
	 * combined at the _end by taking Cartesian product.(we combine results on a tuple basis as results are iterated. This way we avoid generating the full
	 * Cartesian product. Splitting the query ensures the correctness of the answer, e.g. rolling-up technique becomes applicable.
	 * This version also considers individuals to split on (if set), so e.g. C(a), D(b) is split into two queries.
	 *
	 * @param query Query to be split
	 * @param splitOnIndividuals Whether to split on individuals.
	 * @return List of queries (contains the initial query if the initial query is connected)
	 */
	public static List<Query> split(final Query query, boolean splitOnIndividuals)
	{
		try
		{
			final Set<ATermAppl> resultVars = new HashSet<>(query.getResultVars());

			final DisjointSet<ATermAppl> disjointSet = new DisjointSet<>();

			for (final QueryAtom atom : query.getAtoms())
			{
				ATermAppl toMerge = null;

				for (final ATermAppl arg : atom.getArguments())
				{
					if (!(ATermUtils.isVar(arg) || (splitOnIndividuals && query.getKB().isIndividual(arg))))
						continue;

					disjointSet.add(arg);
					if (toMerge != null)
						disjointSet.union(toMerge, arg);
					toMerge = arg;
				}
			}

			final Collection<Set<ATermAppl>> equivalenceSets = disjointSet.getEquivalanceSets();
			if (equivalenceSets.size() == 1)
				return Collections.singletonList(query);

			final Map<ATermAppl, Query> queries = new HashMap<>();
			Query groundQuery = null;
			for (final QueryAtom atom : query.getAtoms())
			{
				ATermAppl representative = null;
				for (final ATermAppl arg : atom.getArguments())
					if (ATermUtils.isVar(arg) || (splitOnIndividuals && query.getKB().isIndividual(arg)))
					{
						representative = disjointSet.find(arg);
						break;
					}

				Query newQuery = null;
				if (representative == null)
				{
					if (groundQuery == null)
						groundQuery = new QueryImpl(query);
					newQuery = groundQuery;
				}
				else
				{
					newQuery = queries.get(representative);
					if (newQuery == null)
					{
						newQuery = new QueryImpl(query);
						queries.put(representative, newQuery);
					}
					for (final ATermAppl arg : atom.getArguments())
					{
						if (resultVars.contains(arg))
							newQuery.addResultVar(arg);

						for (final VarType v : VarType.values())
							if (query.getDistVarsForType(v).contains(arg))
								newQuery.addDistVar(arg, v);
					}
				}

				newQuery.add(atom);
			}

			final List<Query> list = new ArrayList<>(queries.values());

			if (groundQuery != null)
				list.add(0, groundQuery);

			return list;
		}
		catch (final RuntimeException e)
		{
			_logger.log(Level.WARNING, "Query split failed, continuing with query execution.", e);
			return Collections.singletonList(query);
		}
	}

	/**
	 * If a query has disconnected components such as C(x), D(y) then it should be answered as two separate queries. The answers to each query should be
	 * combined at the _end by taking Cartesian product.(we combine results on a tuple basis as results are iterated. This way we avoid generating the full
	 * Cartesian product. Splitting the query ensures the correctness of the answer, e.g. rolling-up technique becomes applicable.
	 *
	 * @param query Query to be split
	 * @return List of queries (contains the initial query if the initial query is connected)
	 */
	public static List<Query> split(final Query query)
	{
		return split(query, false);
	}


	/**
	 * Simplifies the query.
	 *
	 * @param query
	 */
	private static void simplify(final Query query)
	{
		domainRangeSimplification(query);
	}

	private static Query preprocess(final Query query)
	{
		Query q = query;

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
				case Type:
				case DirectType:
					final ATermAppl clazz = a.getArguments().get(1);

					if (undistVars.contains(clazz) && undistVars.contains(a.getArguments().get(0)))
						q.add(QueryAtomFactory.SubClassOfAtom(clazz, clazz));
					break;
				case PropertyValue:
					final ATermAppl property = a.getArguments().get(1);

					if (undistVars.contains(a.getArguments().get(0)) || undistVars.contains(a.getArguments().get(2)) && q.getDistVars().contains(property))
						q.add(QueryAtomFactory.SubPropertyOfAtom(property, property));
					break;
				default:
					break;
			}

		return q;
	}

	public static CoreStrategy getStrategy(@SuppressWarnings("unused") final QueryAtom core)
	{
		return STRATEGY;
	}

	private static void domainRangeSimplification(final Query query)
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
	 * @param query
	 * @return true if query is satisfied
	 */
	public static boolean execBooleanABoxQuery(final Query query) // TODO should take a List of DisjunctiveQueries
	{
		// if (!query.getDistVars().isEmpty()) {
		// throw new InternalReasonerException(
		// "Executing execBoolean with nonboolean query : " + query);
		// }

		// Lukas: somewhere earlier, we would need to transform the query into CNF. Then, a CQ is just a special case of a UCQ in CNF and everything here should work as well.
		// Functions used below (such as isKnownType or isType) can just include a check whether |atoms| == 1 (we are in the CQ case) and then proceed as already implemented. Otherwise, we will use the methods to check unions.
		// TODO mark those lines that are dependent on the assumption |atoms| == 1 (i.e. those that I need to change)

		boolean querySatisfied;

		final KnowledgeBase kb = query.getKB();
		kb.ensureConsistency();

		// unless proven otherwise all (ground) triples are satisfied
		Bool allTriplesSatisfied = Bool.TRUE; // TODO allConjunctsSatisfied

		for (final QueryAtom atom : query.getAtoms()) // TODO for UnionQuery in list
		{
			// by default we don't know if triple is satisfied
			Bool tripleSatisfied = Bool.UNKNOWN;
			// we can only check ground triples
			if (atom.isGround())
			{
				final List<ATermAppl> arguments = atom.getArguments();

				switch (atom.getPredicate())
				{
					case Type:
						tripleSatisfied = kb.isKnownType(arguments.get(0), arguments.get(1));
						break;
					case Annotation:
					case PropertyValue:
						tripleSatisfied = kb.hasKnownPropertyValue(arguments.get(0), arguments.get(1), arguments.get(2));
						break;
					default:
						tripleSatisfied = Bool.UNKNOWN;
				}
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
				// Lukas: isType performs the actual A u {(not C)(a)} cons. check! But only called if the ontology has
				// not been realized before. For UCQ, we would adapt isType to accept sets of atoms and return true iff
				// all models of O satisfy some element of the list (be using the (not C)(a), (not D)(b) method
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
				querySatisfied = !copy.isConsistent(); // TODO needs to change to UCQ method

				if (added)
					topObjectRole.removeDomain(newUC, DependencySet.INDEPENDENT);
			}

		return querySatisfied;
	}

	// TODO should take a disjunctive query instead of an atom
	public static boolean checkGround(final QueryAtom atom, final KnowledgeBase kb)
	{

		final List<ATermAppl> arguments = atom.getArguments();

		// TODO the "isType" functions in the ABoxImpl need to be adapted to handle disjunctions (and should collapse
		//  to the now implemented case for n=1.
		switch (atom.getPredicate())
		{
			case Type:
				return kb.isType(arguments.get(0), arguments.get(1));
			case DirectType:
				return kb.getInstances(arguments.get(1), true).contains(arguments.get(0));
			case Annotation:
				return kb.getAnnotations(arguments.get(0), arguments.get(1)).contains(arguments.get(2));
			case PropertyValue:
				return kb.hasPropertyValue(arguments.get(0), arguments.get(1), arguments.get(2));
			case SameAs:
				return kb.isSameAs(arguments.get(0), arguments.get(1));
			case DifferentFrom:
				return kb.isDifferentFrom(arguments.get(0), arguments.get(1));
			case EquivalentClass:
				return kb.isEquivalentClass(arguments.get(0), arguments.get(1));
			case SubClassOf:
				return kb.isSubClassOf(arguments.get(0), arguments.get(1));
			case DirectSubClassOf:
				for (final Set<ATermAppl> a : kb.getSubClasses(arguments.get(1), true))
					if (a.contains(arguments.get(0)))
						return true;
				return false;
			case StrictSubClassOf:
				return kb.isSubClassOf(arguments.get(0), arguments.get(1)) && !kb.getEquivalentClasses(arguments.get(1)).contains(arguments.get(0));
			case DisjointWith:
				return kb.isDisjoint(arguments.get(0), arguments.get(1));
			case ComplementOf:
				return kb.isComplement(arguments.get(0), arguments.get(1));
			case EquivalentProperty:
				return kb.isEquivalentProperty(arguments.get(0), arguments.get(1));
			case SubPropertyOf:
				return kb.isSubPropertyOf(arguments.get(0), arguments.get(1));
			case DirectSubPropertyOf:
				for (final Set<ATermAppl> a : kb.getSubProperties(arguments.get(1), true))
					if (a.contains(arguments.get(0)))
						return true;
				return false;
			case StrictSubPropertyOf:
				return kb.isSubPropertyOf(arguments.get(0), arguments.get(1)) && !kb.getEquivalentProperties(arguments.get(1)).contains(arguments.get(0));
			case Domain:
				return kb.hasDomain(arguments.get(0), arguments.get(1));
			case Range:
				return kb.hasRange(arguments.get(0), arguments.get(1));
			case InverseOf:
				return kb.isInverse(arguments.get(0), arguments.get(1));
			case ObjectProperty:
				return kb.isObjectProperty(arguments.get(0));
			case DatatypeProperty:
				return kb.isDatatypeProperty(arguments.get(0));
			case Functional:
				return kb.isFunctionalProperty(arguments.get(0));
			case InverseFunctional:
				return kb.isInverseFunctionalProperty(arguments.get(0));
			case Symmetric:
				return kb.isSymmetricProperty(arguments.get(0));
			case Asymmetric:
				return kb.isAsymmetricProperty(arguments.get(0));
			case Reflexive:
				return kb.isReflexiveProperty(arguments.get(0));
			case Irreflexive:
				return kb.isIrreflexiveProperty(arguments.get(0));
			case Transitive:
				return kb.isTransitiveProperty(arguments.get(0));
			case NotKnown:
				for (final QueryAtom notAtom : ((NotKnownQueryAtom) atom).getAtoms())
					if (!checkGround(notAtom, kb))
						return true;
				return false;
			case NegativePropertyValue:
				return kb.isType(arguments.get(0), not(hasValue(arguments.get(1), arguments.get(2))));
			case Union:
				for (final List<QueryAtom> atoms : ((UnionQueryAtom) atom).getUnion())
				{
					for (final QueryAtom unionAtom : atoms)
						if (!checkGround(unionAtom, kb))
							break; // Go to next sequence of atoms
					return true;
				}
				return false;
			case Datatype:
				final ATermAppl l = arguments.get(0);
				final ATermAppl d = arguments.get(1);

				if (!ATermUtils.isLiteral(l))
					return false;

				final DatatypeReasoner dtReasoner = kb.getDatatypeReasoner();
				try
				{
					final Object value = dtReasoner.getValue(l);
					return dtReasoner.isSatisfiable(Collections.singleton(d), value);
				}
				catch (final DatatypeReasonerException e)
				{
					final String msg = format("Unexpected datatype reasoner exception while checking if literal (%s) is in datarange (%s): %s ", l, d, e.getMessage());
					_logger.severe(msg);
					throw new InternalReasonerException(msg, e);
				}

			default:
				throw new IllegalArgumentException("Unknown atom type : " + atom.getPredicate());
		}
	}

}
