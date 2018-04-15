/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

import equ.util.Numbers;

/**
 * @author ueyudiud
 */
public class LexemeFloat extends LexemeAbstract
{
	private final byte[] arg;
	private final String name;
	
	public LexemeFloat(byte[] arg, SourceTrace trace)
	{
		super(trace);
		this.arg = arg;
		name = new String(Numbers.toString(arg));
	}
	
	@Override
	public EnumLexType type()
	{
		return EnumLexType.LIF;
	}
	
	@Override
	public String stringValue()
	{
		return name;
	}
	
	@Override
	public double doubleValue()
	{
		return Numbers.parseDouble(arg);
	}
	
	@Override
	public float floatValue()
	{
		return (float) doubleValue();
	}
	
	@Override
	public boolean boolValue()
	{
		return doubleValue() != 0;
	}
}
