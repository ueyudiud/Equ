/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public interface IScannerFactory
{
	IScanner openSource(IScanner scanner, String path);
	
	void closeScanner(IScanner scanner);
	
	void warn(IScanner scanner, String cause, Object... formats);
	
	void error(IScanner scanner, String cause, Object... formats);
}
