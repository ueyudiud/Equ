/*
 * copyright© 2017 ueyudiud
 */
package equ.util;

import java.io.CharArrayWriter;
import java.util.Arrays;

/**
 * @author ueyudiud
 */
public class CharArrayWriter1 extends CharArrayWriter
{
	public CharArrayWriter1()
	{
	}
	
	public CharArrayWriter1(int initalLength)
	{
		super(initalLength);
	}
	
	public char[] writeTo(char[] array, int off)
	{
		if (array.length < this.count + off)
		{
			array = Arrays.copyOf(array, this.count + off);
		}
		System.arraycopy(this.buf, 0, array, off, this.count);
		return array;
	}
	
	public int count()
	{
		return this.count;
	}
}
