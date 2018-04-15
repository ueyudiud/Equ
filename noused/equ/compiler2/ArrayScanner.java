/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler2;

import static equ.compiler2.LiteralParser.parseInt1;
import static equ.compiler2.LiteralParser.parseInt2;
import static equ.compiler2.LiteralParser.parseLong1;
import static equ.compiler2.LiteralParser.parseLong2;

import java.util.ArrayList;
import java.util.List;

import equ.compiler2.Literal.LiteralType;

/**
 * @author ueyudiud
 */
class ArrayScanner implements IScanner
{
	private static final int BUFFER_LENGTH = 4096;
	private static final char EOF = '\0';
	
	final String fileName;
	
	private final int slen;
	private final char[] source;
	private int spos;
	private int start;
	
	private int markedPos;
	private char markedChar;
	
	private boolean accessed = false;
	private char C;
	
	private char[] buf = new char[BUFFER_LENGTH];
	private Literal literal;
	private String ident;
	int bpos;
	
	private int[] linemap;
	
	private int prevStartPos, prevEndPos;
	
	boolean enableEscape;
	boolean enableUTF16;
	boolean enableSigned;
	
	ArrayScanner(String f, char[] s, int start, int end)
	{
		fileName = f;
		source = s;
		spos = start;
		slen = end;
	}
	
	private int edigit(int pos)
	{
		switch (source[pos])
		{
		case '0' : return 0;
		case '1' : return 1;
		case '2' : return 2;
		case '3' : return 3;
		case '4' : return 4;
		case '5' : return 5;
		case '6' : return 6;
		case '7' : return 7;
		case '8' : return 8;
		case '9' : return 9;
		case 'a' : case 'A' : return 10;
		case 'b' : case 'B' : return 11;
		case 'c' : case 'C' : return 12;
		case 'd' : case 'D' : return 13;
		case 'e' : case 'E' : return 14;
		case 'f' : case 'F' : return 15;
		default:
			scannerErr(pos, pos, "Escape digit expected.");
			return -1;
		}
	}
	
	private void scanWhenAccess()
	{
		if (accessed)
		{
			scan();
			accessed = false;
		}
	}
	
	private char scan()
	{
		if (spos >= slen)
		{
			return C = EOF;
		}
		C = source[spos ++];
		if (enableEscape && C == '\\' && spos < slen && source[spos] == 'u')
		{
			spos += 5;
			if (spos >= slen)
				scannerErr(spos - 5, slen, "Illegal escape character.");
			else
			{
				C = (char) (
						edigit(spos - 3) << 12 |
						edigit(spos - 2) <<  8 |
						edigit(spos - 1) <<  4 |
						edigit(spos    )       );
			}
		}
		return C;
	}
	
	private char get()
	{
		if (spos >= slen)
		{
			return EOF;
		}
		char c = source[spos];
		if (enableEscape && c == '\\' && spos + 1 < slen && source[spos + 1] == 'u')
		{
			if (spos + 5 >= slen)
				scannerErr(spos, slen, "Illegal escape character.");
			else
			{
				return (char) (
						edigit(spos + 2) << 12 |
						edigit(spos + 3) <<  8 |
						edigit(spos + 4) <<  4 |
						edigit(spos + 5)       );
			}
		}
		return c;
	}
	
	private void skip()
	{
		if (spos >= slen)
		{
			return;
		}
		spos ++;
		if (enableEscape && source[spos] == '\\' && spos < slen && source[spos] == 'u')
		{
			spos += 5;
			if (spos >= slen)
				scannerErr(spos - 5, slen, "Illegal escape character.");
		}
	}
	
	private boolean orSkip(char chr)
	{
		if (spos >= slen)
		{
			return false;
		}
		char c = source[spos];
		if (enableEscape && c == '\\' && spos + 1 < slen && source[spos + 1] == 'u')
		{
			if (spos + 6 >= slen)
				scannerErr(spos, slen, "Illegal escape character.");
			else
			{
				c = (char) (
						edigit(spos + 2) << 12 |
						edigit(spos + 3) <<  8 |
						edigit(spos + 4) <<  4 |
						edigit(spos + 5)       );
				if (c == chr)
				{
					spos += 6;
					return true;
				}
				return false;
			}
		}
		else if (c == chr)
		{
			spos ++;
			return true;
		}
		return false;
	}
	
	private void skipLineComment()
	{
		while (true)
		{
			switch (C)
			{
			case '\r':
				orSkip('\n');
			case '\n':
				accessed = true;
				return;
			default:
				scan();
				break;
			}
		}
	}
	
	private void skipBlockComment()
	{
		while (true)
		{
			switch (C)
			{
			case '*' :
				if (orSkip('/'))
				{
					accessed = true;
					return;
				}
			default:
				scan();
				break;
			}
		}
	}
	
	private void store()
	{
		store(C);
	}
	
	private void store(char chr)
	{
		if (bpos == BUFFER_LENGTH)
			scannerErr(spos, spos, "Identifier is too long.");
		buf[bpos ++] = chr;
	}
	
	private String pop()
	{
		String result = new String(buf, 0, bpos);
		bpos = 0;
		return result;
	}
	
	@Override
	public TokenType peek()
	{
		scanWhenAccess();
		for (start = spos;;)
		{
			switch (C)
			{
			case EOF :
				return TokenType.END;
			case ' ' :
			case '\t':
			case '\f':
			case '\1'  : case '\2'  : case '\3'  : case '\4'  :
			case '\5'  : case '\6'  : case '\7'  : case '\10' :
			case '\16' : case '\17' : case '\21' : case '\22' :
			case '\23' : case '\25' : case '\26' : case '\27' :
			case '\30' : case '\31' : case '\33' : case '\177':
				scan();
				continue;
			case '\r':
			case '\n':
				return TokenType.NL;
			case '.' :
			{
				switch (get())
				{
				case '0' : case '1' : case '2' : case '3' : case '4' :
				case '5' : case '6' : case '7' : case '8' : case '9' :
					return TokenType.LITERAL;
				default:
					return TokenType.DOT;
				}
			}
			case ';' : return TokenType.SEMI;
			case '#' : return TokenType.SHARP;
			case '@' : return TokenType.ATTRIBUTE;
			case '(' : return TokenType.LPANC;
			case ')' : return TokenType.RPANC;
			case '[' : return TokenType.LBRACE;
			case ']' : return TokenType.RBRACE;
			case '{' : return TokenType.LBRACKET;
			case '}' : return TokenType.RBRACKET;
			case '<' : return get() == '|' ? TokenType.LNAMING : TokenType.OPERATOR;
			case '|' : return get() == '>' ? TokenType.RNAMING : TokenType.OPERATOR;
			case '/' :
			{
				switch (get())
				{
				case '/' :
					skipLineComment();
					continue;
				case '*' :
					int pos = spos;
					skip();
					if (get() == '*')
					{
						spos = pos;
						return TokenType.DOC;
					}
					skipBlockComment();
					continue;
				default:
					return TokenType.OPERATOR;
				}
			}
			case '0' : case '1' : case '2' : case '3' : case '4' :
			case '5' : case '6' : case '7' : case '8' : case '9' :
			case '\'':
			case '\"':
				return TokenType.LITERAL;
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
				return peekIdentLike();
			case '+' :
			case '-' :
				if (enableSigned)
				{
					int pos0 = spos;
					while (Character.isWhitespace(C))
						scan();
					spos = pos0;
					if (digit10(C) >= 0)
					{
						return TokenType.LITERAL;
					}
				}
			case '~' : case '!' : case '%' :
			case '^' : case '&' : case '*' :
			case ':' : case '=' : case '?' :
			case '>' :
				return TokenType.OPERATOR;
			default :
				if (C < '\u0080')
				{
					scannerErr(spos, spos, "Errored character.");
					scan();
					continue;
				}
				else
				{
					int i = C;
					if (enableUTF16 && Character.isHighSurrogate(C))
					{
						char low = get();
						if (Character.isLowerCase(low))
						{
							i = Character.toCodePoint(C, low);
						}
					}
					if (Character.isJavaIdentifierStart(i))
					{
						return peekIdentLike();
					}
					else if (((1 << Character.MATH_SYMBOL | 1 << Character.OTHER_SYMBOL) & 1 << Character.getType(i)) != 0)
					{
						return TokenType.OPERATOR;
					}
					else if (Character.isWhitespace(i))
					{
						scan();
						continue;
					}
					else
					{
						scannerErr(spos, i > Character.MAX_VALUE ? spos + 1 : spos, "Illegal character.");
						scan();
						continue;
					}
				}
			}
		}
	}
	
	@Override
	public boolean peek(TokenType type)
	{
		scanWhenAccess();
		for (start = spos;;)
		{
			switch (C)
			{
			case EOF :
				return type == TokenType.END;
			case ' ' :
			case '\t':
			case '\f':
			case '\1'  : case '\2'  : case '\3'  : case '\4'  :
			case '\5'  : case '\6'  : case '\7'  : case '\10' :
			case '\16' : case '\17' : case '\21' : case '\22' :
			case '\23' : case '\25' : case '\26' : case '\27' :
			case '\30' : case '\31' : case '\33' : case '\177':
				scan();
				continue;
			case '\r':
			case '\n':
				scan();
				continue;//The new line token will be ignore in this mode, please use peek == type to take effect.
			case '.' :
			{
				switch (get())
				{
				case '0' : case '1' : case '2' : case '3' : case '4' :
				case '5' : case '6' : case '7' : case '8' : case '9' :
					return type == TokenType.LITERAL;
				default:
					return type == TokenType.DOT;
				}
			}
			case ';' : return type == TokenType.SEMI;
			case '#' : return type == TokenType.SHARP;
			case '@' : return type == TokenType.ATTRIBUTE;
			case '(' : return type == TokenType.LPANC;
			case ')' : return type == TokenType.RPANC;
			case '[' : return type == TokenType.LBRACE;
			case ']' : return type == TokenType.RBRACE;
			case '{' : return type == TokenType.LBRACKET;
			case '}' : return type == TokenType.RBRACKET;
			case '<' : return get() == '|' ? type == TokenType.LNAMING : type == TokenType.OPERATOR;
			case '|' : return get() == '>' ? type == TokenType.RNAMING : type == TokenType.OPERATOR;
			case '/' :
			{
				switch (get())
				{
				case '/' :
					skipLineComment();
					continue;
				case '*' :
					int pos = spos;
					skip();
					if (get() == '*')
					{
						spos = pos;
						return type == TokenType.DOC;
					}
					skipBlockComment();
					continue;
				default:
					return type == TokenType.OPERATOR;
				}
			}
			case '0' : case '1' : case '2' : case '3' : case '4' :
			case '5' : case '6' : case '7' : case '8' : case '9' :
			case '\'':
			case '\"':
				return type == TokenType.LITERAL;
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
				return (type == TokenType.IDENTIFIER || type == TokenType.OPERATOR) && type == peekIdentLike();
			case '+' :
			case '-' :
				if (type != TokenType.OPERATOR && type != TokenType.LITERAL)
					return false;
				if (enableSigned)
				{
					int pos0 = spos;
					while (Character.isWhitespace(C))
						scan();
					spos = pos0;
					if (digit10(C) >= 0)
					{
						return type == TokenType.LITERAL;
					}
				}
			case '~' : case '!' : case '%' :
			case '^' : case '&' : case '*' :
			case ':' : case '=' : case '?' :
			case '>' :
				return type == TokenType.OPERATOR;
			default :
				if (C < '\u0080')
				{
					scannerErr(spos, spos, "Errored character.");
					scan();
					continue;
				}
				else
				{
					int i = C;
					if (enableUTF16 && Character.isHighSurrogate(C))
					{
						char low = get();
						if (Character.isLowerCase(low))
						{
							i = Character.toCodePoint(C, low);
						}
					}
					if (Character.isJavaIdentifierStart(i))
					{
						return (type == TokenType.IDENTIFIER || type == TokenType.OPERATOR) && type == peekIdentLike();
					}
					else if (((1 << Character.MATH_SYMBOL | 1 << Character.OTHER_SYMBOL) & 1 << Character.getType(i)) != 0)
					{
						return type == TokenType.OPERATOR;
					}
					else if (Character.isWhitespace(i))
					{
						scan();
						continue;
					}
					else
					{
						scannerErr(spos, i > Character.MAX_VALUE ? spos + 1 : spos, "Illegal character.");
						scan();
						continue;
					}
				}
			}
		}
	}
	
	@Override
	public TokenType next()
	{
		scanWhenAccess();
		TokenType result;
		label: for (start = spos;;)
		{
			switch (C)
			{
			case EOF :
				result = TokenType.END;
				markScan();
				break label;
			case ' ' :
			case '\t':
			case '\f':
			case '\1'  : case '\2'  : case '\3'  : case '\4'  :
			case '\5'  : case '\6'  : case '\7'  : case '\10' :
			case '\16' : case '\17' : case '\21' : case '\22' :
			case '\23' : case '\25' : case '\26' : case '\27' :
			case '\30' : case '\31' : case '\33' : case '\177':
				scan();
				start = spos;
				continue;
			case '\r':
				if (get() == '\n')
				{
					skip();
				}
			case '\n':
				result = TokenType.NL;
				markScan();
				break label;
			case '.' :
			{
				switch (get())
				{
				case '0' : case '1' : case '2' : case '3' : case '4' :
				case '5' : case '6' : case '7' : case '8' : case '9' :
					store('0');
					store('.');
					markScan();
					scanDecFloat();
					result = TokenType.LITERAL;
					break;
				default:
					result = TokenType.DOT;
					break;
				}
				break label;
			}
			case ';' : result = TokenType.SEMI;			markScan(); break label;
			case '#' : result = TokenType.SHARP;		markScan(); break label;
			case '@' : result = TokenType.ATTRIBUTE;	markScan(); break label;
			case '(' : result = TokenType.LPANC;		markScan(); break label;
			case ')' : result = TokenType.RPANC;		markScan(); break label;
			case '[' : result = TokenType.LBRACE;		markScan(); break label;
			case ']' : result = TokenType.RBRACE;		markScan(); break label;
			case '{' : result = TokenType.LBRACKET;		markScan(); break label;
			case '}' : result = TokenType.RBRACKET;		markScan(); break label;
			case '<' :
				if (orSkip('|'))
				{
					result = TokenType.LNAMING;
					markScan();
					break label;
				}
				//TODO
			case '|' :
				if (orSkip('>'))
				{
					result = TokenType.RNAMING;
					markScan();
					break label;
				}
				//TODO
			case '/' :
			{
				switch (get())
				{
				case '/' :
					skipLineComment();
					continue;
				case '*' :
					int pos = spos;
					skip();
					if (get() == '*')
					{
						spos = pos;
						return TokenType.DOC;
					}
					skipBlockComment();
					continue;
				default:
					return TokenType.OPERATOR;
				}
			}
			case '0' :
				label2 :
				{
				switch (get())
				{
				case 'x' :
					skip();
					scan();
					scanHex();
					break;
				case 'o' :
					skip();
					scan();
					scanOct();
					break;
				case 'b' :
					skip();
					scan();
					scanBin();
					break;
				default :
					break label2;
				}
				result = TokenType.LITERAL;
				break label;
				}
			case '1' : case '2' : case '3' :
			case '4' : case '5' : case '6' :
			case '7' : case '8' : case '9' :
				scanDec();
				result = TokenType.LITERAL;
				break;
			case '\'':
				scanChar();
				result = TokenType.LITERAL;
				break;
			case '\"':
			{
				int pos = spos;
				if (scan() == '\"' && scan() == '\"')
				{
					markScan();
					scanMultiString();
				}
				else
				{
					spos = pos;
					scanString();
				}
				result = TokenType.LITERAL;
				break label;
			}
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
				result = peekIdentLike();
				break label;
			case '+' :
			case '-' :
				if (enableSigned)
				{
					int pos0 = spos;
					while (Character.isWhitespace(C))
						scan();
					if (digit10(C) >= 0)
					{
						store('-');
						store();
						scan();
						scanDec();
						return TokenType.LITERAL;
					}
					spos = pos0;
				}
			case '~' : case '!' : case '%' :
			case '^' : case '&' : case '*' :
			case ':' : case '=' : case '?' :
			case '>' :
				result = TokenType.OPERATOR;
				break;
			default :
				if (C < '\u0080')
				{
					scannerErr(spos, spos, "Errored character.");
					scan();
					continue;
				}
				else
				{
					int i = C;
					if (enableUTF16 && Character.isHighSurrogate(C))
					{
						char low = get();
						if (Character.isLowerCase(low))
						{
							i = Character.toCodePoint(C, low);
						}
					}
					if (Character.isJavaIdentifierStart(i))
					{
						result = peekIdentLike();
						break label;
					}
					else if (((1 << Character.MATH_SYMBOL | 1 << Character.OTHER_SYMBOL) & 1 << Character.getType(i)) != 0)
					{
						result = TokenType.OPERATOR;
						break label;
					}
					else if (Character.isWhitespace(i))
					{
						scan();
						continue;
					}
					else
					{
						scannerErr(spos, i > Character.MAX_VALUE ? spos + 1 : spos, "Illegal character.");
						scan();
						continue;
					}
				}
			}
		}
		prevStartPos = start;
		return result;
	}
	
	@Override
	public void next(TokenType type)
	{
		TokenType type1 = next();
		if (type1 != type)
			scannerErr(prevStartPos, prevEndPos, "Expected " + type + ", got " + type1);
	}
	
	private TokenType peekIdentLike()
	{
		int pos = spos;
		char c = C;
		boolean flag = scanId(true);
		bpos = 0;
		spos = pos;
		C = c;
		return flag ? TokenType.OPERATOR : TokenType.IDENTIFIER;
	}
	
	private void scanBin()
	{
		while (digit2(C) >= 0)
		{
			store();
			scan();
		}
		int bpos1 = bpos;
		if (!scanId(false))
			scannerWarn(start, spos, "The number suffix which operator is not suggested.");
		label: switch (bpos - bpos1)
		{
		case 0 :
			literal = new Literal(LiteralType.INT, parseInt2(buf, 0, bpos1, 1));
			break;
		case 1 :
			switch (buf[bpos1])
			{
			case 'l' : case 'L' :
				literal = new Literal(LiteralType.LONG, parseLong2(buf, 0, bpos1, 1));
				break label;
			}
		default:
			literal = new Literal(LiteralType.CINT, new Object[] {new StringBuilder(bpos1 + 2).append("0b").append(buf, 0, bpos1).toString(), new String(buf, bpos1, bpos - bpos1)});
			break;
		}
		bpos = 0;
	}
	
	private void scanOct()
	{
		while (digit8(C) >= 0)
		{
			store();
			scan();
		}
		int bpos1 = bpos;
		if (!scanId(false))
			scannerWarn(start, spos, "The number suffix which operator is not suggested.");
		label: switch (bpos - bpos1)
		{
		case 0 :
			literal = new Literal(LiteralType.INT, parseInt2(buf, 0, bpos1, 3));
			break;
		case 1 :
			switch (buf[bpos1])
			{
			case 'l' : case 'L' :
				literal = new Literal(LiteralType.LONG, parseLong2(buf, 0, bpos1, 3));
				break label;
			}
		default:
			literal = new Literal(LiteralType.CINT, new Object[] {new StringBuilder(bpos1 + 2).append("0o").append(buf, 0, bpos1).toString(), new String(buf, bpos1, bpos - bpos1)});
			break;
		}
		bpos = 0;
	}
	
	private void scanDec()
	{
		while (digit10(C) >= 0)
		{
			store();
			scan();
		}
		char C1;
		if (C == '.' && digit10(C1 = get()) >= 0)
		{
			C = C1;
			skip();
			scanDecFloat();
		}
		else
		{
			int bpos1 = bpos;
			if (!scanId(false))
				scannerWarn(start, spos, "The number suffix which operator is not suggested.");
			label: switch (bpos - bpos1)
			{
			case 0 :
				literal = new Literal(LiteralType.INT, parseInt1(buf, 0, bpos1, 10));
				break;
			case 1 :
				switch (buf[bpos1])
				{
				case 'l' : case 'L' :
					literal = new Literal(LiteralType.LONG, parseLong1(buf, 0, bpos1, 10));
					break label;
				}
			default:
				literal = new Literal(LiteralType.CINT, new Object[] {new String(buf, 0, bpos1), new String(buf, bpos1, bpos - bpos1)});
				break;
			}
			bpos = 0;
		}
	}
	
	private void scanHex()
	{
		while (digit16(C) >= 0)
		{
			store();
			scan();
		}
		char C1;
		if (C == '.' && digit10(C1 = get()) >= 0)
		{
			C = C1;
			skip();
			scanHexFloat();
		}
		else
		{
			int bpos1 = bpos;
			if (!scanId(false))
				scannerWarn(start, spos, "The number suffix which operator is not suggested.");
			label: switch (bpos - bpos1)
			{
			case 0 :
				literal = new Literal(LiteralType.INT, parseInt2(buf, 0, bpos1, 4));
				break;
			case 1 :
				switch (buf[bpos1])
				{
				case 'l' : case 'L' :
					literal = new Literal(LiteralType.LONG, parseLong2(buf, 0, bpos1, 4));
					break label;
				}
			default:
				literal = new Literal(LiteralType.CINT, new Object[] {new StringBuilder(bpos1 + 2).append("0x").append(buf, 0, bpos1).toString(), new String(buf, bpos1, bpos - bpos1)});
				break;
			}
			bpos = 0;
		}
	}
	
	private void scanDecFloat()
	{
		scanWhenAccess();
		while (digit10(C) >= 0)
		{
			store();
			scan();
		}
		if (C == 'e' || C == 'E')
		{
			scan();
			switch (C)
			{
			case '+' :
			case '-' :
				store();
				scan();
			}
			while (digit10(C) >= 0)
			{
				store();
				scan();
			}
		}
		String lit = "0x" + pop();
		if (!scanId(false))
			scannerWarn(start, spos, "The number suffix which operator is not suggested.");
		String suffix = pop();
		switch (suffix)
		{
		case "f" : case "F" :
			literal = new Literal(LiteralType.FLOAT, Float.valueOf(lit));
			break;
		case "d" : case "D" : case "" :
			literal = new Literal(LiteralType.DOUBLE, Double.valueOf(lit));
			break;
		default:
			literal = new Literal(LiteralType.CFLOAT, new Object[] {lit, suffix});
			break;
		}
	}
	
	private void scanHexFloat()
	{
		while (digit16(C) >= 0)
		{
			store();
			scan();
		}
		if (C == 'p' || C == 'P')
		{
			scan();
			switch (C)
			{
			case '+' :
			case '-' :
				store();
				scan();
			}
			while (digit10(C) >= 0)
			{
				store();
				scan();
			}
		}
		String lit = "0x" + pop();
		if (!scanId(false))
			scannerWarn(start, spos, "The number suffix which operator is not suggested.");
		String suffix = pop();
		switch (suffix)
		{
		case "f" : case "F" :
			literal = new Literal(LiteralType.FLOAT, Float.valueOf(lit));
			break;
		case "d" : case "D" : case "" :
			literal = new Literal(LiteralType.DOUBLE, Double.valueOf(lit));
			break;
		default:
			literal = new Literal(LiteralType.CFLOAT, new Object[] {lit, suffix});
			break;
		}
	}
	
	private void scanChar()
	{
		if (!scanEscape('\'', false))
		{
			scannerErr(start, spos, "Illegal character token.");
		}
		if (!orSkip('\''))
		{
			scannerErr(start, spos, "Missing quote.");
		}
		markScan();
		literal = new Literal(LiteralType.CHAR, Character.valueOf(C));
	}
	
	private void scanString()
	{
		scanWhenAccess();
		while (scanEscape('\"', false))
		{
			store();
			scan();
		}
		if (C != '\"')
		{
			scannerErr(start, spos, "Missing quote.");
		}
		markScan();
		literal = new Literal(LiteralType.STRING, pop());
	}
	
	private void scanMultiString()
	{
		scanWhenAccess();
		byte i = 0;
		while (true)
		{
			if (!scanEscape('\"', true))
				if (++i == 3)
					break;
			store();
			scan();
		}
		markScan();
		literal = new Literal(LiteralType.STRING, new String(buf, 0, bpos - 3));
		bpos = 0;
	}
	
	private boolean scanId(boolean start)
	{
		int last = spos;
		if (start)
			label: while (start)
			{
				switch (C)
				{
				case EOF :
					prevEndPos = last;
					return false;
				case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' : case 'g' : case 'h' :
				case 'i' : case 'j' : case 'k' : case 'l' : case 'm' : case 'n' : case 'o' : case 'p' :
				case 'q' : case 'r' : case 's' : case 't' : case 'u' : case 'v' : case 'w' : case 'x' :
				case 'y' : case 'z' : case 'A' : case 'B' : case 'C' : case 'D' : case 'E' : case 'F' :
				case 'G' : case 'H' : case 'I' : case 'J' : case 'K' : case 'L' : case 'M' : case 'N' :
				case 'O' : case 'P' : case 'Q' : case 'R' : case 'S' : case 'T' : case 'U' : case 'V' :
				case 'W' : case 'X' : case 'Y' : case 'Z' : case '$' : case '_' :
					store();
					scan();
					break label;
				case '\1'  : case '\2'  : case '\3'  : case '\4'  :
				case '\5'  : case '\6'  : case '\7'  : case '\10' :
				case '\16' : case '\17' : case '\21' : case '\22' :
				case '\23' : case '\25' : case '\26' : case '\27' :
				case '\30' : case '\31' : case '\33' : case '\177':
					continue label;
				default:
					if (C < '\u0080')
						return false;
					int i = C;
					char low = 0;
					if (enableUTF16 && Character.isHighSurrogate(C))
					{
						low = get();
						if (Character.isLowerCase(low))
						{
							i = Character.toCodePoint(C, low);
						}
					}
					boolean flag = low == 0;
					if (Character.isJavaIdentifierStart(i))
					{
						store();
						if (flag)
						{
							store(low);
							scan();
						}
						scan();
						break label;
					}
					else if (Character.isIdentifierIgnorable(i))
					{
						scan();
						if (flag) scan();
						continue label;
					}
					else
					{
						prevEndPos = last;
						return false;
					}
				}
			}
		last = spos;
		while (true)
		{
			switch (C)
			{
			case EOF :
				prevEndPos = last;
				return false;
			case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' : case 'g' : case 'h' :
			case 'i' : case 'j' : case 'k' : case 'l' : case 'm' : case 'n' : case 'o' : case 'p' :
			case 'q' : case 'r' : case 's' : case 't' : case 'u' : case 'v' : case 'w' : case 'x' :
			case 'y' : case 'z' : case 'A' : case 'B' : case 'C' : case 'D' : case 'E' : case 'F' :
			case 'G' : case 'H' : case 'I' : case 'J' : case 'K' : case 'L' : case 'M' : case 'N' :
			case 'O' : case 'P' : case 'Q' : case 'R' : case 'S' : case 'T' : case 'U' : case 'V' :
			case 'W' : case 'X' : case 'Y' : case 'Z' : case '$' : case '0' : case '1' : case '2' :
			case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
				store();
				last = spos;
				scan();
				break;
			case '\'':
				if (bpos == 3 && buf[0] == 'i' && buf[1] == 's' && buf[2] == 'n' && orSkip('t'))
				{
					store();
					store('t');
					last = spos;
					scan();
					break;
				}
				else if (bpos == 4 && buf[0] == 'a' && buf[1] == 'r' && buf[2] == 'e' && buf[3] == 'n' && orSkip('t'))
				{
					store();
					store('t');
					last = spos;
					scan();
					break;
				}
				prevEndPos = last;
				return false;
			case '_' :
				store();
				last = spos;
				scan();
				{
					int pos = spos;
					boolean flag = enableUTF16 && Character.isHighSurrogate(C);
					int i = C;
					char low = 0;
					if (flag)
					{
						low = get();
						flag &= Character.isLowerCase(low);
						if (flag)
						{
							i = Character.toCodePoint(C, low);
						}
					}
					if (isOperator(i))
					{
						scanOp();
						return pos != spos;
					}
				}
				break;
			case '\1'  : case '\2'  : case '\3'  : case '\4'  :
			case '\5'  : case '\6'  : case '\7'  : case '\10' :
			case '\16' : case '\17' : case '\21' : case '\22' :
			case '\23' : case '\25' : case '\26' : case '\27' :
			case '\30' : case '\31' : case '\33' : case '\177':
				break;
			default:
				if (C < '\u0080')
				{
					prevEndPos = last;
					return false;
				}
				boolean flag = enableUTF16 && Character.isHighSurrogate(C);
				int i = C;
				char low = 0;
				if (flag)
				{
					low = get();
					flag &= Character.isLowerCase(low);
					if (flag)
					{
						i = Character.toCodePoint(C, low);
					}
				}
				if (Character.isJavaIdentifierPart(i))
				{
					store();
					last = spos;
					if (flag)
					{
						store(low);
						scan();
					}
					scan();
					continue;
				}
				else if (Character.isIdentifierIgnorable(i))
				{
					last = spos;
					scan();
					if (flag) scan();
					continue;
				}
				else
				{
					prevEndPos = last;
					return false;
				}
			}
		}
	}
	
	private void scanOp()
	{
		int last = spos;
		while (true)
		{
			switch (C)
			{
			case EOF :
				prevEndPos = last;
				return;
			case '<' :
				if (get() == '|')
				{
					prevEndPos = last;
					return;
				}
				store();
				last = spos;
				scan();
				break;
			case '|' :
				if (get() == '>')
				{
					prevEndPos = last;
					return;
				}
				store();
				last = spos;
				scan();
				break;
			case '/' :
				switch (get())
				{
				case '/' :
				case '*' :
					prevEndPos = last;
					return;
				}
			case '~' : case '!' : case '%' :
			case '^' : case '&' : case '*' :
			case ':' : case '+' : case '-' :
			case '=' : case '?' : case '>' :
				store();
				last = spos;
				scan();
				break;
			case '\1'  : case '\2'  : case '\3'  : case '\4'  :
			case '\5'  : case '\6'  : case '\7'  : case '\10' :
			case '\16' : case '\17' : case '\21' : case '\22' :
			case '\23' : case '\25' : case '\26' : case '\27' :
			case '\30' : case '\31' : case '\33' : case '\177':
				scan();
				last = spos;
				break;
			default:
				if (C < '\u0080')
				{
					prevEndPos = last;
					return;
				}
				boolean flag = enableUTF16 && Character.isHighSurrogate(C);
				int i = C;
				char low = 0;
				if (flag)
				{
					low = get();
					flag &= Character.isLowerCase(low);
					if (flag)
					{
						i = Character.toCodePoint(C, low);
					}
				}
				if (((1 << Character.MATH_SYMBOL | 1 << Character.OTHER_SYMBOL) & 1 << Character.getType(i)) != 0)
				{
					store();
					last = spos;
					if (flag)
					{
						store(low);
						scan();
					}
					scan();
					continue;
				}
				else if (Character.isIdentifierIgnorable(i))
				{
					scan();
					if (flag) scan();
					last = spos;
					continue;
				}
				else
				{
					prevEndPos = last;
					return;
				}
			}
		}
	}
	
	private static boolean isOperator(int chr)
	{
		switch (chr)
		{
		case '~' : case '!' : case '%' : case '^' :
		case '&' : case '*' : case '|' : case ':' :
		case '<' : case '>' : case '+' : case '-' :
		case '=' : case '?' : case '/' :
			return true;
		default:
			return ((1 << Character.MATH_SYMBOL | 1 << Character.OTHER_SYMBOL) & 1 << Character.getType(chr)) != 0;
		}
	}
	
	private static short digit2(char chr)
	{
		return (short) ((chr & ~0x1) == 0x30 ? chr - '0' : -1);
	}
	
	private static short digit8(char chr)
	{
		switch (chr)
		{
		case '0' : return 0;
		case '1' : return 1;
		case '2' : return 2;
		case '3' : return 3;
		case '4' : return 4;
		case '5' : return 5;
		case '6' : return 6;
		case '7' : return 7;
		default : return -1;
		}
	}
	
	private static short digit10(char chr)
	{
		switch (chr)
		{
		case '0' : return 0;
		case '1' : return 1;
		case '2' : return 2;
		case '3' : return 3;
		case '4' : return 4;
		case '5' : return 5;
		case '6' : return 6;
		case '7' : return 7;
		case '8' : return 8;
		case '9' : return 9;
		default : return -1;
		}
	}
	
	private static short digit16(char chr)
	{
		switch (chr)
		{
		case '0' : return 0;
		case '1' : return 1;
		case '2' : return 2;
		case '3' : return 3;
		case '4' : return 4;
		case '5' : return 5;
		case '6' : return 6;
		case '7' : return 7;
		case '8' : return 8;
		case '9' : return 9;
		case 'a' : case 'A' : return 10;
		case 'b' : case 'B' : return 11;
		case 'c' : case 'C' : return 12;
		case 'd' : case 'D' : return 13;
		case 'e' : case 'E' : return 14;
		case 'f' : case 'F' : return 15;
		default : return -1;
		}
	}
	
	private boolean scanEscape(char escape, boolean enableNL)
	{
		int spos1 = spos;
		switch (C)
		{
		case '\\':
			switch (scan())
			{
			case 'r' : C = '\r'; break;
			case 'n' : C = '\n'; break;
			case 'f' : C = '\f'; break;
			case 't' : C = '\t'; break;
			case 'b' : C = '\b'; break;
			case '\'': C = '\''; break;
			case '\"': C = '\"'; break;
			case '\\': C = '\\'; break;
			case '`' : C =  '`'; break;
			case '0' : case '1' : case '2' : case '3' :
				int i = C - '0';
				switch (C = scan())
				{
				case '0' : case '1' : case '2' : case '3' :
				case '4' : case '5' : case '6' : case '7' :
					skip();
					i = i << 4 | C - '0';
					switch (C = scan())
					{
					case '0' : case '1' : case '2' : case '3' :
					case '4' : case '5' : case '6' : case '7' :
						skip();
						i = i << 4 | C - '0';
					}
				}
				C = (char) i;
				break;
			case '4' : case '5' : case '6' : case '7' :
				i = C - '0';
				switch (C = scan())
				{
				case '0' : case '1' : case '2' : case '3' :
				case '4' : case '5' : case '6' : case '7' :
					skip();
					i = i << 4 | C - '0';
				}
				C = (char) i;
				break;
			default:
				scannerErr(spos1, spos, "Unknown escape char.");
				break;
			}
			return true;
		case '\r':
		case '\n':
			return enableNL;
		case EOF :
			return false;
		default :
			return C != escape;
		}
	}
	
	private void markScan()
	{
		accessed = true;
		prevEndPos = spos;
	}
	
	@Override
	public String ident()
	{
		return ident;
	}
	
	@Override
	public void setIdent(String value)
	{
		ident = value;
	}
	
	private void generatePositionMap()
	{
		if (linemap == null)
		{
			List<Integer> list = new ArrayList<>();
			list.add(0);
			int i = 0;
			while (i < buf.length)
			{
				switch (buf[i++])
				{
				case '\r':
					if (i < buf.length && buf[i] == '\n')
						i ++;
				case '\n':
					list.add(i);
				}
			}
			linemap = list.stream().mapToInt(Integer::intValue).toArray();
		}
	}
	
	private static int floor(int[] buf, int idx)
	{
		int low = 0;
		int high = buf.length - 1;
		while (low < high)
		{
			int mid = ((low + high + 1) >>> 1);
			int midVal = buf[mid];
			if (midVal > idx)
			{
				if (low == mid - 1)
					break;
				high = mid - 1;
			}
			else
			{
				low = mid;
				if (midVal < idx)
					break;
			}
		}
		return low;
	}
	
	private SourceTrace resolveTrace(int start, int end)
	{
		generatePositionMap();
		int ln1 = floor(linemap, start);
		int col1 = start - linemap[ln1] + 1;
		int ln2 = floor(linemap, end);
		int col2 = end - linemap[ln2] + 1;
		return new SourceTrace(fileName, start, ln1, col1, end, ln2, col2);
	}
	
	void scannerWarn(int start, int end, String msg)
	{
		resolveTrace(start, end);
	}
	
	void scannerErr(int start, int end, String msg)
	{
		resolveTrace(start, end);
	}
	
	@Override
	public boolean markSupported()
	{
		return true;
	}
	
	@Override
	public void mark()
	{
		markedPos = spos;
		markedChar = C;
	}
	
	@Override
	public void reset()
	{
		spos = markedPos;
		C = markedChar;
	}
	
	@Override
	public Literal literal()
	{
		return literal;
	}
	
	@Override
	public void setLiteral(Literal literal)
	{
		this.literal = literal;
	}
}
