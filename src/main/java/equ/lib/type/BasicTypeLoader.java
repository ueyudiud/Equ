/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import equ.lib.base.SDeclearation;

/**
 * @author ueyudiud
 */
class BasicTypeLoader extends AbstractTypeLoader
{
	@Override
	public SClass loadClass(String name) throws ClassNotFoundException
	{
		switch (name)
		{
		case "equ.Bool" : return STypes.BOOL;
		case "equ.Byte" : return STypes.BYTE;
		case "equ.Short" : return STypes.SHORT;
		case "equ.Int" : return STypes.INT;
		case "equ.Long" : return STypes.LONG;
		case "equ.Float" : return STypes.FLOAT;
		case "equ.Double" : return STypes.DOUBLE;
		case "equ.Char" : return STypes.CHAR;
		case "equ.Unit" : return SVoidClass.INSTANCE;
		case "equ.Any" : return STypes.ANY;
		case "equ.AnyRef" : return STypes.ANY_REF;
		case "equ.AnyVal" : return STypes.ANY_VAL;
		case "equ.None" : return STypes.NONE;
		case "equ.String" : return STypes.STRING;
		default:
			throw new ClassNotFoundException(name + " not found.");
		}
	}
	
	@Override
	public SParameterizedType loadParameterizedType(SDeclearation owner, STypeProvider base, SType... parameters)
	{
		return new SParameterizedTypeImpl(this, owner, base, parameters);
	}
	
	@Override
	public SWildcardType loadWildcardType(SType[] upperBound, SType lowerBound)
	{
		return new SWildcardTypeImpl(this, upperBound, lowerBound);
	}
}
