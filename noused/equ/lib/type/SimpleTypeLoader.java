/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import equ.lib.base.SDeclearation;
import equ.util.Arguments;

/**
 * @author ueyudiud
 */
class SimpleTypeLoader extends CLTypeLoader
{
	private final boolean initalize;
	
	{
		this.loadinClasses.put(Object.class, new SLoadinClass<>(this, Object.class));
	}
	
	SimpleTypeLoader(ClassLoader loader, boolean initalize)
	{
		super(loader);
		this.initalize = initalize;
	}
	
	@Override
	public SClass loadClass(String name) throws ClassNotFoundException
	{
		SClass class1 = loadClass$(name);
		if (class1 != null)
		{
			return class1;
		}
		try
		{
			return loadClass(Class.forName(name, this.initalize, this.loader));
		}
		catch (ClassNotFoundException exception)
		{
			throw new ClassNotFoundException("The SClass with name '" + name + "' not found.");
		}
	}
	
	@Override
	protected SClass loadClass$(String name)
	{
		return null;
	}
	
	@Override
	public SParameterizedType loadParameterizedType(SDeclearation owner, STypeProvider base, SType... parameters)
	{
		if (base == SPrimitiveArrayClasses.ARRAY)
		{
			Arguments.checkLength(1, parameters, "Illegal parameter count.");
			return new SArrayClass(this, parameters[0]);
		}
		return new SParameterizedTypeImpl(this, owner, base, parameters);
	}
	
	@Override
	public SWildcardType loadWildcardType(SType[] upperBound, SType lowerBound)
	{
		return new SWildcardTypeImpl(this, upperBound, lowerBound);
	}
}
