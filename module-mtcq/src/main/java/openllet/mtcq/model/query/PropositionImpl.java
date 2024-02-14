package openllet.mtcq.model.query;

import javax.annotation.Nonnull;

/**
 * Standard implementation of propositions based on an internal string and integer representation.
 */
public class PropositionImpl implements Proposition
{
    private final String _prop;
    private final int _integerRepr;

    public PropositionImpl(String proposition, int integerRepresentation)
    {
        _prop = proposition;
        _integerRepr = integerRepresentation;
    }

    public int getIntegerRepresentation()
    {
        return _integerRepr;
    }

    @Override
    public String toString()
    {
        return _prop;
    }

    @Override
    public int compareTo(@Nonnull Proposition other)
    {
        return Integer.compare(_integerRepr, other.getIntegerRepresentation());
    }

    @Override
    public boolean equals(@Nonnull Object other)
    {
        if (other instanceof Proposition prop)
            return _integerRepr == prop.getIntegerRepresentation();
        else
            return false;
    }

    @Override
    public int hashCode()
    {
        return _integerRepr;
    }
}
