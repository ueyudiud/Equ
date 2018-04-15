/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.base;

import java.util.function.Function;

import equ.lib.type.SType;
import equ.lib.type.SVariableType;

/**
 * @author ueyudiud
 */
public interface SDeclearation<D extends SDeclearation>
{
	String getName();
	
	SDeclearation getOwner();
	
	boolean equal(D declearation);
	
	boolean convert(D declearation);
	
	default SType $transformVariableType(SVariableType<?> type)
	{
		return type;
	}
	
	default D $transformTo(Function<SVariableType<?>, SType> function)
	{
		return (D) this;
	}
}
