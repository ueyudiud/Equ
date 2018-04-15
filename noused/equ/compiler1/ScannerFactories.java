/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler1;

import java.io.File;

/**
 * @author ueyudiud
 */
public final class ScannerFactories
{
	public static IScannerFactory fileFactory(String file)
	{
		return fileFactory(new File(file));
	}
	
	public static IScannerFactory fileFactory(File file)
	{
		return new FileScannerFactory(file);
	}
}
