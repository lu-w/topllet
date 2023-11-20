package openllet.test.tcq;

import junit.framework.JUnit4TestAdapter;
import openllet.test.query.TestBooleanBCQQueries;
import openllet.test.query.TestBooleanUnionQueries;
import openllet.test.query.TestBCQueries;
import openllet.test.query.TestUnionQueries;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CQParserTest.class, TCQParserTest.class, TestMLTL2DFA.class, TestMLTL2LTL.class,
        TestBooleanTCQEngine.class, TestTCQEngine.class, QueryResultTest.class, TestBCQueries.class,
        TestBooleanBCQQueries.class, TestUnionQueries.class, TestBooleanUnionQueries.class, TKBLoaderTest.class})
public class TCQTestSuite
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(openllet.test.tcq.TCQTestSuite.class);
    }
}
