/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.type;

/**
 * @author ueyudiud
 */
public interface ETypeVariable extends EType
{
	EType owner();
	
	EnumVariableBoundType boundType();
	
	EType[] bounds();
}