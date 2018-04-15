/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
abstract class LexemeUTF8 extends LexemeAbstract
{
	protected final char[] source;
	
	public LexemeUTF8(char[] source, SourceTrace trace)
	{
		super(trace);
		this.source = source;
	}
	
	@Override
	public String stringValue()
	{
		return new String(source);
	}
	
	@Override
	public int charsLength()
	{
		return source.length;
	}
	
	@Override
	public char[] charsValue()
	{
		return source.clone();
	}
	
	@Override
	public void copyValue(char[] desc, int descPos)
	{
		System.arraycopy(source, 0, desc, descPos, source.length);
	}
}
