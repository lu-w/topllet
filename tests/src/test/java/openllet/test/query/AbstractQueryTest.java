// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.engine.bcq.BCQQueryEngineSimple;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimple;
import openllet.query.sparqldl.model.*;
import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.bcq.BCQQueryImpl;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.ucq.*;
import openllet.query.sparqldl.model.Query.VarType;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.cq.ConjunctiveQueryImpl;
import openllet.mtcq.engine.MTCQEngine;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.test.AbstractKBTests;
import org.junit.Assert;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

import static openllet.core.utils.TermFactory.var;
import static org.junit.Assert.*;

/**
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public abstract class AbstractQueryTest extends AbstractKBTests
{
	/**
	 * Shared variables for tests
	 */

	protected static final ATermAppl x = var("x");
	protected static final ATermAppl y = var("y");
	protected static final ATermAppl z = var("z");
	protected static final ATermAppl x1 = var("x1");
	protected static final ATermAppl y1 = var("y1");
	protected static final ATermAppl z1 = var("z1");

	/**
	 * Constructors for testing environment
	 */

	protected ATermAppl[] select(final ATermAppl... vars)
	{
		return vars;
	}

	protected QueryAtom[] where(final QueryAtom... atoms)
	{
		return atoms;
	}

	@SafeVarargs
	protected final <T> T[] where(final T... queries)
	{
		return queries;
	}

	protected ConjunctiveQuery ask(final QueryAtom... atoms)
	{
		return query(new ATermAppl[0], atoms);
	}

	private <T extends AbstractQuery<?>> T newQuery(final Class<T> clazz)
	{
		T q = null;
		for (Constructor<?> c : clazz.getConstructors())
			if (c.getParameterCount() == 2)
			{
				try { q = (T) c.newInstance(_kb, false); } catch (Exception ignored) {}
				break;
			}
		return q;
	}

	@SafeVarargs
	private <ST extends Query<ST>, T extends AbstractCompositeQuery<ST, ?>> T cQuery(final Class<T> clazz,
																					 final ST... queries)
	{
		T q = newQuery(clazz);
		if (q != null) q.setQueries(Arrays.stream(queries).toList());
		return q;
	}

	private <ST extends Query<ST>, T extends AbstractCompositeQuery<ST, ?>> T cQuery(final Class<T> clazz,
																					 final ATermAppl[] vars,
																					 final ST[] queries)
	{
		T q = cQuery(clazz, queries);
		if (q != null)
			for (final ATermAppl var : vars)
			{
				q.addResultVar(var);
				q.addDistVar(var, VarType.INDIVIDUAL);
				for (ST query : queries)
					if (query instanceof AtomQuery<?>)
						for (QueryAtom atom : ((AtomQuery<?>) query).getAtoms())
							if (atom.getArguments().contains(var) && !query.getResultVars().contains(var))
							{
								query.addResultVar(var);
								query.addDistVar(var, VarType.INDIVIDUAL);
							}
			}
		return q;
	}

	private <T extends AbstractAtomQuery<?>> T aQuery(final Class<T> clazz, final QueryAtom... atoms)
	{
		T q = newQuery(clazz);
		if (q != null) q.addAtoms(Arrays.stream(atoms).toList());
		return q;
	}

	private <T extends AbstractAtomQuery<?>> T aQuery(final Class<T> clazz, final ATermAppl[] vars,
													  final QueryAtom... atoms)
	{
		T q = aQuery(clazz, atoms);
		if (q != null)
			for (final ATermAppl var : vars)
			{
				q.addResultVar(var);
				q.addDistVar(var, VarType.INDIVIDUAL);
			}
		return q;
	}

	protected ConjunctiveQuery query(final ATermAppl[] vars, final QueryAtom[] atoms)
	{
		final ConjunctiveQuery q = aQuery(ConjunctiveQueryImpl.class, vars, atoms);
		if (q != null)
			for (final ATermAppl var : q.getUndistVars())
				q.addDistVar(var, VarType.INDIVIDUAL);
		return q;
	}

	protected ConjunctiveQuery query(final QueryAtom... atoms)
	{
		return aQuery(ConjunctiveQueryImpl.class, atoms);
	}

	protected ConjunctiveQuery negatedQuery(final QueryAtom... atoms)
	{
		ConjunctiveQuery q = aQuery(ConjunctiveQueryImpl.class, atoms);
		q.setNegation(true);
		return q;
	}

	protected ConjunctiveQuery negatedQuery(final ATermAppl[] vars, final QueryAtom[] atoms)
	{
		ConjunctiveQuery q = aQuery(ConjunctiveQueryImpl.class, vars, atoms);
		q.setNegation(true);
		return q;
	}

	protected DisjunctiveQuery disjunctiveQuery(final ATermAppl[] vars, final QueryAtom[] atoms)
	{
		return aQuery(DisjunctiveQueryImpl.class, vars, atoms);
	}

	protected DisjunctiveQuery disjunctiveQuery(final QueryAtom... atoms)
	{
		return aQuery(DisjunctiveQueryImpl.class, atoms);
	}

	protected UnionQuery unionQuery(final ConjunctiveQuery... queries)
	{
		return cQuery(UnionQueryImpl.class, queries);
	}

	protected UnionQuery unionQuery(final ATermAppl[] vars, final ConjunctiveQuery[] queries)
	{
		return cQuery(UnionQueryImpl.class, vars, queries);
	}

	protected CNFQueryImpl cnfQuery(final DisjunctiveQuery... queries)
	{
		return cQuery(CNFQueryImpl.class, queries);
	}

	protected CNFQuery cnfQuery(final ATermAppl[] vars, final DisjunctiveQuery[] queries)
	{
		return cQuery(CNFQueryImpl.class, vars, queries);
	}

	protected BCQQuery bcqQuery(final ConjunctiveQuery... queries)
	{
		return cQuery(BCQQueryImpl.class, queries);
	}

	protected BCQQuery bcqQuery(final ATermAppl[] vars, final ConjunctiveQuery[] queries)
	{
		return cQuery(BCQQueryImpl.class, vars, queries);
	}

	/**
	 * Testing functionality
	 */

	private QueryResult execQuery(Query<?> query)
	{
		QueryResult result = null;
		try
		{
			if (query instanceof ConjunctiveQuery)
				result = new QueryEngine().exec((ConjunctiveQuery) query);
			else if (query instanceof UnionQuery)
				result = new UnionQueryEngineSimple(UnionQueryEngineSimple.BindingTime.AFTER_CNF).
						exec((UnionQuery) query);
			else if (query instanceof BCQQuery)
				result = new BCQQueryEngineSimple().exec((BCQQuery) query);
			else if (query instanceof MetricTemporalConjunctiveQuery)
				result = new MTCQEngine().exec((MetricTemporalConjunctiveQuery) query);
			else
				fail("Unknown query type " + query.getClass());
			if (result == null)
				fail("No result returned for query " + query);
		}
		catch (IOException | InterruptedException e)
		{
			fail(e.toString());
		}
		return result;
	}

	protected void testQuery(final Query<?> query, final boolean expected)
	{
		assertEquals(expected, !execQuery(query).isEmpty());
	}

	protected void testQuery(final Query<?> query, List<List<ATermAppl>> values)
	{
		final List<ATermAppl> resultVars = query.getResultVars();

		final Map<List<ATermAppl>, Integer> answers = new HashMap<>();
		for (final List<ATermAppl> answer : values)
			answers.merge(answer, 1, Integer::sum);

		final QueryResult result = execQuery(query);
		for (final ResultBinding binding : result)
		{
			final List<ATermAppl> list = new ArrayList<>(resultVars.size());
			for (final ATermAppl var : resultVars)
				list.add(binding.getValue(var));

			final Integer count = answers.get(list);
			if (count == null)
				Assert.fail("Unexpected binding in the result: " + list);
			else if (count == 1)
				answers.remove(list);
			else
				answers.put(list, count - 1);
		}

		assertTrue("Unfound bindings: " + answers.keySet(), answers.isEmpty());
	}

	protected void testQuery(final Query<?> query, final ATermAppl[]... values)
	{
		List<List<ATermAppl>> valuesList = new ArrayList<>();
		for (ATermAppl[] answer : values)
			valuesList.add(Arrays.stream(answer).toList());
		testQuery(query, valuesList);
	}

	/**
	 * Shared helper methods
	 */

	protected static List<List<ATermAppl>> allResults(List<ATermAppl> individuals, int resultSize, boolean distinct)
	{
		List<ATermAppl> resVars = new ArrayList<>();
		for (int i = 0; i < resultSize; i++)
			resVars.add(ATermUtils.makeVar(Integer.toString(i)));
		Collection<ResultBinding> allBindings = QueryResult.allBindings(resVars, individuals, distinct);
		List<List<ATermAppl>> res = new ArrayList<>();
		for (ResultBinding binding : allBindings)
		{
			List<ATermAppl> bindingList = new ArrayList<>();
			for (ATermAppl resVar : resVars)
				bindingList.add(binding.getValue(resVar));
			res.add(bindingList);
		}
		return res;
	}
}
