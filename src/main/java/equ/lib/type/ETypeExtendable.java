/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.type;

/**
 * @author ueyudiud
 */
public interface ETypeExtendable extends EType
{
	ETypeExtendable zuper();
	
	ETypeExtendable[] interfaces();
}
