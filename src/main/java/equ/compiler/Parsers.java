/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

import java.io.IOException;

/**
 * @author ueyudiud
 */
public class Parsers
{
	public static Parser simple(IScannerFactory factory, String path) throws IOException
	{
		return new SimpleParser(factory, factory.openSource(null, path));
	}
}
