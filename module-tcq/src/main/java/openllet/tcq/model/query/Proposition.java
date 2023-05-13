package openllet.tcq.model.query;

public interface Proposition extends Comparable<Proposition>
{
    int getIntegerRepresentation();

    @Override
    String toString();

    @Override
    int compareTo(Proposition other);
}
