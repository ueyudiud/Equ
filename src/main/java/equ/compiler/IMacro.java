/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

import java.util.List;

/**
 * @author ueyudiud
 */
public interface IMacro
{
	String name();
	
	int parameterCount();
	
	List<ILexeme> transform(ILexeme header, List<ILexeme>...parameters);
}
