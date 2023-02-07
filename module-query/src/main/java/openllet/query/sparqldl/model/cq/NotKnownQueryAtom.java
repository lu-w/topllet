// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model.cq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.ResultBinding;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class NotKnownQueryAtom implements QueryAtom
{
	private final List<QueryAtom> _atoms;
	private boolean _isGround;
	private final List<ATermAppl> _args;

	public NotKnownQueryAtom(final QueryAtom atom)
	{
		this(Collections.singletonList(atom));
	}

	public NotKnownQueryAtom(final List<QueryAtom> atoms)
	{
		_atoms = Collections.unmodifiableList(atoms);

		_isGround = true;
		_args = new ArrayList<>();
		for (final QueryAtom atom : atoms)
		{
			_args.addAll(atom.getArguments());
			if (_isGround && !atom.isGround())
				_isGround = false;
		}
	}

	@Override
	public QueryAtom apply(final ResultBinding binding)
	{
		List<QueryAtom> newAtoms;
		if (_atoms.size() == 1)
			newAtoms = Collections.singletonList(_atoms.get(0).apply(binding));
		else
		{
			newAtoms = new ArrayList<>();
			for (final QueryAtom atom : _atoms)
				newAtoms.add(atom.apply(binding));
		}

		return new NotKnownQueryAtom(newAtoms);
	}

	@Override
	public QueryAtom copy()
	{
		List<QueryAtom> notKnownAtoms = new ArrayList<>();
		for (QueryAtom atom : _atoms)
			notKnownAtoms.add(atom.copy());
		return new NotKnownQueryAtom(notKnownAtoms);
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (!(obj instanceof NotKnownQueryAtom))
			return false;

		return _atoms.equals(((NotKnownQueryAtom) obj)._atoms);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ATermAppl> getArguments()
	{
		return _args;
	}

	public List<QueryAtom> getAtoms()
	{
		return _atoms;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryPredicate getPredicate()
	{
		return QueryPredicate.NotKnown;
	}

	@Override
	public int hashCode()
	{
		return 17 * _atoms.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGround()
	{
		return _isGround;
	}

	@Override
	public String toString()
	{
		return "NotKnown" + _atoms;
	}
}
