/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class MacroTransformException extends CompileException
{
	private static final long serialVersionUID = 5386579250968787312L;
	
	public MacroTransformException(String cause)
	{
		super(cause);
	}
	
	public MacroTransformException(String cause, Throwable t)
	{
		super(cause, t);
	}
	
}
