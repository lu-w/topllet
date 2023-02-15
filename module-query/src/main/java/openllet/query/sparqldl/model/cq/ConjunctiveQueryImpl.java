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
import java.util.logging.Level;

import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.atom.OpenError;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.rbox.Role;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.DisjointSet;
import openllet.query.sparqldl.model.AbstractAtomQuery;
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
public class ConjunctiveQueryImpl extends AbstractAtomQuery<ConjunctiveQuery> implements ConjunctiveQuery
{
	private boolean _isNegated = false;

	public ConjunctiveQueryImpl(final KnowledgeBase kb, final boolean distinct)
	{
		super(kb, distinct);
	}

	public ConjunctiveQueryImpl(final Query<?> query)
	{
		this(query.getKB(), query.isDistinct());
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

	@Override
	public ConjunctiveQuery splitAndRollUp(boolean splitOnDistVars)
	{
		if (_logger.isLoggable(Level.FINER))
			_logger.finer("Rolling up for conjunctive query: " + this);
		// 1. step: Find disjoint parts of the query
		List<ConjunctiveQuery> splitQueries = split(true, splitOnDistVars);
		if (_logger.isLoggable(Level.FINER))
		{
			_logger.finer("Split query: " + splitQueries);
			_logger.finer("Now rolling up each separate element.");
		}

		// 2. step: Roll each part up
		ConjunctiveQuery rolledUpQuery = new ConjunctiveQueryImpl(this.getKB(), this.isDistinct());
		for (ConjunctiveQuery connectedQuery : splitQueries)
		{
			if (connectedQuery.getDistVars().size() <= 1)
			{
				final ATermAppl testIndOrVar;
				if (!connectedQuery.getDistVars().isEmpty())
					testIndOrVar = connectedQuery.getDistVars().iterator().next();
				else if (!connectedQuery.getConstants().isEmpty())
					testIndOrVar = connectedQuery.getConstants().iterator().next();
				else if (!connectedQuery.getUndistVars().isEmpty())
					testIndOrVar = connectedQuery.getUndistVars().iterator().next();
				else
					throw new RuntimeException("Rolling up procedure did not find any individual or variable to roll " +
							"up to.");
				final ATermAppl testClass = connectedQuery.rollUpTo(testIndOrVar,
						Collections.emptySet(), false);
				if (_logger.isLoggable(Level.FINER))
					_logger.finer("Rolled-up Boolean query: " + testIndOrVar + " -> " + testClass);
				QueryAtom rolledUpAtom = new QueryAtomImpl(QueryPredicate.Type, testIndOrVar, testClass);
				rolledUpQuery.add(rolledUpAtom);
			}
			else
			{
				// we can not roll-up queries that contain more than one distinguished variables -> just leave it
				for (QueryAtom atom : connectedQuery.getAtoms())
					rolledUpQuery.add(atom);
			}
		}
		return rolledUpQuery;
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
	public boolean isNegated()
	{
		return _isNegated;
	}

	@Override
	public void setNegation(boolean isNegated)
	{
		_isNegated = isNegated;
	}

	public ConjunctiveQuery createQuery(KnowledgeBase kb, boolean isDistinct)
	{
		return new ConjunctiveQueryImpl(kb, isDistinct);
	}

	@Override
	public String getAtomDelimiter()
	{
		return ",";
	}

	@Override
	public String getQueryPrefix()
	{
		return isNegated() ? "!" : "";
	}
}
