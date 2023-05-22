// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import openllet.core.utils.VersionInfo;
import openllet.shared.tools.Log;

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
public class Openllet
{
	public static final Logger _logger = Log.getLogger(Openllet.class);
	public static final OpenlletExceptionFormatter exceptionFormatter = new OpenlletExceptionFormatter();

	private static final Map<String, OpenlletCmdApp> COMMANDS = new TreeMap<>();
	static
	{
		COMMANDS.put("classify", new OpenlletClassify());
		COMMANDS.put("consistency", new OpenlletConsistency());
		COMMANDS.put("realize", new OpenlletRealize());
		COMMANDS.put("unsat", new OpenlletUnsatisfiable());
		COMMANDS.put("explain", new OpenlletExplain());
		COMMANDS.put("query", new OpenlletQuery());
		COMMANDS.put("temporal-query", new OpenlletTemporalQuery());
		COMMANDS.put("modularity", new OpenlletModularity());
		COMMANDS.put("trans-tree", new OpenlletTransTree());
		COMMANDS.put("extract", new OpenlletExtractInferences());
		COMMANDS.put("lint", new Openllint());
		COMMANDS.put("info", new OpenlletInfo());
		COMMANDS.put("entail", new OpenlletEntailment());
	}

	public static void main(final String[] args)
	{
		try
		{
			Openllet.run(args);
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

	public static OpenlletCmdApp getCommand(final String name)
	{
		final OpenlletCmdApp cmd = COMMANDS.get(name.toLowerCase());
		if (cmd == null)
			throw new OpenlletCmdException("Unrecognized subcommand: " + name);
		return cmd;
	}

	private static void run(final String[] args)
	{
		if (args.length == 0)
			throw new OpenlletCmdException("Type 'openllet help' for usage.");

		final String arg = args[0];

		if ("h".equals(arg) || "-h".equals(arg) || "help".equals(arg) || "--help".equals(arg))
		{
			if (args.length == 1)
				mainhelp();
			else
			{
				final OpenlletCmdApp cmd = getCommand(args[1]);
				cmd.help();
			}
		}
		else
			if ("--version".equals(arg) || "-V".equals(arg))
				version();
			else
			{
				final OpenlletCmdApp cmd = getCommand(arg);
				cmd.parseArgs(args);
				cmd.run();
				cmd.finish();
			}
	}

	private static void mainhelp()
	{
		final StringBuffer buf = new StringBuffer();
		final String version = getVersionInfo().getVersionString();

		buf.append("Usage: openllet <subcommand> [options] <file URI>...\n");
		buf.append("Openllet command-line client, version " + version + "." + "\n");
		buf.append("Type 'openllet help <subcommand>' for help on a specific subcommand.\n");
		buf.append("\n");
		buf.append("Available subcommands:\n");

		for (final String cmd : COMMANDS.keySet())
		{
			buf.append("\t");
			buf.append(cmd);
			buf.append("\n");
		}

		buf.append("\n");
		buf.append("Openllet is an OWL2 ontology (DL)reasoner.\n");
		buf.append("This is the Openllet-TCQ variant, which is able to check temporal conjunctive queries.\n");
		buf.append("For more information, see https://www.w3.org/TR/owl2-profiles/");

		System.out.println(buf);
		System.exit(0);
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
