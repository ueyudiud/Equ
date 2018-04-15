/*
 * copyright© 2018 ueyudiud
 */
package equ.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author ueyudiud
 */
public class IOUtil
{
	private static final char[] CHAR_BUF = new char[8192];
	
	public static char[] read(File file) throws IOException
	{
		return read(65536, new FileReader(file));
	}
	
	public static char[] read(Reader reader) throws IOException
	{
		return read(65536, reader);
	}
	
	private static char[] read(int initalizeSize, Reader reader) throws IOException
	{
		char[] array = new char[initalizeSize];
		int length = 0;
		int size;
		synchronized (CHAR_BUF)
		{
			while ((size = reader.read(CHAR_BUF)) != -1)
			{
				if (size + length > array.length)
				{
					char[] a1 = new char[array.length << 1];
					System.arraycopy(array, 0, a1, 0, length);
					array = a1;
				}
				System.arraycopy(CHAR_BUF, 0, array, length, size);
				length += size;
			}
		}
		if (length != array.length)
		{
			char[] a1 = new char[length];
			System.arraycopy(array, 0, a1, 0, length);
			array = a1;
		}
		return array;
	}
}
