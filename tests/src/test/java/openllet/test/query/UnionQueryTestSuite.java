package openllet.test.query;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.JUnit4TestAdapter;

@RunWith(Suite.class)
@SuiteClasses({
        TestBooleanUnionQueries.class,
        TestUnionQueries.class
})
public class UnionQueryTestSuite
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(QueryTestSuite.class);
    }
}
