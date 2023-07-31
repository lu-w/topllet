package openllet.tcq.engine;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Bool;
import openllet.modularity.OntologyDiff;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.shared.tools.Log;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * Stores satisfiability information of an arbitrary BCQ wrt. different time points.
 */
public class SatisfiabilityKnowledge
{
    public static final Logger _logger = Log.getLogger(SatisfiabilityKnowledge.class);
    private final CNCQQuery _cncq;
    private final Collection<ATerm> _relevantTBoxClassesAndRoles;
    private final TemporalConjunctiveQuery _tcq;
    private final Map<Integer, QueryResult> _satisfiableBindings = new HashMap<>();
    private final Map<Integer, QueryResult> _unsatisfiableBindings = new HashMap<>();
    private final List<Integer> _isComplete = new ArrayList<>();

    public SatisfiabilityKnowledge(CNCQQuery query, TemporalConjunctiveQuery tcq)
    {
        _cncq = query;
        _tcq = tcq;
        _relevantTBoxClassesAndRoles = _tcq.getTemporalKB().getConnectedClassesAndRolesInAxiomGraph(
                _cncq.getClassesAndRoles());
    }

    public CNCQQuery getQuery()
    {
        return _cncq;
    }

    public void informAboutSatisfiability(QueryResult bindings, boolean satisfiability, int applicableTimePoint)
    {
        informAboutSatisfiability(bindings, satisfiability, Set.of(applicableTimePoint));
    }

    public void informAboutSatisfiability(QueryResult bindings, boolean satisfiability,
                                          Collection<Integer> applicableTimePoints)
    {
        // The information may have been gained from an impartial query check (e.g. A(?x) when the overall TCQ
        // works over ?x and ?y. Therefore, we expand the query result to all variables of the TCQ.
        _logger.finer(_cncq + " is informed about satisfiability (" + satisfiability + ")");
        if (!bindings.getResultVars().equals(_tcq.getResultVars()))
            bindings.expandToAllVariables(_tcq.getResultVars());
        bindings.explicate();

        Map<Integer, QueryResult> applicableBindings;

        if (satisfiability)
            applicableBindings = _satisfiableBindings;
        else
            applicableBindings = _unsatisfiableBindings;

        for (int applicableTimePoint : applicableTimePoints)
            if (applicableBindings.containsKey(applicableTimePoint))
                applicableBindings.get(applicableTimePoint).addAll(bindings);
            else
                applicableBindings.put(applicableTimePoint, bindings);
    }

    public Map<Bool, QueryResult> getCertainSatisfiabilityKnowledge(int timePoint)
    {
        return getCertainSatisfiabilityKnowledge(timePoint, null);
    }

    public Map<Bool, QueryResult> getCertainSatisfiabilityKnowledge(int timePoint, QueryResult restrictSatToBindings)
    {
        Map<Bool, QueryResult> knowledge = new HashMap<>();
        if (_satisfiableBindings.containsKey(timePoint))
            knowledge.put(Bool.TRUE, filterBindings(_satisfiableBindings.get(timePoint), restrictSatToBindings));
        else
            knowledge.put(Bool.TRUE, new QueryResultImpl(_cncq));
        if (_unsatisfiableBindings.containsKey(timePoint))
            knowledge.put(Bool.FALSE, _unsatisfiableBindings.get(timePoint));
        else
            knowledge.put(Bool.FALSE, new QueryResultImpl(_cncq));
        return knowledge;
    }

    /**
     * @param bindings The bindings to filter
     * @param restrictToBindings The bindings to restrict bindings to
     * @return A new copy of bindings in which only bindings are present that are also present in restrictToBindings
     */
    private QueryResult filterBindings(QueryResult bindings, QueryResult restrictToBindings)
    {
        QueryResult filtered;
        if (restrictToBindings != null)
        {
            // Depending on the sizes of the query results, it is more efficient to compute the intersection in
            // different orders.
            if (restrictToBindings.size() < bindings.size())
            {
                filtered = restrictToBindings.copy();
                filtered.retainAll(bindings);
            }
            else
            {
                filtered = bindings.copy();
                filtered.retainAll(restrictToBindings);
            }
        }
        else
            filtered = bindings;
        return filtered;
    }

    public void transferKnowledgeFromPreviousStepTo(int timePoint)
    {
        if (timePoint > 0 && _satisfiableBindings.containsKey(timePoint - 1))
        {
            QueryResult satKnowledge = _satisfiableBindings.get(timePoint - 1);
            QueryResult unsatKnowledge = _unsatisfiableBindings.get(timePoint - 1);
            OntologyDiff diff = _tcq.getTemporalKB().getDiffToLastKB();
            if (isSatisfiabilityTransferableUnderDifference(diff))
            {
                // TODO is copy() needed?
                informAboutSatisfiability(satKnowledge, true, timePoint);
                informAboutSatisfiability(unsatKnowledge, false, timePoint);
            }
        }
    }

    public boolean isComplete(int timePoint)
    {
        return _isComplete.contains(timePoint) ||
                (getCertainSatisfiabilityKnowledge(timePoint).get(Bool.FALSE).size() +
                getCertainSatisfiabilityKnowledge(timePoint).get(Bool.TRUE).size() ==
                new QueryResultImpl(_cncq).getMaxSize());
    }

    public boolean isEmpty(int timePoint)
    {
        return (getCertainSatisfiabilityKnowledge(timePoint).get(Bool.FALSE).size() +
                getCertainSatisfiabilityKnowledge(timePoint).get(Bool.TRUE).size()) == 0;
    }

    public void setComplete(int timePoint)
    {
        _isComplete.add(timePoint);
    }

    protected boolean isSatisfiabilityTransferableUnderDifference(OntologyDiff diff)
    {
        if (diff == null || _relevantTBoxClassesAndRoles == null)
            return false;
        else
        {
            boolean transferable = true;
            for (OWLAxiom ax : diff.getAdditions())
                transferable &= Collections.disjoint(_relevantTBoxClassesAndRoles, getClassesAndRolesFromAxiom(ax));
            for (OWLAxiom ax : diff.getDeletions())
                transferable &= Collections.disjoint(_relevantTBoxClassesAndRoles, getClassesAndRolesFromAxiom(ax));
            return transferable;
        }
    }

    protected static Collection<ATerm> getClassesAndRolesFromAxiom(OWLAxiom axiom)
    {
        Collection<ATerm> classesAndRoles = new HashSet<>();
        for (OWLClass cls : axiom.getClassesInSignature())
            classesAndRoles.add(ATermUtils.makeTermAppl(cls.getIRI().toString()));
        for (OWLDataProperty dProp : axiom.getDataPropertiesInSignature())
            classesAndRoles.add(ATermUtils.makeTermAppl(dProp.getIRI().toString()));
        for (OWLObjectProperty oProp : axiom.getObjectPropertiesInSignature())
            classesAndRoles.add(ATermUtils.makeTermAppl(oProp.getIRI().toString()));
        return classesAndRoles;
    }
}
