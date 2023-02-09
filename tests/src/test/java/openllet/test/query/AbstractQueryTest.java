// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.engine.ucq.BooleanUnionQueryEngineSimple;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimpleBinding;
import openllet.query.sparqldl.engine.ucq.UnionQueryExec;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.Query.VarType;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.cq.ConjunctiveQueryImpl;
import openllet.query.sparqldl.model.ucq.UnionQueryImpl;
import openllet.test.AbstractKBTests;
import org.junit.Assert;

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

	protected ConjunctiveQuery[] where(final ConjunctiveQuery... queries)
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

	protected UnionQuery unionQuery(final Query... queries)
	{
		final UnionQuery q = new UnionQueryImpl(_kb, false);
		q.setQueries(Arrays.stream(queries).toList());
		return q;
	}

	protected UnionQuery unionQuery(final ATermAppl[] vars, final Query[] queries)
	{
		final UnionQuery q = new UnionQueryImpl(_kb, false);
		for (final ATermAppl var : vars)
		{
			q.addResultVar(var);
			q.addDistVar(var, VarType.INDIVIDUAL);
			for (Query conjQuery : queries)
			{
				ConjunctiveQuery query = (ConjunctiveQuery) conjQuery;
				for (QueryAtom atom : query.getAtoms())
				{
					if (atom.getArguments().contains(var) && !query.getResultVars().contains(var))
					{
						query.addResultVar(var);
						query.addDistVar(var, VarType.INDIVIDUAL);
					}
				}
			}
		}
		q.setQueries(Arrays.stream(queries).toList());
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
		{
			final List<ATermAppl> answer = Arrays.asList(value);
			final Integer count = answers.get(answer);
			if (count == null)
				answers.put(answer, 1);
			else
				answers.put(answer, count + 1);

		}

		final QueryResult result = QueryEngine.exec(query);
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

	protected void testUnionQuery(final UnionQuery query, final boolean expected)
	{
		UnionQueryExec engine = new BooleanUnionQueryEngineSimple();
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
		{
			final Integer count = answers.get(answer);
			if (count == null)
				answers.put(answer, 1);
			else
				answers.put(answer, count + 1);
		}

		UnionQueryEngineSimpleBinding engine = new UnionQueryEngineSimpleBinding();
		engine.setBindingTime(UnionQueryEngineSimpleBinding.BindingTime.AFTER_CNF);
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

	protected static List<List<ATermAppl>> allResults(List<ATermAppl> individuals, int resultSize)
	{
		// https://stackoverflow.com/a/40101377/4145563
		List<List<ATermAppl>> res = new ArrayList<>();
		int[] indexes = new int[individuals.size()];
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
