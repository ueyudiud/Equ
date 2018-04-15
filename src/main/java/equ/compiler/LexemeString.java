/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class LexemeString extends LexemeUTF8
{
	private String deescape;
	
	public LexemeString(char[] source, SourceTrace trace)
	{
		super(source, trace);
	}
	
	@Override
	public EnumLexType type()
	{
		return EnumLexType.LIS;
	}
	
	@Override
	public String display()
	{
		if (deescape == null)
		{
			int i = source.length;
			for (char c : source)
			{
				switch (c)
				{
				case '\\' : case '\"' : case '\'' :
				case '\f' : case '\n' : case '\r' :
				case '\b' : case '\t' : case '\7' :
					i ++;
					break;
				}
			}
			char[] cs = new char[i + 2];
			cs[0] = '"';
			i = 1;
			for (char c : source)
			{
				switch (c)
				{
				case '\\' :
				case '\"' :
				case '\'' :
					cs[i ++] = '\\';
					cs[i ++] = c;
					break;
				case '\f' :
					cs[i ++] = '\\';
					cs[i ++] = 'f';
					break;
				case '\n' :
					cs[i ++] = '\\';
					cs[i ++] = 'n';
					break;
				case '\r' :
					cs[i ++] = '\\';
					cs[i ++] = 'r';
					break;
				case '\b' :
					cs[i ++] = '\\';
					cs[i ++] = 'b';
					break;
				case '\t' :
					cs[i ++] = '\\';
					cs[i ++] = 't';
					break;
				case '\7' :
					cs[i ++] = '\\';
					cs[i ++] = 'a';
					break;
				default :
					cs[i ++]= c;
					break;
				}
			}
			cs[i] = '"';
			deescape = new String(cs);
		}
		return deescape;
	}
}
