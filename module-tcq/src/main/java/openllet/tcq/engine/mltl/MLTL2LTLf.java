package openllet.tcq.engine.mltl;

import openllet.tcq.parser.ParseException;
import org.apache.commons.io.IOUtils;
import java.io.IOException;

public class MLTL2LTLf
{
    static public String convert(String mltlFormula) throws InterruptedException, IOException, ParseException
    {
        String ltlfFormula = "";
        String command = "mltl2ltl";
        String error = "";

        try
        {
            Process child = Runtime.getRuntime().exec(new String[]{command, mltlFormula});
            child.waitFor();
            ltlfFormula = IOUtils.toString(child.getInputStream());
            error = IOUtils.toString(child.getErrorStream());
        }
        catch (IOException e)
        {
            throw new IOException("Can not execute " + command + " - is the '" + command +
                    "' executable in your PATH?");
        }
        if (error.length() > 0)
        {
            String errorMessage = error.replace("[\n\r]", " ");
            if (error.contains("lark.exceptions.Unexpected"))
                errorMessage = "Unexpected input during parsing of " + mltlFormula + "\n" + errorMessage;
            else if (error.contains("recursion depth"))
                errorMessage = "MLTL " + ltlfFormula + " formula has a too large bound for MLTL2LTL";
            else
                errorMessage = "Other error - " + errorMessage;
            throw new ParseException("MLTL2LTL error: " + errorMessage);
        }

        return ltlfFormula;
    }
}
