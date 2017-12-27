/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.parser;

import java.io.IOException;

/**
 * @author ueyudiud
 */
public interface IParsingFactory
{
	IScanner openSource(IScanner source, boolean lib, String code) throws IOException;
	
	void cycledScanner(IScanner scanner);
}
