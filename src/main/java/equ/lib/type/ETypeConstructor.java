/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.type;

/**
 * @author ueyudiud
 */
public interface ETypeConstructor extends EType
{
	EClass owner();
	
	default ETypeParameterized transform(EType...types) { return ETypes.parameterizedType(this, types); }
	
	ETypeParameterized transform_(EType...types);
	
	ETypeVariable[] variables();
}
