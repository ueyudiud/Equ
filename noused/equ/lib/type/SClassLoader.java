/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import equ.lib.base.SDeclearation;

/**
 * @author ueyudiud
 */
public interface SClassLoader
{
	SClass loadClass(String name) throws ClassNotFoundException;
	
	SType loadArrayClass(SType type, int dim);
	
	SParameterizedType loadParameterizedType(SDeclearation owner, STypeProvider base, SType...parameters);
	
	SWildcardType loadWildcardType(SType[] upperBound, SType lowerBound);
}
