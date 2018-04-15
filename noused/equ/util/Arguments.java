/*
 * copyright© 2017 ueyudiud
 */
package equ.util;

import java.util.function.Supplier;

/**
 * @author ueyudiud
 */
public class Arguments
{
	public static void checkLength(int size, Object[] parameters, String message) throws IllegalArgumentException
	{
		if (parameters.length != size)
			throw new IllegalArgumentException(message);
	}
	public static <X extends Throwable> void checkLength(int size, Object[] parameters, Supplier<X> supplier) throws X
	{
		if (parameters.length != size)
			throw supplier.get();
	}
}
