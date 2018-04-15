/*
 * copyright© 2017 ueyudiud
 */
package equ.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @author ueyudiud
 */
public class EquDynamicFactory
{
	public static CallSite applyMethod(MethodHandles.Lookup lookup,
			String name, MethodType parameters, Class<?> owner)
					throws IllegalAccessException, NoSuchMethodException
	{
		return new ConstantCallSite(lookup.findVirtual(owner, name, parameters));
	}
}
