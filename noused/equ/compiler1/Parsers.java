/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler1;

import java.io.IOException;

/**
 * @author ueyudiud
 */
public class Parsers
{
	public static IScanner simple(IScannerFactory factory, String path) throws IOException
	{
		return new SimpleParser(factory, factory.openSource(null, path));
	}
	
	public static IParser toAST(IScannerFactory factory, String path) throws IOException
	{
		return new CodeParser(factory, factory.openSource(null, path));
	}
}
