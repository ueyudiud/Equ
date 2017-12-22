/*
 * copyright© 2017 ueyudiud
 */
package equ.util;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;

/**
 * @author ueyudiud
 */
public class IOUtil
{
	private static final char[] CACHE = new char[4096];
	
	public static char[] readFully(Reader reader) throws IOException
	{
		CharArrayWriter writer = new CharArrayWriter();
		synchronized (CACHE)
		{
			int len;
			while ((len = reader.read(CACHE)) > 0)
			{
				writer.write(CACHE, 0, len);
			}
		}
		reader.close();
		return writer.toCharArray();
	}
}
