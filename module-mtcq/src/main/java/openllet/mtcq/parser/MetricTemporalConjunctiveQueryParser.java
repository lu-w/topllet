package openllet.mtcq.parser;

import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.shared.tools.Log;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.mtcq.model.query.Proposition;
import openllet.mtcq.model.query.PropositionFactory;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQueryImpl;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for temporal conjunctive queries.
 * It extracts the CQs used in the MTCQ and builds a propositional abstraction for them.
 * CQs are expected to be bracketed for easier parsing. It uses the ConjunctiveQueryParser for parsing the single CQs.
 */
public class MetricTemporalConjunctiveQueryParser
{
    public static final Logger _logger = Log.getLogger(MetricTemporalConjunctiveQueryParser.class);

    protected static final String[] validMLTLToken = {"F", "G", "U", "X", "_", "<", "=", "[", "]", "(", ")", "W",
            "X[!]", "last", "end", "first", "start", "R", "V", "M", "->", "<->", "&", "!", "|", "0", "1", "2", "3",
            "4", "5", "6", "7", "8", "9", ",", "true", "false", "tt", "ff"};

    /**
     * Parses the given string as a MTCQ over the given knowledge base.
     * @param input The string to parse.
     * @param kb The knowledge base containing roles and concepts used in the CQs.
     * @return The parsed MTCQ containing the propositional abstraction and the CQs.
     * @throws ParseException If the input was not a valid MTCQ string.
     */
    static public MetricTemporalConjunctiveQuery parse(String input, TemporalKnowledgeBase kb) throws ParseException
    {
        // Removes comments
        Pattern commentLine = Pattern.compile("(?m)(^#.*$)");
        Matcher commentLineMatcher = commentLine.matcher(input);
        String mtcq = commentLineMatcher.replaceAll("");
        Pattern inlineComment = Pattern.compile("(?m)^(.*?)(# .*)$");
        Matcher inlineCommentMatcher = inlineComment.matcher(mtcq);
        mtcq = inlineCommentMatcher.replaceAll("$1");

        // Stores prefixes and removes them from string
        Map<String, String> prefixes = new HashMap<>();
        Scanner scanner = new Scanner(mtcq);
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if (line.startsWith("PREFIX"))
            {
                Pattern pattern = Pattern.compile("^PREFIX *([^ ]*) *<([^ ]*)>$");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find())
                    prefixes.put(matcher.group(1), matcher.group(2));
            }
        }
        scanner.close();
        mtcq = mtcq.replaceAll("PREFIX.*(\r\n|\r|\n)", "");

        // Resolves prefixes in remaining string
        for (String prefix : prefixes.keySet())
            mtcq = mtcq.replaceAll(prefix, prefixes.get(prefix));

        // Removes irrelevant line breaks and tabs / whitespaces
        mtcq = mtcq.replaceAll("(\r\n|\r|\n)[\t ]*", "").trim();

        MetricTemporalConjunctiveQuery parsedMtcq = new MetricTemporalConjunctiveQueryImpl(mtcq, kb, true);
        final PropositionFactory propositionFactory = new PropositionFactory();

        while (!mtcq.replaceAll("[\\s)]", "").isEmpty())
        {
            String remainder = mtcq;
            int curIndex = 0;
            boolean tokenFound = true;
            // iterate until we first observe a non-whitespace non-valid MLTL token
            while (tokenFound)
            {
                tokenFound = false;
                if (!remainder.isEmpty() && Character.isWhitespace(remainder.charAt(0)))
                {
                    remainder = remainder.substring(1);
                    curIndex += 1;
                    tokenFound = true;
                }
                for (String token : validMLTLToken)
                {
                    if (remainder.startsWith(token))
                    {
                        remainder = remainder.substring(token.length());
                        curIndex += token.length();
                        tokenFound = true;
                        break;
                    }
                }
            }
            int cqBeg = curIndex;
            curIndex--;  // we consumed the opening bracket - undo

            // check if this was preceded by an opening bracket (ignoring whitespaces)
            boolean openingBracketFound = false;
            if (mtcq.charAt(curIndex) == '(')
                openingBracketFound = true;
            else
                for (int i = curIndex - 1; i >= 0 && Character.isWhitespace(mtcq.charAt(i)); i--)
                    if (mtcq.charAt(i) == '(')
                    {
                        openingBracketFound = true;
                        break;
                    }

            if (!openingBracketFound)
                throw new ParseException("Can not find opening bracket for conjunctive query in " + mtcq);

            // iterate until we find the closing bracket
            curIndex++;  // consume opening bracket
            int numOpenBrackets = 1;
            while (numOpenBrackets > 0 && curIndex < mtcq.length())
            {
                if (mtcq.charAt(curIndex) == '(')
                    numOpenBrackets++;
                else if (mtcq.charAt(curIndex) == ')')
                    numOpenBrackets--;
                curIndex++;
            }
            int cqEnd = curIndex - 1;  // we do not want the closing bracket included in the CQ itself
            String cqString = mtcq.substring(cqBeg, cqEnd);
            mtcq = mtcq.substring(curIndex);
            ConjunctiveQuery q = ConjunctiveQueryParser.parse(cqString, kb.get(0));
            if (!parsedMtcq.getConjunctiveQueries().contains(q))
            {
                Proposition qProp = propositionFactory.create(q);
                parsedMtcq.addConjunctiveQuery(qProp, q, cqString);
            }
        }

        _logger.fine("Propositional abstraction is " + parsedMtcq.getPropositionalAbstraction());

        return parsedMtcq;
    }
}
