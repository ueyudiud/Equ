/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.func;

import java.util.function.Function;

import com.google.common.collect.ObjectArrays;

import equ.lib.base.SDeclearation;
import equ.lib.type.SClass;
import equ.lib.type.SClassLoader;
import equ.lib.type.SType;
import equ.lib.type.SVariableType;
import equ.util.Arguments;
import equ.util.GS;

/**
 * @author ueyudiud
 */
class STypeMethodImpl implements STypeMethod
{
	final SClassLoader loader;
	final SType[] parameters;
	final SType result;
	
	final SVariableType<?>[] variables;
	
	STypeMethodImpl(SClassLoader loader, SType[] parameters, SType result, SVariableType<?>[] variables)
	{
		this.loader = loader;
		this.parameters = parameters;
		this.result = result;
		this.variables = variables;
	}
	
	@Override
	public SClassLoader getTypeLoader()
	{
		return this.loader;
	}
	
	@Override
	public SType getRawClass()
	{
		return this;
	}
	
	@Override
	public SType[] getMemberTypes()
	{
		return new SType[0];
	}
	
	@Override
	public SFunc[] getFunctions()
	{
		return EMPTY;
	}
	
	@Override
	public SFunc[] getConstructors()
	{
		return EMPTY;
	}
	
	@Override
	public SType $transformTo(Function<SVariableType<?>, SType> function)
	{
		Arguments.checkLength(this.variables.length, this.parameters, "Illegal type variables count.");
		
		if (this.parameters.length == 0)
			return this;
		
		return new STypeMethodImpl(this.loader,
				GS.transform(this.parameters, t->t.$transformTo(function), SType.class),
				this.result.$transformTo(function), new SVariableType[0]);
	}
	
	@Override
	public SDeclearation getOwner()
	{
		return null;
	}
	
	@Override
	public SType toFunctionalType()
	{
		try
		{
			SClass clazz = this.loader.loadClass("Function" + this.parameters.length);
			return this.loader.loadParameterizedType(null, clazz, ObjectArrays.concat(this.parameters, this.result));
		}
		catch (ClassNotFoundException exception)
		{
			throw new IllegalArgumentException("Too many arguments to transform to functional type.", exception);
		}
	}
	
	@Override
	public SType[] getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public SType getResultType()
	{
		return this.result;
	}
	
	@Override
	public SType provideType(SType... parameters)
	{
		Arguments.checkLength(this.variables.length, parameters, "Illegal type variables count.");
		
		if (parameters.length == 0)
			return this;
		
		return $transformTo(t -> {
			int idx = GS.lastIndexOf(this.parameters, t);
			if (idx != -1)
				return parameters[idx];
			return t;
		});
	}
	
	@Override
	public SVariableType<?>[] getTypeParameters()
	{
		return this.variables;
	}
}
