/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

import java.io.PrintStream;

/**
 * @author ueyudiud
 */
public class ScannerException extends Exception
{
	private static final long serialVersionUID = -8084160121076501858L;
	
	public final SourceTrace trace;
	
	public ScannerException(String cause, SourceTrace trace)
	{
		super(cause);
		this.trace = trace;
	}
	
	@Override
	public void printStackTrace(PrintStream s)
	{
		super.printStackTrace(s);
		trace.print(s);
	}
}
