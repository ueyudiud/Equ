/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.func;

import equ.lib.base.SMethodWriter;
import equ.lib.type.SType;
import equ.util.GS;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
class SParameterizedFuncImpl implements SParameterizedFunc
{
	final SType owner;
	final SGenericFunc parent;
	final SType[] typeParameters;
	
	SParameterizedFuncImpl(SType owner, SGenericFunc parent, SType[] types)
	{
		this.owner = owner;
		this.parent = parent;
		this.typeParameters = types;
	}
	
	@Override
	public SType getOwner()
	{
		return this.parent.getOwner();
	}
	
	@Override
	public STypeMethod getMethodType()
	{
		return null;//TODO
	}
	
	@Override
	public String getName()
	{
		return this.parent.getName();
	}
	
	private SType result;
	private SType[] parameters;
	
	@Override
	public SType getResult()
	{
		if (this.result == null)
		{
			this.result = this.parent.getResult().$transformTo(this::$transformVariableType);
		}
		return this.result;
	}
	
	@Override
	public SType[] getParameters()
	{
		if (this.parameters == null)
		{
			this.parameters = GS.transform(this.parent.getParameters(), t->t.$transformTo(this::$transformVariableType), SType.class);
		}
		return this.parameters;
	}
	
	@Override
	public SType getGenericOwner()
	{
		return this.owner;
	}
	
	@Override
	public SGenericFunc getRawDeclearation()
	{
		return this.parent;
	}
	
	@Override
	public SType[] getActualTypeArguments()
	{
		return this.typeParameters;
	}
	
	@Override
	public SMethodWriter writer()
	{
		return this.parent.writer();
	}
	
	@Override
	public String toString()
	{
		return "Loadin { " + getResult().getName() + " " + getName() + "(" + Strings.toString(getParameters(), SType::getName)  + ") }";
	}
}
