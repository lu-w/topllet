package openllet.mtcq.engine.mltl;

import openllet.mtcq.parser.ParseException;
import org.apache.commons.io.IOUtils;
import java.io.IOException;

/**
 * Converter from an MLTL string to an LTLf string based on the mltl2ltl command line tool.
 * Calls the tool using IOUtils.
 */
@Deprecated
public class MLTL2LTLf
{
    static public String convert(String mltlFormula)
    {
        final String command = "mltl2ltl";
        String ltlfFormula = "";
        String error = "";
        try
        {
            Process child = Runtime.getRuntime().exec(new String[]{command, mltlFormula});
            child.waitFor();
            ltlfFormula = IOUtils.toString(child.getInputStream());
            error = IOUtils.toString(child.getErrorStream());
        }
        catch (IOException | InterruptedException e)
        {
            throw new MLTL2LTLfException("Can not execute " + command + " - is the '" + command +
                    "' executable in your PATH?");
        }
        if (!error.isEmpty())
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
