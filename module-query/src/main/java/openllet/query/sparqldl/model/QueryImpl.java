// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.atom.OpenError;
import openllet.core.KnowledgeBase;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.utils.ATermUtils;

/**
 * <p>
 * Title: Default implementation of the {@link Query}
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
public class QueryImpl extends UnionQueryImpl implements Query
{

	private final List<QueryAtom> _allAtoms;

	public QueryImpl(final KnowledgeBase kb, final boolean distinct)
	{
		super(kb, distinct);

		_queries = List.of(); // prevents adding disjunctions to a non-disjunctive query (internally)
		_allAtoms = new ArrayList<>();
	}

	public QueryImpl(final Query query)
	{
		this(query.getKB(), query.isDistinct());

		_name = query.getName();
		_parameters = query.getQueryParameters();
	}

	@Override
	public void setQueries(List<Query> queries)
	{
		throw new UnsupportedOperationException("Can not set disjunctions for a conjunctive query.");
	}

	@Override
	public void addQuery(Query query)
	{
		throw new UnsupportedOperationException("Can not add disjunctions to a conjunctive query.");
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
			{
				if (!_allVars.contains(a))
					_allVars.add(a);
			}
			else
				if (ATermUtils.isLiteral(a) || _kb.isIndividual(a))
					if (!_individualsAndLiterals.contains(a))
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
	public Query apply(final ResultBinding binding)
	{
		final List<QueryAtom> atoms = new ArrayList<>();

		for (final QueryAtom atom : getAtoms())
			atoms.add(atom.apply(binding));

		final QueryImpl query = new QueryImpl(this);

		query._resultVars.addAll(_resultVars);
		query._resultVars.removeAll(binding.getAllVariables());

		for (final UnionQuery.VarType type : UnionQuery.VarType.values())
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
		if (getDistVarsForType(UnionQuery.VarType.LITERAL).contains(var) && !getDistVarsForType(UnionQuery.VarType.INDIVIDUAL).contains(var) && !_individualsAndLiterals.contains(var))
			throw new InternalReasonerException("Trying to roll up to the variable '" + var + "' which is not distinguished and _individual.");

		ATermList classParts = ATermUtils.EMPTY_LIST;

		final Set<ATermAppl> visited = new HashSet<>();

		if (stopOnConstants)
			visited.addAll(getConstants());

		final Collection<QueryAtom> inEdges = findAtoms(QueryPredicate.PropertyValue, null, null, var);
		for (final QueryAtom a : inEdges)
			classParts = classParts.append(rollEdgeIn(QueryPredicate.PropertyValue, a, visited, stopList));

		final Collection<QueryAtom> outEdges = findAtoms(QueryPredicate.PropertyValue, var, null, null);
		for (final QueryAtom a : outEdges)
			classParts = classParts.append(rollEdgeOut(QueryPredicate.PropertyValue, a, visited, stopList));

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

	// TODO this should die if called on a literal _node
	private ATermAppl rollEdgeIn(final QueryPredicate allowed, final QueryAtom atom, final Set<ATermAppl> visited, final Collection<ATermAppl> stopList)
	{
		switch (atom.getPredicate())
		{
			case PropertyValue:
				final ATermAppl subj = atom.getArguments().get(0);
				final ATermAppl pred = atom.getArguments().get(1);
				final ATermAppl obj = atom.getArguments().get(2);
				final ATermAppl invPred = _kb.getRBox().getRole(pred).getInverse().getName();

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
	public Query reorder(final int[] ordering)
	{
		if (ordering.length != _allAtoms.size())
			throw new InternalReasonerException("Ordering permutation must be of the same size as the query : " + ordering.length);
		final QueryImpl newQuery = new QueryImpl(this);

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
			for (final Entry<UnionQuery.VarType, Set<ATermAppl>> entry : _distVars.entrySet())
				entry.getValue().remove(a);
			_resultVars.remove(a);
			_individualsAndLiterals.remove(a);
		}
	}
}
