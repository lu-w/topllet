// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.engine.ucq.BooleanUnionQueryEngineSimple;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimple;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.AbstractQuery;
import openllet.query.sparqldl.model.CompositeQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cncq.CNCQQueryImpl;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.query.sparqldl.model.ucq.*;
import openllet.query.sparqldl.model.Query.VarType;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.cq.ConjunctiveQueryImpl;
import openllet.test.AbstractKBTests;
import org.junit.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static openllet.core.utils.TermFactory.var;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
	protected static final ATermAppl x = var("x");
	protected static final ATermAppl y = var("y");
	protected static final ATermAppl z = var("z");
	protected static final ATermAppl x1 = var("x1");
	protected static final ATermAppl y1 = var("y1");
	protected static final ATermAppl z1 = var("z1");

	protected ATermAppl[] select(final ATermAppl... vars)
	{
		return vars;
	}

	protected QueryAtom[] where(final QueryAtom... atoms)
	{
		return atoms;
	}

	@SafeVarargs
	protected final <QueryType> QueryType[] where(final QueryType... queries)
	{
		return queries;
	}

	protected ConjunctiveQuery ask(final QueryAtom... atoms)
	{
		return query(new ATermAppl[0], atoms);
	}

	protected ConjunctiveQuery query(final ATermAppl[] vars, final QueryAtom[] atoms)
	{
		final ConjunctiveQuery q = new ConjunctiveQueryImpl(_kb, true);
		for (final ATermAppl var : vars)
			q.addResultVar(var);
		for (final QueryAtom atom : atoms)
			q.add(atom);
		for (final ATermAppl var : q.getUndistVars())
			q.addDistVar(var, VarType.INDIVIDUAL);
		return q;
	}

	protected ConjunctiveQuery query(final QueryAtom... atoms)
	{
		final ConjunctiveQuery q = new ConjunctiveQueryImpl(_kb, true);
		for (final QueryAtom atom : atoms)
			q.add(atom);
		return q;
	}

	protected UnionQuery unionQuery(final ConjunctiveQuery... queries)
	{
		return tQuery(UnionQueryImpl.class, queries);
	}

	protected UnionQuery unionQuery(final ATermAppl[] vars, final ConjunctiveQuery[] queries)
	{
		return tQuery(UnionQueryImpl.class, vars, queries);
	}

	protected DisjunctiveQuery disjunctiveQuery(final ATermAppl[] vars, final QueryAtom[] atoms)
	{
		DisjunctiveQuery q = disjunctiveQuery(atoms);
		q.setResultVars(List.of(vars));
		for (ATermAppl var : vars)
			q.addDistVar(var, VarType.INDIVIDUAL);
		return q;
	}

	protected DisjunctiveQuery disjunctiveQuery(final QueryAtom... atoms)
	{
		final DisjunctiveQuery q = new DisjunctiveQueryImpl(_kb, true);
		for (final QueryAtom atom : atoms)
			q.add(atom);
		return q;
	}

	protected CNFQueryImpl cnfQuery(final DisjunctiveQuery... queries)
	{
		return tQuery(CNFQueryImpl.class, queries);
	}

	protected CNFQuery cnfQuery(final ATermAppl[] vars, final DisjunctiveQuery[] queries)
	{
		return tQuery(CNFQueryImpl.class, vars, queries);
	}

	protected <ST extends Query<ST>, T extends AbstractCompositeQuery<ST, ?>> T tQuery(final Class<T> clazz,
																					   final ST... queries)
	{
		T q = null;
		for (Constructor<?> c : clazz.getConstructors())
			if (c.getParameterCount() == 2)
			{
				try
				{
					q = (T) c.newInstance(_kb, false);
				}
				catch (Exception e)
				{
				}
				break;
			}
		if (q != null)
			q.setQueries(Arrays.stream(queries).toList());
		return q;
	}

	protected <ST extends Query<ST>, T extends AbstractCompositeQuery<ST, ?>> T tQuery(final Class<T> clazz,
																					   final ATermAppl[] vars,
																					   final ST[] queries)
	{
		T q = tQuery(clazz, queries);
		for (final ATermAppl var : vars)
		{
			q.addResultVar(var);
			q.addDistVar(var, VarType.INDIVIDUAL);
			for (ST query : queries)
				if (query instanceof Query.AtomQuery)
					for (QueryAtom atom : ((Query.AtomQuery) query).getAtoms())
						if (atom.getArguments().contains(var) && !query.getResultVars().contains(var))
						{
							query.addResultVar(var);
							query.addDistVar(var, VarType.INDIVIDUAL);
						}
		}
		return q;
	}

	protected CNCQQuery cncqQuery(final ConjunctiveQuery... queries)
	{
		final CNCQQuery q = new CNCQQueryImpl(_kb, false);
		q.setQueries(Arrays.stream(queries).toList());
		return q;
	}

	protected CNCQQuery cncqQuery(final ATermAppl[] vars, final ConjunctiveQuery[] queries)
	{
		CNCQQuery q = cncqQuery(queries);
		q.setResultVars(List.of(vars));
		for (ATermAppl var : vars)
			q.addDistVar(var, VarType.INDIVIDUAL);
		return q;
	}

	protected void testQuery(final ConjunctiveQuery query, final boolean expected)
	{
		final QueryResult result = QueryEngine.exec(query);

		assertEquals(expected, !result.isEmpty());
	}

	protected void testQuery(final ConjunctiveQuery query, final ATermAppl[]... values)
	{
		final List<ATermAppl> resultVars = query.getResultVars();

		final Map<List<ATermAppl>, Integer> answers = new HashMap<>();
		for (final ATermAppl[] value : values)
			answers.merge(Arrays.asList(value), 1, Integer::sum);

		final QueryResult result = QueryEngine.exec(query);
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

	protected void testUnionQuery(final UnionQuery query, final boolean expected)
	{
		QueryExec<UnionQuery> engine = new BooleanUnionQueryEngineSimple();
		QueryResult result = new QueryResultImpl(query);
		if (expected)
			result.add(new ResultBindingImpl());
		assertEquals(result, engine.exec(query));
	}

	protected void testUnionQuery(final UnionQuery query, final ATermAppl[]... values)
	{
		List<List<ATermAppl>> valuesList = new ArrayList<>();
		for (ATermAppl[] answer : values)
			valuesList.add(Arrays.stream(answer).toList());
		testUnionQuery(query, valuesList);
	}

	protected void testUnionQuery(final UnionQuery query, final List<List<ATermAppl>> values)
	{
		final List<ATermAppl> resultVars = query.getResultVars();

		final Map<List<ATermAppl>, Integer> answers = new HashMap<>();
		for (final List<ATermAppl> answer : values)
			answers.merge(answer, 1, Integer::sum);

		UnionQueryEngineSimple engine = new UnionQueryEngineSimple();
		engine.setBindingTime(UnionQueryEngineSimple.BindingTime.AFTER_CNF);
		final QueryResult result = engine.exec(query);
		for (final ResultBinding binding : result)
		{
			final List<ATermAppl> list = new ArrayList<>(resultVars.size());
			for (final ATermAppl var : resultVars)
				list.add(binding.getValue(var));

			final Integer count = answers.get(list);
			if (count == null)
				Assert.fail("Unexpected binding in the result: " + list);
			else
				if (count == 1)
					answers.remove(list);
				else
					answers.put(list, count - 1);
		}

		assertTrue("Unfound bindings: " + answers.keySet(), answers.isEmpty());
	}

	protected void testCNCQQuery(final CNCQQuery query, final boolean expected)
	{
		assertTrue(true);
	}

	protected static List<List<ATermAppl>> allResults(List<ATermAppl> individuals, int resultSize)
	{
		// https://stackoverflow.com/a/40101377/4145563
		List<List<ATermAppl>> res = new ArrayList<>();
		int[] indexes = new int[Math.max(individuals.size(), resultSize)];
		ATermAppl[] permutation = new ATermAppl[resultSize];
		for (int j = (int) Math.pow(individuals.size(), resultSize); j > 0; j--)
		{
			for (int i = 0; i < resultSize; i++)
				permutation[i] = individuals.get(indexes[i]);
			res.add(Arrays.stream(permutation).toList());
			for (int i = 0; i < resultSize; i++)
			{
				if (indexes[i] >= individuals.size() - 1)
					indexes[i] = 0;
				else
				{
					indexes[i]++;
					break;
				}
			}
		}
		return res;
	}
}
