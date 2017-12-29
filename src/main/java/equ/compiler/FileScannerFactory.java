/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import equ.util.IOUtil;

/**
 * @author ueyudiud
 */
class FileScannerFactory implements IScannerFactory
{
	private final File root;
	private final Map<IScanner, File> scanners = new HashMap<>();
	private final Map<IScanner, List<Error>> errors = new HashMap<>();
	
	static class Error
	{
		boolean errored;
		SourcePosition position;
		String cause;
		Object[] formats;
		
		Error(boolean errored, SourcePosition position, String cause, Object[] fomats)
		{
			this.errored = errored;
			this.position = position;
			this.cause = cause;
			this.formats = fomats;
		}
	}
	
	FileScannerFactory(File file)
	{
		this.root = file;
	}
	
	@Override
	public IScanner openSource(IScanner scanner, String path) throws IOException
	{
		File file = new File(this.scanners.getOrDefault(scanner, this.root), path);
		char[] codes = IOUtil.readFully(new FileReader(file));
		IScanner result = new FileScanner(file.getAbsolutePath(), codes, true, this);
		this.scanners.put(result, file);
		return result;
	}
	
	@Override
	public void closeScanner(IScanner scanner)
	{
		this.scanners.remove(scanner);
		List<Error> list = this.errors.remove(scanner);
		if (list != null)
		{
			System.out.println("caught.during.lexing");
			for (Error error : list)
			{
				System.out.println(String.format(error.cause, error.formats));
				System.out.println(error.position.getMessage());
				error.position.printCode(System.out);
			}
		}
	}
	
	private static List<Error> newlist(IScanner scanner)
	{
		return new ArrayList<>();
	}
	
	@Override
	public void warn(IScanner scanner, String cause, Object... formats)
	{
		this.errors.computeIfAbsent(scanner, FileScannerFactory::newlist)
		.add(new Error(false, scanner.current(), cause, formats));
	}
	
	@Override
	public void error(IScanner scanner, String cause, Object... formats)
	{
		this.errors.computeIfAbsent(scanner, FileScannerFactory::newlist)
		.add(new Error(true, scanner.current(), cause, formats));
	}
}
