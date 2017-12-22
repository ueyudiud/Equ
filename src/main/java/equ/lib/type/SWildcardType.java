/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import java.util.function.Function;

import equ.util.GS;

/**
 * @author ueyudiud
 */
public interface SWildcardType extends SType
{
	SType[] getUpperBounds();
	
	SType getLowerBounds();
	
	@Override
	default boolean convert(SType type)
	{
		return type == this || type == SSpecialClasses.ANY || GS.all(getUpperBounds(), type::convertFrom);
	}
	
	@Override
	default boolean convertFrom(SType type)
	{
		if (type == this || type == SSpecialClasses.NONE)
			return true;
		if (type instanceof SWildcardType)
		{
			SType[] upperBounds1 = getUpperBounds();
			SType[] upperBounds2 = ((SWildcardType) type).getUpperBounds();
			SType lowerBounds1 = getLowerBounds();
			SType lowerBounds2 = ((SWildcardType) type).getLowerBounds();
			return GS.all(upperBounds2, t1->GS.any(upperBounds1, t1::convert)) &&
					lowerBounds1.convert(lowerBounds2);
		}
		return GS.all(getUpperBounds(), type::convert) && type.convert(getLowerBounds());
	}
	
	@Override
	default boolean cast(SType type)
	{
		return convertFrom(type);
	}
	
	@Override
	default SType $transformTo(Function<SVariableType<?>, SType> function)
	{
		return getTypeLoader().loadWildcardType(
				STypeHelper.transform(getUpperBounds(), function),
				STypeHelper.transform(getLowerBounds(), function));
	}
}
