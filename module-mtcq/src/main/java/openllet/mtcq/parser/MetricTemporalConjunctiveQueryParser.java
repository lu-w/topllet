package openllet.mtcq.parser;

import openllet.mtcq.model.query.MTCQFormula;
import openllet.shared.tools.Log;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.logging.Logger;

/**
 * A parser for metric temporal conjunctive queries.
 */
public class MetricTemporalConjunctiveQueryParser
{
    public static final Logger _logger = Log.getLogger(MetricTemporalConjunctiveQueryParser.class);

    /**
     * Parses the given string as a MTCQ over the given knowledge base.
     * @param input The string to parse.
     * @param tkb The knowledge base containing roles and concepts used in the CQs.
     * @return The parsed MTCQ containing the propositional abstraction and the CQs.
     * @throws ParseException If the input was not a valid MTCQ string.
     */
    static public MTCQFormula parse(String input, TemporalKnowledgeBase tkb)
    {
        return parse(input, tkb, true);
    }

    /**
     * Parses the given string as a MTCQ over the given knowledge base.
     * @param input The string to parse.
     * @param tkb The knowledge base containing roles and concepts used in the CQs.
     * @param isDistinct whether the answers variable to the MTCQ shall be mapped to distinct individuals.
     * @return The parsed MTCQ containing the propositional abstraction and the CQs.
     * @throws ParseException If the input was not a valid MTCQ string.
     */
    static public MTCQFormula parse(String input, TemporalKnowledgeBase tkb, boolean isDistinct)
    {
        MTCQLexer lexer = new MTCQLexer(CharStreams.fromString(input));
        MTCQParser parser = new MTCQParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());
        try
        {
            ParseTree tree = parser.start();
            MTCQBuilder builder = new MTCQBuilder(tkb, isDistinct);
            return builder.visit(tree);
        }
        catch (ParseCancellationException e)
        {
            throw new ParseException(e.getMessage());
        }
    }
}
