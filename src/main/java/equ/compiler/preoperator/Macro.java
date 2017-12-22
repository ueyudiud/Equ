/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import equ.compiler.util.ISourceInfo;

/**
 * @author ueyudiud
 */
public interface Macro<H> extends ISourceInfo
{
	int PREOPERATE = 1;
	int PARSE = 2;
	int LEXERALIZE = 3;
	
	int type();
	
	void affect(H handler) throws Exception;
}
