/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class CompileException extends RuntimeException
{
	private static final long serialVersionUID = 4209276385956868587L;
	
	public CompileException(String cause)
	{
		super(cause);
	}
	public CompileException(String cause, Throwable t)
	{
		super(cause, t);
	}
}
