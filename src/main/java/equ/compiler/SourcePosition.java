/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

import java.io.PrintStream;
import java.util.Arrays;

import equ.util.Strings;

/**
 * @author ueyudiud
 */
public class SourcePosition
{
	public final String path;
	public final int ln, col;
	
	private final char[] code;
	private final int from;
	private final int to;
	
	public SourcePosition(String path, int ln, int col, char[] code, int from, int to)
	{
		this.path = path;
		this.col = col;
		this.ln = ln;
		this.code = code;
		this.from = from;
		this.to = to;
	}
	
	public String getMessage()
	{
		return "at " + this.path + " ln: " + this.ln + " col: " + this.col;
	}
	
	public void printCode(PrintStream stream)
	{
		if (this.col == -1)
			stream.println("<unknown source code>");
		else if (this.ln == -1)
			stream.println("<eof>");
		else
		{
			stream.println(Arrays.copyOfRange(this.code, this.from, this.to));
			stream.print(Strings.repeat(' ', this.col));
			stream.println('^');
		}
	}
}
