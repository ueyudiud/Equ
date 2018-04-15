/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public interface IScanner
{
	String path();
	
	ILexeme lexeme();
	
	void next() throws ScannerException;
}
