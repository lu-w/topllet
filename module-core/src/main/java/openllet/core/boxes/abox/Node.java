// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under the terms of the MIT License.
//
// The MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package openllet.core.boxes.abox;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.DependencySet;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.rbox.Role;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.tableau.completion.queue.NodeSelector;
import openllet.core.tableau.completion.queue.QueueElement;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Bool;
import openllet.core.utils.CollectionUtils;
import openllet.shared.tools.Log;

/**
 * FIXME : many many data-structures that doesn't support concurrency are use in concurrent context here.
 *
 * @author Evren Sirin
 */
public abstract class Node
{
	public final static Logger _logger = Log.getLogger(Node.class);

	public final static int BLOCKABLE = Integer.MAX_VALUE;
	public final static int NOMINAL = 0;

	public final static int ATOM = 0;
	public final static int OR = 1;
	public final static int SOME = 2;
	public final static int ALL = 3;
	public final static int MIN = 4;
	public final static int MAX = 5;
	public final static int NOM = 6;
	public final static int TYPES = 7;

	protected final ABox _abox;
	protected final ATermAppl _name;
	protected final Map<ATermAppl, DependencySet> _depends;
	private final boolean _isRoot;
	private volatile boolean _isConceptRoot;

	/**
	 * If this _node is merged to another one, points to that _node otherwise points to itself. This is a linked list implementation of disjoint-union _data
	 * structure.
	 */
	protected volatile Node _mergedTo = this;

	protected volatile EdgeList _inEdges;

	/**
	 * Dependency information about why merged happened (if at all)
	 */
	protected volatile DependencySet _mergeDepends = null;

	protected volatile DependencySet _pruned = null;

	/**
	 * Set of other _nodes that have been merged to this _node. Note that this is only the set of _nodes directly merged to this one. A recursive traversal is
	 * required to get all the merged _nodes.
	 */
	protected volatile Set<Node> _merged;

	protected volatile Map<Node, DependencySet> _differents;

	protected Node(final ATermAppl name, final ABox abox)
	{
		_name = name;
		_abox = abox;

		_isRoot = !ATermUtils.isAnon(name);
		_isConceptRoot = false;

		_mergeDepends = DependencySet.INDEPENDENT;
		_differents = CollectionUtils.makeMap();
		_depends = CollectionUtils.makeMap();

		_inEdges = new EdgeList();
	}

	protected Node(final Node node, final ABoxImpl abox)
	{
		_name = node.getName();
		_abox = abox;

		_isRoot = node._isRoot;
		_isConceptRoot = node._isConceptRoot;

		_mergeDepends = node._mergeDepends;
		_mergedTo = node._mergedTo;
		_merged = node._merged;
		_pruned = node._pruned;

		// do not copy _differents right now because we need to
		// update _node references later anyway
		_differents = node._differents;
		_depends = CollectionUtils.makeMap(node._depends);

		_inEdges = node._inEdges;
	}

	@Override
	public int hashCode()
	{
		return _name.hashCode();
	}

	@Override
	public boolean equals(final Object obj)
	{
		return obj == this || obj.getClass() == getClass() && ((Node) obj)._name.equals(_name);
	}

	protected void updateNodeReferences()
	{
		_mergedTo = _abox.getNode(_mergedTo.getName());

		final Map<Node, DependencySet> diffs = new HashMap<>(_differents.size());
		for (final Map.Entry<Node, DependencySet> entry : _differents.entrySet())
		{
			final Node node = entry.getKey();

			diffs.put(_abox.getNode(node.getName()), entry.getValue());
		}
		_differents = diffs;

		if (_merged != null)
		{
			final Set<Node> sames = new HashSet<>(_merged.size());
			for (final Node node : _merged)
				sames.add(_abox.getNode(node.getName()));
			_merged = sames;
		}

		final EdgeList oldEdges = _inEdges;
		_inEdges = new EdgeList(oldEdges.size());
		for (int i = 0; i < oldEdges.size(); i++)
		{
			final Edge edge = oldEdges.get(i);

			final Individual from = _abox.getIndividual(edge.getFrom().getName());

			if (null == from)
			{
				_logger.severe(() -> "The 'from' individual, " + edge.getFrom() + " is now null in the edge when " +
						"updating references. The edge is ignored.");
				continue;
			}

			final Edge newEdge = new DefaultEdge(edge.getRole(), from, this, edge.getDepends());

			_inEdges.add(newEdge);
			if (!isPruned())
				from.getOutEdges().add(newEdge);
		}
	}

	/**
	 * Indicates that _node has been changed in a way that requires us to recheck the concepts of given type.
	 *
	 * @param type type of concepts that need to be rechecked
	 */
	public void setChanged(final int type)
	{
		//Check if we need to updated the completion _queue
		//Currently we only updated the changed lists for checkDatatypeCount()
		final QueueElement newElement = new QueueElement(this);

		//update the datatype _queue
		if ((type == Node.ALL || type == Node.MIN) && OpenlletOptions.USE_COMPLETION_QUEUE)
			_abox.getCompletionQueue().add(newElement, NodeSelector.DATATYPE);

		// add _node to effected list
		if (_abox.getBranchIndex() >= 0 && OpenlletOptions.TRACK_BRANCH_EFFECTS)
			_abox.getBranchEffectTracker().add(_abox.getBranchIndex(), getName());
	}

	/**
	 * @return true if this is the _node created for the concept satisfiability check.
	 */
	public boolean isConceptRoot()
	{
		return _isConceptRoot;
	}

	public void setConceptRoot(final boolean isConceptRoot)
	{
		_isConceptRoot = isConceptRoot;
	}

	public boolean isBnode()
	{
		return ATermUtils.isBnode(_name);
	}

	public boolean isNamedIndividual()
	{
		return _isRoot && !_isConceptRoot && !isBnode();
	}

	public boolean isRoot()
	{
		return _isRoot || isNominal();
	}

	public abstract boolean isLeaf();

	public boolean isRootNominal()
	{
		return _isRoot && isNominal();
	}

	public abstract Node copyTo(ABoxImpl abox);

	protected void addInEdge(final Edge edge)
	{
		_inEdges.add(edge);
	}

	public EdgeList getInEdges()
	{
		return _inEdges;
	}

	public boolean removeInEdge(final Edge edge)
	{
		final boolean removed = _inEdges.removeEdge(edge);

		if (!removed)
			throw new InternalReasonerException("Trying to remove a non-existing edge " + edge);

		return true;
	}

	//	public void removeInEdges()
	//	{
	//		_inEdges = new EdgeList();
	//	}

	public void reset(final boolean onlyApplyTypes)
	{
		assert onlyApplyTypes || isRootNominal() : "Only asserted individuals can be reset: " + this;

		if (OpenlletOptions.USE_COMPLETION_QUEUE)
			_abox.getCompletionQueue().add(new QueueElement(this));

		if (onlyApplyTypes)
			return;

		if (_pruned != null)
			unprune(DependencySet.NO_BRANCH);

		_mergedTo = this;
		_mergeDepends = DependencySet.INDEPENDENT;
		_merged = null;

		final Iterator<DependencySet> i = _differents.values().iterator();
		while (i.hasNext())
		{
			final DependencySet d = i.next();
			if (d.getBranch() != DependencySet.NO_BRANCH)
				i.remove();
		}

		resetTypes();

		_inEdges.reset();
	}

	protected void resetTypes()
	{
		final Iterator<DependencySet> i = _depends.values().iterator();
		while (i.hasNext())
		{
			final DependencySet d = i.next();
			if (d.getBranch() != DependencySet.NO_BRANCH)
				i.remove();
		}
	}

	public Boolean restorePruned(final int branch)
	{

		if (OpenlletOptions.TRACK_BRANCH_EFFECTS)
			_abox.getBranchEffectTracker().add(_abox.getBranchIndex(), _name);

		if (_pruned != null)
			if (_pruned.getBranch() > branch)
			{
				if (_logger.isLoggable(Level.FINE))
					_logger.fine("RESTORE: " + this + " merged _node " + _mergedTo + " " + _mergeDepends);

				if (_mergeDepends.getBranch() > branch)
					undoSetSame();

				unprune(branch);

				if (OpenlletOptions.USE_INCREMENTAL_CONSISTENCY)
					_abox.getIncrementalChangeTracker().addUnprunedNode(this);

				// we may need to remerge this _node
				if (this instanceof Individual)
				{
					final Individual ind = (Individual) this;

					if (OpenlletOptions.USE_COMPLETION_QUEUE)
					{
						ind._applyNext[Node.NOM] = 0;
						_abox.getCompletionQueue().add(new QueueElement(this), NodeSelector.NOMINAL);
					}

				}

				return Boolean.TRUE;
			}
			else
			{
				if (_logger.isLoggable(Level.FINE))
					_logger.fine("DO NOT RESTORE: pruned _node " + this + " = " + _mergedTo + " " + _mergeDepends);

				return Boolean.FALSE;
			}

		return null;
	}

	public boolean restore(final int branch)
	{

		if (OpenlletOptions.TRACK_BRANCH_EFFECTS)
			_abox.getBranchEffectTracker().add(_abox.getBranchIndex(), _name);

		boolean restored = false;

		final List<ATermAppl> conjunctions = new ArrayList<>();

		boolean removed = false;

		for (final Iterator<ATermAppl> i = getTypes().iterator(); i.hasNext();)
		{
			final ATermAppl c = i.next();
			final DependencySet d = getDepends(c);

			final boolean removeType = OpenlletOptions.USE_SMART_RESTORE
					//                ? ( !d.contains( _branch ) )
					? d.max() >= branch
					: d.getBranch() > branch;

			if (removeType)
			{
				removed = true;

				_logger.fine(() -> "RESTORE: " + this + " remove type " + c + " " + d + " " + branch);

				//track that this _node is affected
				if (OpenlletOptions.USE_INCREMENTAL_CONSISTENCY && this instanceof Individual)
					_abox.getIncrementalChangeTracker().addDeletedType(this, c);

				i.remove();
				removeType(c);
				restored = true;
			}
			else
				if (OpenlletOptions.USE_SMART_RESTORE && ATermUtils.isAnd(c))
					conjunctions.add(c);
		}

		//update the _queue with things that could readd this type
		if (removed && OpenlletOptions.USE_COMPLETION_QUEUE && this instanceof Individual)
		{
			final Individual ind = (Individual) this;
			ind._applyNext[Node.ATOM] = 0;
			ind._applyNext[Node.OR] = 0;

			final QueueElement qe = new QueueElement(this);
			_abox.getCompletionQueue().add(qe, NodeSelector.DISJUNCTION);
			_abox.getCompletionQueue().add(qe, NodeSelector.ATOM);
		}

		// with smart restore there is a possibility that we remove a conjunct
		// but not the conjunction. this is the case if conjunct was added before
		// the conjunction but depended on an earlier _branch. so we need to make
		// sure all conjunctions are actually applied
		if (OpenlletOptions.USE_SMART_RESTORE)
			for (final ATermAppl c : conjunctions)
			{
				final DependencySet d = getDepends(c);
				for (ATermList cs = (ATermList) c.getArgument(0); !cs.isEmpty(); cs = cs.getNext())
				{
					final ATermAppl conj = (ATermAppl) cs.getFirst();

					addType(conj, d);
				}
			}

		for (final Iterator<Entry<Node, DependencySet>> i = _differents.entrySet().iterator(); i.hasNext();)
		{
			final Entry<Node, DependencySet> entry = i.next();
			final Node node = entry.getKey();
			final DependencySet d = entry.getValue();

			if (d.getBranch() > branch)
			{
				_logger.fine(() -> "RESTORE: " + _name + " delete difference " + node);
				i.remove();
				restored = true;
			}
		}

		removed = false;
		for (final Iterator<Edge> i = _inEdges.iterator(); i.hasNext();)
		{
			final Edge e = i.next();
			final DependencySet d = e.getDepends();

			if (d.getBranch() > branch)
			{
				_logger.fine(() -> "RESTORE: " + _name + " delete reverse edge " + e);

				if (OpenlletOptions.USE_INCREMENTAL_CONSISTENCY)
					_abox.getIncrementalChangeTracker().addDeletedEdge(e);

				i.remove();
				restored = true;
				removed = true;
			}
		}

		if (removed && OpenlletOptions.USE_COMPLETION_QUEUE)
		{
			final QueueElement qe = new QueueElement(this);
			_abox.getCompletionQueue().add(qe, NodeSelector.EXISTENTIAL);
			_abox.getCompletionQueue().add(qe, NodeSelector.MIN_NUMBER);
		}

		return restored;
	}

	protected DependencySet forceAddType(final ATermAppl c, final DependencySet ds)
	{
		// add to effected list
		if (OpenlletOptions.TRACK_BRANCH_EFFECTS && _abox.getBranchIndex() >= 0)
			_abox.getBranchEffectTracker().add(_abox.getBranchIndex(), getName());

		// if we are checking entailment using a precompleted ABox, _abox.branch
		// is set to -1. however, since applyAllValues is done automatically
		// and the edge used in applyAllValues may depend on a _branch we want
		// this type to be deleted when that edge goes away, i.e. we backtrack
		// to a position before the max dependency of this type
		int b = _abox.getBranchIndex();
		final int max = ds.max();
		if (b == -1 && max != 0)
			b = max + 1;

		final DependencySet out = ds.copy(b);
		_depends.put(c, out);

		_abox.setChanged(true);

		return out;
	}

	public void addType(final ATermAppl c, final DependencySet ds)
	{
		if (isPruned())
			throw new InternalReasonerException("Adding type to a pruned node " + this + " " + c + "\t" + getPruned());
		else
			if (isMerged())
				return;

		forceAddType(c, ds);
	}

	public boolean removeType(final ATermAppl c)
	{
		return _depends.remove(c) != null;
	}

	public boolean hasType(final ATerm c)
	{
		return _depends.containsKey(c);
	}

	public Bool hasObviousType(final ATermAppl c)
	{
		DependencySet ds = getDepends(c);

		if (ds != null)
		{
			if (ds.isIndependent())
				return Bool.TRUE;
		}
		else
			if ((ds = getDepends(ATermUtils.negate(c))) != null)
			{
				if (ds.isIndependent())
					return Bool.FALSE;
			}
			else
				if (isIndividual() && ATermUtils.isNominal(c))
					// TODO probably redundant if : Bool.FALSE
					if (!c.getArgument(0).equals(getName()))
						return Bool.FALSE;
					else
						return Bool.TRUE;

		if (isIndividual())
		{
			ATermAppl r = null;
			ATermAppl d = null;

			if (ATermUtils.isNot(c))
			{
				final ATermAppl notC = (ATermAppl) c.getArgument(0);
				if (ATermUtils.isAllValues(notC))
				{
					r = (ATermAppl) notC.getArgument(0);
					d = ATermUtils.negate((ATermAppl) notC.getArgument(1));
				}
			}
			else
				if (ATermUtils.isSomeValues(c))
				{
					r = (ATermAppl) c.getArgument(0);
					d = (ATermAppl) c.getArgument(1);
				}

			if (r != null)
			{
				final Individual ind = (Individual) this;

				final Role role = _abox.getRole(r);

				if (!role.isObjectRole() || !role.isSimple())
					return Bool.UNKNOWN;

				final EdgeList edges = ind.getRNeighborEdges(role);

				Bool ot = Bool.FALSE;

				for (final Edge edge : edges)
				{
					if (!edge.getDepends().isIndependent())
					{
						ot = Bool.UNKNOWN;
						continue;
					}

					final Individual y = (Individual) edge.getNeighbor(ind);

					// TODO all this stuff in one method - this is only for
					// handling AND
					// clauses - they are implemented in _abox.isKnownType
					ot = ot.or(_abox.isKnownType(y, d, Collections.emptySet()));// y.hasObviousType(d));

					if (ot.isTrue())
						return ot;
				}
				return ot;
			}
		}

		return Bool.UNKNOWN;
	}

	public boolean hasObviousType(final Collection<ATermAppl> coll)
	{
		for (final ATermAppl c : coll)
		{
			final DependencySet ds = getDepends(c);

			if (ds != null && ds.isIndependent())
				return true;
		}

		return false;
	}

	//	protected boolean hasPredecessor(final Individual x)
	//	{
	//		return x.hasSuccessor(this);
	//	}

	public abstract boolean hasSuccessor(Node x);

	public abstract DependencySet getNodeDepends();

	public DependencySet getDepends(final ATerm c)
	{
		return _depends.get(c);
	}

	public Map<ATermAppl, DependencySet> getDepends()
	{
		return _depends;
	}

	public Set<ATermAppl> getTypes()
	{
		return _depends.keySet();
	}

	public Stream<ATermAppl> types()
	{
		return _depends.keySet().stream();
	}

	//	public void removeTypes()
	//	{
	//		_depends.clear();
	//	}

	//	public int prunedAt()
	//	{
	//		return _pruned.getBranch();
	//	}

	public boolean isPruned()
	{
		return _pruned != null;
	}

	public DependencySet getPruned()
	{
		return _pruned;
	}

	public abstract void prune(DependencySet ds);

	public void unprune(final int branch)
	{
		_pruned = null;

		boolean added = false;

		for (final Edge edge : _inEdges)
		{
			final DependencySet d = edge.getDepends();

			if (d.getBranch() <= branch)
			{
				final Individual pred = edge.getFrom();
				final Role role = edge.getRole();

				// if both pred and *this* were merged to other _nodes (in that _order)
				// there is a chance we might duplicate the edge so first check for
				// the existence of the edge
				if (!pred.getOutEdges().hasExactEdge(pred, role, this))
				{
					pred.addOutEdge(edge);

					// update affected
					if (OpenlletOptions.TRACK_BRANCH_EFFECTS)
					{
						_abox.getBranchEffectTracker().add(d.getBranch(), pred._name);
						_abox.getBranchEffectTracker().add(d.getBranch(), _name);
					}

					if (OpenlletOptions.USE_COMPLETION_QUEUE)
					{
						added = true;
						pred._applyNext[Node.MAX] = 0;

						final QueueElement qe = new QueueElement(pred);
						_abox.getCompletionQueue().add(qe, NodeSelector.MAX_NUMBER);
						_abox.getCompletionQueue().add(qe, NodeSelector.GUESS);
						_abox.getCompletionQueue().add(qe, NodeSelector.CHOOSE);
						_abox.getCompletionQueue().add(qe, NodeSelector.UNIVERSAL);
					}

					if (_logger.isLoggable(Level.FINE))
						_logger.fine("RESTORE: " + _name + " ADD reverse edge " + edge);
				}
			}
		}

		if (added)
			if (this instanceof Individual)
			{
				final Individual ind = (Individual) this;
				ind._applyNext[Node.MAX] = 0;
				final QueueElement qe = new QueueElement(ind);
				_abox.getCompletionQueue().add(qe, NodeSelector.MAX_NUMBER);
				_abox.getCompletionQueue().add(qe, NodeSelector.GUESS);
				_abox.getCompletionQueue().add(qe, NodeSelector.CHOOSE);
				_abox.getCompletionQueue().add(qe, NodeSelector.UNIVERSAL);
			}
	}

	public abstract int getNominalLevel();

	public abstract boolean isNominal();

	public abstract boolean isBlockable();

	public abstract boolean isLiteral();

	public abstract boolean isIndividual();

	//	public int mergedAt()
	//	{
	//		return _mergeDepends.getBranch();
	//	}

	public boolean isMerged()
	{
		return _mergedTo != this;
	}

	public Node getMergedTo()
	{
		return _mergedTo;
	}

	/**
	 * Get the dependency if this node is merged to another node. This node may be merged to another node which is later merged to another node and so on. This
	 * function may return the dependency for the first step or the union of all steps.
	 *
	 * @param all
	 * @return the dependency set resulting of the merge
	 */
	public DependencySet getMergeDependency(final boolean all)
	{
		if (!isMerged() || !all)
			return _mergeDepends;

		DependencySet ds = _mergeDepends;
		Node node = _mergedTo;
		while (node.isMerged())
		{
			ds = ds.union(node._mergeDepends, _abox.doExplanation());
			node = node._mergedTo;
		}

		return ds;
	}

	public Node getSame()
	{
		if (_mergedTo == this)
			return this;

		return _mergedTo.getSame();
	}

	public void undoSetSame()
	{
		_mergedTo.removeMerged(this);
		_mergeDepends = DependencySet.INDEPENDENT;
		_mergedTo = this;
	}

	private void addMerged(final Node node)
	{
		if (_merged == null)
			_merged = new HashSet<>(3);
		_merged.add(node);
	}

	public Set<Node> getMerged()
	{
		if (_merged == null)
			return Collections.emptySet();
		return _merged;
	}

	public Map<Node, DependencySet> getAllMerged()
	{
		final Map<Node, DependencySet> result = new HashMap<>();
		getAllMerged(DependencySet.INDEPENDENT, result);
		return result;
	}

	private void getAllMerged(final DependencySet ds, final Map<Node, DependencySet> result)
	{
		if (_merged == null)
			return;

		for (final Node mergedNode : _merged)
		{
			final DependencySet mergeDS = ds.union(mergedNode.getMergeDependency(false), false);
			result.put(mergedNode, mergeDS);
			mergedNode.getAllMerged(mergeDS, result);
		}
	}

	private void removeMerged(final Node node)
	{
		_merged.remove(node);
		if (_merged.isEmpty())
			_merged = null; // free space
	}

	public boolean setSame(final Node node, final DependencySet ds)
	{
		if (isSame(node))
			return false;
		if (isDifferent(node))
		{
			//CHW - added for incremental reasoning support - this is needed as we will need to backjump if possible
			if (OpenlletOptions.USE_INCREMENTAL_CONSISTENCY)
				_abox.setClash(Clash.nominal(this, ds.union(_mergeDepends, _abox.doExplanation()).union(node._mergeDepends, _abox.doExplanation()), node.getName()));
			else
				_abox.setClash(Clash.nominal(this, ds, node.getName()));

			return false;
		}

		_mergedTo = node;
		_mergeDepends = ds.copy(_abox.getBranchIndex());
		node.addMerged(this);
		return true;
	}

	public boolean isSame(final Node node)
	{
		return getSame().equals(node.getSame());
	}

	public boolean isDifferent(final Node node)
	{
		return _differents.containsKey(node);
	}

	public Set<Node> getDifferents()
	{
		return _differents.keySet();
	}

	public DependencySet getDifferenceDependency(final Node node)
	{
		return _differents.get(node);
	}

	public boolean setDifferent(final Node node, final DependencySet dsParam)
	{
		DependencySet ds = dsParam;

		// add to effected list
		if (_abox.getBranchIndex() >= 0 && OpenlletOptions.TRACK_BRANCH_EFFECTS)
			_abox.getBranchEffectTracker().add(_abox.getBranchIndex(), node.getName());

		if (isDifferent(node))
			return false;

		if (isSame(node))
		{
			ds = ds.union(getMergeDependency(true), _abox.doExplanation());
			ds = ds.union(node.getMergeDependency(true), _abox.doExplanation());
			_abox.setClash(Clash.nominal(this, ds, node.getName()));

			if (!ds.isIndependent())
				return false;
		}

		ds = ds.copy(_abox.getBranchIndex());
		_differents.put(node, ds);
		node.setDifferent(this, ds);
		_abox.setChanged(true);
		return true;
	}

	public void inheritDifferents(final Node y, final DependencySet ds)
	{
		for (final Map.Entry<Node, DependencySet> entry : y._differents.entrySet())
		{
			final Node yDiff = entry.getKey();
			final DependencySet finalDS = ds.union(entry.getValue(), _abox.doExplanation());

			setDifferent(yDiff, finalDS);
		}
	}

	public ATermAppl getName()
	{
		return _name;
	}

	public abstract ATermAppl getTerm();

	public String getNameStr()
	{
		return _name.getName();
	}

	@Override
	public String toString()
	{
		return ATermUtils.toString(_name);
	}

	/**
	 * A string that identifies this _node either using its _name or the path of individuals that comes to this _node. For example, a _node that has been
	 * generated by the completion rules needs to be identified with respect to a named _individual. Ultimately, we need the shortest path or something like
	 * that but right now we just use the first inEdge
	 *
	 * @return the path as terms
	 */
	public List<ATermAppl> getPath()
	{
		final LinkedList<ATermAppl> path = new LinkedList<>();

		if (isNamedIndividual())
			path.add(_name);
		else
		{
			final Set<Node> cycle = new HashSet<>();
			Node node = this;
			while (!node.getInEdges().isEmpty())
			{
				final Edge inEdge = node.getInEdges().get(0);
				node = inEdge.getFrom();
				if (cycle.contains(node))
					break;
				else
					cycle.add(node);
				path.addFirst(inEdge.getRole().getName());
				if (node.isNamedIndividual())
				{
					path.addFirst(node.getName());
					break;
				}
			}
		}

		return path;
	}

	public ABox getABox()
	{
		return _abox;
	}
}
