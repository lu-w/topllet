package openllet.query.sparqldl.model;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.cq.Filter;
import openllet.query.sparqldl.model.cq.QueryParameters;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Query<QueryType extends Query<QueryType>>
{
    enum VarType
    {
        CLASS, PROPERTY, INDIVIDUAL, LITERAL
    }

    /**
     * Adds a distinguished variable to the query with its type - there can be more variable types to support punning.
     *
     * @param a the distinguished variable
     * @param type type of the variable
     */
    void addDistVar(final ATermAppl a, final VarType type);

    /**
     * @param a is the distinguished variable to add that appears in the result projection to the query.
     */
    void addResultVar(final ATermAppl a);

    /**
     * Removes the distinguished variable from the query, and also from the result variables, if present.
     * @param a is the distinguished variable to remove.
     */
    void removeDistVar(final ATermAppl a);

    /**
     * @param a is the result variable to remove.
     */
    void removeResultVar(final ATermAppl a);

    /**
     * Sets all the variables that will be in the results. For SPARQL, these are the variables in the SELECT clause.
     * @param resultVars list of variables
     */
    void setResultVars(final List<ATermAppl> resultVars);

    /**
     * Return all the distinguished variables. These are variables that will be bound to individuals (or _data
     * values) existing in the KB.
     * @param distVars Set of variables
     */
    void setDistVars(final EnumMap<VarType, Set<ATermAppl>> distVars);

    /**
     * @return an unmodifiable view on all the variables used in this query.
     */
    Set<ATermAppl> getVars();

    /**
     * Return a copy of all undistinguished variables used in this query.
     *
     * @return Set of variables
     */
    Set<ATermAppl> getUndistVars();

    /**
     * @return an unmodifiable view on all individuals and literals used in this query.
     */
    Set<ATermAppl> getConstants();

    /**
     * Return an unmodifiable view on all the variables that will be in the results. For SPARQL, these are the variables
     * in the SELECT clause.
     *
     * @return list of variables
     */
    List<ATermAppl> getResultVars();

    /**
     * Return a copy of all the distinguished variables. These are variables that will be bound to individuals (or _data
     * values) existing in the KB.
     *
     * @return Set of variables
     */
    Set<ATermAppl> getDistVars();

    /**
     * Return a copy of all the distinguished variables, including their var type.
     *
     * @return Map var types to set of variables
     */
    Map<VarType, Set<ATermAppl>> getDistVarsWithVarType();

    /**
     * @param filter to sets for this query.
     */
    void setFilter(final Filter filter);

    /**
     * @return the filter for this query.
     */
    Filter getFilter();

    /**
     * @return true if distinct results are required.
     */
    boolean isDistinct();

    /**
     * @return The KB that will be used to answer this query.
     */
    KnowledgeBase getKB();

    /**
     * Sets the KB that will be used to answer this query.
     *
     * @param kb KB that will be used to answer this query
     */
    void setKB(KnowledgeBase kb);

    /**
     * Checks whether the query is ground.
     *
     * @return true iff the query is ground
     */
    boolean isGround();

    /**
     * Checks whether the query contains no checkable components at all.
     * @return true iff. the query is equivalent to the empty query
     */
    boolean isEmpty();

    /**
     * @param queryType #VarType
     * @return variables that occur in the subquery specified by the given type.
     */
    Set<ATermAppl> getDistVarsForType(final VarType queryType);

    /**
     * Replace the variables in the query with the values specified in the binding and return a new query instance
     * (without modifying this query).
     *
     * @param binding the binding to apply
     * @return the query changed
     */
    QueryType apply(ResultBinding binding);

    /**
     * Return the name of this query
     *
     * @return name of the query
     */
    ATermAppl getName();

    /**
     * Sets the name of this query
     *
     * @param name name of the query
     */
    void setName(ATermAppl name);

    /**
     * @param parameters to set for the query parameterization
     */
    void setQueryParameters(QueryParameters parameters);

    /**
     * Get the query parameterization values
     *
     * @return QueryParameters
     */
    QueryParameters getQueryParameters();

    /**
     * Returns a new (deep) copy of this query.
     * @return A copy of this query.
     */
    QueryType copy();

    /**
     * Creates a subquery from the given query (shallow copy). Queries are listed according to the 'queries' parameter.
     *
     * @param queries selected query indices
     * @return subquery
     */
    QueryType reorder(int[] queries);

    /**
     * Checks if one of the disjuncts contains a cycle in its query graph. Note that this function only looks for
     * cycles in property atoms. It ignores other kind of atom types (e.g. same as).
     * @return True iff one of the disjuncts contains a cycle
     */
    boolean hasCycle();

    /**
     * Splits the query into disjoint parts.
     * @return A list of disjoint queries
     */
    List<QueryType> split();


    /**
     * Creates a new query of the given type - to be implemented by any concrete class.
     * @param kb Knowledge base
     * @param isDistinct whether the variable are distinct
     * @return A new query instance
     */
    QueryType createQuery(KnowledgeBase kb, boolean isDistinct);

    String toString(boolean multiLine, boolean onlyQueryBody);
}
