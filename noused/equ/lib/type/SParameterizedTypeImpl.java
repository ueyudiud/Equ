/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import java.util.function.Function;

import equ.lib.base.SDeclearation;
import equ.lib.base.SParameterizedDeclearation;
import equ.lib.func.SFunc;
import equ.lib.func.SFuncs;
import equ.lib.func.SGenericFunc;
import equ.lib.func.SParameterizedFunc;
import equ.util.GS;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
class SParameterizedTypeImpl implements SParameterizedType
{
	final SClassLoader loader;
	final SDeclearation owner;
	final STypeProvider rawType;
	final SType[] parameters;
	
	SParameterizedTypeImpl(SClassLoader loader, SDeclearation owner, STypeProvider rawType, SType[] parameters)
	{
		this.loader = loader;
		this.owner = owner;
		this.rawType = rawType;
		this.parameters = parameters;
	}
	
	@Override
	public SClassLoader getTypeLoader()
	{
		return this.loader;
	}
	
	@Override
	public String getName()
	{
		return (this.owner != null ? this.owner.getName() + "." : "") + this.rawType.getName() + "<" + Strings.toString(this.parameters, SType::getName) + ">";
	}
	
	@Override
	public SType getSupertype()
	{
		return this.rawType.getSupertype();
	}
	
	private SType genericSuperType;
	private SType[] members;
	
	@Override
	public SType getGenericSupertypes()
	{
		if (this.genericSuperType == null)
		{
			SType type = this.rawType.getGenericSupertypes();
			if (type instanceof SClass)
			{
				this.genericSuperType = this.loader.loadParameterizedType(this.owner, (SClass) type, STypeHelper.EMPTY_TYPE);
			}
			else
			{
				SParameterizedType type1 = (SParameterizedType) type;
				return this.loader.loadParameterizedType(this.owner, type1.getRawClass(),
						STypeHelper.transform(type1.getActualTypeArguments(), this::$transformVariableType));
			}
		}
		return this.genericSuperType;
	}
	
	@Override
	public SType[] getMemberTypes()
	{
		if (this.members == null)
		{
			this.members = STypeHelper.transform(this.rawType.getMemberTypes(), this::$transformVariableType);
		}
		return this.members;
	}
	
	private SFunc[] functions;
	private SFunc[] constructors;
	
	@Override
	public SFunc[] getFunctions()
	{
		if (this.functions == null)
		{
			this.functions = GS.transform(this.rawType.getFunctions(), this::$transformFuncWithVariable, SFunc.class);
		}
		return this.functions;
	}
	
	@Override
	public SFunc[] getConstructors()
	{
		if (this.constructors == null)
		{
			this.constructors = GS.transform(this.rawType.getConstructors(), this::$transformFuncWithVariable, SFunc.class);
		}
		return this.constructors;
	}
	
	private SFunc $transformFuncWithVariable(SFunc func)
	{
		if (func instanceof SParameterizedFunc)
		{
			return SFuncs.getParameterizedFunc(this, ((SParameterizedFunc) func).getRawDeclearation(),
					STypeHelper.transform(((SParameterizedFunc) func).getActualTypeArguments(), this::$transformVariableType));
		}
		else if (func instanceof SGenericFunc)
		{
			return SFuncs.getParameterizedFunc(this, (SGenericFunc) func,
					STypeHelper.transform(((SGenericFunc) func).getTypeParameters(), this::$transformVariableType));
		}
		else return func;
	}
	
	@Override
	public SDeclearation getOwner()
	{
		return this.owner instanceof SParameterizedDeclearation<?, ?> ?
				((SParameterizedDeclearation<?, ?>) this.owner).getRawDeclearation() : this.owner;
	}
	
	@Override
	public SDeclearation getGenericOwner()
	{
		return this.owner;
	}
	
	@Override
	public STypeProvider getRawDeclearation()
	{
		return this.rawType;
	}
	
	@Override
	public SType[] getActualTypeArguments()
	{
		return this.parameters;
	}
	
	@Override
	public SType $transformTo(Function<SVariableType<?>, SType> function)
	{
		return this.loader.loadParameterizedType(this.owner == null ? null :
			this.owner.$transformTo(function), this.rawType, STypeHelper.transform(this.parameters, function));
	}
	
	@Override
	public String toString()
	{
		return "Parameterized { " + getName() + " }";
	}
}
