/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * @author ueyudiud
 */
public final class STypeLoaders
{
	public static SClassLoader simple(boolean initalize)
	{
		return simple(MethodHandles.lookup().lookupClass().getClassLoader(), initalize);
	}
	
	public static SClassLoader simple(@Nonnull ClassLoader loader, boolean initalize)
	{
		return new SimpleTypeLoader(Objects.requireNonNull(loader), initalize);
	}
	
	private STypeLoaders() {}
}
