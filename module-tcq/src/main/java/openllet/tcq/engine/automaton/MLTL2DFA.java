package openllet.tcq.engine.automaton;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.words.impl.ListAlphabet;
import openllet.tcq.engine.mltl.MLTL2LTLf;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MLTL2DFA
{
    public static CompactDFA<String> convert(String mltlFormula) throws IOException, InterruptedException,
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

        // AutomataLib assumes a certain shape for DFAs that Lydia does not adhere to. Some fixes are required.
        dotAutomaton = dotAutomaton.replace("init", "__start0").
                replace("node [height = .5, width = .5];", "");
        Pattern r1 = Pattern.compile("node \\[shape = doublecircle\\]; ([0-9]+)");
        Matcher m1 = r1.matcher(dotAutomaton);
        if (m1.find())
            dotAutomaton = m1.replaceAll("s$1 node [shape = doublecircle];");
        Pattern r2 = Pattern.compile("node \\[shape = circle\\]; ([0-9 ]+)");
        Matcher m2 = r2.matcher(dotAutomaton);
        if (m2.find())
            dotAutomaton = m2.replaceAll("s$1 node [shape = circle];");
        Pattern r3 = Pattern.compile("([^a-z])([0-9]+) ");
        Matcher m3 = r3.matcher(dotAutomaton);
        if (m3.find())
            dotAutomaton = m3.replaceAll("$1s$2 ");
        Pattern r4 = Pattern.compile(" ([0-9]+)([^\".])");
        Matcher m4 = r4.matcher(dotAutomaton);
        if (m4.find())
            dotAutomaton = m4.replaceAll(" s$1$2");

        try
        {
            return DOTParsers.dfa().readModel(dotAutomaton.getBytes()).model;
        }
        catch (IOException e)
        {
            return new CompactDFA<>(new ListAlphabet<>(new ArrayList<>()));
        }
    }
}
