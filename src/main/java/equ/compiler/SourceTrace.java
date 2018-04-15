/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * @author ueyudiud
 */
public class SourceTrace
{
	public final String path;
	public final int ln;
	public final int col;
	public final int len;
	public final SourceTrace[] traces;
	
	private final char[] source;
	private final int start;
	private final int end;
	
	public SourceTrace(String path)
	{
		this(path, 0, 0, 0, null, 0, 0);
	}
	
	public SourceTrace(SourceTrace source, SourceTrace...traces)
	{
		this(source.path, source.ln, source.col, source.len, source.source, source.start, source.end, traces);
	}
	
	public SourceTrace(String path, int ln, int col, int len, char[] source, int start, int end, SourceTrace...traces)
	{
		this.path = path;
		this.ln = ln;
		this.col = col;
		this.len = len;
		this.source = source;
		this.start = start;
		this.end = end;
		this.traces = traces;
	}
	
	public void print(PrintStream stream)
	{
		print(stream, "");
	}
	
	public void print(PrintStream stream, String tab)
	{
		stream.print(tab);
		stream.println(pathMsg());
		if (source != null)
		{
			char[] arrays = new char[end - start];
			for (int i = 0; i < end - start; ++i)
			{
				if (Character.isWhitespace(source[start + i]))
				{
					arrays[i] = ' ';
				}
				else
				{
					arrays[i] = source[start + i];
				}
			}
			stream.println(arrays);
			char[] a = new char[col + len - 1];
			Arrays.fill(a, 0, col - 1, ' ');
			a[col - 1] = '^';
			Arrays.fill(a, col, a.length, '~');
			stream.println(a);
		}
		tab += "\t";
		for (SourceTrace trace : traces)
		{
			trace.print(stream, tab);
		}
	}
	
	public String pathMsg()
	{
		return "at: " + path + (source == null ? "<unknown source>" : "") + ", ln: " + ln + ", col: " + col;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		toString(builder, "");
		return builder.toString();
	}
	
	private void toString(StringBuilder builder, String tab)
	{
		builder.append(pathMsg());
		tab += "\t";
		for (SourceTrace trace : traces)
		{
			trace.toString(builder, tab);
		}
	}
}
