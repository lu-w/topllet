package openllet.mtcq.parser;

/**
 * Is thrown in case the parser encounters an invalid input.
 */
public class ParseException extends Exception
{
    public ParseException(String errorMessage)
    {
        super(errorMessage);
    }
}
