package openllet.tcq.model.query;

public class PropositionImpl implements Proposition
{
    private final String _prop;

    public PropositionImpl(String proposition)
    {
        _prop = proposition;
    }

    @Override
    public String toString()
    {
        return _prop;
    }
}
