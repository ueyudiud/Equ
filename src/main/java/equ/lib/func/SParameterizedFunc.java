/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.func;

import equ.lib.base.SParameterizedDeclearation;
import equ.lib.type.SType;

/**
 * @author ueyudiud
 */
public interface SParameterizedFunc extends  SParameterizedDeclearation<SGenericFunc, SFunc>, SFunc
{
	SType getGenericOwner();
}
