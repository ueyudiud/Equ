/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import static equ.lib.func.builtin.SFuncsPrimitive.ofBool;
import static equ.lib.func.builtin.SFuncsPrimitive.ofFloat;
import static equ.lib.func.builtin.SFuncsPrimitive.ofInteger;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import equ.lib.func.SFunc;
import equ.util.Spliterators1;

/**
 * @author ueyudiud
 */
enum SPrimitiveClasses implements SPrimitiveClassAbstract
{
	BYTE(byte.class, Byte.class, "equ.Byte", "B", () -> ofInteger(0)),
	SHORT(short.class, Short.class, "equ.Short", "S", () -> ofInteger(1)),
	CHAR(char.class, Character.class, "equ.Char", "C", () -> ofInteger(2)),
	INT(int.class, Integer.class, "equ.Int", "I", () -> ofInteger(3)),
	LONG(long.class, Long.class, "equ.Long", "J", () -> ofInteger(4)),
	FLOAT(float.class, Float.class, "equ.Float", "F", () -> ofFloat(5)),
	DOUBLE(double.class, Double.class, "equ.Double", "D", () -> ofFloat(6)),
	BOOL(boolean.class, Boolean.class, "equ.Bool", "Z", () -> ofBool(7));
	
	final Class<?> unboxType;
	final Class<?> boxType;
	final String name;
	final String desc;
	final Supplier<List<?>> supplier;
	SFunc[] functions;
	
	SPrimitiveClasses(Class<?> unbox, Class<?> box, String name, String desc, Supplier<List<?>> supplier)
	{
		this.unboxType = unbox;
		this.boxType = box;
		this.name = name;
		this.desc = desc;
		this.supplier = supplier;
	}
	
	@Override
	public boolean convert(SType type)
	{
		return type == this || (type instanceof SClass && type.getName().equals("equ.Serializable"));
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public String getDescriptor()
	{
		return this.desc;
	}
	
	@Override
	public SFunc[] getFunctions()
	{
		if (this.functions == null)
		{
			this.functions = this.supplier.get().stream().flatMap(
					i -> i instanceof SFunc[] ?
							Arrays.asList((SFunc[]) i).stream() :
								StreamSupport.stream(Spliterators1.single(i), false)).toArray(i -> new SFunc[i]);
		}
		return this.functions;
	}
	
	@Override
	public SClass getSupertype()
	{
		return SSpecialClasses.ANY_VAL;
	}
	
	@Override
	public boolean isInterface()
	{
		return false;
	}
}
