/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.base;

import equ.lib.type.SType;
import equ.lib.type.SVariableType;
import equ.util.GS;

/**
 * @author ueyudiud
 */
public interface SGenericDeclearation<D extends SDeclearation> extends SDeclearation<D>
{
	SVariableType<?>[] getTypeParameters();
	
	@Override
	default boolean equal(D declearation)
	{
		return declearation == this;
	}
	
	@Override
	default boolean convert(D declearation)
	{
		return declearation == this;
	}
	
	@Override
	default SType $transformVariableType(SVariableType<?> type)
	{
		if (GS.indexOf(getTypeParameters(), type) != -1)
			return type;
		else
		{
			SDeclearation owner = getOwner();
			return owner != null ? owner.$transformVariableType(type) : type;
		}
	}
}
