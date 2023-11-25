// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet;

import openllet.core.utils.VersionInfo;
import openllet.shared.tools.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * <p>
 * Description: Openllet main command line entry point
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Markus Stocker
 * @author Evren Sirin
 */
public class Topllet
{
	public static final Logger _logger = Log.getLogger(Topllet.class);
	public static final OpenlletExceptionFormatter exceptionFormatter = new OpenlletExceptionFormatter();

	public static void main(final String[] args)
	{
		try
		{
			Topllet.run(args);
		}
		catch (final OpenlletCmdException e)
		{
			printError(e);

			final StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			_logger.fine(sw.toString());
			_logger.throwing(null, null, e);
			System.exit(1);
		}
	}

	private static void printError(final Throwable e)
	{
		System.err.println(exceptionFormatter.formatException(e));
	}

	private static void run(final String[] args)
	{
		if (args.length == 0)
			throw new OpenlletCmdException("Type 'topllet help' for usage.");

		final String arg = args[0];

		if ("h".equals(arg) || "-h".equals(arg) || "help".equals(arg) || "--help".equals(arg))
		{
			mainhelp();
		}
		else
			if ("--version".equals(arg) || "-V".equals(arg))
				version();
			else
			{
				final OpenlletCmdApp cmd = new OpenlletTemporalQuery();
				cmd.parseArgs(args);
				cmd.run();
				cmd.finish();
			}
	}

	private static void mainhelp()
	{
		new OpenlletTemporalQuery().help();
	}

	private static VersionInfo getVersionInfo()
	{
		return VersionInfo.getInstance();
	}

	private static void version()
	{
		System.out.println(getVersionInfo());
	}
}
