/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.lexer3;

/**
 * @author ueyudiud
 */
class SimpleLexerBuilder<T> implements ILexerBuilder<T>
{
	@Override
	public ILexerBuilder<T> add(String name, String format, ICharTransformer transformer)
	{
		// TODO Auto-generated method stub
		return this;
	}
	
	@Override
	public ILexerBuilder<T> addSet(String name, String set)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ILexerBuilder<T> addRanged(String name, char from, char to)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ILexerBuilder<T> addPredicator(String name, CharPredicate p)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ILexerBuilder<T> addSkiped(String name, String format)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Lexer<T> build()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
