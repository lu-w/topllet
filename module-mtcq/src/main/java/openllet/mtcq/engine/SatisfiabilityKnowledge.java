package openllet.mtcq.engine;

import openllet.aterm.ATerm;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Bool;
import openllet.modularity.OntologyDiff;
import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.shared.tools.Log;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * Stores satisfiability and unsatisfiability information of an arbitrary BCQ w.r.t. different time points.
 * This knowledge is updated by the satisfiability knowledge manager, which globally manages all knowledge.
 */
@Deprecated
public class SatisfiabilityKnowledge
{
    public static final Logger _logger = Log.getLogger(SatisfiabilityKnowledge.class);
    private final BCQQuery _bcq;
    private final Collection<ATerm> _relevantTBoxClassesAndRoles;
    private final MetricTemporalConjunctiveQuery _mtcq;
    private final Map<Integer, QueryResult> _satisfiableBindings = new HashMap<>();
    private final Map<Integer, QueryResult> _unsatisfiableBindings = new HashMap<>();
    private final List<Integer> _isComplete = new ArrayList<>();

    /**
     * Creates a new satisfiability knowledge data storage object for the given BCQ, which is used when checking
     * satisfiability of the given MTCQ.
     * @param query The BCQ to store knowledge on.
     * @param mtcq The MTCQ from whose DFA the BCQ originates.
     */
    public SatisfiabilityKnowledge(BCQQuery query, MetricTemporalConjunctiveQuery mtcq)
    {
        _bcq = query;
        _mtcq = mtcq;
        _relevantTBoxClassesAndRoles = _mtcq.getTemporalKB().getConnectedClassesAndRolesInAxiomGraph(
                _bcq.getClassesAndRoles());
    }

    /**
     * @return A pointer to the BCQ that is managed.
     */
    public BCQQuery getQuery()
    {
        return _bcq;
    }

    /**
     * If externally some knowledge on the satisfiability of the BCQ was gained, this informs the existing knowledge
     * about it s.t. it can be incorporated.
     * @param bindings The new knowledge.
     * @param satisfiability Whether the knowledge is on unsatisfiability (true) or satisfiability (false).
     * @param applicableTimePoint The time point to which the knowledge refers to.
     */
    public void informAboutSatisfiability(QueryResult bindings, boolean satisfiability, int applicableTimePoint)
    {
        informAboutSatisfiability(bindings, satisfiability, Set.of(applicableTimePoint));
    }

    /**
     * If externally some knowledge on the satisfiability of the BCQ was gained, this informs the existing knowledge
     * about it s.t. it can be incorporated.
     * @param bindings The new knowledge.
     * @param satisfiability Whether the knowledge is on unsatisfiability (true) or satisfiability (false).
     * @param applicableTimePoints The time points to which the knowledge refers to.
     */
    public void informAboutSatisfiability(QueryResult bindings, boolean satisfiability,
                                          Collection<Integer> applicableTimePoints)
    {
        // The information may have been gained from an impartial query check (e.g. A(?x) when the overall MTCQ
        // works over ?x and ?y. Therefore, we expand the query result to all variables of the MTCQ.
        _logger.finer(_bcq + " is informed about satisfiability (" + satisfiability + ")");
        if (!bindings.getResultVars().equals(_mtcq.getResultVars()))
            bindings.expandToAllVariables(_mtcq.getResultVars());
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

    /**
     * @param timePoint The time point for which to get the knowledge.
     * @return The knowledge this data object has for on the satisfiability (i.e., a mapping from true to the
     * satisfiable and false to the unsatisfiable bindings).
     */
    public Map<Bool, QueryResult> getCertainSatisfiabilityKnowledge(int timePoint)
    {
        return getCertainSatisfiabilityKnowledge(timePoint, null);
    }

    /**
     * @param timePoint The time point for which to get the knowledge.
     * @param restrictSatToBindings Satisfiability knowledge is restricted (filtered) to these bindings.
     * @return The knowledge this data object has for on the satisfiability (i.e., a mapping from true to the
     * satisfiable and false to the unsatisfiable bindings). Note that this may return a pointer and not a copy to the
     * resulting bindings. A copy is only returned for satisfiable bindings if they had to be restricted.
     */
    public Map<Bool, QueryResult> getCertainSatisfiabilityKnowledge(int timePoint, QueryResult restrictSatToBindings)
    {
        Map<Bool, QueryResult> knowledge = new HashMap<>();
        if (_satisfiableBindings.containsKey(timePoint))
            knowledge.put(Bool.TRUE, filterBindings(_satisfiableBindings.get(timePoint), restrictSatToBindings));
        else
            knowledge.put(Bool.TRUE, new QueryResultImpl(_bcq));
        if (_unsatisfiableBindings.containsKey(timePoint))
            knowledge.put(Bool.FALSE, _unsatisfiableBindings.get(timePoint));
        else
            knowledge.put(Bool.FALSE, new QueryResultImpl(_bcq));
        return knowledge;
    }

    /**
     * Filters the given bindings s.t. they at most contain the bindings in restrictToBindings.
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

    /**
     * Transfers the knowledge from the previous time point to the given time point, if possible.
     * @param timePoint The current time point.
     */
    public void transferKnowledgeFromPreviousStepTo(int timePoint)
    {
        if (timePoint > 0 && _satisfiableBindings.containsKey(timePoint - 1))
        {
            QueryResult satKnowledge = _satisfiableBindings.get(timePoint - 1);
            QueryResult unsatKnowledge = _unsatisfiableBindings.get(timePoint - 1);
            OntologyDiff diff = _mtcq.getTemporalKB().getDiffToLastKB();
            if (isSatisfiabilityTransferableUnderDifference(diff))
            {
                informAboutSatisfiability(satKnowledge, true, timePoint);
                informAboutSatisfiability(unsatKnowledge, false, timePoint);
            }
        }
    }

    /**
     * Note: this function is cached.
     * @param timePoint The current time point.
     * @return True iff. the satisfiability knowledge is complete at the given time point, i.e., the size of both
     * satisfiable and unsatisfiable bindings make up the maximum size.
     */
    public boolean isComplete(int timePoint)
    {
        return _isComplete.contains(timePoint) ||
                (getCertainSatisfiabilityKnowledge(timePoint).get(Bool.FALSE).size() +
                getCertainSatisfiabilityKnowledge(timePoint).get(Bool.TRUE).size() ==
                new QueryResultImpl(_bcq).getMaxSize());
    }

    /**
     * @param timePoint The current time point.
     * @return True iff. no certain satisfiability knowledge is currently stored in this data object.
     */
    public boolean isEmpty(int timePoint)
    {
        return (getCertainSatisfiabilityKnowledge(timePoint).get(Bool.FALSE).size() +
                getCertainSatisfiabilityKnowledge(timePoint).get(Bool.TRUE).size()) == 0;
    }

    /**
     * For performance reasons, knowledge can be set complete (i.e., no further checks are required).
     * @param timePoint The current time point.
     */
    public void setComplete(int timePoint)
    {
        _isComplete.add(timePoint);
    }

    /**
     * @param diff Difference between two ABoxes.
     * @return True iff. the difference allows to retain this satisfiability knowledge.
     */
    protected boolean isSatisfiabilityTransferableUnderDifference(OntologyDiff diff)
    {
        if (diff == null || _relevantTBoxClassesAndRoles == null)
            return false;
        else
        {
            boolean transferable = true;
            for (OWLAxiom ax : diff.getAdditions())
            {
                transferable = Collections.disjoint(_relevantTBoxClassesAndRoles, getClassesAndRolesFromAxiom(ax));
                if (!transferable)
                    break;
            }
            if (transferable)
                for (OWLAxiom ax : diff.getDeletions())
                {
                    transferable = Collections.disjoint(_relevantTBoxClassesAndRoles, getClassesAndRolesFromAxiom(ax));
                    if (!transferable)
                        break;
                }
            return transferable;
        }
    }

    /**
     * Helper function to extract classes and roles from an OWLAxiom.
     * @param axiom Axiom to extract classes and roles from.
     * @return Classes and roles (in ATerms) for the given axiom.
     */
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
