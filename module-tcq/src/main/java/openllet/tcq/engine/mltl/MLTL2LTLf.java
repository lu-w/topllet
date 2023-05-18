package openllet.tcq.engine.mltl;

import org.apache.commons.io.IOUtils;
import java.io.IOException;

public class MLTL2LTLf
{
    static public String convert(String mltlFormula) throws InterruptedException, IOException, RuntimeException {
        String ltlfFormula = "";
        String command = "mltl2ltl";
        String error = "";

        Process child = Runtime.getRuntime().exec(new String[]{command, mltlFormula});
        child.waitFor();
        ltlfFormula = IOUtils.toString(child.getInputStream());
        error = IOUtils.toString(child.getErrorStream());

        if (error.length() > 0)
            throw new RuntimeException("Lydia error: " + error);

        return ltlfFormula;
    }
}