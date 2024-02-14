package openllet.mtcq.parser;

/**
 * Is thrown in case the parser encounters an invalid input.
 */
public class ParseException extends RuntimeException
{
    public ParseException(String errorMessage)
    {
        super(errorMessage);
    }
}
