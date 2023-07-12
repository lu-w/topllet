package openllet.query.sparqldl.model;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.cq.QueryPredicate;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.util.*;

import static openllet.query.sparqldl.model.cq.QueryPredicate.*;
import static openllet.query.sparqldl.model.cq.QueryPredicate.DirectType;

public abstract class AbstractAtomQuery<QueryType extends AtomQuery<QueryType>> extends AbstractQuery<QueryType> implements AtomQuery<QueryType>
{
    private final Set<QueryPredicate> ternaryQueryPredicates = Set.of(PropertyValue, NegativePropertyValue);

    private final Set<QueryPredicate> binaryQueryPredicates = Set.of(SameAs, DifferentFrom, SubClassOf, EquivalentClass,
            DisjointWith, ComplementOf, SubPropertyOf, EquivalentProperty, Domain, Range, InverseOf,
            propertyDisjointWith, Annotation, StrictSubClassOf, DirectSubClassOf, DirectSubPropertyOf,
            StrictSubPropertyOf, DirectType);

    protected List<QueryAtom> _allAtoms = new ArrayList<>();

    public AbstractAtomQuery(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    public AbstractAtomQuery(QueryType query)
    {
        super(query);
    }

    @Override
    public void addAtoms(List<QueryAtom> atoms)
    {
        for (QueryAtom a : atoms)
            add(a);
    }

    @Override
    public void add(final QueryAtom atom)
    {
        if (_allAtoms.contains(atom))
            return;
        _allAtoms.add(atom);
        for (final ATermAppl a : atom.getArguments())
            if (ATermUtils.isVar(a) && !_allVars.contains(a))
                _allVars.add(a);
            else if (ATermUtils.isLiteral(a) || _kb.isIndividual(a))
                _individualsAndLiterals.add(a);
        _ground = _ground && atom.isGround();
    }

    @Override
    public List<QueryAtom> getAtoms()
    {
        return Collections.unmodifiableList(_allAtoms);
    }

    @Override
    public QueryType apply(final ResultBinding binding)
    {
        final List<QueryAtom> atoms = new ArrayList<>();

        for (final QueryAtom atom : getAtoms())
            atoms.add(atom.apply(binding));

        final QueryType query = copy();

        for (QueryAtom atom : _allAtoms)
            query.remove(atom);

        for (ATermAppl var : _resultVars)
            query.addResultVar(var);

        for (ATermAppl var : binding.getAllVariables())
            query.removeResultVar(var);

        for (final VarType type : VarType.values())
            for (final ATermAppl atom : getDistVarsForType(type))
                if (!binding.isBound(atom))
                    query.addDistVar(atom, type);

        for (final QueryAtom atom : atoms)
            query.add(atom);

        return query;
    }

    protected List<QueryAtom> _findAtoms(final Collection<ATermAppl> stopList, final QueryPredicate predicate,
                                 final ATermAppl... args)
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

    @Override
    public List<QueryAtom> findAtoms(final QueryPredicate predicate, final ATermAppl... args)
    {
        return _findAtoms(Collections.<ATermAppl> emptySet(), predicate, args);
    }

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
            for (final Map.Entry<VarType, Set<ATermAppl>> entry : _distVars.entrySet())
                entry.getValue().remove(a);
            _resultVars.remove(a);
            _individualsAndLiterals.remove(a);
        }
    }

    @Override
    public QueryType reorder(final int[] ordering)
    {
        System.out.println(this);
        if (ordering.length != _allAtoms.size())
            throw new InternalReasonerException("Ordering permutation must be of the same size as the query : " + ordering.length);
        final QueryType newQuery = copy();

        // shallow copies for faster processing
        for (final int element : ordering)
            newQuery.add(_allAtoms.get(element));

        newQuery.setDistVars(new EnumMap<>(_distVars));
        newQuery.setResultVars(new ArrayList<>(_resultVars));

        return newQuery;
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
            ATermAppl[] nodes = getNodes(edge);
            if (nodes[0] == curNode && nodes[1] != prevNode)
                neighbors.add(nodes[1]);
            if (nodes[0] != prevNode && nodes[1] == curNode)
                neighbors.add(nodes[0]);
        }
        boolean hasCycle = false;
        for (ATermAppl neighbor : neighbors)
            hasCycle |= cycle(neighbor, visitedNodes, finishedNodes, edges, curNode);
        finishedNodes.add(curNode);
        return hasCycle;
    }

    private ATermAppl[] getNodes(QueryAtom edge)
    {
        ATermAppl n1 = edge.getArguments().get(0);
        ATermAppl n2;
        if (ternaryQueryPredicates.contains(edge.getPredicate()))
            n2 = edge.getArguments().get(2);
        else
            n2 = edge.getArguments().get(1);
        return new ATermAppl[] {n1, n2};
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
        return hasCycle || hasTwoNodeCycle(nodes, edges);
    }

    private boolean hasTwoNodeCycle(Collection<ATermAppl> nodes, Collection<QueryAtom> edges)
    {
        for (QueryAtom edge1 : edges)
            for (QueryAtom edge2 : edges)
                if (!edge1.equals(edge2))
                {
                    ATermAppl[] nodes1 = getNodes(edge1);
                    ATermAppl[] nodes2 = getNodes(edge2);
                    if (nodes1[0].equals(nodes2[1]) && nodes1[1].equals(nodes2[0]))
                        return true;
                }
        return false;
    }

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
    public boolean isEmpty()
    {
        boolean isEmpty = true;
        for (QueryAtom a : _allAtoms)
            if (!(a.getArguments().isEmpty()))
            {
                isEmpty = false;
                break;
            }
        return isEmpty;
    }

    @Override
    public QueryType copy()
    {
        QueryType copy = super.copy();
        for (QueryAtom atom : _allAtoms)
            copy.add(atom.copy());
        return copy;
    }

    @Override
    public String getQueryPrefix()
    {
        return "";
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
            sb.append(")").append(" :- ");
        }

        if (getAtoms().size() > 0)
        {
            if (multiLine)
                sb.append("\n");
            if (!"".equals(getQueryPrefix()))
                sb.append(getQueryPrefix()).append("(");
            for (int j = 0; j < getAtoms().size(); j++) {
                final QueryAtom a = getAtoms().get(j);
                if (j > 0) {
                    sb.append(" ").append(getAtomDelimiter()).append(" ");
                    if (multiLine)
                        sb.append("\n");
                }

                sb.append(indent);
                sb.append(a.toString()); // TODO qNameProvider
            }
            if (!"".equals(getQueryPrefix()))
                sb.append(")");
        }
        return sb.toString();
    }

    public boolean hasOnlyClassesOrPropertiesInKB()
    {
        boolean hasOnlyClassesOrRolesInKB = true;
        for (QueryAtom atom : getAtoms())
        {
            hasOnlyClassesOrRolesInKB = switch (atom.getPredicate())
            {
                case Type -> _kb.isClass(atom.getArguments().get(1));
                case PropertyValue -> _kb.isProperty(atom.getArguments().get(1));
                default -> true;
            };
            if (!hasOnlyClassesOrRolesInKB)
            {
                _logger.warning("Query atom " + atom + " uses a class or role not present in its knowledge base");
                break;
            }
        }
        return hasOnlyClassesOrRolesInKB;
    }
}
