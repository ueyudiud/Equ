/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import java.util.function.Function;

import equ.lib.base.SDeclearation;
import equ.lib.base.SParameterizedDeclearation;
import equ.util.GS;

/**
 * @author ueyudiud
 */
public interface SParameterizedType extends SType, SParameterizedDeclearation<STypeProvider, SType>
{
	@Override
	default STypeProvider getRawClass()
	{
		return getRawDeclearation();
	}
	
	@Override
	default boolean equal(SType type)
	{
		if (type == this)
			return true;
		else if (type instanceof SParameterizedType)
		{
			SParameterizedType type2 = (SParameterizedType) type;
			return getRawClass().equal(type2.getRawClass()) &&
					(getGenericOwner() == null || getGenericOwner().equal(type2.getRawDeclearation())) &&
					GS.orderedAll(getActualTypeArguments(), type2.getActualTypeArguments(), SDeclearation::equal);
		}
		else
			return false;
	}
	
	@Override
	default boolean convert(SType type)
	{
		if (type == this || getRawClass().equal(type) || type == SSpecialClasses.ANY)
			return true;
		else if (type instanceof SParameterizedType)
		{
			SParameterizedType type2 = (SParameterizedType) type;
			return getRawClass().convert(type2.getRawClass()) &&
					(getGenericOwner() == null || getGenericOwner().convert(type2.getRawDeclearation())) &&
					$checkTypeArguments(type2.getActualTypeArguments());
		}
		else if (type instanceof SWildcardType)
			return type.convertFrom(this);
		else
			return false;
	}
	
	@Override
	default boolean convertFrom(SType type)
	{
		return type == SSpecialClasses.NONE || SType.super.convertFrom(type);
	}
	
	@Override
	default SType $transformTo(Function<SVariableType<?>, SType> function)
	{
		return getTypeLoader().loadParameterizedType(getOwner() == null ? null :
			getOwner().$transformTo(function), getRawClass(),
			STypeHelper.transform(getActualTypeArguments(), function));
	}
	
	@Override
	default SType $transformVariableType(SVariableType<?> type)
	{
		return SParameterizedDeclearation.super.$transformVariableType(type);
	}
}
