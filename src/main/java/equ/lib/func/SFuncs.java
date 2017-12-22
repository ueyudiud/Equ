/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.func;

import equ.lib.func.builtin.SFuncsControl;
import equ.lib.type.SClassLoader;
import equ.lib.type.SType;
import equ.lib.type.SVariableType;

/**
 * @author ueyudiud
 */
public class SFuncs
{
	public static final SFunc
	IF = SFuncsControl.IF,
	WHILE = SFuncsControl.WHILE;
	
	public static SParameterizedFunc getParameterizedFunc(SType owner, SGenericFunc function, SType...typeParameters)
	{
		return new SParameterizedFuncImpl(owner, function, typeParameters);
	}
	
	public static STypeMethod toMethodType(SClassLoader loader, SType[][] arguments, SType result, SVariableType<?>[] variables)
	{
		switch (arguments.length)
		{
		case 0 : return new STypeMethodImpl(loader, new SType[0], result, variables);
		case 1 : return new STypeMethodImpl(loader, arguments[0],
				new STypeMethodImpl(loader, new SType[0], result, variables), variables);
		default:
			STypeMethod type = new STypeMethodImpl(loader, new SType[0], result, variables);
			for (int i = arguments.length - 1; i >= 0; i--)
			{
				type = new STypeMethodImpl(loader, arguments[i], type, variables);
			}
			return type;
		}
	}
}
