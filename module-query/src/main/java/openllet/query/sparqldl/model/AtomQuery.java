package openllet.query.sparqldl.model;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.cq.QueryPredicate;

import java.util.List;

public interface AtomQuery<QueryType extends AtomQuery<QueryType>> extends Query<QueryType> {
    /**
     * @return a string representing a delimiter to be printed between atoms
     */
    String getAtomDelimiter();

    /**
     * @return a string representing a prefix to be optionally printed before the query
     */
    String getQueryPrefix();

    /**
     * Adds a query atom to the query.
     *
     * @param atom the atom to add
     */
    void add(final QueryAtom atom);

    /**
     * Adds query atoms to the query.
     *
     * @param atoms the atoms to add
     */
    void addAtoms(final List<QueryAtom> atoms);

    /**
     * Removes a query atom from the query.
     * @param atom the atom to remove
     */
    void remove(final QueryAtom atom);

    /**
     * @return an unmodifiable view on all the atoms in the query.
     */
    List<QueryAtom> getAtoms();

    /**
     * Searches for given atom pattern. This also might be used for different types of rolling-up, involving various
     * sets of allowed atom types.
     *
     * @param predicate the predicate to search for
     * @param arguments arguments to search for
     * @return query atoms in the order as they appear in the query
     */
    List<QueryAtom> findAtoms(final QueryPredicate predicate, final ATermAppl... arguments);
}
