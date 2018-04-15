/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import equ.lib.func.SFunc;

/**
 * @author ueyudiud
 */
enum SSpecialClasses implements SPrimitiveClassAbstract
{
	ANY("Any")
	{
		@Override
		public boolean convertFrom(SType type)
		{
			return true;
		}
		
		@Override
		public boolean convert(SType type)
		{
			return type == this;
		}
	},
	ANY_REF("AnyRef")
	{
		@Override
		public SClass getSupertype()
		{
			return ANY;
		}
		
		@Override
		public boolean convertFrom(SType type)
		{
			do
			{
				if (type == this)
					return true;
			}
			while ((type = type.getSupertype()) != null);
			return false;
		}
		
		@Override
		public boolean convert(SType type)
		{
			return type == ANY || type == this;
		}
	},
	ANY_VAL("AnyVal")
	{
		@Override
		public SClass getSupertype()
		{
			return ANY;
		}
		
		@Override
		public boolean convertFrom(SType type)
		{
			do
			{
				if (type == this)
					return true;
			}
			while ((type = type.getSupertype()) != null);
			return false;
		}
		
		@Override
		public boolean convert(SType type)
		{
			return type == SSpecialClasses.ANY || type == this;
		}
		
		@Override
		public boolean isPrimitiveType()
		{
			return true;
		}
	},
	NONE("None")
	{
		@Override
		public SClass getSupertype()
		{
			return ANY;
		}
		
		@Override
		public boolean convertFrom(SType type)
		{
			return type == this;
		}
		
		@Override
		public boolean convert(SType type)
		{
			return type != SVoidClass.INSTANCE;
		}
	};
	
	final String name;
	
	SSpecialClasses(String name)
	{
		this.name = name;
	}
	
	@Override
	public SClass getSupertype()
	{
		return null;
	}
	
	@Override
	public SType getGenericSupertypes()
	{
		return getSupertype();
	}
	
	@Override
	public boolean convert(SType type)
	{
		return false;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public String getDescriptor()
	{
		return "Ljava/lang/Object;";
	}
	
	@Override
	public SFunc[] getFunctions()
	{
		return null;//TODO
	}
	
	@Override
	public boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public boolean isInterface()
	{
		return false;
	}
}
