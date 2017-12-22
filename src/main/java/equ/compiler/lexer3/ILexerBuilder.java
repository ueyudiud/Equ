/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.lexer3;

/**
 * @author ueyudiud
 */
public interface ILexerBuilder<T>
{
	ILexerBuilder<T> add(String name, String format, ICharTransformer transformer);
	
	ILexerBuilder<T> addSet(String name, String set);
	
	ILexerBuilder<T> addRanged(String name, char from, char to);
	
	ILexerBuilder<T> addPredicator(String name, CharPredicate p);
	
	ILexerBuilder<T> addSkiped(String name, String format);
	
	Lexer<T> build();
}
