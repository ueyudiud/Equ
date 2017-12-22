/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import equ.compiler.util.ISourceInfoProvider;

/**
 * @author ueyudiud
 */
public interface MacroFactory<H extends ISourceInfoProvider>
{
	Macro<?> parse(H handler, String line) throws PreoperationException;
}
