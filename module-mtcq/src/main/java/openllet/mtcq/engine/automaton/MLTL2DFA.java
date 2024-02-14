package openllet.mtcq.engine.automaton;

import net.automatalib.serialization.dot.DOTParsers;
import openllet.shared.tools.Log;
import openllet.mtcq.engine.mltl.MLTL2LTLf;
import openllet.mtcq.model.automaton.DFA;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.parser.ParseException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converter from MLTL string to a DFA based on the MLTL2LTLf converter and the lydia command line tool.
 * Calls the tool using IOUtils.
 */
public class MLTL2DFA
{
    public static final Logger _logger = Log.getLogger(MLTL2DFA.class);

    public static DFA convert(String mltlFormula)
    {
        return convert(mltlFormula, null);
    }

    public static DFA convert(String mltlFormula, MetricTemporalConjunctiveQuery mtcq)
    {
        String ltlfFormula;
        ltlfFormula = MLTL2LTLf.convert(mltlFormula);
        ltlfFormula = ltlfFormula.replace("\n", "");
        File file;
        try
        {
            file = File.createTempFile("mltl2dfa-", "");
        }
        catch (IOException e)
        {
            throw new LydiaException(e.getMessage());
        }
        String tmpFile = file.getPath();
        file.delete();
        final String command = "lydia";
        String error = "";
        String[] commandString = new String[]{command, "-l", "ltlf", "-i", ltlfFormula, "-p", "-g", tmpFile};
        try
        {
            Process child = Runtime.getRuntime().exec(commandString);
            child.waitFor();
            error = IOUtils.toString(child.getErrorStream());
        }
        catch (InterruptedException | IOException e)
        {
            throw new LydiaException("Can not execute " + command + " - is the '" + command +
                    "' executable in your PATH?");
        }
        if (!error.isEmpty())
            throw new ParseException("Lydia error: " + error.replaceAll("[\r\n]", " "));

        tmpFile += ".dot";
        FileInputStream fis;
        String dotAutomaton;
        try
        {
            fis = new FileInputStream(tmpFile);
            dotAutomaton = IOUtils.toString(fis, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new LydiaException(e.getMessage());
        }

        _logger.fine("Lydia DFA is located at " + tmpFile);

        // AutomataLib assumes a certain shape for DFAs that Lydia/Mona does not adhere to. Some fixes are required.
        Pattern r1 = Pattern.compile("([0-9]+) -> ([0-9]+)");
        Matcher m1 = r1.matcher(dotAutomaton);
        if (m1.find())
            dotAutomaton = m1.replaceAll("s$1 -> s$2");
        Pattern r2 = Pattern.compile("(;[^s\\[]*[s0-9]* )([0-9]+)");
        Matcher m2 = r2.matcher(dotAutomaton);
        while (m2.find())
        {
            dotAutomaton = m2.replaceAll("$1s$2");
            m2 = r2.matcher(dotAutomaton);
        }
        dotAutomaton = dotAutomaton.replace("init", "__start0").
                replace("node [height = .5, width = .5];", "");

        try
        {
            return new DFA(DOTParsers.dfa().readModel(dotAutomaton.getBytes()).model, mtcq);
        }
        catch (IOException e)
        {
            throw new ParseException("AutomataLib can not read .dot DFA: " + e);
        }
    }
}
