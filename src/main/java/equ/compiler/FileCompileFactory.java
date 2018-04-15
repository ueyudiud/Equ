/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import equ.compiler.preprocess.Preprocessor;
import equ.compiler.scanner.Scanner;
import equ.util.IOUtil;

/**
 * @author ueyudiud
 */
public class FileCompileFactory extends AbstractCompileContext implements CompileFactory
{
	@Override
	public CompileContext defaultContext()
	{
		return this;
	}
	
	@Override
	public boolean isOptionEnable(int optionID)
	{
		switch (optionID)
		{
		case UTF8_ESCAPE:
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public void warn(String cause, SourceTrace trace)
	{
		System.err.println("Warn: " + cause);
		trace.print(System.err);
	}
	
	@Override
	public void error(String cause, SourceTrace trace)
	{
		System.err.println("Error: " + cause);
		trace.print(System.err);
	}
	
	@Override
	public void compile(File src, File dest, CompileContext context) throws CompileException
	{
		context.clear();
		dest.mkdirs();
		compileFiles(src, dest, "", context);
	}
	
	private final void compileFiles(File src, File dest, String name, CompileContext context)
	{
		for (File file : src.listFiles())
		{
			if (file.isDirectory())
			{
				File dest1 = new File(dest, file.getName());
				if (dest1.mkdir())
				{
					compileFiles(file, dest1, name + "/" + file.getName(), context);
				}
				else
				{
					throw new CompileException("Fail to create directory at " + dest1);
				}
			}
			else if (file.getName().endsWith(".els"))
			{
				String fn = file.getName();
				fn = fn.substring(0, fn.length() - ".els".length());
				try
				{
					compileFile(IOUtil.read(file), fn, name.length() == 0 ? fn : name + "/" + fn, file.getAbsolutePath(), dest, context);
				}
				catch (IOException exception)
				{
					throw new CompileException("Fail to load source from " + file, exception);
				}
			}
		}
	}
	
	protected void compileFile(char[] src, String name, String path, String source, File dest, CompileContext context) throws CompileException
	{
		Scanner scanner = new Scanner(context, source, src);
		Preprocessor preprocessor = new Preprocessor(context, source, scanner);
		System.out.println("Scanning " + path);
		try
		{
			do
			{
				preprocessor.next();
				ILexeme lexeme = preprocessor.lexeme();
				System.out.println(lexeme);
				lexeme.trace().print(System.out);
				if (lexeme.type() == EnumLexType.END)
				{
					break;
				}
			}
			while (true);
		}
		catch (ScannerException exception)
		{
			error(exception.getLocalizedMessage(), exception.trace);
			CompileException e = new CompileException("Catching an exception during scanning file.");
			e.setStackTrace(exception.getStackTrace());
			throw e;
		}
	}
	
	@Override
	public IScanner openScanner(String src, boolean lib, String path)
	{
		if (!lib && !isBuiltInPath(src))
		{
			File target = new File(new File(src).getParentFile(), path);
			if (target.exists())
			{
				try
				{
					return new Scanner(this, target.getAbsolutePath(), IOUtil.read(target));
				}
				catch (IOException exception)
				{
					throw new CompileException("Fail to open file " + target, exception);
				}
			}
		}
		throw new CompileException("Source " + path + " not found.");
	}
	
	private boolean isBuiltInPath(String path)
	{
		return path.charAt(0) == '<' && path.charAt(path.length() - 1) == '>';
	}
	
	@Override
	public void eval(Reader reader) throws IOException, CompileException
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void eval(String code) throws CompileException
	{
		throw new UnsupportedOperationException();
	}
}
