/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import equ.compiler.ScannerException;
import equ.compiler.SourceTrace;

/**
 * @author ueyudiud
 */
class CharScanner implements SpecialCodePoint
{
	private static int[][] generateLimemap(int slen, char[] source)
	{
		//Each line is 64 char predicated.
		//At least 1 line exist.
		List<int[]> list = new ArrayList<>(1 + (slen >> 6));
		int i = 0, j = 0;
		while (i < slen)
		{
			final int l = i;
			switch (source[i])
			{
			case '\r' :
				if (i + 1 < slen && source[i + 1] == '\n')
				{
					i ++;
				}
			case '\n' :
				if (l != j)
				{
					list.add(new int[] {j, l - j});
				}
				j = ++ i;
				break;
			default :
				i ++;
				break;
			}
		}
		if (slen != j)
		{
			list.add(new int[] {j, slen - j});
		}
		list.add(new int[] {slen, 0});
		return list.toArray(new int[list.size()][]);
	}
	
	final String P;
	final int[][] linemap;
	
	final char[] S;
	final int slen;
	int C = '\r';
	int sidx = 0;
	int slastidx = -1;
	boolean sacc = false;
	private int smark;
	
	final char[] cbuf = new char[clen];
	static final int clen = 8192;
	int cidx = 0;
	
	boolean enableUTF8Escape = true;
	
	CharScanner(String path, char[] source)
	{
		P = path;
		S = source;
		slen = source.length;
		linemap = generateLimemap(slen, source);
	}
	
	private int floor(int idx)
	{
		if (idx > slen) return linemap.length - 1;
		
		int low = 0;
		int high = linemap.length - 1;
		
		while (low < high)
		{
			int mid = (low + high >>> 1);
			if (linemap[mid][0] > idx)
			{
				high = mid - 1;
			}
			else if (linemap[mid + 1][0] <= idx)
			{
				low = mid + 1;
			}
			else
			{
				return mid;
			}
		}
		return low;
	}
	
	SourceTrace trace()
	{
		return trace(smark, (sacc ? sidx : slastidx) - smark);
	}
	
	/**
	 * Trace source from index to ending of line.
	 * @param idx
	 * @return
	 */
	SourceTrace trace(int idx)
	{
		int ln = floor(idx);
		int[] d = linemap[ln];
		int col = idx - d[0];
		return new SourceTrace(P, ln + 1, col + 1, Math.max(d[1] - col, 1), S, d[0], d[0] + d[1]);
	}
	
	/**
	 * Trace source from index with specific length.
	 * @param idx
	 * @param len
	 * @return
	 */
	SourceTrace trace(int idx, int len)
	{
		int ln = floor(idx);
		int[] d = linemap[ln];
		return new SourceTrace(P, ln + 1, idx - d[0] + 1, len, S, d[0], d[0] + d[1]);
	}
	
	int peek() throws ScannerException
	{
		if (sacc)
		{
			scan();
			sacc = false;
		}
		return C;
	}
	
	private void scan() throws ScannerException
	{
		slastidx = sidx;
		char c = scan0();
		if (isHighSurrogate(c) && sidx < slen)
		{
			int mark = sidx;
			C = toCodePoint(c, scan0());
			if (C <= Character.MAX_VALUE)
			{
				sidx = mark;
				C = c;
			}
		}
		else
		{
			C = c;
		}
	}
	
	private static boolean isHighSurrogate(char c)
	{
		switch (c)
		{
		case '|': case '<': case ']': case '/': case '*':
			return true;
		default:
			return Character.isHighSurrogate(c);
		}
	}
	
	private static int toCodePoint(char high, char low)
	{
		switch (high)
		{
		case '<' : return low == '|' ? CP_LEFT_NAMING : low == '[' ? CP_LEFT_TEMPLATE : '<';
		case '|' : return low == '>' ? CP_RIGHT_NAMING : '|';
		case ']' : return low == '>' ? CP_RIGHT_TEMPLATE : ']';
		case '/' : return low == '/' ? CP_SINGLE_COMMENT : low == '*' ? CP_LEFT_MCOMMENT : '/';
		case '*' : return low == '/' ? CP_RIGHT_MCOMMENT : '*';
		default : return Character.isLowSurrogate(low) ? Character.toCodePoint(high, low) : high;
		}
	}
	
	private int escape(char chr, int start) throws ScannerException
	{
		switch (chr)
		{
		case '0' : return 0;
		case '1' : return 0;
		case '2' : return 0;
		case '3' : return 0;
		case '4' : return 0;
		case '5' : return 0;
		case '6' : return 0;
		case '7' : return 0;
		case '8' : return 0;
		case '9' : return 0;
		case 'a' : case 'A' : return 0;
		case 'b' : case 'B' : return 0;
		case 'c' : case 'C' : return 0;
		case 'd' : case 'D' : return 0;
		case 'e' : case 'E' : return 0;
		case 'f' : case 'F' : return 0;
		default  : throw new ScannerException("Illegal escape character.", trace(start, 6));
		}
	}
	
	private char scan0() throws ScannerException
	{
		if (sidx >= slen)
		{
			return 0;
		}
		else if (S[sidx] == '\\' && sidx + 1 < slen && S[sidx + 1] == 'u')
		{
			if (sidx + 6 > slen)
				throw new ScannerException("Illegal escape token.", trace(sidx));
			char result = (char) (
					escape(S[sidx + 2], sidx) << 12 |
					escape(S[sidx + 3], sidx) <<  8 |
					escape(S[sidx + 4], sidx) <<  4 |
					escape(S[sidx + 5], sidx)       );
			sidx += 6;
			return result;
		}
		else
		{
			return S[sidx ++];
		}
	}
	
	void consume()
	{
		sacc = true;
	}
	
	void mark()
	{
		smark = sacc ? sidx : slastidx;
	}
	
	void store(int codePoint) throws ScannerException
	{
		if (Character.isSupplementaryCodePoint(codePoint))
		{
			storePair(Character.highSurrogate(codePoint), Character.lowSurrogate(codePoint));
		}
		else switch (codePoint)
		{
		case CP_LEFT_NAMING :
			storePair('<', '|');
			break;
		case CP_RIGHT_NAMING :
			storePair('|', '>');
			break;
		case CP_LEFT_TEMPLATE :
			storePair('<', '[');
			break;
		case CP_RIGHT_TEMPLATE :
			storePair(']', '>');
			break;
		case CP_SINGLE_COMMENT:
			storePair('/', '/');
			break;
		case CP_LEFT_MCOMMENT:
			storePair('/', '*');
			break;
		case CP_RIGHT_MCOMMENT:
			storePair('*', '/');
			break;
		default:
			store((char) codePoint);
			break;
		}
	}
	
	void store(char c) throws ScannerException
	{
		if (cidx >= clen)
			throw new ScannerException("Token is too long!", trace(smark, sidx - smark));
		cbuf[cidx ++] = c;
	}
	
	private void storePair(char left, char right) throws ScannerException
	{
		if (cidx + 2 > clen)
			throw new ScannerException("Token is too long!", trace(smark, sidx - smark));
		cbuf[cidx ++] = left;
		cbuf[cidx ++] = right;
	}
	
	char[] load()
	{
		char[] result = Arrays.copyOf(cbuf, cidx);
		cidx = 0;
		return result;
	}
}
