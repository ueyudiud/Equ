/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler1;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author ueyudiud
 */
class MemoryScannerFactory implements IScannerFactory
{
	private final Map<String, Supplier<IScanner>> scanners;
	
	MemoryScannerFactory(Map<String, Supplier<IScanner>> scanners)
	{
		this.scanners = scanners;
	}
	
	@Override
	public IScanner openSource(IScanner _0, String path) throws IOException
	{
		Supplier<IScanner> supplier = this.scanners.get(path);
		if (supplier == null)
			throw new IOException(path + " not found.");
		return supplier.get();
	}
	
	@Override
	public void closeScanner(IScanner scanner)
	{
		//Do nothing.
	}
	
	@Override
	public void warn(IScanner scanner, String cause, Object... formats)
	{
		System.out.println(String.format(cause, formats));
	}
	
	@Override
	public void error(IScanner scanner, String cause, Object... formats)
	{
		System.out.println(String.format(cause, formats));
	}
}
