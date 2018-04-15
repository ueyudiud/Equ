/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

import java.util.List;
import java.util.Map;

/**
 * @author ueyudiud
 */
public interface CompileContext extends Map<String, IMacro>
{
	int UTF8_ESCAPE = 1;
	
	boolean isOptionEnable(int optionID);
	
	void warn(String cause, SourceTrace trace);
	
	void error(String cause, SourceTrace trace);
	
	ILexeme eval(SourceTrace evalCaller, CompileContext context, List<ILexeme> values) throws CompileException;
	
	default ILexeme eval(SourceTrace evalCaller, List<ILexeme> values) throws CompileException
	{
		return eval(evalCaller, this, values);
	}
	
	IScanner openScanner(String src, boolean lib, String path);
}
