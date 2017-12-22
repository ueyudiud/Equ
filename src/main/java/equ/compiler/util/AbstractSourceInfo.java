/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.util;

/**
 * @author ueyudiud
 */
public abstract class AbstractSourceInfo implements ISourceInfo
{
	protected String source = null;
	protected String line = null;
	protected int ln = 0;
	protected int col = 0;
	
	protected void setSource(ISourceInfoProvider provider)
	{
		setSource(provider.applyInfo());
	}
	
	protected void setSource(ISourceInfo info)
	{
		this.source = info.source();
		this.line = info.line();
		this.ln = info.ln();
		this.col = info.col();
	}
	
	@Override public String source() { return this.source; }
	@Override public String line() { return this.line; }
	@Override public int ln() { return this.ln; }
	@Override public int col() { return this.col; }
}
