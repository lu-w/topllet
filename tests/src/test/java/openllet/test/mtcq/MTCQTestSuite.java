package openllet.test.mtcq;

import junit.framework.JUnit4TestAdapter;
import openllet.test.query.TestBooleanBCQQueries;
import openllet.test.query.TestBooleanUnionQueries;
import openllet.test.query.TestBCQueries;
import openllet.test.query.TestUnionQueries;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CQParserTest.class, MTCQParserTest.class, TestBooleanMTCQEngine.class, TestMTCQEngine.class,
        QueryResultTest.class, TestBCQueries.class, TestBooleanBCQQueries.class, TestUnionQueries.class,
        TestBooleanUnionQueries.class, TKBLoaderTest.class, TestFileBasedMTCQEngine.class})
public class MTCQTestSuite
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(openllet.test.mtcq.MTCQTestSuite.class);
    }
}
