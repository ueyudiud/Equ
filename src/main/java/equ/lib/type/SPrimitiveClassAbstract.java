/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import equ.lib.base.SDeclearation;
import equ.lib.func.SFunc;

/**
 * @author ueyudiud
 */
abstract interface SPrimitiveClassAbstract extends SClass
{
	SVariableType<?>[] NO_VARIABLE_TYPES = new SVariableType[0];
	
	@Override
	default SType provideType(SType... parameters)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	default SType[] getMemberTypes()
	{
		return STypeHelper.EMPTY_TYPE;
	}
	
	@Override
	default SFunc[] getConstructors()
	{
		return EMPTY;
	}
	
	@Override
	default SDeclearation getOwner()
	{
		return null;
	}
	
	@Override
	default SVariableType<?>[] getTypeParameters()
	{
		return NO_VARIABLE_TYPES;
	}
	
	@Override
	default SClassLoader getTypeLoader()
	{
		return null;
	}
	
	@Override
	default boolean isPrimitiveType()
	{
		return true;
	}
	
	@Override
	default SType getGenericSupertypes()
	{
		return getSupertype();
	}
}
