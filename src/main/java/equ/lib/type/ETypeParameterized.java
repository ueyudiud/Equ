/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.type;

import java.util.function.Function;

/**
 * @author ueyudiud
 */
public interface ETypeParameterized extends ETypeExtendable
{
	EClass rawType();
	
	ETypeConstructor constructor();
	
	EType[] parameters();
}

class ETypeParameterized_ implements ETypeParameterized
{
	ETypeConstructor constructor;
	EType[] parameters;
	
	private EType zuper;
	
	ETypeParameterized_(ETypeConstructor constructor, EType...parameters)
	{
		this.constructor = constructor;
		this.parameters = parameters;
	}
	
	@Override
	public ETypeExtendable zuper()
	{
		if (zuper == null)
		{
			zuper = constructor.owner().zuper();
			if (zuper instanceof ETypeConstructor)
			{
				
			}
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ETypeExtendable[] interfaces()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getName()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(constructor);
		builder.append("<|").append(parameters[0]);
		for (int i = 1; i < parameters.length; ++i)
		{
			builder.append(", ").append(parameters[i]);
		}
		builder.append("|>");
		return builder.toString();
	}
	
	@Override
	public EClass rawType()
	{
		return constructor.owner().rawType();
	}
	
	@Override
	public ETypeConstructor constructor()
	{
		return constructor;
	}
	
	@Override
	public EType[] parameters()
	{
		return parameters.clone();
	}
	
	@Override
	public EType map(Function<EType, EType> mapping)
	{
		EType constructor = mapping.apply(this.constructor);
		EType[] types = new EType[parameters.length];
		for (int i = 0; i < types.length; ++i)
		{
			types[i] = mapping.apply(parameters[i]);
		}
		return new ETypeParameterized_((ETypeConstructor) constructor, types);
	}
}
