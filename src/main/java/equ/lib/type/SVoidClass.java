/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import equ.lib.func.SFunc;

/**
 * @author ueyudiud
 */
class SVoidClass implements SPrimitiveClassAbstract
{
	public static final SVoidClass INSTANCE = new SVoidClass();
	
	private SVoidClass()
	{
	}
	
	@Override
	public SClass getSupertype()
	{
		return null;
	}
	
	@Override
	public boolean convert(SType type)
	{
		return type == this;
	}
	
	@Override
	public String getName()
	{
		return "equ.Unit";
	}
	
	@Override
	public String getDescriptor()
	{
		return "V";
	}
	
	@Override
	public SFunc[] getFunctions()
	{
		return EMPTY;
	}
	
	@Override
	public boolean isInterface()
	{
		return false;
	}
	
	@Override
	public boolean isPrimitiveType()
	{
		return false;
	}
}
