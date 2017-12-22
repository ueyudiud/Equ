/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import equ.lib.base.SDeclearation;
import equ.lib.func.SFunc;
import equ.util.Arguments;

/**
 * @author ueyudiud
 */
class SBlockClass implements SClass
{
	static final SBlockClass INSTANCE = new SBlockClass();
	static final SVariableType<SClass> VARIABLE = new SVariableType<SClass>()
	{
		@Override
		public SClassLoader getTypeLoader()
		{
			return null;
		}
		
		@Override
		public String getName()
		{
			return "T";
		}
		
		@Override
		public SType[] getMemberTypes()
		{
			return STypeHelper.EMPTY_TYPE;
		}
		
		@Override
		public SFunc[] getFunctions()
		{
			return EMPTY;
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
		public SClass getGenericDeclaration()
		{
			return INSTANCE;
		}
		
		@Override
		public SType[] getBounds()
		{
			return new SType[] {SSpecialClasses.ANY_REF};
		}
	};
	
	@Override
	public SType provideType(SType... parameters)
	{
		Arguments.checkLength(0, parameters, "Illegal parameter count.");
		return this;
	}
	
	@Override
	public boolean convert(SType type)
	{
		return this == type.getRawClass() ||
				type == SSpecialClasses.NONE;
	}
	
	@Override
	public String getName()
	{
		return "equ.Block";
	}
	
	@Override
	public boolean isInterface()
	{
		return true;
	}
	
	@Override
	public SType[] getMemberTypes()
	{
		return STypeHelper.EMPTY_TYPE;
	}
	
	@Override
	public SFunc[] getFunctions()
	{
		return EMPTY;
	}
	
	@Override
	public SFunc[] getConstructors()
	{
		return EMPTY;
	}
	
	@Override
	public SDeclearation getOwner()
	{
		return null;
	}
	
	@Override
	public SVariableType<?>[] getTypeParameters()
	{
		return SPrimitiveClassAbstract.NO_VARIABLE_TYPES;
	}
	
	@Override
	public SClassLoader getTypeLoader()
	{
		return null;
	}
	
	@Override
	public SClass getSupertype()
	{
		return SSpecialClasses.ANY_REF;
	}
	
	@Override
	public SType getGenericSupertypes()
	{
		return SSpecialClasses.ANY_REF;
	}
	
	@Override
	public String getDescriptor()
	{
		return "Ljava/lang/Object;";//TODO
	}
}
