// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.utils;

/**
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren sirin
 * @param <F> kind of first element
 * @param <S> kind of second element
 */
public class Pair<F, S>
{
	public F first;
	public S second;

	public Pair(final F first_, final S second_)
	{
		// We allow nullable pair elements.
		// if (first_ == null || second_ == null)
		//	throw new IllegalArgumentException();

		this.first = first_;
		this.second = second_;
	}

	public static <F, S> Pair<F, S> create(final F f, final S s)
	{
		return new Pair<>(f, s);
	}

	@Override
	public int hashCode()
	{
		if (first != null && second != null)
			return first.hashCode() + second.hashCode();
		else if (first != null)
			return first.hashCode();
		else if (second != null)
			return second.hashCode();
		else
			return -1;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (o == this)
			return true;

		if (!(o instanceof Pair))
			return false;

		final Pair<?, ?> p = (Pair<?, ?>) o;

		if (first != null && p.first != null && second != null && p.second != null)
			return first.equals(p.first) && second.equals(p.second);
		else if (first != null && p.first != null)
			return first.equals(p.first);
		else if (second != null && p.second != null)
			return second.equals(p.second);
		else
			return false;
	}

	@Override
	public String toString()
	{
		String f_string = "";
		String s_string = "";
		if (first != null)
			f_string = first.toString();
		if (second != null)
			s_string = second.toString();
		return "[" + f_string + ", " + s_string + "]";
	}
}
