/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import java.util.function.Function;

import equ.lib.base.SDeclearation;
import equ.lib.func.SFunc;

/**
 * @author ueyudiud
 */
public interface SType extends SDeclearation<SType>
{
	/**
	 * 获得类加载器，可能为null
	 * @return
	 */
	SClassLoader getTypeLoader();
	
	String getName();
	
	SType getRawClass();
	
	SType getSupertype();
	
	SType getGenericSupertypes();
	
	SType[] getMemberTypes();
	
	/**
	 * 相当于'≡'算符.
	 * @param type
	 * @return
	 */
	default boolean equal(SType type)
	{
		return equals(type);
	}
	
	/**
	 * 相当于'<:'算符.
	 * @param type
	 * @return
	 */
	boolean convert(SType type);
	
	/**
	 * 相当于'>:'算符.
	 * @param type
	 * @return
	 */
	default boolean convertFrom(SType type)
	{
		return type.convert(this);
	}
	
	/**
	 * 相当于'?='算符.
	 * @param type
	 * @return
	 */
	default boolean cast(SType type)
	{
		return equal(type);
	}
	
	SFunc[] EMPTY = new SFunc[0];
	
	SFunc[] getFunctions();
	
	SFunc[] getConstructors();
	
	SType $transformTo(Function<SVariableType<?>, SType> function);
}
