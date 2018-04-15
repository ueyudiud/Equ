/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler2;

import java.io.PrintStream;

/**
 * @author ueyudiud
 */
public interface ISourceProvider
{
	void printSource(PrintStream stream, int pos, int ln, int col);
}
