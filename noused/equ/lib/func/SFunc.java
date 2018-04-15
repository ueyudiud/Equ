/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.func;

import equ.lib.base.SDeclearation;
import equ.lib.base.SMethodWriter;
import equ.lib.type.SType;

/**
 * @author ueyudiud
 */
public interface SFunc extends SDeclearation<SFunc>
{
	SType getOwner();
	
	STypeMethod getMethodType();
	
	SType[] getParameters();
	
	SType getResult();
	
	SMethodWriter writer();
}
