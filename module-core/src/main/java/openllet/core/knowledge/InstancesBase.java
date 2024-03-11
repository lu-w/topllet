package openllet.core.knowledge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.atom.OpenError;
import openllet.core.OpenlletOptions;
import openllet.core.OpenlletOptions.InstanceRetrievalMethod;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.rbox.Role;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.taxonomy.TaxonomyUtils;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Bool;
import openllet.core.utils.Timer;
import openllet.shared.tools.Logging;

/**
 * Groupment of all methods related to instances.
 *
 * @since 2.6.4
 */
public interface InstancesBase extends MessageBase, Logging, Base
{

	default void linearInstanceRetrieval(final ATermAppl c, final List<ATermAppl> candidates, final Collection<ATermAppl> results)
	{
		if (null == c || null == candidates || null == results)
			return;

		for (final ATermAppl ind : candidates)
			if (getABox().isType(ind, c))
				results.add(ind);
	}

	@Override
	default void binaryInstanceRetrieval(final ATermAppl c, final List<ATermAppl> candidates, final Collection<ATermAppl> results)
	{
		if (null == c || null == candidates || null == results)
			return;

		if (candidates.isEmpty())
			return;
		else
			partitionInstanceRetrieval(c, partition(candidates), results);
	}

	default void partitionInstanceRetrieval(final ATermAppl c, final List<ATermAppl>[] partitions, final Collection<ATermAppl> results)
	{
		if (partitions[0].size() == 1)
		{
			final ATermAppl i = partitions[0].get(0);
			binaryInstanceRetrieval(c, partitions[1], results);

			if (getABox().isType(i, c))
				results.add(i);
		}
		else
			if (!getABox().existType(partitions[0], c))
				binaryInstanceRetrieval(c, partitions[1], results);
			else
				if (!getABox().existType(partitions[1], c))
					binaryInstanceRetrieval(c, partitions[0], results);
				else
				{
					binaryInstanceRetrieval(c, partitions[0], results);
					binaryInstanceRetrieval(c, partitions[1], results);
				}
	}

	@SuppressWarnings("unchecked")
	static List<ATermAppl>[] partition(final List<ATermAppl> candidates)
	{
		final List<ATermAppl>[] partitions = new List[2];
		final int n = candidates.size();
		if (n <= 1)
		{
			partitions[0] = candidates;
			partitions[1] = Collections.emptyList();
		}
		else
		{
			partitions[0] = candidates.subList(0, n / 2);
			partitions[1] = candidates.subList(n / 2, n);
		}

		return partitions;
	}

	/**
	 * Returns all the instances of concept c. If TOP concept is used every individual in the knowledge base will be returned
	 *
	 * @param c class whose instances are returned
	 * @return A set of ATerm objects
	 */
	default Set<ATermAppl> getInstances(final ATermAppl c)
	{
		return getInstances(c, null);
	}

	default Set<ATermAppl> getInstances(final ATermAppl c, Set<ATermAppl> restrictToInstances)
	{
		if (null == c)
			return Collections.emptySet();

		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAnClass);
			return Collections.emptySet();
		}

		if (getInstances().containsKey(c))
			return getInstances().get(c);
		else
			if (isRealized())
			{
				final Taxonomy<ATermAppl> taxonomy = getTaxonomyBuilder().getTaxonomy();

				if (taxonomy == null)
					throw new OpenError("Taxonomy is null");

				if (taxonomy.contains(c) && ATermUtils.isPrimitive(c))
					return TaxonomyUtils.getAllInstances(taxonomy, c);
			}

		Set<ATermAppl> candidates;
		if (restrictToInstances == null)
			candidates = getIndividuals();
		else
			candidates = restrictToInstances;

		return new HashSet<>(retrieve(c, candidates));
	}

	/**
	 * Returns the instances of class c. Depending on the second parameter the resulting list will include all or only the direct getInstances(). An _individual
	 * x is a direct instance of c iff x is of type c and there is no subclass d of c such that x is of type d.
	 * <p>
	 * *** This function will first realize the whole ontology ***
	 * </p>
	 *
	 * @param c class whose getInstances() are returned
	 * @param direct if true return only the direct getInstances(), otherwise return all the getInstances()
	 * @return A set of ATerm objects
	 */
	default Set<ATermAppl> getInstances(final ATermAppl c, final boolean direct)
	{
		return getInstances(c, direct, null);
	}

	default Set<ATermAppl> getInstances(final ATermAppl c, final boolean direct, Set<ATermAppl> restrictToInstances)
	{
		if (null == c)
			return Collections.emptySet();

		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAnClass);
			return Collections.emptySet();
		}

		// All getInstances() for anonymous concepts
		if (!direct)
			return getInstances(c, restrictToInstances);

		realize();

		final Taxonomy<ATermAppl> taxonomy = getTaxonomyBuilder().getTaxonomy();

		if (taxonomy == null)
			throw new OpenError("Taxonomy is null");

		// Named concepts
		if (ATermUtils.isPrimitive(c))
			return TaxonomyUtils.getDirectInstances(taxonomy, c);

		if (!taxonomy.contains(c))
			getTaxonomyBuilder().classify(c);

		// Direct getInstances() for anonymous concepts
		final Set<ATermAppl> ret = new HashSet<>();
		final Set<Set<ATermAppl>> sups = getSuperClasses(c, true);

		for (final Set<ATermAppl> s : sups)
		{
			final Iterator<ATermAppl> i = s.iterator();
			final ATermAppl term = i.next();
			final Set<ATermAppl> cand = TaxonomyUtils.getDirectInstances(taxonomy, term);

			if (ret.isEmpty())
				ret.addAll(cand);
			else
				ret.retainAll(cand);

			if (ret.isEmpty())
				return ret;
		}

		return retrieve(c, ret);
	}

	/**
	 * @param d
	 * @param individuals
	 * @return all the individuals that belong to the given class which is not necessarily a named class.
	 */
	default Set<ATermAppl> retrieve(final ATermAppl d, final Collection<ATermAppl> individuals)
	{
		if (null == d || null == individuals)
			return Collections.emptySet();

		ensureConsistency();

		final ATermAppl c = ATermUtils.normalize(d);

		if (ATermUtils.isAnd(c))
		{
			Set<ATermAppl> terms = new HashSet<>(individuals);

			if (1 != c.getArity())
				throw new OpenError("arity isn't 1.");

			if (1 == c.getArity())
			{
				final ATerm arg = c.getArgument(0);
				if (arg instanceof ATermList)
					for (final ATerm term : (ATermList) arg)
						terms = retrieve((ATermAppl) term, terms);
			}
			else
				for (final ATerm term : c.getArgumentArray())
					terms = retrieve((ATermAppl) term, terms);

			return terms;
		}
		else
		{
			final Optional<Timer> timer = getTimers().startTimer("retrieve");

			final ATermAppl notC = ATermUtils.negate(c);
			final List<ATermAppl> knowns = new ArrayList<>();

			// this is mostly to ensure that a model for notC is cached
			if (!getABox().isSatisfiable(notC))
				knowns.addAll(getIndividuals()); // if negation is unsat c itself is TOP
			else
				if (getABox().isSatisfiable(c))
				{
					Set<ATermAppl> subs = Collections.emptySet();
					if (isClassified())
					{
						final Taxonomy<ATermAppl> taxonomy = getTaxonomyBuilder().getTaxonomy();

						if (taxonomy == null)
							throw new NullPointerException("Taxonomy");

						if (taxonomy.contains(c))
							subs = taxonomy.getFlattenedSubs(c, false);
					}

					final List<ATermAppl> unknowns = new ArrayList<>();
					for (final ATermAppl x : individuals)
					{
						final Bool isType = getABox().isKnownType(x, c, subs);
						if (isType.isTrue())
							knowns.add(x);
						else
							if (isType.isUnknown())
								unknowns.add(x);
					}

					if (!unknowns.isEmpty())
						if (OpenlletOptions.INSTANCE_RETRIEVAL == InstanceRetrievalMethod.TRACING_BASED && OpenlletOptions.USE_TRACING)
							tracingBasedInstanceRetrieval(c, unknowns, knowns);
						else
							if (getABox().existType(unknowns, c))
								if (OpenlletOptions.INSTANCE_RETRIEVAL == InstanceRetrievalMethod.BINARY)
									binaryInstanceRetrieval(c, unknowns, knowns);
								else
									linearInstanceRetrieval(c, unknowns, knowns);

				}

			timer.ifPresent(Timer::stop);

			final Set<ATermAppl> result = Collections.unmodifiableSet(new HashSet<>(knowns));

			if (OpenlletOptions.CACHE_RETRIEVAL)
				getInstances().put(c, result);

			return result;
		}
	}

	default void tracingBasedInstanceRetrieval(final ATermAppl c, final List<ATermAppl> candidates, final Collection<ATermAppl> results)
	{
		if (null == c || null == candidates || null == results)
			return;

		List<ATermAppl> individuals = candidates;
		final boolean doExplanation = doExplanation();
		setDoExplanation(true);

		final ATermAppl notC = ATermUtils.negate(c);
		while (getABox().existType(individuals, c))
		{
			final Set<ATermAppl> explanationSet = getExplanationSet();

			for (final ATermAppl axiom : explanationSet)
				if (axiom.getAFun().equals(ATermUtils.TYPEFUN) && axiom.getArgument(1).equals(notC))
				{
					final ATermAppl ind = (ATermAppl) axiom.getArgument(0);
					final int index = individuals.indexOf(ind);
					if (index >= 0)
					{

						getLogger().finer(() -> "Filter instance " + axiom + " while retrieving " + c);
						Collections.swap(individuals, index, 0);
						results.add(ind);
						individuals = individuals.subList(1, individuals.size());
						break;
					}
				}
		}

		setDoExplanation(doExplanation);
	}

	/**
	 * @param r
	 * @return individuals which possibly have a property value for the given property.
	 */
	default List<ATermAppl> retrieveIndividualsWithProperty(final ATermAppl r)
	{
		if (null == r)
			return Collections.emptyList();

		ensureConsistency();

		final Role role = getRBox().getRole(r);
		if (role == null)
		{
			Base.handleUndefinedEntity(r + _isNotAnKnowProperty);
			return Collections.emptyList();
		}

		final List<ATermAppl> result = new ArrayList<>();
		for (final ATermAppl ind : getIndividuals())
			if (!getABox().hasObviousPropertyValue(ind, r, null).isFalse())
				result.add(ind);

		return result;
	}

	/**
	 * Returns the (named) classes individual belongs to. Depending on the second parameter the result will include either all types or only the direct types.
	 *
	 * @param ind An _individual name
	 * @param direct If true return only the direct types, otherwise return all types
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	default Set<Set<ATermAppl>> getTypes(final ATermAppl ind, final boolean direct)
	{
		if (null == ind)
			return Collections.emptySet();

		if (!isIndividual(ind))
		{
			Base.handleUndefinedEntity(ind + _isNotAnIndividual);
			return Collections.emptySet();
		}

		if (OpenlletOptions.AUTO_REALIZE)
			realize();

		Set<Set<ATermAppl>> types = isClassified() ? getPrimitiveTypes(ind, direct) : Collections.<Set<ATermAppl>> emptySet();

		if (types.isEmpty() && !OpenlletOptions.AUTO_REALIZE)
		{
			classify();
			getTaxonomyBuilder().realize(ind);
			types = getPrimitiveTypes(ind, direct);
		}

		return types;
	}

	default Set<Set<ATermAppl>> getPrimitiveTypes(final ATermAppl ind, final boolean direct)
	{
		final Set<Set<ATermAppl>> types = new HashSet<>();
		for (final Set<ATermAppl> t : TaxonomyUtils.getTypes(getTaxonomyBuilder().getTaxonomy(), ind, direct))
		{
			final Set<ATermAppl> eqSet = ATermUtils.primitiveOrBottom(t);
			if (!eqSet.isEmpty())
				types.add(eqSet);
		}
		return types;
	}

	/**
	 * @param name
	 * @return all the indviduals asserted to be equal to the given individual inluding the individual itself.
	 */
	default Set<ATermAppl> getAllSames(final ATermAppl name)
	{
		if (null == name)
			return Collections.emptySet();

		ensureConsistency();

		final Set<ATermAppl> knowns = new HashSet<>();
		final Set<ATermAppl> unknowns = new HashSet<>();

		final Individual ind = getABox().getIndividual(name);
		if (ind == null)
		{
			Base.handleUndefinedEntity(name + _isNotAnIndividual);
			return Collections.emptySet();
		}

		if (ind.isMerged() && !ind.getMergeDependency(true).isIndependent())
		{
			knowns.add(name);
			getABox().getSames(ind.getSame(), unknowns, unknowns);
			unknowns.remove(name);
		}
		else
			getABox().getSames(ind.getSame(), knowns, unknowns);

		for (final ATermAppl other : unknowns)
			if (getABox().isSameAs(name, other))
				knowns.add(other);

		return knowns;
	}
}
