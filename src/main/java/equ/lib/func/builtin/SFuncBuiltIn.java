/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.func.builtin;

import equ.lib.base.SMethodWriter;
import equ.lib.func.SFunc;
import equ.lib.func.SFuncs;
import equ.lib.func.STypeMethod;
import equ.lib.type.SType;
import equ.lib.type.STypes;
import equ.lib.type.SVariableType;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
class SFuncBuiltIn1 implements SFunc
{
	final String name;
	final SType owner;
	final SType result;
	final SType[][] parameters;
	final SMethodWriter writer;
	
	SFuncBuiltIn1(String name, SMethodWriter writer, SType owner, SType result, SType...parameters)
	{
		this.name = name;
		this.writer = writer;
		this.owner = owner;
		this.result = result;
		this.parameters = new SType[][] {parameters};
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public boolean equal(SFunc func)
	{
		return this == func;
	}
	
	@Override
	public boolean convert(SFunc func)
	{
		return this == func;
	}
	
	@Override
	public SType getOwner()
	{
		return this.owner;
	}
	
	@Override
	public STypeMethod getMethodType()
	{
		return SFuncs.toMethodType(this.owner.getTypeLoader(), this.parameters, this.result, new SVariableType[0]);
	}
	
	@Override
	public SType[] getParameters()
	{
		return this.parameters[0];
	}
	
	@Override
	public SType getResult()
	{
		return this.result;
	}
	
	@Override
	public SMethodWriter writer()
	{
		return this.writer;
	}
	
	@Override
	public String toString()
	{
		return "Bulitin {" + this.result.getName() + " " + this.name + "(" + Strings.toString(this.parameters[0], SType::getName) + ") }";
	}
}

abstract interface SFuncStruct extends SFunc
{
	@Override
	default boolean convert(SFunc func)
	{
		return func == this;
	}
	
	@Override
	default boolean equal(SFunc func)
	{
		return func == this;
	}
	
	@Override
	default SType getOwner()
	{
		return STypes.ANY;
	}
	
	@Override
	default SType getResult()
	{
		return STypes.UNIT;
	}
}
