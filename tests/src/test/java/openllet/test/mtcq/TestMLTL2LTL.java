package openllet.test.mtcq;

import openllet.mtcq.engine.automaton.MLTL2DFA;
import openllet.mtcq.engine.mltl.MLTL2LTLf;
import openllet.mtcq.model.automaton.DFA;
import openllet.mtcq.parser.ParseException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TestMLTL2LTL
{
    protected String convert(String mltlFormula)
    {
        String ltlf = "";
        try
        {
            ltlf = MLTL2LTLf.convert(mltlFormula);
        }
        catch (IOException | InterruptedException | ParseException e)
        {
            fail("Can not convert " + mltlFormula + " to LTLf: " + e);
        }
        return ltlf;
    }

    @Test
    public void testMLTLConversion1()
    {
        String ltlf = convert("G_[10,20] a");
        assertEquals("(X[!](X[!](X[!](X[!](X[!](X[!](X[!](X[!](X[!](X[!](a&(X[!](a&(X[!](a&(X[!](a&(X[!](a&" +
                "(X[!](a&(X[!](a&(X[!](a&(X[!](a&(X[!](a&(X[!]a))))))))))))))))))))))))))))))\n", ltlf);
    }

    @Test
    public void testMLTLConversion2()
    {
        String ltlf = convert("F_<=1 a & (b U_[5,10] c) -> (G b | F_[0,20] d)");
        assertEquals("(a|(X[!]a))&((X[!](X[!](X[!](X[!](X[!](c|(b&(X[!](c|(b&(X[!](c|(b&(X[!](c|(b&(X[!](c|(b&(X[!]c)" +
                "))))))))))))))))))))->(G b|(d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!]" +
                "(d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!](d|(X[!]d))))))))))" +
                ")))))))))))))))))))))))))))))))\n", ltlf);
    }

    @Test
    public void testWrongUseOfMLTLOperator()
    {
        assertThrows(ParseException.class, () -> MLTL2LTLf.convert("G[10,20] a"));
    }
}
