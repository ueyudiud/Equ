/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.type;

/**
 * @author ueyudiud
 */
public interface EArrayType extends ETypeParameterized
{
	EClass rawType();
	
	EType componentType();
	
	default EType[] parameters() { return new EType[] { componentType() }; }
}
