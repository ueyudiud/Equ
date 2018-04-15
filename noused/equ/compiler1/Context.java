/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler1;

import equ.lib.type.SType;

/**
 * @author ueyudiud
 */
public abstract class Context
{
	Context parent;
	
	public SType getTypeBySimpleName(String token)
	{
		return this.parent != null ? this.parent.getTypeBySimpleName(token) : null;
	}
	
	public SType getTypeByFullName(String[] tokens)
	{
		return this.parent != null ? this.parent.getTypeByFullName(tokens) : null;
	}
}
