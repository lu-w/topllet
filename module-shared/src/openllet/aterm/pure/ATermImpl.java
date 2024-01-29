/*
 * Copyright (c) 2002-2007, CWI and INRIA
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of California, Berkeley nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package openllet.aterm.pure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import openllet.aterm.AFun;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermFactory;
import openllet.aterm.ATermPlaceholder;
import openllet.aterm.VisitFailure;
import openllet.atom.OpenError;
import openllet.shared.hash.SharedObjectWithID;

public abstract class ATermImpl extends ATermVisitableImpl implements ATerm, SharedObjectWithID
{
	protected PureFactory _factory;

	private int _hashCode;

	private int _uniqueId;

	/**
	 * depricated Use the new constructor instead.
	 *
	 * @param factory x
	 */
	protected ATermImpl(final PureFactory factory)
	{
		super();
		_factory = factory;
	}

	@Override
	public final int hashCode()
	{
		return _hashCode;
	}

	protected void setHashCode(final int hashcode)
	{
		_hashCode = hashcode;
	}

	/**
	 * Deprecated Just here for backwards compatibility : use new ATermImpl instead (or super)
	 *
	 * @param hashCode x
	 */
	protected void init(final int hashCode)
	{
		_hashCode = hashCode;
	}

	@Override
	public ATermFactory getFactory()
	{
		return _factory;
	}

	protected PureFactory getPureFactory()
	{
		return _factory;
	}

	@Override
	public List<Object> match(final String pattern)
	{
		return match(_factory.parsePattern(pattern));
	}

	@Override
	public List<Object> match(final ATerm pattern)
	{
		final List<Object> list = new LinkedList<>();
		if (match(pattern, list))
			return list;
		return null;
	}

	@Override
	public boolean isEqual(final ATerm term)
	{
		if (term instanceof ATermImpl)
			return this == term;

		return PureFactory.isDeepEqual(this, term);
	}

	@Override
	public boolean equals(final Object obj)
	{
		return this == obj;
	}

	protected boolean match(final ATerm pattern, final List<Object> list)
	{
		if (pattern.getType() == PLACEHOLDER)
		{
			final ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
			if (type.getType() == ATerm.APPL)
			{
				final ATermAppl appl = (ATermAppl) type;
				final AFun afun = appl.getAFun();
				if (afun.getName().equals("term") && afun.getArity() == 0 && !afun.isQuoted())
				{
					list.add(this);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public ATerm make(final List<Object> list)
	{
		return this;
	}

	@SuppressWarnings("resource") // the resource is manage by ATermWriter
	public void writeToTextFile(final ATermWriter writer) throws IOException
	{
		try
		{
			writer.voidVisitChild(this);
			writer.getStream().flush();
		}
		catch (final VisitFailure e)
		{
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void writeToSharedTextFile(final OutputStream stream) throws IOException
	{
		final ATermWriter writer = new ATermWriter(stream);
		writer.initializeSharing();
		stream.write('!');
		writeToTextFile(writer);
	}

	@Override
	public void writeToTextFile(final OutputStream stream) throws IOException
	{
		writeToTextFile(new ATermWriter(stream));
	}

	@Override
	public String toString()
	{
		try
		{
			final OutputStream stream = new ByteArrayOutputStream();
			final ATermWriter writer = new ATermWriter(stream);
			writeToTextFile(writer);

			return stream.toString();
		}
		catch (final IOException e)
		{
			throw new OpenError("IOException: " + e.getMessage());
		}
	}

	@Override
	public int getNrSubTerms()
	{
		return 0;
	}

	@Override
	public ATerm getSubTerm(final int index)
	{
		throw new OpenError("no children!");
	}

	@Override
	public ATerm setSubTerm(final int index, final ATerm t)
	{
		throw new OpenError("no children!");
	}

	@Override
	public int getUniqueIdentifier()
	{
		return _uniqueId;
	}

	@Override
	public void setUniqueIdentifier(final int uniqueId)
	{
		_uniqueId = uniqueId;
	}

}
