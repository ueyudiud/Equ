/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler1;

import java.util.ArrayList;
import java.util.List;

import equ.compiler1.Literal.LiteralType;

/**
 * @author ueyudiud
 */
abstract class AbstractScanner implements IScanner
{
	protected static final char EOF = '\0';
	
	protected final IScannerFactory factory;
	protected LexType type = LexType.NL;
	protected Identifier ident = null;
	protected Literal lit = null;
	protected Comment comment = null;
	
	AbstractScanner(IScannerFactory factory)
	{
		this.factory = factory;
	}
	
	@Override
	public IScannerFactory getFactory()
	{
		return factory;
	}
	
	@Override
	public LexType type()
	{
		return type;
	}
	
	@Override
	public void setIdent(Identifier identifier)
	{
		ident = identifier;
	}
	
	@Override
	public Identifier ident()
	{
		return ident;
	}
	
	@Override
	public void setLit(Literal literal)
	{
		lit = literal;
	}
	
	@Override
	public Literal lit()
	{
		return lit;
	}
	
	@Override
	public void setCom(Comment comment)
	{
		this.comment = comment;
	}
	
	@Override
	public Comment com()
	{
		return comment;
	}
	
	public void warn(String cause, Object... formats)
	{
		factory.warn(this, cause, formats);
	}
	
	public void error(String cause, Object... formats)
	{
		factory.error(this, cause, formats);
	}
	
	SourcePosition errPos;
	
	@Override
	public void setErrPos(SourcePosition pos)
	{
		errPos = pos;
	}
	
	@Override
	public SourcePosition err()
	{
		return errPos == null ? new SourcePosition("<unknown file>", 0, -1, new char[0], 0, 0) : errPos;
	}
}

class FileScanner extends AbstractScanner
{
	private static final int BUFFER_LENGTH = 4096;
	
	final String fileName;
	private final char[] buf;
	private int pos;
	
	private int[] linemap;
	
	private int currentPos, prevPos;
	
	private int markPos, markPrevPos;
	private char markC;
	
	private char C = '\n';
	
	private char[] ibuf = new char[BUFFER_LENGTH];
	private int ibufPos;
	
	private boolean enableEscape;
	private boolean enableUTF16;
	
	FileScanner(String fileName, char[] buf, boolean enableEscape, IScannerFactory factory)
	{
		super(factory);
		this.fileName = fileName;
		this.buf = buf;
		this.enableEscape = enableEscape;
	}
	
	@Override
	public void scan()
	{
		prevPos = currentPos;
		currentPos = pos;
		while (pos <= buf.length)
		{
			switch (C)
			{
			case EOF :
				type = LexType.END;
				return;
			case ' ' :
			case '\t':
			case '\f':
			case '\1' : case '\2' : case '\3' : case '\4' :
			case '\5' : case '\6' : case '\7' : case '\10':
			case '\16': case '\17': case '\21': case '\22':
			case '\23': case '\25': case '\26': case '\27':
			case '\30': case '\31': case '\33': case '\177':
				currentPos = pos;
				poll();
				continue;
			case '\r':
				if (peek() == '\n')
					skip();
			case '\n':
				skipAndSetType(LexType.NL);
				return;
			case '#' :
				skipAndSetType(LexType.SHARP);
				return;
			case '@' :
				poll();
				scanIdentifier();
				type = LexType.ATTRIBUTE;
				return;
			case ',' :
				skipAndSetType(LexType.COMMA);
				return;
			case '.' :
				switch (poll())
				{
				case '0' :
				case '1' : case '2' : case '3' :
				case '4' : case '5' : case '6' :
				case '7' : case '8' : case '9' :
					store('0');
					store('.');
					scanDecFloatNumber();
					return;
				default:
					type = LexType.DOT;
					return;
				}
			case ';' :
				skipAndSetType(LexType.SEMI);
				return;
			case '(' :
				skipAndSetType(LexType.LPANC);
				return;
			case '[' :
				skipAndSetType(LexType.LBRACE);
				return;
			case '{' :
				skipAndSetType(LexType.LBRACKET);
				return;
			case ')' :
				skipAndSetType(LexType.RPANC);
				return;
			case ']' :
				skipAndSetType(LexType.RBRACE);
				return;
			case '}' :
				skipAndSetType(LexType.RBRACKET);
				return;
			case '<' :
				if (peek() == '|')
				{
					skip();
					skipAndSetType(LexType.LNAMING);
					return;
				}
				scanOperator();
				return;
			case '|' :
				if (peek() == '>')
				{
					skip();
					skipAndSetType(LexType.RNAMING);
					return;
				}
				scanOperator();
				return;
			case '0' :
				switch (peek())
				{
				case 'x' : case 'X' :
					skip();
					scanHexNumber();
					return;
				case 'o' : case 'O' :
					skip();
					scanOctNumber();
					break;
				case 'b' : case 'B' :
					skip();
					scanBinNumber();
					break;
				}
				scanDecNumber();
				return;
			case '1' : case '2' : case '3' :
			case '4' : case '5' : case '6' :
			case '7' : case '8' : case '9' :
				scanDecNumber();
				return;
			case '\'':
				poll();
				scanCharacter();
				return;
			case '\"':
			{
				int pos0 = pos;
				if (poll() == '\"' && poll() == '\"')
				{
					poll();
					scanMultiString();
				}
				else
				{
					pos = pos0;
					poll();
					scanString();
				}
				return;
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
				scanIdentifier();
				return;
			case '/' :
				switch (peek())
				{
				case '/' :
					skip();
					poll();
					scanLineComment();
					return;
				case '*' :
					skip();
					if (peek() == '*')
					{
						skip();
						type = LexType.COMMENT_DOC;
					}
					else
					{
						type = LexType.COMMENT_BLOCK;
					}
					poll();
					scanBlockComment();
					return;
				}
			case '~' : case '!' : case '%' :
			case '^' : case '&' : case '*' :
			case ':' : case '+' : case '-' :
			case '=' : case '?' : case '>' :
				scanOperator();
				return;
			default :
				if (C < '\u0080')
				{
					error("illegal.character");
					poll();
				}
				else
				{
					char high = scanSurrogate();
					int i;
					if (high == 0)
					{
						i = high;
					}
					else
					{
						i = Character.toCodePoint(high, C);
					}
					if (Character.isJavaIdentifierStart(i))
					{
						scanIdentifier();
						return;
					}
					else if (isOperator(i))
					{
						scanOperator();
						return;
					}
					else if (Character.isWhitespace(i))
					{
						poll();
						continue;
					}
					else
					{
						error("illegal.character");
					}
				}
				break;
			}
		}
		type = LexType.END;
	}
	
	private void scanOperator()
	{
		type = LexType.OPERATOR;
		scanOp();
		ident = new Identifier(pop());
	}
	
	private void scanIdentifier()
	{
		if (scanId(true))
		{
			if (ibufPos == 0)
				error("identifier.empty");
			type = LexType.OPERATOR;
			ident = new Identifier(pop());
		}
		else
		{
			if (ibufPos == 0)
				error("identifier.empty");
			String value = pop();
			switch (value)
			{
			case "if" :
				type = LexType.IF;
				return;
			case "else" :
				type = LexType.ELSE;
				return;
			case "while" :
				type = LexType.WHILE;
				return;
			case "do" :
				type = LexType.DO;
				return;
			case "switch" :
				type = LexType.SWITCH;
				return;
			case "match" :
				type = LexType.MATCH;
				return;
			case "case" :
				type = LexType.CASE;
				return;
			case "val" :
				type = LexType.VAL;
				return;
			case "var" :
				type = LexType.VAR;
				return;
			case "class" :
				type = LexType.CLASS;
				return;
			case "enum" :
				type = LexType.ENUM;
				return;
			case "interface" :
				type = LexType.INTERFACE;
				return;
			case "object" :
				type = LexType.OBJECT;
				return;
			case "package" :
				type = LexType.PACKAGE;
				return;
			case "import" :
				type = LexType.IMPORT;
				return;
			case "extends" :
				type = LexType.EXTENDS;
				return;
			case "super" :
				type = LexType.SUPER;
				return;
			case "this" :
				type = LexType.THIS;
				return;
			case "that" :
				type = LexType.THAT;
				return;
			case "true" :
				type = LexType.LITERAL;
				lit = new Literal(LiteralType.BOOL, Boolean.TRUE);
				return;
			case "false" :
				type = LexType.LITERAL;
				lit = new Literal(LiteralType.BOOL, Boolean.FALSE);
				return;
			default:
				type = LexType.IDENTIFIER;
				ident = new Identifier(value);
				return;
			}
		}
	}
	
	private void notLetterMatch(char chr)
	{
		if (Character.isLetter(chr))
			error("illegal.number.suffix");
	}
	
	private void scanHexNumber()
	{
		while (Character.digit(C, 16) >= 0)
		{
			store();
			poll();
		}
		if (C == '.')
		{
			store();
			poll();
			scanHexFloatNumber();
		}
		else
		{
			try
			{
				switch (C)
				{
				//				case 'l' : case 'L' :
				//					poll();
				//					this.lit = new Literal(LiteralType.LONG, Long.valueOf(Strings.parseLong2(pop(), 4)));
				//					break;
				//				case 'f' : case 'F' :
				//					poll();
				//					this.lit = new Literal(LiteralType.FLOAT, Float.intBitsToFloat(Strings.parseInt2(pop(), 4)));
				//					break;
				//				case 'd' : case 'D' :
				//					poll();
				//					this.lit = new Literal(LiteralType.DOUBLE, Double.longBitsToDouble(Strings.parseLong2(pop(), 4)));
				//					break;
				//				default:
				//					this.lit = new Literal(LiteralType.INT, Integer.valueOf(Strings.parseInt2(pop(), 4)));
				//					break;
				}
			}
			catch (NumberFormatException exception)
			{
				error("illegal.number");
			}
			notLetterMatch(peek());
			type = LexType.LITERAL;
		}
	}
	
	private void scanHexFloatNumber()
	{
		while (Character.digit(C, 16) >= 0)
		{
			store();
			poll();
		}
		if (C == 'p' || C == 'P')
		{
			store('p');
			switch (poll())
			{
			case '+' :
			case '-' :
				store();
				poll();
				break;
			default:
				if (Character.digit(C, 10) >= 0)
					break;
				error("illegal.number");
			}
			do
			{
				store();
				poll();
			}
			while (Character.digit(C, 10) >= 0);
		}
		String num = "0x" + pop();
		try
		{
			switch (C)
			{
			case 'd': case 'D' :
				lit = new Literal(LiteralType.DOUBLE, Double.valueOf(num));
				break;
			case 'f' : case 'F' :
				lit = new Literal(LiteralType.FLOAT, Float.valueOf(num));
				break;
			}
		}
		catch (NumberFormatException exception)
		{
			error("illegal.number");
		}
		notLetterMatch(peek());
		type = LexType.LITERAL;
	}
	
	private void scanOctNumber()
	{
		while (Character.digit(C, 8) >= 0)
		{
			store();
			poll();
		}
		try
		{
			switch (C)
			{
			//			case 'l' : case 'L' :
			//				poll();
			//				lit = new Literal(LiteralType.LONG, Long.valueOf(Strings.parseLong2(pop(), 3)));
			//				break;
			//			default:
			//				lit = new Literal(LiteralType.INT, Integer.valueOf(Strings.parseInt2(pop(), 3)));
			//				break;
			}
		}
		catch (NumberFormatException exception)
		{
			error("illegal.number");
		}
		notLetterMatch(peek());
		type = LexType.LITERAL;
	}
	
	private void scanBinNumber()
	{
		while (Character.digit(C, 2) >= 0)
		{
			store();
			poll();
		}
		try
		{
			switch (C)
			{
			//			case 'l' : case 'L' :
			//				poll();
			//				lit = new Literal(LiteralType.LONG, Long.valueOf(Strings.parseLong2(pop(), 1)));
			//				break;
			//			case 'f' : case 'F' :
			//				poll();
			//				lit = new Literal(LiteralType.FLOAT, Float.intBitsToFloat(Strings.parseInt2(pop(), 1)));
			//				break;
			//			case 'd' : case 'D' :
			//				poll();
			//				lit = new Literal(LiteralType.DOUBLE, Double.longBitsToDouble(Strings.parseLong2(pop(), 1)));
			//				break;
			//			default:
			//				lit = new Literal(LiteralType.INT, Integer.valueOf(Strings.parseInt2(pop(), 1)));
			//				break;
			}
		}
		catch (NumberFormatException exception)
		{
			error("illegal.number");
		}
		notLetterMatch(peek());
		type = LexType.LITERAL;
	}
	
	private void scanDecNumber()
	{
		while (Character.digit(C, 10) >= 0)
		{
			store();
			poll();
		}
		if (C == '.')
		{
			store();
			poll();
			scanDecFloatNumber();
		}
		else
		{
			String num = pop();
			if (scanId(false))
				warn("number.end.with.operator");
			String identifier = pop();
			try
			{
				switch (identifier)
				{
				case "" :
					lit = new Literal(LiteralType.INT, Integer.valueOf(num));
					break;
				case "l" : case "L" :
					lit = new Literal(LiteralType.LONG, Long.valueOf(num));
					break;
				default:
					lit = new Literal(LiteralType.CINT, new Object[] {num, identifier});
					break;
				}
			}
			catch (NumberFormatException exception)
			{
				error("illegal.number");
			}
			type = LexType.LITERAL;
		}
	}
	
	private void scanDecFloatNumber()
	{
		while (Character.digit(C, 10) >= 0)
		{
			store();
			poll();
		}
		if (C == 'e' || C == 'E')
		{
			store('e');
			switch (poll())
			{
			case '+' :
			case '-' :
				store();
				poll();
				break;
			default:
				if (Character.digit(C, 10) >= 0)
					break;
				error("illegal.number");
			}
			do
			{
				store();
				poll();
			}
			while (Character.digit(C, 10) >= 0);
		}
		String num = pop();
		if (scanId(false))
			warn("number.end.with.operator");
		String identifier = pop();
		try
		{
			switch (identifier)
			{
			case "" : case "d" : case "D" :
				lit = new Literal(LiteralType.DOUBLE, Double.valueOf(num));
				break;
			case "f" : case "F" :
				lit = new Literal(LiteralType.FLOAT, Float.valueOf(num));
				break;
			default:
				lit = new Literal(LiteralType.CFLOAT, new Object[] {num, identifier});
				break;
			}
		}
		catch (NumberFormatException exception)
		{
			error("illegal.number");
		}
		type = LexType.LITERAL;
	}
	
	private void scanMultiString()
	{
		int i = 0;
		while (true)
		{
			if (scanEscapeChar('"', true))
			{
				if (++i == 3)
				{
					poll();
					break;
				}
				store('"');
			}
			else
			{
				i = 0;
				store();
			}
			poll();
		}
		type = LexType.LITERAL;
		ibufPos -= 3;
		lit = new Literal(LiteralType.STRING, pop());
	}
	
	private void scanString()
	{
		while (!scanEscapeChar('\"', false))
		{
			store();
			poll();
		}
		if (C != '\"')
			error("string.missing.limit");
		poll();
		type = LexType.LITERAL;
		lit = new Literal(LiteralType.STRING, pop());
	}
	
	private void scanCharacter()
	{
		if (scanEscapeChar('\'', false))
			error("character.illegal.limit");
		store();
		if (poll() != '\'')
			error("character.missing.limit");
		poll();
		type = LexType.LITERAL;
		lit = new Literal(LiteralType.CHAR, ibuf[0]);
		ibufPos = 0;
	}
	
	private boolean scanEscapeChar(char exclude, boolean enableEnter)
	{
		if (C == '\\')
		{
			int i;
			switch (poll())
			{
			case 'f' : C = '\f'; break;
			case 'r' : C = '\r'; break;
			case 'n' : C = '\n'; break;
			case '\'': C = '\''; break;
			case '\"': C = '\"'; break;
			case '\\': C = '\\'; break;
			case '0' : case '1' : case '2' : case '3' :
				i = C - '0';
				switch (C = peek())
				{
				case '0' : case '1' : case '2' : case '3' :
				case '4' : case '5' : case '6' : case '7' :
					skip();
					i = i << 4 | C - '0';
					switch (C = peek())
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
				switch (C = peek())
				{
				case '0' : case '1' : case '2' : case '3' :
				case '4' : case '5' : case '6' : case '7' :
					skip();
					i = i << 4 | C - '0';
				}
				C = (char) i;
				break;
			default :
				error("escape.unknown.control");
			}
			return false;
		}
		else if (C == EOF)
		{
			error("missing.char.limit");
			return false;
		}
		else if (!enableEnter)
		{
			switch (C)
			{
			case '\r' :
			case '\n' :
				error("illegal.char");
				return false;
			default:
				return C == exclude;
			}
		}
		else
		{
			return C == exclude;
		}
	}
	
	private void scanLineComment()
	{
		{
			label: while (pos < buf.length)
			{
				switch (C)
				{
				case '\r' :
					if (peek() == '\n')
						poll();
				case '\n' :
					poll();
					break label;
				default:
					store();
					poll();
					break;
				}
			}
		//For last character failed to store by upper loop.
		store();
		skip();
		}
		type = LexType.COMMENT_LINE;
		comment = new Comment(pop());
	}
	
	private void scanBlockComment()
	{
		{
			label0: while (pos < buf.length)
			{
				boolean white = false;
				label:
				{
					switch (C)
					{
					case ' '  :
					case '\t' :
					case '\f' :
						white = true;
						poll();
						continue;
					case '\r' :
						if (peek() == '\n')
							skip();
					case '\n' :
						poll();
						break label;
					case '*' :
						if (peek() == '/')
						{
							skip();
							comment = new Comment(pop());
							return;
						}
					default:
						if (Character.isWhitespace(C))
						{
							white = true;
							poll();
							continue label0;
						}
						else
						{
							if (white)
							{
								store(' ');
								white = false;
							}
							store();
							poll();
						}
						break;
					}
					continue label0;
				}
				label: while (pos < buf.length)
				{
					switch (C)
					{
					case ' ' :
					case '\t':
					case '\f':
						break;
					case '*' :
						poll();
						if (C == '/')
						{
							skip();
							comment = new Comment(pop());
							return;
						}
						break label;
					default :
						if (Character.isWhitespace(C))
							break;
						else
						{
							store();
							break label;
						}
					}
					poll();
				}
			}
		}
		warn("comment.no.ending");
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
	
	private boolean scanId(boolean matchStart)
	{
		if (matchStart)
		{
			if (!Character.isJavaIdentifierStart(C))
				return false;
			store();
			poll();
		}
		{
			label: while (true)
			{
				switch (C)
				{
				case '_' :
					store();
					if (isOperator(peek()))
					{
						int i = ibufPos;
						poll();
						scanOp();
						return i != ibufPos;
					}
				case '1' : case '2' : case '3' :
				case '4' : case '5' : case '6' :
				case '7' : case '8' : case '9' :
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
					store();
					poll();
					break;
				case '\1' : case '\2' : case '\3' : case '\4' :
				case '\5' : case '\6' : case '\7' : case '\10':
				case '\16': case '\17': case '\21': case '\22':
				case '\23': case '\25': case '\26': case '\27':
				case '\30': case '\31': case '\33': case '\177':
					poll();
					break;
				default:
					if (C < '\u0080')
						break label;
					if (Character.isIdentifierIgnorable(C))
					{
						poll();
						break;
					}
					char high = scanSurrogate();
					if (high != 0)
					{
						store(high);
						if (Character.isJavaIdentifierPart(Character.toCodePoint(high, C)))
						{
							store(C);
							poll();
						}
						else
						{
							ibufPos --;
							break label;
						}
						break;
					}
					else if (Character.isJavaIdentifierPart(C))
					{
						store();
						poll();
						break;
					}
					else
						break label;
				}
			}
		}
		return false;
	}
	
	private char scanSurrogate()
	{
		if (enableUTF16 && Character.isHighSurrogate(C))
		{
			char i = C;
			if (Character.isLowSurrogate(poll()))
			{
				return C;
			}
			C = i;
		}
		return 0;
	}
	
	private void scanOp()
	{
		while (true)
		{
			switch (C)
			{
			case '\1' : case '\2' : case '\3' : case '\4' :
			case '\5' : case '\6' : case '\7' : case '\10':
			case '\16': case '\17': case '\21': case '\22':
			case '\23': case '\25': case '\26': case '\27':
			case '\30': case '\31': case '\33': case '\177':
				poll();
				continue;
			case '<' :
				if (peek() == '|')
					return;
				break;
			case '|' :
				if (peek() == '>')
					return;
				break;
			case '/' :
				switch (peek())
				{
				case '/' : case '*' :
					return;
				}
				break;
			default:
				if (C < '\u0080')
					return;
				char high = scanSurrogate();
				if (high != 0)
				{
					store(high);
					if (isOperator(Character.toCodePoint(high, C)))
					{
						store(C);
						poll();
					}
					else
					{
						ibufPos --;
						return;
					}
					continue;
				}
				if (isOperator(C))
				{
					break;
				}
				else if (Character.isIdentifierIgnorable(C))
				{
					poll();
					continue;
				}
				return;
			case '~' : case '!' : case '%' :
			case '^' : case '&' : case '*' :
			case ':' : case '+' : case '-' :
			case '=' : case '?' : case '>' :
			}
			store();
			poll();
		}
	}
	
	private void skipAndSetType(LexType type)
	{
		this.type = type;
		poll();
	}
	
	private int escapeDigit(char i)
	{
		if (i >= '0' && i <= '9')
			return i - '0';
		else if (i >= 'a' && i <= 'f')
			return i + (10 - 'a');
		else if (i >= 'A' && i <= 'F')
			return i + (10 - 'A');
		else
		{
			error("illegal.escape.digit", i);
			return -1;
		}
	}
	
	private char peek()
	{
		if (pos >= buf.length)
		{
			return EOF;
		}
		if (enableEscape && buf[pos] == '\\' && pos + 1 < buf.length && buf[pos + 1] == 'u')
		{
			return (char) (
					escapeDigit(buf[pos + 2]) << 12 |
					escapeDigit(buf[pos + 3]) <<  8 |
					escapeDigit(buf[pos + 4]) <<  4 |
					escapeDigit(buf[pos + 5])       );
		}
		return buf[pos];
	}
	
	private char poll()
	{
		if (pos >= buf.length)
		{
			return C = EOF;
		}
		if (enableEscape && buf[pos] == '\\' && pos + 1 < buf.length && buf[pos + 1] == 'u')
		{
			pos += 6;
			return C = (char) (
					escapeDigit(buf[pos - 4]) << 12 |
					escapeDigit(buf[pos - 3]) <<  8 |
					escapeDigit(buf[pos - 2]) <<  4 |
					escapeDigit(buf[pos - 1])       );
		}
		return C = buf[pos ++];
	}
	
	private void skip()
	{
		if (pos < buf.length)
			if (enableEscape && buf[pos] == '\\' && pos + 1 < buf.length && buf[pos + 1] == 'u')
				pos += 6;
			else
				pos ++;
		else if (pos == buf.length)
			pos ++;
	}
	
	private void store()
	{
		store(C);
	}
	
	private void store(char chr)
	{
		if (ibufPos == BUFFER_LENGTH)
			error("identifier.buf.too.long");
		ibuf[ibufPos ++] = chr;
	}
	
	private String pop()
	{
		String result = new String(ibuf, 0, ibufPos);
		ibufPos = 0;
		return result;
	}
	
	@Override
	public void mark()
	{
		markC       = C;
		markPos     = pos;
		markPrevPos = prevPos;
	}
	
	@Override
	public void reset()
	{
		C          = markC;
		pos        = markPos;
		prevPos    = markPrevPos;
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
	
	private SourcePosition resolvePosition(int idx)
	{
		if (idx >= buf.length)
			return new SourcePosition(fileName, 0, -1, buf, -1, -1);//EOF
		else
		{
			generatePositionMap();
			int ln = floor(linemap, idx);
			int pos = linemap[ln];
			int pos2;
			return new SourcePosition(fileName, ln + 1, idx - pos + 1, buf, pos,
					ln + 1 == linemap.length ? buf.length : buf[(pos2 = linemap[ln + 1] - 1)] == '\r' ? pos2 - 2 : pos2 - 1);
		}
	}
	
	@Override
	public void warn(String cause, Object... formats)
	{
		errPos = current();
		super.warn(cause, formats);
	}
	
	@Override
	public void error(String cause, Object... formats)
	{
		errPos = current();
		super.error(cause, formats);
	}
	
	@Override
	public SourcePosition current()
	{
		return resolvePosition(currentPos);
	}
	
	@Override
	public SourcePosition previous()
	{
		return resolvePosition(prevPos);
	}
	
	@Override
	public SourcePosition err()
	{
		return errPos == null ? new SourcePosition(fileName, 0, -1, new char[0], 0, 0) : errPos;
	}
}
