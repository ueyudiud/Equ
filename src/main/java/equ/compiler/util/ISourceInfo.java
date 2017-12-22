/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.util;

import equ.util.Strings;

/**
 * @author ueyudiud
 */
public interface ISourceInfo
{
	String source();
	
	String line();
	
	int ln();
	
	int col();
	
	default String toSourceInfo()
	{
		return line() + "\r" + (col() == 0 ? "" : Strings.repeat(' ', col() - 1)) + "^\r";
	}
}
