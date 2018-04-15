/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * @author ueyudiud
 */
public interface CompileFactory
{
	CompileContext defaultContext();
	
	default void compile(File src, File dest) throws CompileException
	{
		compile(src, dest, defaultContext());
	}
	
	void compile(File src, File dest, CompileContext context) throws CompileException;
	
	void eval(Reader reader) throws IOException, CompileException;
	
	void eval(String code) throws CompileException;
}
