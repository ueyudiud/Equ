/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.lexer1;

import equ.compiler.util.ISourceInfo;

/**
 * @author ueyudiud
 */
public class LexeralizeException extends RuntimeException
{
	private static final long serialVersionUID = -4044069008838666892L;
	
	private ISourceInfo info;
	
	public LexeralizeException(String msg, ISourceInfo info)
	{
		super(msg);
		this.info = info;
	}
	public LexeralizeException(Throwable cause, ISourceInfo info)
	{
		super(cause);
		this.info = info;
	}
	
	@Override
	public String getMessage()
	{
		return super.getMessage() + "\r"
				+ "at file " + this.info.source()
				+ " ln: " + this.info.ln()
				+ " col: " + this.info.col()
				+ "\r" + this.info.toSourceInfo();
	}
}
