/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

import equ.lib.base.SGenericDeclearation;
import equ.util.GS;

/**
 * @author ueyudiud
 */
abstract class AbstractTypeLoader implements SClassLoader
{
	@Override
	public SType loadArrayClass(SType type, int dim)
	{
		switch (dim)
		{
		case 0 :
			return type;
		case 1 :
			if (type instanceof SPrimitiveClasses)
			{
				switch ((SPrimitiveClasses) type)
				{
				case BOOL   : return SPrimitiveArrayClasses.BOOL_ARRAY;
				case BYTE   : return SPrimitiveArrayClasses.BYTE_ARRAY;
				case SHORT  : return SPrimitiveArrayClasses.SHORT_ARRAY;
				case INT    : return SPrimitiveArrayClasses.INT_ARRAY;
				case LONG   : return SPrimitiveArrayClasses.LONG_ARRAY;
				case FLOAT  : return SPrimitiveArrayClasses.FLOAT_ARRAY;
				case DOUBLE : return SPrimitiveArrayClasses.DOUBLE_ARRAY;
				default     : return SPrimitiveArrayClasses.CHAR_ARRAY;
				}
			}
			else if (type == SVoidClass.INSTANCE)
				throw new IllegalArgumentException("The array class type can't be 'void'.");
			else return new SArrayClass(this, type);
		default:
			type = loadArrayClass(type, 1);
			while (--dim > 0)
				type = new SArrayClass(this, type);
			return type;
		}
	}
}

abstract class CLTypeLoader extends AbstractTypeLoader
{
	protected final ClassLoader loader;
	protected Map<Class<?>, SLoadinClass> loadinClasses = new HashMap<>();
	
	CLTypeLoader(ClassLoader loader)
	{
		this.loader = loader;
	}
	
	SType loadClassUnsafe(Class<?> class1)
	{
		if (class1.isArray())
		{
			int i = 1;
			while ((class1 = class1.getComponentType()).isArray())
				++i;
			return loadArrayClass(loadClass(class1), i);
		}
		else if (class1.isPrimitive())
		{
			switch (class1.getName())
			{
			case "boolean" : return SPrimitiveClasses.BOOL;
			case "byte"    : return SPrimitiveClasses.BYTE;
			case "short"   : return SPrimitiveClasses.SHORT;
			case "int"     : return SPrimitiveClasses.INT;
			case "long"    : return SPrimitiveClasses.LONG;
			case "float"   : return SPrimitiveClasses.FLOAT;
			case "double"  : return SPrimitiveClasses.DOUBLE;
			case "char"    : return SPrimitiveClasses.CHAR;
			default        : return SVoidClass.INSTANCE;
			}
		}
		else
		{
			return loadClass(class1);
		}
	}
	
	protected SLoadinClass<?> loadClass(Class<?> class1)
	{
		return this.loadinClasses.computeIfAbsent(class1, c->new SLoadinClass(this, c));
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
			return loadClass(Class.forName(name, false, this.loader));
		}
		catch (ClassNotFoundException exception)
		{
			throw new ClassNotFoundException("The SClass with name '" + name + "' not found.");
		}
	}
	
	protected abstract SClass loadClass$(String name);
	
	SClass getSupertypeOf(SLoadinClass<?> type)
	{
		Class<?> clazz = type.clazz.getSuperclass();
		return clazz == null ? SSpecialClasses.ANY_REF : loadClass(clazz);
	}
	
	SType getTypeOf(Type type)
	{
		if (type instanceof Class<?>)
		{
			return loadClassUnsafe((Class<?>) type);
		}
		else if (type instanceof TypeVariable<?>)
		{
			SGenericDeclearation declearation = getGenericDeclearation(((TypeVariable) type).getGenericDeclaration());
			String name = type.getTypeName();
			for (SType t : declearation.getTypeParameters())
			{
				if (t.getName().equals(name))
					return t;
			}
			throw new IllegalStateException();
		}
		else if (type instanceof GenericArrayType)
		{
			int dim = 1;
			Type t = ((GenericArrayType) type).getGenericComponentType();
			while (t instanceof GenericArrayType)
			{
				t = ((GenericArrayType) t).getGenericComponentType();
				dim ++;
			}
			return loadArrayClass(getTypeOf(t), dim);
		}
		else if (type instanceof ParameterizedType)
		{
			return loadParameterizedType(((ParameterizedType) type).getOwnerType() == null ?
					null : getTypeOf(((ParameterizedType) type).getOwnerType()),
					(STypeProvider) getTypeOf(((ParameterizedType) type).getRawType()),
					GS.transform(((ParameterizedType) type).getActualTypeArguments(), this::getTypeOf, SType.class));
		}
		else if (type instanceof WildcardType)
		{
			return loadWildcardType(
					GS.transform(((WildcardType) type).getUpperBounds(), this::getTypeOf, SType.class),
					((WildcardType) type).getLowerBounds().length == 0 ? SSpecialClasses.NONE :
						getTypeOf(((WildcardType) type).getLowerBounds()[0]));
		}
		else throw new InternalError();
	}
	
	SGenericDeclearation getGenericDeclearation(GenericDeclaration declaration)
	{
		if (declaration instanceof Class<?>)
		{
			return loadClass((Class<?>) declaration);
		}
		else if (declaration instanceof Method)
		{
			SLoadinClass class1 = loadClass(((Executable) declaration).getDeclaringClass());
			return GS.of(class1.getFunctions(), f->f instanceof SLoadinMethod && f.function == declaration);
		}
		else if (declaration instanceof Constructor<?>)
		{
			SLoadinClass class1 = loadClass(((Executable) declaration).getDeclaringClass());
			return GS.of(class1.getConstructors(), f->f instanceof SLoadinConstructor && f.constructor == declaration);
		}
		else throw new InternalError();
	}
}