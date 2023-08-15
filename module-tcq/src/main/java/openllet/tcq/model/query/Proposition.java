package openllet.tcq.model.query;

/**
 * A proposition is an atomic element in the atomic conjunctions of the propositional abstraction of a TCQ.
 */
public interface Proposition extends Comparable<Proposition>
{
    /**
     * @return The integer representation of the proposition. Can be used for easier comparison.
     */
    int getIntegerRepresentation();

    /**
     * @return A string representation of the proposition.
     */
    @Override
    String toString();

    /**
     * Compares the integer representation of this proposition to the other proposition.
     * @param other Proposition to compare against.
     * @return 0 if equals, less than 0 if this is less than the other proposition, and greater than 1 if this is
     * greater than the other proposition.
     */
    @Override
    int compareTo(Proposition other);
}
