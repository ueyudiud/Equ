/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.type;

/**
 * @author ueyudiud
 */
public interface EClass extends ETypeExtendable
{
	default EClass rawType() { return this; }
	
	EPath path();
	
	boolean primitive();
}
