/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.type;

import java.util.function.Function;

/**
 * @author ueyudiud
 */
public interface EType
{
	EType rawType();
	
	String getName();
	
	EType map(Function<EType, EType> mapping);
}
