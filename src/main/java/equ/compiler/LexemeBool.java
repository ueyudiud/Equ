/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class LexemeBool extends LexemeAbstract
{
	private final boolean flag;
	
	public LexemeBool(boolean flag, SourceTrace trace)
	{
		super(trace);
		this.flag = flag;
	}
	
	@Override
	public EnumLexType type()
	{
		return EnumLexType.LIB;
	}
	
	@Override
	public String stringValue()
	{
		return Boolean.toString(flag);
	}
	
	@Override
	public boolean boolValue()
	{
		return flag;
	}
}
