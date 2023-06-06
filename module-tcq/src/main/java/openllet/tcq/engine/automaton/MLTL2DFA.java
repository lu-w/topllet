package openllet.tcq.engine.automaton;

import net.automatalib.serialization.dot.DOTParsers;
import openllet.shared.tools.Log;
import openllet.tcq.engine.mltl.MLTL2LTLf;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.parser.ParseException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MLTL2DFA
{
    public static final Logger _logger = Log.getLogger(MLTL2DFA.class);

    public static DFA convert(String mltlFormula) throws IOException, ParseException, InterruptedException
    {
        return convert(mltlFormula, null);
    }

    public static DFA convert(String mltlFormula, TemporalConjunctiveQuery tcq)
            throws IOException, InterruptedException, ParseException
    {
        String ltlfFormula = MLTL2LTLf.convert(mltlFormula);
        ltlfFormula = ltlfFormula.replace("\n", "");
        File file = File.createTempFile("mltl2dfa-", "");
        String tmpFile = file.getPath();
        file.delete();
        String command = "lydia";
        String error = "";
        String[] commandString = new String[]{command, "-l", "ltlf", "-i", ltlfFormula, "-p", "-g", tmpFile};
        try
        {
            Process child = Runtime.getRuntime().exec(commandString);
            child.waitFor();
            error = IOUtils.toString(child.getErrorStream());
        }
        catch (IOException e)
        {
            throw new IOException("Can not execute " + command + " - is the '" + command +
                    "' executable in your PATH?");
        }
        if (error.length() > 0)
            throw new ParseException("Lydia error: " + error.replaceAll("[\r\n]", " "));

        tmpFile += ".dot";
        FileInputStream fis = new FileInputStream(tmpFile);
        String dotAutomaton = IOUtils.toString(fis, StandardCharsets.UTF_8);

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
            return new DFA(DOTParsers.dfa().readModel(dotAutomaton.getBytes()).model, tcq);
        }
        catch (IOException e)
        {
            throw new ParseException("AutomataLib can not read .dot DFA: " + e);
        }
    }
}
