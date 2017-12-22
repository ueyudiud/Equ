/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.lexer3;

import java.util.Iterator;

/**
 * @author ueyudiud
 */
public interface Lexer<T>
{
	default Iterator<T> parse(char[] array)
	{
		return parse(array, 0, array.length);
	}
	
	Iterator<T> parse(char[] array, int start, int end);
}
