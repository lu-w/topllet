// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.cq;

import java.util.*;
import java.util.Map.Entry;

import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.atom.OpenError;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.rbox.Role;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.DisjointSet;
import openllet.query.sparqldl.model.AbstractQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.ResultBinding;

import static openllet.core.utils.TermFactory.term;
import static openllet.query.sparqldl.model.cq.QueryPredicate.*;

/**
 * <p>
 * Title: Default implementation of the {@link ConjunctiveQuery}
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
public class ConjunctiveQueryImpl extends AbstractQuery<ConjunctiveQuery> implements ConjunctiveQuery
{
	private final Set<QueryPredicate> ternaryQueryPredicates = Set.of(PropertyValue, NegativePropertyValue);

	private final Set<QueryPredicate> binaryQueryPredicates = Set.of(SameAs, DifferentFrom, SubClassOf, EquivalentClass,
			DisjointWith, ComplementOf, SubPropertyOf, EquivalentProperty, Domain, Range, InverseOf,
			propertyDisjointWith, Annotation, StrictSubClassOf, DirectSubClassOf, DirectSubPropertyOf,
			StrictSubPropertyOf, DirectType);

	private final List<QueryAtom> _allAtoms;

	public ConjunctiveQueryImpl(final KnowledgeBase kb, final boolean distinct)
	{
		super(kb, distinct);
		_allAtoms = new ArrayList<>();
	}

	public ConjunctiveQueryImpl(final Query<?> query)
	{
		this(query.getKB(), query.isDistinct());
		_name = query.getName();
		_parameters = query.getQueryParameters();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(final QueryAtom atom)
	{
		if (_allAtoms.contains(atom))
			return;
		_allAtoms.add(atom);
		for (final ATermAppl a : atom.getArguments())
			if (ATermUtils.isVar(a))
				_allVars.add(a);
			else if (ATermUtils.isLiteral(a) || _kb.isIndividual(a))
				_individualsAndLiterals.add(a);
		_ground = _ground && atom.isGround();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<QueryAtom> getAtoms()
	{
		return Collections.unmodifiableList(_allAtoms);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConjunctiveQuery apply(final ResultBinding binding)
	{
		final List<QueryAtom> atoms = new ArrayList<>();

		for (final QueryAtom atom : getAtoms())
			atoms.add(atom.apply(binding));

		final ConjunctiveQueryImpl query = new ConjunctiveQueryImpl(this);

		query._resultVars.addAll(_resultVars);
		query._resultVars.removeAll(binding.getAllVariables());

		for (final VarType type : VarType.values())
			for (final ATermAppl atom : getDistVarsForType(type))
				if (!binding.isBound(atom))
					query.addDistVar(atom, type);

		for (final QueryAtom atom : atoms)
			query.add(atom);

		return query;
	}

	/**
	 * {@inheritDoc} TODO
	 */
	@Override
	public ATermAppl rollUpTo(final ATermAppl var, final Collection<ATermAppl> stopList, final boolean stopOnConstants)
	{
		if (getDistVarsForType(VarType.LITERAL).contains(var) && !getDistVarsForType(VarType.INDIVIDUAL).contains(var) && !_individualsAndLiterals.contains(var))
			throw new InternalReasonerException("Trying to roll up to the variable '" + var + "' which is not distinguished and _individual.");

		ATermList classParts = ATermUtils.EMPTY_LIST;

		final Set<ATermAppl> visited = new HashSet<>();

		if (stopOnConstants)
			visited.addAll(getConstants());

		final Collection<QueryAtom> inEdges = findAtoms(PropertyValue, null, null, var);
		for (final QueryAtom a : inEdges)
			classParts = classParts.append(rollEdgeIn(PropertyValue, a, visited, stopList));

		final Collection<QueryAtom> outEdges = findAtoms(PropertyValue, var, null, null);
		for (final QueryAtom a : outEdges)
			classParts = classParts.append(rollEdgeOut(PropertyValue, a, visited, stopList));

		classParts = classParts.concat(getClasses(var));

		return ATermUtils.makeAnd(classParts);
	}

	// TODO optimize - _cache
	private ATermList getClasses(final ATermAppl a)
	{
		final List<ATermAppl> aterms = new ArrayList<>();

		for (final QueryAtom atom : findAtoms(QueryPredicate.Type, a, null))
		{
			final ATermAppl arg = atom.getArguments().get(1);
			if (ATermUtils.isVar(arg))
				throw new InternalReasonerException("Variables as predicates are not supported yet");
			aterms.add(arg);
		}

		if (!ATermUtils.isVar(a))
			aterms.add(ATermUtils.makeValue(a));

		return ATermUtils.makeList(aterms);
	}

	/**
	 * TODO
	 */
	private ATermAppl rollEdgeOut(final QueryPredicate allowed, final QueryAtom atom, final Set<ATermAppl> visited, final Collection<ATermAppl> stopList)
	{
		switch (atom.getPredicate())
		{
			case PropertyValue:
				final ATermAppl subj = atom.getArguments().get(0);
				final ATermAppl pred = atom.getArguments().get(1);
				final ATermAppl obj = atom.getArguments().get(2);

				if (ATermUtils.isVar(pred))
					// variables as predicates are not supported yet.
					return ATermUtils.TOP;

				visited.add(subj);

				if (visited.contains(obj))
				{
					final ATermList temp = getClasses(obj);
					if (temp.getLength() == 0)
					{
						if (_kb.isDatatypeProperty(pred))
							// return ATermUtils.makeSoMin(pred, 1,
							// ATermUtils.TOP_LIT);
							return ATermUtils.makeSomeValues(pred, ATermUtils.TOP_LIT);
						else
							return ATermUtils.makeSomeValues(pred, ATermUtils.TOP);
					}
					else
						return ATermUtils.makeSomeValues(pred, ATermUtils.makeAnd(temp));

				}

				if (ATermUtils.isLiteral(obj))
				{
					final ATermAppl type = ATermUtils.makeValue(obj);
					return ATermUtils.makeSomeValues(pred, type);
				}

				// TODO
				// else if (litVars.contains(obj))
				// {
				//    Datatype dtype = getDatatype(obj);
				//    return ATermUtils.makeSomeValues(pred, dtype.getName());
				// }

				ATermList targetClasses = getClasses(obj);

				for (final QueryAtom in : _findAtoms(stopList, allowed, null, null, obj))
					if (!in.equals(atom))
						targetClasses = targetClasses.append(rollEdgeIn(allowed, in, visited, stopList));

				final List<QueryAtom> targetOuts = _findAtoms(stopList, allowed, obj, null, null);

				if (targetClasses.isEmpty())
				{
					if (targetOuts.size() == 0)
					{
						// this is a simple leaf _node
						if (_kb.isDatatypeProperty(pred))
							return ATermUtils.makeSomeValues(pred, ATermUtils.TOP_LIT);
						else
							return ATermUtils.makeSomeValues(pred, ATermUtils.TOP);
					}
					else
					{
						// not a leaf _node, recurse over all outgoing edges
						ATermList outs = ATermUtils.EMPTY_LIST;

						for (final QueryAtom currEdge : targetOuts)
							outs = outs.append(rollEdgeOut(allowed, currEdge, visited, stopList));

						return ATermUtils.makeSomeValues(pred, ATermUtils.makeAnd(outs));
					}
				}
				else
					if (targetOuts.size() == 0)
						// this is a simple leaf _node, but with classes specified
						return ATermUtils.makeSomeValues(pred, ATermUtils.makeAnd(targetClasses));
					else
					{
						// not a leaf _node, recurse over all outgoing edges
						ATermList outs = ATermUtils.EMPTY_LIST;

						for (final QueryAtom currEdge : targetOuts)
							outs = outs.append(rollEdgeOut(allowed, currEdge, visited, stopList));

						for (int i = 0; i < targetClasses.getLength(); i++)
							outs = outs.append(targetClasses.elementAt(i));

						return ATermUtils.makeSomeValues(pred, ATermUtils.makeAnd(outs));

					}
			default:
				throw new OpenError("This atom cannot be included to rolling-up : " + atom);
		}
	}

	private ATermAppl rollEdgeIn(final QueryPredicate allowed, final QueryAtom atom, final Set<ATermAppl> visited, final Collection<ATermAppl> stopList)
	{
		switch (atom.getPredicate())
		{
			case PropertyValue:
				final ATermAppl subj = atom.getArguments().get(0);
				final ATermAppl pred = atom.getArguments().get(1);
				final ATermAppl obj = atom.getArguments().get(2);
				if (ATermUtils.isLiteral(obj))
					throw new UnsupportedOperationException("Can not roll edge " + subj + " -" + pred + "-> " + obj +
							" in because it ends on a literal.");
				// We may not assume the existence of an inverse role. If this is not present, we'll create it.
				Role inv = _kb.getRBox().getRole(pred).getInverse();
				ATermAppl invPred;
				if (inv == null)
				{
					invPred = term(pred + "__INV__");
					_kb.getRBox().addRole(invPred);
					_kb.getRBox().addInverseRole(pred, invPred, DependencySet.INDEPENDENT);
				}
				else
					invPred = inv.getName();

				if (ATermUtils.isVar(pred))
					throw new InternalReasonerException("Variables as predicates are not supported yet");
				// TODO variables as predicates are not supported yet.
				// return ATermUtils.TOP;

				visited.add(obj);

				if (visited.contains(subj))
				{
					final ATermList temp = getClasses(subj);
					if (temp.getLength() == 0)
					{
						if (_kb.isDatatypeProperty(invPred))
							return ATermUtils.makeSomeValues(invPred, ATermUtils.TOP_LIT);
						else
							return ATermUtils.makeSomeValues(invPred, ATermUtils.TOP);
					}
					else
						return ATermUtils.makeSomeValues(invPred, ATermUtils.makeAnd(temp));
				}

				ATermList targetClasses = getClasses(subj);

				final List<QueryAtom> targetIns = _findAtoms(stopList, allowed, null, null, subj);

				for (final QueryAtom o : _findAtoms(stopList, allowed, subj, null, null))
					if (!o.equals(atom))
						targetClasses = targetClasses.append(rollEdgeOut(allowed, o, visited, stopList));

				if (targetClasses.isEmpty())
				{
					if (targetIns.isEmpty())
					{
						// this is a simple leaf _node
						if (_kb.isDatatypeProperty(pred))
							return ATermUtils.makeSomeValues(invPred, ATermUtils.TOP_LIT);
						else
							return ATermUtils.makeSomeValues(invPred, ATermUtils.TOP);
					}
					else
					{
						// not a leaf _node, recurse over all incoming edges
						ATermList ins = ATermUtils.EMPTY_LIST;

						for (final QueryAtom currEdge : targetIns)
							ins = ins.append(rollEdgeIn(allowed, currEdge, visited, stopList));

						return ATermUtils.makeSomeValues(invPred, ATermUtils.makeAnd(ins));
					}
				}
				else
					if (targetIns.isEmpty())
						return ATermUtils.makeSomeValues(invPred, ATermUtils.makeAnd(targetClasses));
					else
					{
						// not a leaf _node, recurse over all outgoing edges
						ATermList ins = ATermUtils.EMPTY_LIST;

						for (final QueryAtom currEdge : targetIns)
							ins = ins.append(rollEdgeIn(allowed, currEdge, visited, stopList));

						for (int i = 0; i < targetClasses.getLength(); i++)
							ins = ins.append(targetClasses.elementAt(i));

						return ATermUtils.makeSomeValues(invPred, ATermUtils.makeAnd(ins));

					}
			default:
				throw new OpenError("This atom cannot be included to rolling-up : " + atom);

		}
	}

	private List<QueryAtom> _findAtoms(final Collection<ATermAppl> stopList, final QueryPredicate predicate, final ATermAppl... args)
	{
		final List<QueryAtom> list = new ArrayList<>();
		for (final QueryAtom atom : _allAtoms)
			if (predicate.equals(atom.getPredicate()))
			{
				int i = 0;
				boolean add = true;
				for (final ATermAppl arg : atom.getArguments())
				{
					final ATermAppl argValue = args[i++];
					if (argValue != null && argValue != arg || stopList.contains(arg))
					{
						add = false;
						break;
					}
				}

				if (add)
					list.add(atom);
			}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<QueryAtom> findAtoms(final QueryPredicate predicate, final ATermAppl... args)
	{
		return _findAtoms(Collections.<ATermAppl> emptySet(), predicate, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConjunctiveQuery reorder(final int[] ordering)
	{
		if (ordering.length != _allAtoms.size())
			throw new InternalReasonerException("Ordering permutation must be of the same size as the query : " + ordering.length);
		final ConjunctiveQueryImpl newQuery = new ConjunctiveQueryImpl(this);

		// shallow copies for faster processing
		for (final int element : ordering)
			newQuery._allAtoms.add(_allAtoms.get(element));

		newQuery._allVars = _allVars;
		newQuery._distVars = _distVars;
		newQuery._individualsAndLiterals = _individualsAndLiterals;
		newQuery._resultVars = _resultVars;
		newQuery._ground = _ground;

		return newQuery;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final QueryAtom atom)
	{
		if (!_allAtoms.contains(atom))
			return;

		_allAtoms.remove(atom);

		final Set<ATermAppl> rest = new HashSet<>();

		boolean ground = true;

		for (final QueryAtom atom2 : _allAtoms)
		{
			ground &= atom2.isGround();
			rest.addAll(atom2.getArguments());
		}

		_ground = ground;

		final Set<ATermAppl> toRemove = new HashSet<>(atom.getArguments());
		toRemove.removeAll(rest);

		for (final ATermAppl a : toRemove)
		{
			_allVars.remove(a);
			for (final Entry<VarType, Set<ATermAppl>> entry : _distVars.entrySet())
				entry.getValue().remove(a);
			_resultVars.remove(a);
			_individualsAndLiterals.remove(a);
		}
	}

	/**
	 * Recursive implementation of DFS cycle detection.
	 * @param curNode The node to start the DFS from
	 * @param visitedNodes The node that have been or are currently visited (empty set at the beginning)
	 * @param finishedNodes The nodes that the DFS was already finished on (empty set at the beginning)
	 * @param edges The set of edges
	 * @param prevNode The node that DFS was called from (null at the beginning)
	 * @return True iff. a cycle is reachable from curNode in the given edges
	 */
	private boolean cycle(ATermAppl curNode, Collection<ATermAppl> visitedNodes, Collection<ATermAppl> finishedNodes,
						  Collection<QueryAtom> edges, ATermAppl prevNode)
	{
		if (finishedNodes.contains(curNode))
			return false;
		if (visitedNodes.contains(curNode))
			return true;
		visitedNodes.add(curNode);
		Set<ATermAppl> neighbors = new HashSet<>();
		for (QueryAtom edge : edges)
		{
			ATermAppl n1 = edge.getArguments().get(0);
			ATermAppl n2;
			if (ternaryQueryPredicates.contains(edge.getPredicate()))
				n2 = edge.getArguments().get(2);
			else
				n2 = edge.getArguments().get(1);
			if (n1 == curNode && n2 != prevNode)
				neighbors.add(n2);
			if (n1 != prevNode && n2 == curNode)
				neighbors.add(n1);
		}
		boolean hasCycle = false;
		for (ATermAppl neighbor : neighbors)
			hasCycle |= cycle(neighbor, visitedNodes, finishedNodes, edges, curNode);
		finishedNodes.add(curNode);
		return hasCycle;
	}

	/**
	 * Entry point for recursive DFS to search for cycles in the (undirected) query graph. Excludes trivial cycles
	 * (i.e. a single undirected edge) but considers self-loops.
	 * @param nodes The nodes of the query graph
	 * @param edges The edges of the query graph
	 * @return True iff the query contains a cycle
	 */
	private boolean cycle(Collection<ATermAppl> nodes, Collection<QueryAtom> edges)
	{
		boolean hasCycle = false;
		if (nodes.size() > 1)
		{
			Set<ATermAppl> visitedNodes = new HashSet<>();
			Set<ATermAppl> finishedNodes = new HashSet<>();
			for (ATermAppl node : nodes)
				hasCycle |= cycle(node, visitedNodes, finishedNodes, edges, null);
		}
		return hasCycle;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasCycle()
	{
		// Find all edges: We shall only consider binary elements of the query as edges.
		List<QueryAtom> binaryAtomsWithOnlyUndistVars =
				_allAtoms.stream().filter(
						(a) -> (ternaryQueryPredicates.contains(a.getPredicate()) &&
									getUndistVars().contains(a.getArguments().get(0))&&
									getUndistVars().contains(a.getArguments().get(2))
								||
								(binaryQueryPredicates.contains(a.getPredicate()) &&
										getUndistVars().contains(a.getArguments().get(0))&&
										getUndistVars().contains(a.getArguments().get(1))))
				).toList();
		// Find all nodes of these edges
		Set<ATermAppl> nodes = new HashSet<>();
		for (QueryAtom edge : binaryAtomsWithOnlyUndistVars)
		{
			nodes.add(edge.getArguments().get(0));
			if (ternaryQueryPredicates.contains(edge.getPredicate()))
				nodes.add(edge.getArguments().get(2));
			else
				nodes.add(edge.getArguments().get(1));
		}
		// Recursive DFS for cycle identification
		return cycle(nodes, binaryAtomsWithOnlyUndistVars);
	}

	@Override
	public List<ConjunctiveQuery> split()
	{
		return split(false, false);
	}

	public List<ConjunctiveQuery> split(boolean splitOnIndividuals, boolean splitOnDistVars)
	{
		final Set<ATermAppl> resultVars = new HashSet<>(getResultVars());

		final DisjointSet<ATermAppl> disjointSet = new DisjointSet<>();

		for (final QueryAtom atom : getAtoms())
		{
			ATermAppl toMerge = null;

			for (final ATermAppl arg : atom.getArguments())
			{
				if (!(ATermUtils.isVar(arg) || ((splitOnIndividuals && getKB().isIndividual(arg))) ||
						(splitOnDistVars && getDistVars().contains(arg))))
					continue;

				disjointSet.add(arg);
				if (toMerge != null)
					disjointSet.union(toMerge, arg);
				toMerge = arg;
			}
		}

		final Collection<Set<ATermAppl>> equivalenceSets = disjointSet.getEquivalanceSets();
		if (equivalenceSets.size() == 1)
			return Collections.singletonList(this);

		final Map<ATermAppl, ConjunctiveQuery> queries = new HashMap<>();
		ConjunctiveQuery groundQuery = null;
		for (final QueryAtom atom : getAtoms())
		{
			ATermAppl representative = null;
			for (final ATermAppl arg : atom.getArguments())
				if (ATermUtils.isVar(arg) || (splitOnIndividuals && getKB().isIndividual(arg)) ||
						(splitOnDistVars && getDistVars().contains(arg)))
				{
					representative = disjointSet.find(arg);
					break;
				}

			ConjunctiveQuery newQuery;
			if (representative == null)
			{
				if (groundQuery == null)
					groundQuery = new ConjunctiveQueryImpl(this);
				newQuery = groundQuery;
			}
			else
			{
				newQuery = queries.get(representative);
				if (newQuery == null)
				{
					newQuery = new ConjunctiveQueryImpl(this);
					queries.put(representative, newQuery);
				}
				for (final ATermAppl arg : atom.getArguments())
				{
					if (resultVars.contains(arg) && !newQuery.getResultVars().contains(arg))
						newQuery.addResultVar(arg);

					for (final VarType v : VarType.values())
						if (getDistVarsForType(v).contains(arg) && !newQuery.getDistVarsForType(v).contains(arg))
							newQuery.addDistVar(arg, v);
				}
			}

			newQuery.add(atom);
		}

		final List<ConjunctiveQuery> list = new ArrayList<>(queries.values());

		if (groundQuery != null)
			list.add(0, groundQuery);

		return list;
	}

	@Override
	public ConjunctiveQuery copy()
	{
		ConjunctiveQuery copy = super.copy();
		for (QueryAtom atom : _allAtoms)
			copy.add(atom.copy());
		return copy;
	}

	@Override
	public String toString()
	{
		return toString(false, false);
	}

	@Override
	public String toString(boolean multiLine, boolean onlyQueryBody)
	{
		final StringBuilder sb = new StringBuilder();
		final String indent = multiLine ? "    " : "";

		if (!onlyQueryBody)
		{
			sb.append(ATermUtils.toString(_name)).append("(");
			for (int i = 0; i < _resultVars.size(); i++)
			{
				final ATermAppl var = _resultVars.get(i);
				if (i > 0)
					sb.append(", ");
				sb.append(ATermUtils.toString(var));
			}
			sb.append(")").append(" :-");
		}

		if (getAtoms().size() > 0)
		{
			if (multiLine)
				sb.append("\n");
			if (isNegated())
				sb.append("!").append("(");
			for (int j = 0; j < getAtoms().size(); j++) {
				final QueryAtom a = getAtoms().get(j);
				if (j > 0) {
					sb.append(",");
					if (multiLine)
						sb.append("\n");
				}

				sb.append(indent);
				sb.append(a.toString()); // TODO qNameProvider
			}
			if (isNegated())
				sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public boolean isNegated()
	{
		return false;
	}

	public ConjunctiveQuery createQuery(KnowledgeBase kb, boolean isDistinct)
	{
		return new ConjunctiveQueryImpl(kb, isDistinct);
	}
}
