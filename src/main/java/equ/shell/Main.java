/*
 * copyright© 2017 ueyudiud
 */
package equ.shell;

import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import equ.util.Strings;

/**
 * @author ueyudiud
 */
public class Main
{
	private static final String VERSION = "@VERSION@";
	
	private static final Options OPTIONS;
	
	static
	{
		OPTIONS = new Options();
		OPTIONS.addOption("h", "help", false, "获得Equ Shell使用帮助");
		OPTIONS.addOption("e", "exit", false, "退出Equ Shell");
		OPTIONS.addOption("v", "version", false, "获得Equ Shell版本号");
	}
	
	public static void main(String[] args)
	{
		if (args.length != 0)
		{
			parseValues(args);
		}
		else
		{
			try (Scanner scanner = new Scanner(System.in))
			{
				System.out.println("欢迎使用Equ Shell，输入--help查看帮助。");
				while (true)
				{
					String line = scanner.nextLine();
					parseValues(Strings.split(line, ' '));
				}
			}
		}
	}
	
	private static void parseValues(String[] args)
	{
		try
		{
			CommandLine line = new DefaultParser().parse(OPTIONS, args);
			if (line.hasOption('e'))
			{
				System.exit(0);
				return;
			}
			if (line.hasOption('h'))
			{
				OPTIONS.getOptions().forEach(o->System.out.println(
						"--" + o.getLongOpt() + "\t" + "-" + o.getOpt() + "\t" + o.getDescription()));
				return;
			}
			if (line.hasOption("version"))
			{
				System.out.println(VERSION);
				return;
			}
			throw new ParseException("");
		}
		catch (ParseException exception)
		{
			System.out.println("未知命令，输入help寻找帮助。");
		}
	}
}
