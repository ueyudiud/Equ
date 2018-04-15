/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler.scanner;

import java.util.Arrays;

import equ.compiler.CompileContext;
import equ.compiler.ILexeme;
import equ.compiler.IScanner;
import equ.compiler.LexemeChar;
import equ.compiler.LexemeFloat;
import equ.compiler.LexemeIdent;
import equ.compiler.LexemeInt;
import equ.compiler.LexemeMacroStart;
import equ.compiler.LexemeMisc;
import equ.compiler.LexemeOperator;
import equ.compiler.LexemeString;
import equ.compiler.ScannerException;
import equ.util.Numbers;

/**
 * @author ueyudiud
 */
public
class Scanner extends CharScanner implements IScanner
{
	final CompileContext factory;
	boolean macroMode;
	
	private ILexeme lexeme;
	private byte[] dbuf = new byte[dlen];
	private static final int dlen = 128;
	private int didx = 0;
	
	public
	Scanner(CompileContext factory, String path, char[] source)
	{
		super(path, source);
		this.factory = factory;
		enableUTF8Escape = factory.isOptionEnable(CompileContext.UTF8_ESCAPE);
	}
	
	@Override
	public String path()
	{
		return P;
	}
	
	public ILexeme lexeme()
	{
		ILexeme result = lexeme;
		lexeme = null;
		return result;
	}
	
	public void next() throws ScannerException
	{
		while (sidx < slen)
		{
			switch (peek())
			{
			case '\u0000':
			case '\u0001': case '\u0002': case '\u0003': case '\u0004':
			case '\u0005': case '\u0006': case '\u0007': case '\u0008':
			case '\u000E': case '\u000F': case '\u0011': case '\u0012':
			case '\u0013': case '\u0015': case '\u0016': case '\u0017':
			case '\u0018': case '\u0019': case '\u001B': case '\u007F':
			case ' ' : case '\t': case '\f':
				consume();
				continue;
			case '\r':
				if (macroMode)
				{
					macroMode = false;
					mark();
					lexeme = LexemeMisc.macroEnding(trace(sidx, 1));
					return;
				}
				consume();
				if (peek() == '\n')
				{
					consume();
				}
				if (peek() == '#')
				{
					macroMode = true;
					mark();
					consume();
					next$identifier();
					lexeme = new LexemeMacroStart(load(), trace());
					return;
				}
				continue;
			case '\n':
				if (macroMode)
				{
					macroMode = false;
					mark();
					lexeme = LexemeMisc.macroEnding(trace(sidx, 1));
					return;
				}
				consume();
				if (peek() == '#')
				{
					macroMode = true;
					mark();
					consume();
					next$identifier();
					lexeme = new LexemeMacroStart(load(), trace());
				}
				continue;
			case CP_SINGLE_COMMENT:
				consume();
				label: while (sidx < slen)
				{
					switch (peek())
					{
					case '\r': case '\n':
						break label;
					}
					consume();
				}
				continue;
			case CP_LEFT_MCOMMENT:
				consume();
				label:
				{
					while (sidx < slen)
					{
						if (peek() == CP_RIGHT_MCOMMENT)
						{
							break label;
						}
						consume();
					}
					factory.error("The multi comment is missing right mark.", trace(slen - 1));
				}
				consume();
				continue;
			case '(' :
				mark();
				consume();
				lexeme = LexemeMisc.leftPanc(trace());
				return;
			case ')' :
				mark();
				consume();
				lexeme = LexemeMisc.rightPanc(trace());
				return;
			case '[' :
				mark();
				consume();
				lexeme = LexemeMisc.leftBrace(trace());
				return;
			case ']' :
				mark();
				consume();
				lexeme = LexemeMisc.rightBrace(trace());
				return;
			case '{' :
				mark();
				consume();
				lexeme = LexemeMisc.leftBracket(trace());
				return;
			case '}' :
				mark();
				consume();
				lexeme = LexemeMisc.rightBracket(trace());
				return;
			case '.' :
			{
				mark();
				consume();
				if (check$number(false))
				{
					dstore(Numbers.MODE_DEC);
					dstore(0);
					dstore(Numbers.DOT);
					next$dec_digits(true);
					lexeme = new LexemeFloat(dload(), trace());
				}
				else
				{
					lexeme = LexemeMisc.dot(trace());
				}
				return;
			}
			case ',' :
				mark();
				consume();
				lexeme = LexemeMisc.comma(trace());
				return;
			case ';' :
				mark();
				consume();
				lexeme = LexemeMisc.semi(trace());
				return;
			case '@' :
				mark();
				consume();
				lexeme = LexemeMisc.at(trace());
				return;
			case CP_LEFT_NAMING :
				mark();
				consume();
				lexeme = LexemeMisc.leftNaming(trace());
				return;
			case CP_RIGHT_NAMING :
				mark();
				consume();
				lexeme = LexemeMisc.rightNaming(trace());
				return;
			case CP_LEFT_TEMPLATE :
				mark();
				consume();
				lexeme = LexemeMisc.leftTemplate(trace());
				return;
			case CP_RIGHT_TEMPLATE :
				mark();
				consume();
				lexeme = LexemeMisc.rightTemplate(trace());
				return;
			case '"' :
				mark();
				consume();
				next$singlelineutf8('"');
				lexeme = new LexemeString(load(), trace());
				return;
			case '\'':
				mark();
				consume();
				next$singlelineutf8('\'');
				if (cidx != 1)
				{
					factory.error("Illegal character token.", trace());
				}
				lexeme = new LexemeChar(load()[0], trace());
				return;
			case 'a' : case 'b' : case 'c' : case 'd' :
			case 'e' : case 'f' : case 'g' : case 'h' :
			case 'i' : case 'j' : case 'k' : case 'l' :
			case 'm' : case 'n' : case 'o' : case 'p' :
			case 'q' : case 'r' : case 's' : case 't' :
			case 'u' : case 'v' : case 'w' : case 'x' :
			case 'y' : case 'z' : case 'A' : case 'B' :
			case 'C' : case 'D' : case 'E' : case 'F' :
			case 'G' : case 'H' : case 'I' : case 'J' :
			case 'K' : case 'L' : case 'M' : case 'N' :
			case 'O' : case 'P' : case 'Q' : case 'R' :
			case 'S' : case 'T' : case 'U' : case 'V' :
			case 'W' : case 'X' : case 'Y' : case 'Z' :
			case '$' :
			case '_' :
				mark();
				store((char) C);
				consume();
				next$identifier();
				if (cbuf[cidx - 1] == '_' && check$operator(false))
				{
					next$operator();
					lexeme = new LexemeOperator(load(), trace());
				}
				else
				{
					lexeme = new LexemeIdent(load(), trace());
				}
				return;
			case '0' :
				mark();
				consume();
				switch (peek())
				{
				case 'x':
					int mark = slastidx;
					consume();
					if (check$hex_number(false))
					{
						dstore(Numbers.MODE_HEX);
						lexeme = next$hex_number() ? new LexemeFloat(dload(), trace()) : new LexemeInt(dload(), trace());
					}
					else
					{
						sidx = mark;
						lexeme = new LexemeInt(new byte[] {Numbers.MODE_DEC, '\0'}, trace());
					}
					return;
				default:
					dstore(Numbers.MODE_DEC);
					dstore(0);
					consume();
					lexeme = next$dec_number(false) ? new LexemeFloat(dload(), trace()) : new LexemeInt(dload(), trace());
					return;
				}
			case '1' : case '2' : case '3' :
			case '4' : case '5' : case '6' :
			case '7' : case '8' : case '9' :
				mark();
				dstore(Numbers.MODE_DEC);
				dstore(C - '0');
				consume();
				lexeme = next$dec_number(false) ? new LexemeFloat(dload(), trace()) : new LexemeInt(dload(), trace());
				return;
			case '<' : case '>' : case '|' : case '?' : case '/' :
			case '~' : case '!' : case '%' : case '^' : case '&' :
			case '*' : case '+' : case '-' : case ':' : case '=' :
				mark();
				store((char) C);
				consume();
				next$operator();
				lexeme = new LexemeOperator(load(), trace());
				return;
			default :
				if (check$identifier())
				{
					next$identifier();
					if (cbuf[cidx - 1] == '_' && check$operator(true))
					{
						next$operator();
						lexeme = new LexemeOperator(load(), trace());
					}
					else
					{
						lexeme = new LexemeIdent(load(), trace());
					}
					return;
				}
				else if (check$operator(true))
				{
					next$operator();
					lexeme = new LexemeOperator(load(), trace());
					return;
				}
				else if (check$number(true))
				{
					dstore(Numbers.MODE_DEC);
					dstore(Character.digit(C, 10));
					consume();
					lexeme = next$dec_number(false) ? new LexemeFloat(dload(), trace()) : new LexemeInt(dload(), trace());
					return;
				}
				else if (Character.isWhitespace(C))
				{
					consume();
					break;
				}
				throw new ScannerException("Unknown character.", trace(slastidx, sidx - slastidx));
			}
		}
		lexeme = LexemeMisc.end(trace(slen));
	}
	
	private void next$singlelineutf8(char end) throws ScannerException
	{
		while (sidx < slen)
		{
			switch (peek())
			{
			case '\n' :
			case '\r' :
				factory.error("Missing a quote '" + end + "'", trace());
				return;
			case '\\' :
				consume();
				switch (peek())
				{
				case '\\':
					store('\\');
					consume();
					break;
				case '"':
					store('"');
					consume();
					break;
				case '\'':
					store('\'');
					consume();
					break;
				case 'r':
					store('\r');
					consume();
					break;
				case 'n':
					store('\n');
					consume();
					break;
				case 'f':
					store('\f');
					consume();
					break;
				case 't':
					store('\t');
					consume();
					break;
				case 'b':
					store('\b');
					consume();
					break;
				case 'a':
					store('\7');
					consume();
					break;
				case '0': case '1': case '2': case '3':
				{
					int i = C - '0';
					consume();
					switch (peek())
					{
					case '0': case '1': case '2': case '3':
					case '4': case '5': case '6': case '7':
						i = i << 3 | C - '0';
						consume();
						switch (peek())
						{
						case '0': case '1': case '2': case '3':
						case '4': case '5': case '6': case '7':
							i = i << 3 | C - '0';
							break;
						}
						break;
					}
					store((char) i);
					break;
				}
				case '4': case '5': case '6': case '7':
				{
					int i = C - '0';
					consume();
					switch (peek())
					{
					case '0': case '1': case '2': case '3':
					case '4': case '5': case '6': case '7':
						i = i << 3 | C - '0';
						consume();
						break;
					}
					store((char) i);
					break;
				}
				default:
					factory.error("Illegal escape character.", trace());
					consume();
					break;
				}
			default:
				if (C == end)
				{
					consume();
					return;
				}
				store(C);
				consume();
				break;
			}
		}
	}
	
	private boolean check$identifier() throws ScannerException
	{
		switch (peek())
		{
		case 'a' : case 'b' : case 'c' : case 'd' :
		case 'e' : case 'f' : case 'g' : case 'h' :
		case 'i' : case 'j' : case 'k' : case 'l' :
		case 'm' : case 'n' : case 'o' : case 'p' :
		case 'q' : case 'r' : case 's' : case 't' :
		case 'u' : case 'v' : case 'w' : case 'x' :
		case 'y' : case 'z' : case 'A' : case 'B' :
		case 'C' : case 'D' : case 'E' : case 'F' :
		case 'G' : case 'H' : case 'I' : case 'J' :
		case 'K' : case 'L' : case 'M' : case 'N' :
		case 'O' : case 'P' : case 'Q' : case 'R' :
		case 'S' : case 'T' : case 'U' : case 'V' :
		case 'W' : case 'X' : case 'Y' : case 'Z' :
		case '_' : case '$' :
			mark();
			store((char) C);
			consume();
			return true;
		default:
			if (Character.isJavaIdentifierStart(C))
			{
				mark();
				store(C);
				consume();
				return true;
			}
			return false;
		}
	}
	
	private void next$identifier() throws ScannerException
	{
		while (sidx < slen)
		{
			switch (peek())
			{
			case '\u0000':
			case '\u0001': case '\u0002': case '\u0003': case '\u0004':
			case '\u0005': case '\u0006': case '\u0007': case '\u0008':
			case '\u000E': case '\u000F': case '\u0011': case '\u0012':
			case '\u0013': case '\u0015': case '\u0016': case '\u0017':
			case '\u0018': case '\u0019': case '\u001B': case '\u007F':
				consume();
				break;
			case '\u001A'://EOI
				consume();
				return;
			case 'a' : case 'b' : case 'c' : case 'd' :
			case 'e' : case 'f' : case 'g' : case 'h' :
			case 'i' : case 'j' : case 'k' : case 'l' :
			case 'm' : case 'n' : case 'o' : case 'p' :
			case 'q' : case 'r' : case 's' : case 't' :
			case 'u' : case 'v' : case 'w' : case 'x' :
			case 'y' : case 'z' : case 'A' : case 'B' :
			case 'C' : case 'D' : case 'E' : case 'F' :
			case 'G' : case 'H' : case 'I' : case 'J' :
			case 'K' : case 'L' : case 'M' : case 'N' :
			case 'O' : case 'P' : case 'Q' : case 'R' :
			case 'S' : case 'T' : case 'U' : case 'V' :
			case 'W' : case 'X' : case 'Y' : case 'Z' :
			case '_' : case '$' :
			case '0' : case '1' : case '2' : case '3' : case '4' :
			case '5' : case '6' : case '7' : case '8' : case '9' :
				store((char) C);
				consume();
				break;
			default:
				if (C < '\u0100')
				{
					return;
				}
				else if (Character.isJavaIdentifierPart(C))
				{
					store(C);
					consume();
					break;
				}
				else if (Character.isIdentifierIgnorable(C))
				{
					consume();
					break;
				}
				else
				{
					return;
				}
			}
		}
	}
	
	private boolean check$operator(boolean mark) throws ScannerException
	{
		switch (peek())
		{
		case '<' : case '>' : case '|' : case '?' : case '/' :
		case '~' : case '!' : case '%' : case '^' : case '&' :
		case '*' : case '+' : case '-' : case ':' :
			if (mark)
			{
				mark();
			}
			store((char) C);
			consume();
			return true;
		case CP_RIGHT_MCOMMENT:
			if (mark)
			{
				mark();
			}
			store(CP_RIGHT_MCOMMENT);
			consume();
			return true;
		default:
			if (C < '\u0100')
			{
				return false;
			}
			else if (((1 << Character.getType(C)) |
					(1 << Character.MATH_SYMBOL | 1 << Character.OTHER_SYMBOL)) != 0)
			{
				if (mark)
				{
					mark();
				}
				store(C);
				consume();
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	private void next$operator() throws ScannerException
	{
		while (sidx < slen)
		{
			switch (peek())
			{
			case '\u0000':
			case '\u0001': case '\u0002': case '\u0003': case '\u0004':
			case '\u0005': case '\u0006': case '\u0007': case '\u0008':
			case '\u000E': case '\u000F': case '\u0011': case '\u0012':
			case '\u0013': case '\u0015': case '\u0016': case '\u0017':
			case '\u0018': case '\u0019': case '\u001B': case '\u007F':
				consume();
				continue;
			case '\u001A'://EOI
				consume();
				return;
			case '<' : case '>' : case '|' : case '?' : case '/' :
			case '~' : case '!' : case '%' : case '^' : case '&' :
			case '*' : case '+' : case '-' : case ':' :
				store((char) C);
				consume();
				break;
			case CP_RIGHT_MCOMMENT:
				store(C);
				consume();
				break;
			default:
				if (C < '\u0100')
				{
					return;
				}
				else if (((1 << Character.getType(C)) |
						(1 << Character.MATH_SYMBOL | 1 << Character.OTHER_SYMBOL)) != 0)
				{
					store(C);
					consume();
					break;
				}
				else if (Character.isIdentifierIgnorable(C))
				{
					consume();
					break;
				}
				else
				{
					return;
				}
			}
		}
	}
	
	private boolean check$number(boolean mark) throws ScannerException
	{
		switch (peek())
		{
		case '0' : case '1' : case '2' : case '3' : case '4' :
		case '5' : case '6' : case '7' : case '8' : case '9' :
			if (mark)
			{
				mark();
			}
			return true;
		default:
			if (Character.digit(C, 10) != -1)
			{
				if (mark)
				{
					mark();
				}
				return true;
			}
			return false;
		}
	}
	
	private boolean check$hex_number(boolean mark) throws ScannerException
	{
		switch (peek())
		{
		case '0' : case '1' : case '2' : case '3' :
		case '4' : case '5' : case '6' : case '7' :
			if (mark)
			{
				mark();
			}
			return true;
		default:
			if (Character.digit(C, 10) != -1)
			{
				if (mark)
				{
					mark();
				}
				return true;
			}
			return false;
		}
	}
	
	private boolean next$dec_number(boolean start) throws ScannerException
	{
		next$dec_digits(start);
		if (peek() == '.')
		{
			int mark = sidx;
			consume();
			if (check$number(false))
			{
				dstore(Numbers.DOT);
				next$dec_digits(true);
				switch (peek())
				{
				case 'e' : case 'E' :
					dstore(Numbers.EXP);
					consume();
					switch (peek())
					{
					case '+' :
						dstore(Numbers.POS);
						consume();
						break;
					case '-' :
						dstore(Numbers.NEG);
						consume();
						break;
					}
					next$dec_digits(true);
					break;
				}
				return true;
			}
			else
			{
				sidx = mark;
				C = '.';
				sacc = false;
			}
		}
		return false;
	}
	
	private boolean next$hex_number() throws ScannerException
	{
		next$dec_digits(true);
		if (peek() == '.')
		{
			int mark = sidx;
			consume();
			if (Character.digit(C, 16) != -1)
			{
				dstore(Numbers.DOT);
				next$hex_digits();
				switch (peek())
				{
				case 'p' : case 'P' :
					dstore(Numbers.EXP);
					consume();
					switch (peek())
					{
					case '+' :
						dstore(Numbers.POS);
						consume();
						break;
					case '-' :
						dstore(Numbers.NEG);
						consume();
						break;
					}
					next$dec_digits(true);
					break;
				}
				return true;
			}
			else
			{
				sidx = mark;
				C = '.';
				sacc = false;
			}
		}
		return false;
	}
	
	private void next$dec_digits(boolean start) throws ScannerException
	{
		boolean underline = start;
		while (sidx < slen)
		{
			switch (peek())
			{
			case '0' : case '1' : case '2' : case '3' : case '4' :
			case '5' : case '6' : case '7' : case '8' : case '9' :
				dstore(C - '0');
				consume();
				underline = false;
				break;
			case '_' :
				if (underline)
				{
					factory.error("'_' character can not be linked or at the start of number token.", trace(1));
				}
				consume();
				underline = true;
				break;
			default:
				int digit;
				if (C >= '\u0100')
				{
					if ((digit = Character.digit(C, 10)) != -1)
					{
						dstore(digit);
						consume();
						factory.warn("The non-ISO-LATIN-1 digit is not suggested for number token.", trace());
						break;
					}
					else if (Character.isIdentifierIgnorable(C))
					{
						consume();
						break;
					}
				}
				if (underline)
				{
					factory.error("Can not let '_' at end of a number token.", trace());
				}
				return;
			}
		}
	}
	
	private void next$hex_digits() throws ScannerException
	{
		boolean underline = true;
		while (sidx < slen)
		{
			switch (peek())
			{
			case '0' : case '1' : case '2' : case '3' : case '4' :
			case '5' : case '6' : case '7' : case '8' : case '9' :
				dstore(C - '0');
				consume();
				underline = false;
				break;
			case 'a' : case 'b' : case 'c' :
			case 'd' : case 'e' : case 'f' :
				dstore(C - 'a' + 10);
				consume();
				underline = false;
				break;
			case 'A' : case 'B' : case 'C' :
			case 'D' : case 'E' : case 'F' :
				dstore(C - 'A' + 10);
				consume();
				underline = false;
				break;
			case '_' :
				if (underline)
				{
					factory.error("'_' character can not be linked or at the start of number token.", trace(1));
				}
				consume();
				underline = true;
				break;
			default:
				int digit;
				if (C >= '\u0100')
				{
					if ((digit = Character.digit(C, 16)) != -1)
					{
						store(digit);
						consume();
						factory.warn("The non-ISO-LATIN-1 digit is not suggested for number token.", trace());
						break;
					}
					else if (Character.isIdentifierIgnorable(C))
					{
						consume();
						break;
					}
				}
				if (underline)
				{
					factory.error("Can not let '_' at end of a number token.", trace());
				}
				return;
			}
		}
	}
	
	private void dstore(int digit) throws ScannerException
	{
		if (didx >= dlen)
			throw new ScannerException("Number is too long to store.", trace());
		dbuf[didx ++] = (byte) digit;
	}
	
	private byte[] dload()
	{
		byte[] result = Arrays.copyOf(dbuf, didx);
		didx = 0;
		return result;
	}
}
