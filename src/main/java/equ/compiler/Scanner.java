/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

import java.util.ArrayList;
import java.util.List;

import equ.compiler.Literal.LiteralType;
import equ.util.Strings;

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
		return this.factory;
	}
	
	@Override
	public LexType type()
	{
		return this.type;
	}
	
	@Override
	public void setIdent(Identifier identifier)
	{
		this.ident = identifier;
	}
	
	@Override
	public Identifier ident()
	{
		return this.ident;
	}
	
	@Override
	public void setLit(Literal literal)
	{
		this.lit = literal;
	}
	
	@Override
	public Literal lit()
	{
		return this.lit;
	}
	
	@Override
	public void setCom(Comment comment)
	{
		this.comment = comment;
	}
	
	@Override
	public Comment com()
	{
		return this.comment;
	}
	
	public void warn(String cause, Object... formats)
	{
		this.factory.warn(this, cause, formats);
	}
	
	public void error(String cause, Object... formats)
	{
		this.factory.error(this, cause, formats);
	}
	
	SourcePosition errPos;
	
	@Override
	public void setErrPos(SourcePosition pos)
	{
		this.errPos = pos;
	}
	
	@Override
	public SourcePosition err()
	{
		return this.errPos == null ? new SourcePosition("<unknown file>", 0, -1, new char[0], 0, 0) : this.errPos;
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
		this.prevPos = this.pos;
		while (this.pos <= this.buf.length)
		{
			switch (this.C)
			{
			case EOF :
				this.type = LexType.END;
				return;
			case ' ' :
			case '\t':
			case '\f':
			case '\1' : case '\2' : case '\3' : case '\4' :
			case '\5' : case '\6' : case '\7' : case '\10':
			case '\16': case '\17': case '\21': case '\22':
			case '\23': case '\25': case '\26': case '\27':
			case '\30': case '\31': case '\33': case '\177':
				this.prevPos = this.pos;
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
				this.type = LexType.ATTRIBUTE;
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
					this.type = LexType.DOT;
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
					skipAndSetType(LexType.LNAMING);
					return;
				}
				scanOperator();
				return;
			case '|' :
				if (peek() == '>')
				{
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
				int pos0 = this.pos;
				if (poll() == '\"' && poll() == '\"')
				{
					poll();
					scanMultiString();
				}
				else
				{
					this.pos = pos0;
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
					scanLineComment();
					return;
				case '*' :
					if (peek() == '*')
					{
						poll();
						this.type = LexType.COMMENT_DOC;
					}
					else
					{
						this.type = LexType.COMMENT_BLOCK;
					}
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
				if (this.C < '\u0080')
				{
					error("illegal.character");
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
						i = Character.toCodePoint(high, this.C);
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
		this.type = LexType.END;
	}
	
	private void scanOperator()
	{
		this.type = LexType.OPERATOR;
		scanOp();
		this.ident = new Identifier(pop());
	}
	
	private void scanIdentifier()
	{
		if (scanId(true))
		{
			if (this.ibufPos == 0)
				error("identifier.empty");
			this.type = LexType.OPERATOR;
			this.ident = new Identifier(pop());
		}
		else
		{
			if (this.ibufPos == 0)
				error("identifier.empty");
			String value = pop();
			switch (value)
			{
			case "if" :
				this.type = LexType.IF;
				return;
			case "else" :
				this.type = LexType.ELSE;
				return;
			case "while" :
				this.type = LexType.WHILE;
				return;
			case "do" :
				this.type = LexType.DO;
				return;
			case "switch" :
				this.type = LexType.SWITCH;
				return;
			case "match" :
				this.type = LexType.MATCH;
				return;
			case "case" :
				this.type = LexType.CASE;
				return;
			case "val" :
				this.type = LexType.VAL;
				return;
			case "var" :
				this.type = LexType.VAR;
				return;
			case "class" :
				this.type = LexType.CLASS;
				return;
			case "enum" :
				this.type = LexType.ENUM;
				return;
			case "interface" :
				this.type = LexType.INTERFACE;
				return;
			case "object" :
				this.type = LexType.OBJECT;
				return;
			case "package" :
				this.type = LexType.PACKAGE;
				return;
			case "import" :
				this.type = LexType.IMPORT;
				return;
			case "extends" :
				this.type = LexType.EXTENDS;
				return;
			case "super" :
				this.type = LexType.SUPER;
				return;
			case "this" :
				this.type = LexType.THIS;
				return;
			case "that" :
				this.type = LexType.THAT;
				return;
			case "true" :
				this.type = LexType.LITERAL;
				this.lit = new Literal(LiteralType.BOOL, Boolean.TRUE);
				return;
			case "false" :
				this.type = LexType.LITERAL;
				this.lit = new Literal(LiteralType.BOOL, Boolean.FALSE);
				return;
			default:
				this.type = LexType.IDENTIFIER;
				this.ident = new Identifier(value);
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
		while (Character.digit(this.C, 16) >= 0)
		{
			store();
			poll();
		}
		if (this.C == '.')
		{
			store();
			poll();
			scanHexFloatNumber();
		}
		else
		{
			try
			{
				switch (this.C)
				{
				case 'l' : case 'L' :
					poll();
					this.lit = new Literal(LiteralType.LONG, Long.valueOf(Strings.parseLong2(pop(), 4)));
					break;
				case 'f' : case 'F' :
					poll();
					this.lit = new Literal(LiteralType.FLOAT, Float.intBitsToFloat(Strings.parseInt2(pop(), 4)));
					break;
				case 'd' : case 'D' :
					poll();
					this.lit = new Literal(LiteralType.DOUBLE, Double.longBitsToDouble(Strings.parseLong2(pop(), 4)));
					break;
				default:
					this.lit = new Literal(LiteralType.INT, Integer.valueOf(Strings.parseInt2(pop(), 4)));
					break;
				}
			}
			catch (NumberFormatException exception)
			{
				error("illegal.number");
			}
			notLetterMatch(peek());
			this.type = LexType.LITERAL;
		}
	}
	
	private void scanHexFloatNumber()
	{
		while (Character.digit(this.C, 16) >= 0)
		{
			store();
			poll();
		}
		if (this.C == 'p' || this.C == 'P')
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
				if (Character.digit(this.C, 10) >= 0)
					break;
				error("illegal.number");
			}
			do
			{
				store();
				poll();
			}
			while (Character.digit(this.C, 10) >= 0);
		}
		String num = "0x" + pop();
		try
		{
			switch (this.C)
			{
			case 'd': case 'D' :
				this.lit = new Literal(LiteralType.DOUBLE, Double.valueOf(num));
				break;
			case 'f' : case 'F' :
				this.lit = new Literal(LiteralType.FLOAT, Float.valueOf(num));
				break;
			}
		}
		catch (NumberFormatException exception)
		{
			error("illegal.number");
		}
		notLetterMatch(peek());
		this.type = LexType.LITERAL;
	}
	
	private void scanOctNumber()
	{
		while (Character.digit(this.C, 8) >= 0)
		{
			store();
			poll();
		}
		try
		{
			switch (this.C)
			{
			case 'l' : case 'L' :
				poll();
				this.lit = new Literal(LiteralType.LONG, Long.valueOf(Strings.parseLong2(pop(), 3)));
				break;
			default:
				this.lit = new Literal(LiteralType.INT, Integer.valueOf(Strings.parseInt2(pop(), 3)));
				break;
			}
		}
		catch (NumberFormatException exception)
		{
			error("illegal.number");
		}
		notLetterMatch(peek());
		this.type = LexType.LITERAL;
	}
	
	private void scanBinNumber()
	{
		while (Character.digit(this.C, 2) >= 0)
		{
			store();
			poll();
		}
		try
		{
			switch (this.C)
			{
			case 'l' : case 'L' :
				poll();
				this.lit = new Literal(LiteralType.LONG, Long.valueOf(Strings.parseLong2(pop(), 1)));
				break;
			case 'f' : case 'F' :
				poll();
				this.lit = new Literal(LiteralType.FLOAT, Float.intBitsToFloat(Strings.parseInt2(pop(), 1)));
				break;
			case 'd' : case 'D' :
				poll();
				this.lit = new Literal(LiteralType.DOUBLE, Double.longBitsToDouble(Strings.parseLong2(pop(), 1)));
				break;
			default:
				this.lit = new Literal(LiteralType.INT, Integer.valueOf(Strings.parseInt2(pop(), 1)));
				break;
			}
		}
		catch (NumberFormatException exception)
		{
			error("illegal.number");
		}
		notLetterMatch(peek());
		this.type = LexType.LITERAL;
	}
	
	private void scanDecNumber()
	{
		while (Character.digit(this.C, 10) >= 0)
		{
			store();
			poll();
		}
		if (this.C == '.')
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
					this.lit = new Literal(LiteralType.INT, Integer.valueOf(num));
					break;
				case "l" : case "L" :
					this.lit = new Literal(LiteralType.LONG, Long.valueOf(num));
					break;
				default:
					this.lit = new Literal(LiteralType.CINT, new Object[] {num, identifier});
					break;
				}
			}
			catch (NumberFormatException exception)
			{
				error("illegal.number");
			}
			this.type = LexType.LITERAL;
		}
	}
	
	private void scanDecFloatNumber()
	{
		while (Character.digit(this.C, 10) >= 0)
		{
			store();
			poll();
		}
		if (this.C == 'e' || this.C == 'E')
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
				if (Character.digit(this.C, 10) >= 0)
					break;
				error("illegal.number");
			}
			do
			{
				store();
				poll();
			}
			while (Character.digit(this.C, 10) >= 0);
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
				this.lit = new Literal(LiteralType.DOUBLE, Double.valueOf(num));
				break;
			case "f" : case "F" :
				this.lit = new Literal(LiteralType.FLOAT, Float.valueOf(num));
				break;
			default:
				this.lit = new Literal(LiteralType.CFLOAT, new Object[] {num, identifier});
				break;
			}
		}
		catch (NumberFormatException exception)
		{
			error("illegal.number");
		}
		this.type = LexType.LITERAL;
	}
	
	private void scanMultiString()
	{
		while (!scanEscapeChar('\0', true))
		{
			if (this.C == '"')
			{
				int pos1 = this.pos;
				if (poll() == '"' && poll() == '"')
				{
					break;
				}
				else
				{
					this.pos = pos1;
					this.C = '"';
				}
			}
			store();
		}
		this.type = LexType.LITERAL;
		this.lit = new Literal(LiteralType.STRING, pop());
	}
	
	private void scanString()
	{
		while (!scanEscapeChar('\"', false))
		{
			store();
			poll();
		}
		if (this.C != '\"')
			error("string.missing.limit");
		poll();
		this.type = LexType.LITERAL;
		this.lit = new Literal(LiteralType.STRING, pop());
	}
	
	private void scanCharacter()
	{
		if (scanEscapeChar('\'', false))
			error("character.illegal.limit");
		store();
		if (poll() != '\'')
			error("character.missing.limit");
		this.type = LexType.LITERAL;
		this.lit = new Literal(LiteralType.CHAR, this.ibuf[0]);
		this.ibufPos = 0;
	}
	
	private boolean scanEscapeChar(char exclude, boolean enableEnter)
	{
		if (this.C == '\\')
		{
			int i;
			switch (poll())
			{
			case 'f' : this.C = '\f'; break;
			case 'r' : this.C = '\r'; break;
			case 'n' : this.C = '\n'; break;
			case '\'': this.C = '\''; break;
			case '\"': this.C = '\"'; break;
			case '\\': this.C = '\\'; break;
			case '0' : case '1' : case '2' : case '3' :
				i = this.C - '0';
				switch (peek())
				{
				case '0' : case '1' : case '2' : case '3' :
				case '4' : case '5' : case '6' : case '7' :
					poll();
					i = i << 4 | this.C - '0';
					switch (peek())
					{
					case '0' : case '1' : case '2' : case '3' :
					case '4' : case '5' : case '6' : case '7' :
						poll();
						i = i << 4 | this.C - '0';
					}
				}
				this.C = (char) i;
				break;
			case '4' : case '5' : case '6' : case '7' :
				i = this.C - '0';
				switch (peek())
				{
				case '0' : case '1' : case '2' : case '3' :
				case '4' : case '5' : case '6' : case '7' :
					poll();
					i = i << 4 | this.C - '0';
				}
				this.C = (char) i;
				break;
			default :
				error("escape.unknown.control");
			}
			return false;
		}
		else if (this.C == EOF)
		{
			error("missing.char.limit");
			return false;
		}
		else if (!enableEnter)
		{
			switch (this.C)
			{
			case '\r' :
			case '\n' :
				error("illegal.char");
				return false;
			default:
				return this.C == exclude;
			}
		}
		else
		{
			return this.C == exclude;
		}
	}
	
	private void scanLineComment()
	{
		while (this.pos < this.buf.length)
		{
			switch (this.C)
			{
			case '\r' :
				if (peek() == '\n')
					poll();
			case '\n' :
				poll();
				return;
			default:
				store();
				poll();
				break;
			}
		}
	}
	
	private void scanBlockComment()
	{
		{
			label0: while (this.pos < this.buf.length)
			{
				boolean white = false;
				label:
				{
					switch (this.C)
					{
					case ' '  :
					case '\t' :
					case '\f' :
						white = true;
						continue;
					case '\r' :
						if (peek() == '\n')
							poll();
					case '\n' :
						poll();
						break label;
					case '*' :
						if (peek() == '/')
						{
							this.comment = new Comment(pop());
							return;
						}
					default:
						if (Character.isWhitespace(this.C))
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
				label: while (this.pos < this.buf.length)
				{
					switch (this.C)
					{
					case ' ' :
					case '\t':
					case '\f':
						break;
					case '*' :
						poll();
						if (this.C == '/')
						{
							this.comment = new Comment(pop());
							return;
						}
						break label;
					default :
						if (Character.isWhitespace(this.C))
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
			if (!Character.isJavaIdentifierStart(this.C))
				return false;
			store();
			poll();
		}
		{
			label: while (true)
			{
				switch (this.C)
				{
				case '_' :
					store();
					if (isOperator(peek()))
					{
						int i = this.ibufPos;
						poll();
						scanOp();
						return i != this.ibufPos;
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
					if (this.C < '\u0080')
						break label;
					if (Character.isIdentifierIgnorable(this.C))
					{
						poll();
						break;
					}
					char high = scanSurrogate();
					if (high != 0)
					{
						store(high);
						if (Character.isJavaIdentifierPart(Character.toCodePoint(high, this.C)))
						{
							store(this.C);
							poll();
						}
						else
						{
							this.ibufPos --;
							break label;
						}
						break;
					}
					else if (Character.isJavaIdentifierPart(this.C))
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
		if (this.enableUTF16 && Character.isHighSurrogate(this.C))
		{
			char i = this.C;
			if (Character.isLowSurrogate(poll()))
			{
				return this.C;
			}
			this.C = i;
		}
		return 0;
	}
	
	private void scanOp()
	{
		while (true)
		{
			switch (this.C)
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
				if (this.C < '\u0080')
					return;
				char high = scanSurrogate();
				if (high != 0)
				{
					store(high);
					if (isOperator(Character.toCodePoint(high, this.C)))
					{
						store(this.C);
						poll();
					}
					else
					{
						this.ibufPos --;
						return;
					}
					continue;
				}
				if (isOperator(this.C))
				{
					break;
				}
				else if (Character.isIdentifierIgnorable(this.C))
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
		if (this.pos >= this.buf.length)
		{
			return EOF;
		}
		if (this.enableEscape && this.buf[this.pos] == '\\' && this.pos + 1 < this.buf.length && this.buf[this.pos + 1] == 'u')
		{
			return (char) (
					escapeDigit(this.buf[this.pos + 2]) << 12 |
					escapeDigit(this.buf[this.pos + 3]) <<  8 |
					escapeDigit(this.buf[this.pos + 4]) <<  4 |
					escapeDigit(this.buf[this.pos + 5])       );
		}
		return this.buf[this.pos];
	}
	
	private char poll()
	{
		if (this.pos >= this.buf.length)
		{
			return this.C = EOF;
		}
		if (this.enableEscape && this.buf[this.pos] == '\\' && this.pos + 1 < this.buf.length && this.buf[this.pos + 1] == 'u')
		{
			this.pos += 6;
			return this.C = (char) (
					escapeDigit(this.buf[this.pos - 3]) << 12 |
					escapeDigit(this.buf[this.pos - 2]) <<  8 |
					escapeDigit(this.buf[this.pos - 1]) <<  4 |
					escapeDigit(this.buf[this.pos    ])       );
		}
		return this.C = this.buf[this.pos ++];
	}
	
	private void skip()
	{
		if (this.enableEscape && this.buf[this.pos] == '\\' && this.pos + 1 < this.buf.length && this.buf[this.pos + 1] == 'u')
			this.pos += 6;
		else if (this.pos < this.buf.length)
			++this.pos;
	}
	
	private void store()
	{
		store(this.C);
	}
	
	private void store(char chr)
	{
		if (this.ibufPos == BUFFER_LENGTH)
			error("identifier.buf.too.long");
		this.ibuf[this.ibufPos ++] = chr;
	}
	
	private String pop()
	{
		String result = new String(this.ibuf, 0, this.ibufPos);
		this.ibufPos = 0;
		return result;
	}
	
	@Override
	public void mark()
	{
		this.markC       = this.C;
		this.markPos     = this.pos;
		this.markPrevPos = this.prevPos;
	}
	
	@Override
	public void reset()
	{
		this.C          = this.markC;
		this.pos        = this.markPos;
		this.prevPos    = this.markPrevPos;
	}
	
	private void generatePositionMap()
	{
		if (this.linemap == null)
		{
			List<Integer> list = new ArrayList<>();
			list.add(0);
			int i = 0;
			while (i < this.buf.length)
			{
				switch (this.buf[i++])
				{
				case '\r':
					if (i < this.buf.length && this.buf[i] == '\n')
						i ++;
				case '\n':
					list.add(i);
				}
			}
			this.linemap = list.stream().mapToInt(Integer::intValue).toArray();
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
		if (idx >= this.buf.length)
			return new SourcePosition(this.fileName, 0, -1, this.buf, -1, -1);//EOF
		else
		{
			generatePositionMap();
			int ln = floor(this.linemap, idx);
			int pos = this.linemap[ln];
			int pos2;
			return new SourcePosition(this.fileName, ln + 1, idx - pos + 1, this.buf, pos,
					ln + 1 == this.linemap.length ? this.buf.length : this.buf[(pos2 = this.linemap[ln + 1] - 1)] == '\r' ? pos2 - 2 : pos2 - 1);
		}
	}
	
	@Override
	public void warn(String cause, Object... formats)
	{
		this.errPos = current();
		super.warn(cause, formats);
	}
	
	@Override
	public void error(String cause, Object... formats)
	{
		this.errPos = current();
		super.error(cause, formats);
	}
	
	@Override
	public SourcePosition current()
	{
		return resolvePosition(this.currentPos);
	}
	
	@Override
	public SourcePosition previous()
	{
		return resolvePosition(this.prevPos);
	}
	
	@Override
	public SourcePosition err()
	{
		return this.errPos == null ? new SourcePosition(this.fileName, 0, -1, new char[0], 0, 0) : this.errPos;
	}
}
