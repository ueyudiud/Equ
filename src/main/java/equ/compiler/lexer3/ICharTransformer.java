/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.lexer3;

import javax.annotation.Nonnull;

/**
 * @author ueyudiud
 */
public interface ICharTransformer<T>
{
	@Nonnull T transform(Object[] array);
}
