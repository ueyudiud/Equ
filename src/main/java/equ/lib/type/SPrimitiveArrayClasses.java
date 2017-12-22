/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import static equ.lib.type.SPrimitiveClasses.BOOL;
import static equ.lib.type.SPrimitiveClasses.BYTE;
import static equ.lib.type.SPrimitiveClasses.CHAR;
import static equ.lib.type.SPrimitiveClasses.DOUBLE;
import static equ.lib.type.SPrimitiveClasses.FLOAT;
import static equ.lib.type.SPrimitiveClasses.INT;
import static equ.lib.type.SPrimitiveClasses.LONG;
import static equ.lib.type.SPrimitiveClasses.SHORT;

import java.util.function.Function;

import equ.lib.base.SDeclearation;
import equ.lib.base.SGenericDeclearation;
import equ.lib.func.SFunc;

/**
 * @author ueyudiud
 */
enum SPrimitiveArrayClasses implements SPrimitiveClassAbstract
{
	ARRAY("Array", SSpecialClasses.ANY)
	{
		final SVariableType<?> type = new SVariableType<SPrimitiveClassAbstract>()
		{
			@Override
			public SClassLoader getTypeLoader()
			{
				return null;
			}
			
			@Override
			public String getName()
			{
				return "E";
			}
			
			@Override
			public SType[] getMemberTypes()
			{
				return STypeHelper.EMPTY_TYPE;
			}
			
			@Override
			public SFunc[] getFunctions()
			{
				return null;//TODO
			}
			
			@Override
			public SFunc[] getConstructors()
			{
				return EMPTY;
			}
			
			@Override
			public EnumVariableType getVariableType()
			{
				return EnumVariableType.COVARIANCE;
			}
			
			@Override
			public SPrimitiveClassAbstract getGenericDeclaration()
			{
				return ARRAY;
			}
			
			@Override
			public SType[] getBounds()
			{
				return STypeHelper.EMPTY_TYPE;//TODO
			}
		};
		
		@Override
		public SVariableType<?>[] getTypeParameters()
		{
			return new SVariableType<?>[] {this.type};
		}
	},
	
	BYTE_ARRAY("Array<Byte>", BYTE),
	SHORT_ARRAY("Array<Short>", SHORT),
	INT_ARRAY("Array<Int>", INT),
	LONG_ARRAY("Array<Long>", LONG),
	FLOAT_ARRAY("Array<Float>", FLOAT),
	DOUBLE_ARRAY("Array<Double>", DOUBLE),
	CHAR_ARRAY("Array<Char>", CHAR),
	BOOL_ARRAY("Array<Bool>", BOOL);
	
	final String name;
	final SClass ct;
	
	SPrimitiveArrayClasses(String name, SClass ct)
	{
		this.name = name;
		this.ct = ct;
	}
	
	@Override
	public boolean convert(SType type)
	{
		return type == this || type == SSpecialClasses.ANY_VAL ||
				type == SSpecialClasses.ANY ||
				(type instanceof SClass && (
						type.getName().equals("equ.Serializable")));
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public String getDescriptor()
	{
		return "[" + this.ct.getDescriptor();
	}
	
	@Override
	public SFunc[] getFunctions()
	{
		return null;//TODO
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

class SArrayClass implements SParameterizedType, SGenericDeclearation<SType>
{
	private final SClassLoader loader;
	private final SType type;
	
	SArrayClass(SClassLoader loader, SType type)
	{
		this.loader = loader;
		this.type = type;
	}
	
	@Override
	public SClassLoader getTypeLoader()
	{
		return this.loader;
	}
	
	@Override
	public String getName()
	{
		return "Array<" + this.type.getName() + ">";
	}
	
	@Override
	public SType getSupertype()
	{
		return SPrimitiveArrayClasses.ARRAY;
	}
	
	@Override
	public SType getGenericSupertypes()
	{
		return SPrimitiveArrayClasses.ARRAY;
	}
	
	@Override
	public SType[] getMemberTypes()
	{
		return STypeHelper.EMPTY_TYPE;
	}
	
	@Override
	public SFunc[] getFunctions()
	{
		return null;//TODO
	}
	
	@Override
	public SFunc[] getConstructors()
	{
		return null;//TODO
	}
	
	@Override
	public SType $transformTo(Function<SVariableType<?>, SType> function)
	{
		return new SArrayClass(this.loader, STypeHelper.transform(this.type, function));
	}
	
	@Override
	public SDeclearation getOwner()
	{
		return null;
	}
	
	@Override
	public SDeclearation getGenericOwner()
	{
		return null;
	}
	
	@Override
	public STypeProvider getRawDeclearation()
	{
		return SPrimitiveArrayClasses.ARRAY;
	}
	
	@Override
	public SType[] getActualTypeArguments()
	{
		return new SType[] {this.type};
	}
	
	@Override
	public SType $transformVariableType(SVariableType<?> type)
	{
		return SParameterizedType.super.$transformVariableType(type);
	}
	
	@Override
	public SVariableType<?>[] getTypeParameters()
	{
		return SPrimitiveClassAbstract.NO_VARIABLE_TYPES;
	}
	
	@Override
	public boolean equal(SType type)
	{
		return SParameterizedType.super.equal(type);
	}
	
	@Override
	public boolean convert(SType type)
	{
		return SParameterizedType.super.convert(type);
	}
}
