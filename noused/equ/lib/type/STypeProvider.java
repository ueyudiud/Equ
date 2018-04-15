/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import java.util.function.Function;

import equ.lib.base.SGenericDeclearation;
import equ.util.GS;

/**
 * @author ueyudiud
 */
public interface STypeProvider extends SType, SGenericDeclearation<SType>
{
	SType provideType(SType...parameters);
	
	boolean convert(SType type);
	
	@Override
	default SType $transformTo(Function<SVariableType<?>, SType> function)
	{
		if (getTypeParameters().length != 0)
			return provideType(GS.transform(getTypeParameters(), function, SType.class));
		else
			return this;
	}
	
	@Override
	default boolean equal(SType declearation)
	{
		return SGenericDeclearation.super.equal(declearation);
	}
}
