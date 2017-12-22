/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.func;

import equ.lib.type.SType;
import equ.lib.type.STypeProvider;
import equ.util.GS;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
public interface STypeMethod extends STypeProvider
{
	@Override
	default String getName()
	{
		SType[] parameters = getParameters();
		SType result = getResultType();
		return (result instanceof STypeMethod || parameters.length != 0 ?
				"(" + Strings.toString(parameters, SType::getName) + ")" + Strings.EMPTY :
				"=>") + result.getName();
	}
	
	@Override
	default SType getSupertype()
	{
		return null;
	}
	
	@Override
	default SType getGenericSupertypes()
	{
		return null;
	}
	
	SType toFunctionalType();
	
	SType[] getParameters();
	
	SType getResultType();
	
	@Override
	default boolean equal(SType type)
	{
		if (type instanceof STypeMethod)
		{
			return GS.orderedAll(getParameters(), ((STypeMethod) type).getParameters(), SType::equal) &&
					getResultType().equal(((STypeMethod) type).getResultType());
		}
		return false;
	}
	
	@Override
	default boolean convert(SType type)
	{
		if (type instanceof STypeMethod)
		{
			return GS.orderedAll(getParameters(), ((STypeMethod) type).getParameters(), SType::convert) &&
					getResultType().convert(((STypeMethod) type).getResultType());
		}
		return false;
	}
}
