/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import org.objectweb.asm.Type;

/**
 * @author ueyudiud
 */
public final class STypes
{
	static final SClassLoader LOADER = new BasicTypeLoader();
	
	public static final SClass
	ANY = SSpecialClasses.ANY,
	ANY_REF = SSpecialClasses.ANY_REF,
	ANY_VAL = SSpecialClasses.ANY_VAL,
	NONE = SSpecialClasses.NONE,
	
	BOOL = SPrimitiveClasses.BOOL,
	BYTE = SPrimitiveClasses.BYTE,
	SHORT = SPrimitiveClasses.SHORT,
	INT = SPrimitiveClasses.INT,
	LONG = SPrimitiveClasses.LONG,
	FLOAT = SPrimitiveClasses.FLOAT,
	DOUBLE = SPrimitiveClasses.DOUBLE,
	CHAR = SPrimitiveClasses.CHAR,
	
	UNIT = SVoidClass.INSTANCE,
	
	STRING = SPrimitiveArrayClasses.CHAR_ARRAY,
	
	BLOCK = SBlockClass.INSTANCE;//TODO
	
	public static String getBoxedInternalName(SClass type)
	{
		if (type instanceof SLoadinClass<?>)
		{
			return Type.getInternalName(((SLoadinClass<?>) type).clazz);
		}
		else if (type instanceof SPrimitiveClasses)
		{
			return Type.getInternalName(((SPrimitiveClasses) type).boxType);
		}
		else if (type instanceof SSpecialClasses)
		{
			return "java/lang/Object";
		}
		else
		{
			return type.getName().replace('.', '/');
		}
	}
}
