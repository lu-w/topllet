package openllet.tcq.model.query;

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
    public int compareTo(Proposition other) {
        return Integer.compare(_integerRepr, other.getIntegerRepresentation());
    }
}
