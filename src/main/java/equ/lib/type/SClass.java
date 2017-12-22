/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

/**
 * @author ueyudiud
 */
public interface SClass extends STypeProvider
{
	SClassLoader getTypeLoader();
	
	SClass getSupertype();
	
	SType getGenericSupertypes();
	
	default SType[] getGenericInterfaces()
	{
		return STypeHelper.EMPTY_TYPE;
	}
	
	boolean isInterface();
	
	String getDescriptor();
	
	default boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	default SClass getRawClass()
	{
		return this;
	}
}