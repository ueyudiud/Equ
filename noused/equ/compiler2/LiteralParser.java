/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler2;

/**
 * @author ueyudiud
 */
class LiteralParser
{
	private static final int[][] LIMIT1 = {
			{},
			{32, 32},
			{16, 16, 16, 16},
			{11, 11, 11, 11, 10, 10, 10, 10},
			{8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8}
	};
	
	private static final int[][] LIMIT2 = {
			{},
			{64, 64},
			{32, 32, 32, 32},
			{22, 22, 21, 21, 21, 21, 21, 21},
			{16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16}
	};
	
	private static final int[][] LIMIT3 = {
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,2,1,1,2,1,2,2,2,1,2,1,1,0,2,0,2,1,0,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,},
			{1,3,3,4,4,2,2,3,4,3,4,0,4,2,},
			{5,5,3,0,3,2,0,0,5,5,3,1,},
			{1,0,4,1,3,4,2,1,1,1,6,1,},
			{1,7,7,7,7,7,7,7,7,7,7,},
			{5,4,7,8,7,7,3,6,7,1,},
			{2,1,4,7,4,8,3,6,4,7,},
			{10,0,2,2,2,0,2,8,1,},
			{4,11,11,2,3,0,8,10,7,},
			{2,8,2,11,10,4,10,10,10,},
			{1,6,5,2,12,10,9,3,1,},
			{12,8,7,14,6,6,11,7,},
			{7,15,15,15,15,15,15,15,},
			{5,3,16,7,15,5,4,8,},
			{3,9,2,8,16,3,17,1,},
			{2,7,12,5,7,17,3,2,},
			{1,13,11,1,15,9,2,7,},
			{1,4,0,17,2,13,9,1,},
			{18,20,15,5,11,15,1,},
			{14,11,14,21,15,9,5,},
			{11,5,16,16,14,5,7,},
			{8,19,22,13,23,20,22,},
			{6,24,19,8,18,24,23,},
			{5,14,17,23,12,20,10,},
			{4,12,21,22,9,8,15,},
			{3,17,20,7,9,8,7,},
			{2,28,11,6,12,28,7,},
			{2,13,0,9,30,12,1,},
			{1,31,31,31,31,31,31,},
			{1,21,28,26,29,21,1,},
			{1,13,8,33,26,27,25,},
			{1,5,31,2,2,30,22,},
			{35,18,20,0,35,19,},
	};
	
	private static final int[][] LIMIT4 = {
			{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
			{1,2,1,1,2,1,2,2,2,1,2,1,1,0,2,0,2,1,0,2,},
			{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
			{1,3,3,4,4,2,2,3,4,3,4,0,4,3,},
			{5,5,3,0,3,2,0,0,5,5,3,2,},
			{1,0,4,1,3,4,2,1,1,1,6,2,},
			{2,0,0,0,0,0,0,0,0,0,0,},
			{5,4,7,8,7,7,3,6,7,2,},
			{2,1,4,7,4,8,3,6,4,8,},
			{10,0,2,2,2,0,2,8,2,},
			{4,11,11,2,3,0,8,10,8,},
			{2,8,2,11,10,4,10,10,11,},
			{1,6,5,2,12,10,9,3,2,},
			{12,8,7,14,6,6,11,8,},
			{8,0,0,0,0,0,0,0,},
			{5,3,16,7,15,5,4,9,},
			{3,9,2,8,16,3,17,2,},
			{2,7,12,5,7,17,3,3,},
			{1,13,11,1,15,9,2,8,},
			{1,4,0,17,2,13,9,2,},
			{18,20,15,5,11,15,2,},
			{14,11,14,21,15,9,6,},
			{11,5,16,16,14,5,8,},
			{8,19,22,13,23,20,23,},
			{6,24,19,8,18,24,24,},
			{5,14,17,23,12,20,11,},
			{4,12,21,22,9,8,16,},
			{3,17,20,7,9,8,8,},
			{2,28,11,6,12,28,8,},
			{2,13,0,9,30,12,2,},
			{2,0,0,0,0,0,0,},
			{1,21,28,26,29,21,2,},
			{1,13,8,33,26,27,26,},
			{1,5,31,2,2,30,23,},
			{35,18,20,0,35,20,},
	};
	
	private static final int[][] LIMIT5 = {
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{2,0,2,1,1,1,0,0,1,1,0,2,2,2,1,0,0,1,2,1,0,2,0,1,0,0,2,1,2,2,0,1,0,1,2,2,0,2,2,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,},
			{1,1,0,4,3,3,2,4,0,1,3,0,4,4,2,2,4,3,4,3,1,0,3,1,1,2,1,2,},
			{1,5,4,0,2,4,1,0,0,3,0,3,1,0,3,0,2,2,2,1,2,2,2,1,1,},
			{2,2,3,4,1,0,1,0,6,1,1,2,4,5,0,5,2,0,5,2,3,0,0,},
			{7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,},
			{6,7,4,0,4,2,8,3,1,7,2,1,0,7,8,1,1,8,2,7,},
			{9,2,2,3,3,7,2,0,3,6,8,5,4,7,7,5,8,0,7,},
			{1,7,2,8,0,0,2,6,3,5,2,1,4,5,9,0,6,9,7,},
			{4,1,10,7,9,2,6,7,8,5,1,5,1,2,0,3,6,7,},
			{1,0,11,2,6,9,5,4,9,0,7,5,4,3,3,12,3,7,},
			{4,3,4,0,7,2,4,12,6,12,7,1,13,12,7,10,7,},
			{1,6,0,14,2,10,13,3,2,4,6,3,6,6,8,0,7,},
			{7,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,},
			{3,3,13,3,13,8,3,0,7,11,2,1,4,0,0,8,},
			{1,6,10,16,17,5,9,5,13,15,8,2,5,15,10,7,},
			{11,10,6,4,3,13,12,18,0,15,15,14,14,17,17,},
			{5,12,11,15,19,18,10,3,15,17,2,6,19,10,7,},
			{2,17,14,18,12,18,18,18,14,8,2,13,17,9,7,},
			{1,10,13,10,18,11,11,2,1,13,12,20,15,10,7,},
			{18,6,20,4,4,8,12,15,4,1,9,2,12,2,},
			{10,12,13,7,7,2,19,23,12,9,21,0,21,7,},
			{6,4,18,14,1,15,24,12,23,23,5,16,7,7,},
			{3,18,16,24,14,12,19,11,22,12,10,6,8,7,},
			{2,7,12,4,8,21,5,11,3,7,24,10,24,25,},
			{1,11,20,3,9,15,3,10,17,3,13,22,26,7,},
			{26,1,28,14,8,15,0,22,0,4,18,28,11,},
			{17,10,19,25,25,11,12,1,15,12,2,0,7,},
			{11,22,0,3,18,9,5,17,18,10,4,3,7,},
			{7,31,31,31,31,31,31,31,31,31,31,31,31,},
			{5,17,16,4,12,20,9,19,13,4,30,3,7,},
			{3,29,13,29,20,1,31,8,19,6,29,25,25,},
			{2,25,18,19,22,18,20,14,33,27,33,25,7,},
			{1,34,2,25,0,18,19,3,2,14,8,14,7,},
	};
	
	private static final int[][] LIMIT6 = {
			{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
			{2,0,2,1,1,1,0,0,1,1,0,2,2,2,1,0,0,1,2,1,0,2,0,1,0,0,2,1,2,2,0,1,0,1,2,2,0,2,2,2,},
			{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
			{1,1,0,4,3,3,2,4,0,1,3,0,4,4,2,2,4,3,4,3,1,0,3,1,1,2,1,3,},
			{1,5,4,0,2,4,1,0,0,3,0,3,1,0,3,0,2,2,2,1,2,2,2,1,2,},
			{2,2,3,4,1,0,1,0,6,1,1,2,4,5,0,5,2,0,5,2,3,0,1,},
			{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
			{6,7,4,0,4,2,8,3,1,7,2,1,0,7,8,1,1,8,2,8,},
			{9,2,2,3,3,7,2,0,3,6,8,5,4,7,7,5,8,0,8,},
			{1,7,2,8,0,0,2,6,3,5,2,1,4,5,9,0,6,9,8,},
			{4,1,10,7,9,2,6,7,8,5,1,5,1,2,0,3,6,8,},
			{1,0,11,2,6,9,5,4,9,0,7,5,4,3,3,12,3,8,},
			{4,3,4,0,7,2,4,12,6,12,7,1,13,12,7,10,8,},
			{1,6,0,14,2,10,13,3,2,4,6,3,6,6,8,0,8,},
			{8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
			{3,3,13,3,13,8,3,0,7,11,2,1,4,0,0,9,},
			{1,6,10,16,17,5,9,5,13,15,8,2,5,15,10,8,},
			{11,10,6,4,3,13,12,18,0,15,15,14,14,17,18,},
			{5,12,11,15,19,18,10,3,15,17,2,6,19,10,8,},
			{2,17,14,18,12,18,18,18,14,8,2,13,17,9,8,},
			{1,10,13,10,18,11,11,2,1,13,12,20,15,10,8,},
			{18,6,20,4,4,8,12,15,4,1,9,2,12,3,},
			{10,12,13,7,7,2,19,23,12,9,21,0,21,8,},
			{6,4,18,14,1,15,24,12,23,23,5,16,7,8,},
			{3,18,16,24,14,12,19,11,22,12,10,6,8,8,},
			{2,7,12,4,8,21,5,11,3,7,24,10,24,26,},
			{1,11,20,3,9,15,3,10,17,3,13,22,26,8,},
			{26,1,28,14,8,15,0,22,0,4,18,28,12,},
			{17,10,19,25,25,11,12,1,15,12,2,0,8,},
			{11,22,0,3,18,9,5,17,18,10,4,3,8,},
			{8,0,0,0,0,0,0,0,0,0,0,0,0,},
			{5,17,16,4,12,20,9,19,13,4,30,3,8,},
			{3,29,13,29,20,1,31,8,19,6,29,25,26,},
			{2,25,18,19,22,18,20,14,33,27,33,25,8,},
			{1,34,2,25,0,18,19,3,2,14,8,14,8,},
	};
	
	private static void checkRadixAndString(char[] array, int from, int to, int radix) throws NumberFormatException
	{
		if (array == null || to <= from)
			throw new NumberFormatException("null");
		
		if (radix < Character.MIN_RADIX)
			throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
		
		if (radix > Character.MAX_RADIX)
			throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
	}
	
	private static NumberFormatException exception(char[] array, int from, int to)
	{
		return new NumberFormatException(
				new StringBuilder().append("For input string: \"").append(array, from, to - from).append('"').toString());
	}
	
	static int parseInt1(char[] array, int from, int to, int radix) throws NumberFormatException
	{
		checkRadixAndString(array, from, to, radix);
		
		final int[] limit;
		
		int result = 0;
		int i = from, j;
		int digit;
		
		switch (array[from])
		{
		case '+' :
			if (to == ++i)
				throw exception(array, from, to);
		default:
			limit = LIMIT3[radix];
			if (to - i > limit.length)
				throw exception(array, from, to);
			if (to - i == limit.length)
			{
				j = 0;
				label: while (i < to)
				{
					digit = Character.digit(array[i ++], radix);
					if (digit < 0 || j > limit[j])
						throw exception(array, from, to);
					result = result * radix + digit;
					if (j < limit[j ++])
						break label;
				}
			}
			while (i < to)
			{
				digit = Character.digit(array[i ++], radix);
				if (digit < 0)
					throw exception(array, from, to);
				result = result * radix + digit;
			}
			return result;
		case '-' :
			limit = LIMIT4[radix];
			if (to == ++i)
				throw exception(array, from, to);
			if (to - i > limit.length)
				throw exception(array, from, to);
			if (to - i == limit.length)
			{
				j = 0;
				label: while (i < to)
				{
					digit = Character.digit(array[i ++], radix);
					if (digit < 0 || j > limit[j])
						throw exception(array, from, to);
					result = result * radix - digit;
					if (j < limit[j ++])
						break label;
				}
			}
			while (i < to)
			{
				digit = Character.digit(array[i ++], radix);
				if (digit < 0)
					throw exception(array, from, to);
				result = result * radix + digit;
			}
			return result;
		}
	}
	
	static long parseLong1(char[] array, int from, int to, int radix) throws NumberFormatException
	{
		checkRadixAndString(array, from, to, radix);
		
		final int[] limit;
		
		long result = 0;
		int i = from, j;
		int digit;
		
		switch (array[from])
		{
		case '+' :
			if (to == ++i)
				throw exception(array, from, to);
		default:
			limit = LIMIT5[radix];
			if (to - i > limit.length)
				throw exception(array, from, to);
			if (to - i == limit.length)
			{
				j = 0;
				label: while (i < to)
				{
					digit = Character.digit(array[i ++], radix);
					if (digit < 0 || j > limit[j])
						throw exception(array, from, to);
					result = result * radix + digit;
					if (j < limit[j ++])
						break label;
				}
			}
			while (i < to)
			{
				digit = Character.digit(array[i ++], radix);
				if (digit < 0)
					throw exception(array, from, to);
				result = result * radix + digit;
			}
			return result;
		case '-' :
			limit = LIMIT6[radix];
			if (to == ++i)
				throw exception(array, from, to);
			if (to - i > limit.length)
				throw exception(array, from, to);
			if (to - i == limit.length)
			{
				j = 0;
				label: while (i < to)
				{
					digit = Character.digit(array[i ++], radix);
					if (digit < 0 || j > limit[j])
						throw exception(array, from, to);
					result = result * radix - digit;
					if (j < limit[j ++])
						break label;
				}
			}
			while (i < to)
			{
				digit = Character.digit(array[i ++], radix);
				if (digit < 0)
					throw exception(array, from, to);
				result = result * radix + digit;
			}
			return result;
		}
	}
	
	static int parseInt2(char[] array, int from, int to, int lpradix) throws NumberFormatException
	{
		final int radix = 1 << lpradix;
		
		checkRadixAndString(array, from, to, radix);
		
		int result;
		int digit;
		int i = from;
		
		result = Character.digit(array[i ++], radix);
		if (result < 0 || to - from > LIMIT1[lpradix][result])
		{
			throw exception(array, from, to);
		}
		
		while (i < to)
		{
			digit = Character.digit(array[i ++], radix);
			if (digit < 0)
			{
				throw exception(array, from, to);
			}
			result = result << lpradix | digit;
		}
		return result;
	}
	
	static long parseLong2(char[] array, int from, int to, int lpradix) throws NumberFormatException
	{
		final int radix = 1 << lpradix;
		
		checkRadixAndString(array, from, to, radix);
		
		long result;
		int digit;
		int i = from;
		
		result = Character.digit(array[i ++], radix);
		if (result < 0 || to - from > LIMIT2[lpradix][(int) result])
		{
			throw exception(array, from, to);
		}
		
		while (i < to)
		{
			digit = Character.digit(array[i ++], radix);
			if (digit < 0)
			{
				throw exception(array, from, to);
			}
			result = result << lpradix | digit;
		}
		return result;
	}
}
