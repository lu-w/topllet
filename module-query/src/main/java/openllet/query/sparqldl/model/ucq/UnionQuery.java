package openllet.query.sparqldl.model.ucq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.ResultBinding;
import openllet.query.sparqldl.model.cq.Filter;
import openllet.query.sparqldl.model.cq.Query;
import openllet.query.sparqldl.model.cq.QueryParameters;

import java.util.List;
import java.util.Set;

public interface UnionQuery
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
    void addDistVar(final ATermAppl a, final UnionQuery.VarType type);

    /**
     * @param a is the distinguished variable to add that appears in the result projection to the query;
     */
    void addResultVar(final ATermAppl a);

    /**
     * @return all the variables used in this query.
     */
    Set<ATermAppl> getVars();

    /**
     * Return all undistinguished variables used in this query.
     *
     * @return Set of variables
     */
    Set<ATermAppl> getUndistVars();

    /**
     * @return individuals and literals used in this query.
     */
    Set<ATermAppl> getConstants();

    /**
     * Return all the variables that will be in the results. For SPARQL, these are the variables in the SELECT clause.
     *
     * @return list of variables
     */
    List<ATermAppl> getResultVars();

    /**
     * Return all the distinguished variables. These are variables that will be bound to individuals (or _data values) existing in the KB.
     *
     * @return Set of variables
     */
    Set<ATermAppl> getDistVars();

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
     * @return The list of queries that represents the union.
     */
    List<Query> getQueries();

    /**
     * Sets the list of queries that represents the union.
     *
     * @param queries The list of queries that represents the union
     */
    void setQueries(List<Query> queries);

    /**
     * Checks whether the query is ground.
     *
     * @return true iff the query is ground
     */
    boolean isGround();

    /**
     * @param queryType #VarType
     * @return variables that occur in the subquery specified by the given type.
     */
    Set<ATermAppl> getDistVarsForType(final UnionQuery.VarType queryType);

    /**
     * Replace the variables in the query with the values specified in the binding and return a new query instance (without modifying this query).
     *
     * @param binding the binding to apply
     * @return the query changed
     */
    UnionQuery apply(ResultBinding binding);

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
     * Adds a query to the union query.
     *
     * @param query the query to add
     */
    void addQuery(final Query query);

    /**
     * Converts this union query to conjunctive normal form, i.e. of the form (a v ... v b) ^ ... ^ (c v ... v d).
     * Does not change this query.
     * @return A list of union queries, where each conjunctive query of each union query contains only one atom.
     */
    List<DisjunctiveQuery> toCNF();

    /**
     * Creates a subquery from the given query. Queries are listed according to the 'queries' parameter.
     *
     * @param queries selected query indices
     * @return subquery
     */
    UnionQuery reorder(int[] queries);

    /**
     * Checks if there are two disjuncts that share a common undistinguished variable.
     * @return True iff there are two disjuncts that share a common undistinguished variable
     */
    boolean disjunctsShareUndistVars();

    /**
     * Rolls this query up by applying the rolling-up procedure to each disjunct separately.
     * @return A new rolled-up union query.
     */
    UnionQuery rollUp();

    /**
     * Checks if one of the disjuncts contains a cycle in its query graph. Note that this function only looks for
     * cycles in property atoms. It ignores other kind of atom types (e.g. same as).
     * @return True iff one of the disjuncts contains a cycle
     */
    boolean hasCycle();

    /**
     * Returns a new (deep) copy of this query.
     * @return A copy of this query.
     */
    UnionQuery copy();
}