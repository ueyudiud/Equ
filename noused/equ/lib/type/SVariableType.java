/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import java.util.function.Function;

import equ.lib.base.SDeclearation;
import equ.lib.base.SGenericDeclearation;
import equ.util.GS;

/**
 * @author ueyudiud
 */
public interface SVariableType<D extends SGenericDeclearation> extends SType
{
	EnumVariableType getVariableType();
	
	@Override
	default SType getRawClass()
	{
		return null;
	}
	
	@Override
	default SDeclearation getOwner()
	{
		return null;
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
	
	D getGenericDeclaration();
	
	SType[] getBounds();
	
	@Override
	default boolean equal(SType type)
	{
		return type == this;
	}
	
	@Override
	default boolean convert(SType type)
	{
		return cast(type);
	}
	
	@Override
	default boolean convertFrom(SType type)
	{
		return type == this || type == SSpecialClasses.NONE || GS.any(getBounds(), type::convertFrom) || type.convert(this);
	}
	
	@Override
	default boolean cast(SType type)
	{
		return type == this || type == SSpecialClasses.ANY || GS.all(getBounds(), type::convert);
	}
	
	@Override
	default SType $transformTo(Function<SVariableType<?>, SType> function)
	{
		return function.apply(this);
	}
}
