// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.el;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.rbox.Role;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.CollectionUtils;
import openllet.core.utils.MultiValueMap;
import openllet.shared.tools.Log;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Harris Lin
 */
public class RoleChainCache
{
	public static final Logger _logger = Log.getLogger(RoleChainCache.class);

	private static final String ANON_ROLE = "anonRole";

	private int _anonRoleCount;
	private final Map<ATermAppl, MultiValueMap<ATermAppl, ATermAppl>> _binaryRoleInclusions;

	public RoleChainCache(final KnowledgeBase kb)
	{
		_anonRoleCount = 0;
		_binaryRoleInclusions = CollectionUtils.makeMap();

		for (final Role supRole : kb.getRBox().getRoles().values())
		{
			if (supRole.isAnon())
				continue;

			for (ATermList chain : supRole.getSubRoleChains())
			{
				final int chainLength = chain.getLength();
				if (chainLength <= 1)
					continue;

				ATermAppl r1 = (ATermAppl) chain.getFirst();
				chain = chain.getNext();
				ATermAppl r2 = (ATermAppl) chain.getFirst();
				ATermAppl superRole = createSuperRoleFor(r1, r2);
				for (int i = 1; i < chainLength - 1; i++)
				{
					add(kb, r1, r2, superRole);

					r1 = superRole;
					chain = chain.getNext();
					r2 = (ATermAppl) chain.getFirst();
					superRole = createSuperRoleFor(r1, r2);
				}

				add(kb, r1, r2, supRole.getName());
			}
		}
	}

	public static boolean isAnon(final ATermAppl r)
	{
		return r.getName().startsWith(ANON_ROLE);
	}

	public Set<ATermAppl> getAllSuperRoles(final ATermAppl r1, final ATermAppl r2)
	{
		final MultiValueMap<ATermAppl, ATermAppl> innerMap = _binaryRoleInclusions.get(r1);
		if (innerMap == null)
			return Collections.emptySet();

		final Set<ATermAppl> superRoles = innerMap.get(r2);
		if (superRoles == null)
			return Collections.emptySet();

		return superRoles;
	}

	private ATermAppl createSuperRoleFor(final ATermAppl r1, final ATermAppl r2)
	{
		final Set<ATermAppl> superRoles = getAllSuperRoles(r1, r2);

		return superRoles.isEmpty() ? //
				ATermUtils.makeTermAppl(ANON_ROLE + _anonRoleCount++) : //
				superRoles.iterator().next();
	}

	private void add(final KnowledgeBase kb, final ATermAppl r1, final ATermAppl r2, final ATermAppl superRole)
	{
		final Role role1 = kb.getRole(r1);
		final Role role2 = kb.getRole(r2);

		if (role1 == null)
		{
			if (role2 == null)
				add(r1, r2, superRole);
			else
				for (final Role sub2 : role2.getSubRoles())
					add(r1, sub2.getName(), superRole);
		}
		else
			if (role2 == null)
				for (final Role sub1 : role1.getSubRoles())
					add(sub1.getName(), r2, superRole);
			else
				for (final Role sub1 : role1.getSubRoles())
					for (final Role sub2 : role2.getSubRoles())
						add(sub1.getName(), sub2.getName(), superRole);
	}

	private boolean add(final ATermAppl r1, final ATermAppl r2, final ATermAppl superRole)
	{
		MultiValueMap<ATermAppl, ATermAppl> innerMap = _binaryRoleInclusions.get(r1);
		if (innerMap == null)
		{
			innerMap = new MultiValueMap<>();
			_binaryRoleInclusions.put(r1, innerMap);
		}

		return innerMap.add(r2, superRole);
	}

	public void print()
	{
		_logger.finer("Role Chains:");
		_logger.finer(_binaryRoleInclusions.toString());
	}
}
