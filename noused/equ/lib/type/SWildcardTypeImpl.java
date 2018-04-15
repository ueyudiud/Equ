/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import equ.lib.base.SDeclearation;
import equ.lib.func.SFunc;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
class SWildcardTypeImpl implements SWildcardType
{
	final SClassLoader loader;
	final SType[] upperBounds;
	final SType lowerBounds;
	
	SWildcardTypeImpl(SClassLoader loader, SType[] upperBounds, SType lowerBound)
	{
		this.loader = loader;
		this.upperBounds = upperBounds;
		this.lowerBounds = lowerBound;
	}
	
	@Override
	public SClassLoader getTypeLoader()
	{
		return this.loader;
	}
	
	@Override
	public String getName()
	{
		return this.lowerBounds == SSpecialClasses.NONE ?
				(this.upperBounds.length == 1 && this.upperBounds[0] == SSpecialClasses.ANY ? "?" :
					"? <: " + Strings.toString(this.upperBounds, SType::getName)) :
						(this.upperBounds.length == 1 && this.upperBounds[0] == SSpecialClasses.ANY ?
								"? >: " + this.lowerBounds.getName() :
									this.lowerBounds.getName() + " <: ? <: " + Strings.toString(this.upperBounds, SType::getName));
	}
	
	@Override
	public SType getRawClass()
	{
		return null;
	}
	
	@Override
	public SType getSupertype()
	{
		return this.upperBounds[0].getRawClass();
	}
	
	@Override
	public SType getGenericSupertypes()
	{
		return this.upperBounds[0];
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
		return SType.EMPTY;
	}
	
	@Override
	public SDeclearation getOwner()
	{
		return null;
	}
	
	@Override
	public SType[] getUpperBounds()
	{
		return this.upperBounds;
	}
	
	@Override
	public SType getLowerBounds()
	{
		return this.lowerBounds;
	}
	
	@Override
	public String toString()
	{
		return "Wildcard { " + getName() + " }";
	}
}
