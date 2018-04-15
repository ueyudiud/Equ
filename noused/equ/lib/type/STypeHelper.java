/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import java.util.function.Function;

import equ.util.GS;

/**
 * @author ueyudiud
 */
class STypeHelper
{
	static final SType[] EMPTY_TYPE = new SType[0];
	
	static SType[] transform(SType[] type, Function<SVariableType<?>, SType> function)
	{
		return GS.transform(type, t->t.$transformTo(function), SType.class);
	}
	
	static SType transform(SType type, Function<SVariableType<?>, SType> function)
	{
		return type == null ? null : type.$transformTo(function);
	}
}
