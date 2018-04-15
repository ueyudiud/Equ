/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler2;

import java.io.PrintStream;

import javax.annotation.Nullable;

/**
 * @author ueyudiud
 */
public class SourceTrace
{
	public String file;
	public int posStart, posEnd, lnStart, colStart, lnEnd, colEnd;
	
	public SourceTrace(String file, int posStart, int lnStart, int colStart, int posEnd, int lnEnd, int colEnd)
	{
		this.file = file;
		this.posStart = posStart;
		this.posEnd = posEnd;
		this.lnStart = lnStart;
		this.lnEnd = lnEnd;
		this.colStart = colStart;
		this.colEnd = colEnd;
	}
	
	public void printSource(PrintStream stream, @Nullable ISourceProvider provider)
	{
		if (provider == null)
		{
			stream.println("<unknown source>");
		}
		else
		{
			stream.print(file);
			stream.print(':');
			stream.print(lnStart);
			stream.print(':');
			stream.print(colStart);
			stream.println();
			provider.printSource(stream, posStart, lnStart, colStart);
		}
	}
}
