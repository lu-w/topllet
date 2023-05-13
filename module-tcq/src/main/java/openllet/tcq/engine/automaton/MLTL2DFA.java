package openllet.tcq.engine.automaton;

import net.automatalib.serialization.dot.DOTParsers;
import openllet.shared.tools.Log;
import openllet.tcq.engine.BooleanTCQEngineImpl;
import openllet.tcq.engine.mltl.MLTL2LTLf;
import openllet.tcq.model.automaton.DFA;
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

    public static DFA convert(String mltlFormula) throws IOException, InterruptedException,
            RuntimeException {
        String ltlfFormula = MLTL2LTLf.convert(mltlFormula);
        ltlfFormula = ltlfFormula.replace("\n", "");
        File file = File.createTempFile("mltl2dfa-", "");
        String tmpFile = file.getPath();
        file.delete();
        String command = "lydia";
        String error = "";
        String[] commandString = new String[]{command, "-l", "ltlf", "-i", ltlfFormula, "-p", "-g", tmpFile};
        Process child = Runtime.getRuntime().exec(commandString);
        child.waitFor();
        error = IOUtils.toString(child.getErrorStream());
        if (error.length() > 0)
            throw new RuntimeException("Lydia error: " + error);

        tmpFile += ".dot";
        FileInputStream fis = new FileInputStream(tmpFile);
        String dotAutomaton = IOUtils.toString(fis, StandardCharsets.UTF_8);

        _logger.info("Lydia DFA is located at " + tmpFile);

        // AutomataLib assumes a certain shape for DFAs that Lydia does not adhere to. Some fixes are required.
        Pattern r1 = Pattern.compile("([0-9]+) ");
        Matcher m1 = r1.matcher(dotAutomaton);
        if (m1.find())
            dotAutomaton = m1.replaceAll("s$1 ");
        Pattern r2 = Pattern.compile(" ([0-9]+)");
        Matcher m2 = r2.matcher(dotAutomaton);
        if (m2.find())
            dotAutomaton = m2.replaceAll(" s$1");
        dotAutomaton = dotAutomaton.replace("init", "__start0").
                replace("node [height = .5, width = .5];", "");

        try
        {
            return new DFA(DOTParsers.dfa().readModel(dotAutomaton.getBytes()).model);
        }
        catch (IOException e)
        {
            return new DFA();
        }
    }
}
