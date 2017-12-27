/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.parser;

import equ.compiler.util.ILiteralFactory;
import equ.compiler.util.Literal;
import equ.compiler.util.Literal.LitType;
import equ.compiler.util.Name;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
public class Scanner implements IScanner
{
	private final ILiteralFactory factory;
	
	private final char[] buf;
	private char C = '\n';
	private int bufidx;
	private char Cmark;
	private int bufmark = -1;
	
	private char[] lbuf = new char[512];
	private int lbufidx;
	
	private Literal literal;
	private Name identifier;
	
	boolean useUnicodeConvert;
	boolean macroMode;
	
	public Scanner(char[] buf, ILiteralFactory factory)
	{
		this.buf = buf;
		this.factory = factory;
	}
	
	public LexemeTokenType scan()
	{
		while (this.bufidx <= this.buf.length)
		{
			switch (this.C)
			{
			case ' ' :
			case '\t':
			case '\f':
				scanChar();
				return LexemeTokenType.WHITE;
			case '\r':
				mark();
				if (scanChar())
				{
					if (this.C != '\n')
						reset();
				}
			case '\n':
				mark();
				if (this.macroMode)
				{
					this.macroMode = false;
					return LexemeTokenType.MACRO_ENDING;
				}
				if (scanChar())
				{
					if (this.C == '#')
					{
						this.macroMode = true;
						scanChar();
						scanIdentifier();
						this.identifier = new Name(new String(this.lbuf, 0, this.lbufidx));
						this.lbufidx = 0;
						return LexemeTokenType.MACRO;
					}
				}
				break;
			case '#' :
				scanChar();
				return LexemeTokenType.SHARP;
			case '(' :
				scanChar();
				return LexemeTokenType.LPANC;
			case '[' :
				scanChar();
				return LexemeTokenType.LBRACE;
			case '{' :
				scanChar();
				return LexemeTokenType.LBRACKET;
			case ')' :
				scanChar();
				return LexemeTokenType.RPANC;
			case ']' :
				scanChar();
				return LexemeTokenType.RBRACE;
			case '}' :
				scanChar();
				return LexemeTokenType.RBRACKET;
			case ',' :
				scanChar();
				return LexemeTokenType.COMMA;
			case '.' :
				scanChar();
				return LexemeTokenType.DOT;
			case ';' :
				scanChar();
				return LexemeTokenType.SEMI;
			case '"' :
				scanString();
				return LexemeTokenType.LITERAL;
			case '\'' :
				scanLitChar();
				return LexemeTokenType.LITERAL;
			case '`' :
				scanCustom();
				return LexemeTokenType.LITERAL;
			case '0' :
			{
				mark();
				scanChar();
				switch (this.C)
				{
				case 'x' : case 'X' ://Hex
					scanChar();
					scanHexNumber();
					return LexemeTokenType.LITERAL;
				case 'o' : case 'O' ://Octal
					scanChar();
					scanOctNumber();
					return LexemeTokenType.LITERAL;
				case 'b' : case 'B' ://Binary
					scanChar();
					scanBinNumber();
					return LexemeTokenType.LITERAL;
					//				case 'r' : case 'R' ://Specific Radix
					//
					//					return TokenType.LITERAL;
				default:
					reset();
				}
			}
			case '1' : case '2' : case '3' :
			case '4' : case '5' : case '6' :
			case '7' : case '8' : case '9' :
				scanDecNumber();
				return LexemeTokenType.LITERAL;
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
				String v = new String(this.lbuf, 0, this.lbufidx);
				this.lbufidx = 0;
				switch (v)
				{
				case "return" : return LexemeTokenType.RETURN;
				case "val" : return LexemeTokenType.VAL;
				case "var" : return LexemeTokenType.VAR;
				case "new" : return LexemeTokenType.NEW;
				case "public" : return LexemeTokenType.PUBLIC;
				case "protected" : return LexemeTokenType.PROTECTED;
				case "private" : return LexemeTokenType.PRIVATE;
				case "native" : return LexemeTokenType.NATIVE;
				case "static" : return LexemeTokenType.STATIC;
				case "template" : return LexemeTokenType.TEMPLATE;
				case "type" : return LexemeTokenType.TYPE;
				case "typedef" : return LexemeTokenType.TYPEDEF;
				case "class" : return LexemeTokenType.CLASS;
				case "enum" : return LexemeTokenType.ENUM;
				case "object" : return LexemeTokenType.OBJECT;
				case "this" : return LexemeTokenType.THIS;
				case "super" : return LexemeTokenType.SUPER;
				case "extends" : return LexemeTokenType.EXTENDS;
				case "if" : return LexemeTokenType.IF;
				case "else" : return LexemeTokenType.ELSE;
				case "while" : return LexemeTokenType.WHILE;
				case "do" : return LexemeTokenType.DO;
				case "true" : this.literal = new Literal(LitType.BOOL, true); return LexemeTokenType.LITERAL;
				case "false" : this.literal = new Literal(LitType.BOOL, false); return LexemeTokenType.LITERAL;
				case "null" : this.literal = new Literal(LitType.NULL, null); return LexemeTokenType.LITERAL;
				case "asm" : return LexemeTokenType.ASM;
				default:
					this.identifier = new Name(v);
					return LexemeTokenType.IDENTIFIER;
				}
			case '<' :
				mark();
				if (scanChar())
				{
					if (this.C == '|')
					{
						scanChar();
						return LexemeTokenType.LNAMING;
					}
					reset();
				}
				scanOperator();
				String value = new String(this.lbuf, 0, this.lbufidx);
				this.lbufidx = 0;
				if ("<:".equals(value))
					return LexemeTokenType.TYPE_LOWER;
				this.identifier = new Name(value);
				return LexemeTokenType.OPERATOR;
			case '|' :
				mark();
				if (scanChar())
				{
					if (this.C == '>')
					{
						scanChar();
						return LexemeTokenType.RNAMING;
					}
					reset();
				}
			case '>' :
				scanOperator();
				value = new String(this.lbuf, 0, this.lbufidx);
				this.lbufidx = 0;
				if (value.equals(">:"))
					return LexemeTokenType.TYPE_GREATER;
				this.identifier = new Name(value);
				return LexemeTokenType.OPERATOR;
			case '~' : case '!' : case '%' :
			case '^' : case '&' : case '*' :
			case ':' : case '+' : case '-' :
			case '=' : case '?' : case '/' :
				scanOperator();
				this.identifier = new Name(new String(this.lbuf, 0, this.lbufidx));
				this.lbufidx = 0;
				return LexemeTokenType.OPERATOR;
			default :
				if (Character.isLetter(this.C))
				{
					scanIdentifier();
					LexemeTokenType type = isOperator(this.lbuf[this.lbufidx - 1]) ||
							(this.lbufidx == 2 &&
							(this.lbuf[0] == 'i' || this.lbuf[0] == 'a') && this.lbuf[1] == 's') ?
									LexemeTokenType.OPERATOR : LexemeTokenType.IDENTIFIER;
					this.identifier = new Name(new String(this.lbuf, 0, this.lbufidx));
					this.lbufidx = 0;
					return type;
				}
				else if (Character.isDigit(this.C))
				{
					scanDecNumber();
					return LexemeTokenType.LITERAL;
				}
				else if (isOperator(this.C))
				{
					scanOperator();
					this.identifier = new Name(new String(this.lbuf, 0, this.lbufidx));
					this.lbufidx = 0;
					return LexemeTokenType.OPERATOR;
				}
				else
				{
					err("unknown.char", this.bufidx);
				}
				break;
			}
		}
		return LexemeTokenType.END;
	}
	
	/**
	 * 语法：
	 * <li> hex := '0x' int|float
	 * <li> int := {hexdigit} id?
	 * <li> float := {hexdigit} '.' {hexdigit} 'p'|'P' ('+'|'-')? {digit} id?
	 * </li>
	 */
	private void scanHexNumber()
	{
		final int err = this.bufidx;
		try
		{
			while (Character.digit(this.C, 16) >= 0)
			{
				this.lbuf[this.lbufidx++] = this.C;
				scanChar();
			}
			String type;
			if (this.C == '.')
			{
				mark();
				if (scanChar() && Character.digit(this.C, 16) >= 0)
				{
					this.lbuf[this.lbufidx++] = '.';
					do
					{
						this.lbuf[this.lbufidx++] = this.C;
						if (!scanChar())
							throw new Exception();
					}
					while (Character.digit(this.C, 16) >= 0);
					if (this.C != 'p' && this.C != 'P')
						throw new Exception();
					this.lbuf[this.lbufidx++] = 'p';
					scanChar();
					switch (this.C)
					{
					case '+' :
						this.lbuf[this.lbufidx++] = '+';
						scanChar();
						break;
					case '-' :
						this.lbuf[this.lbufidx++] = '-';
						scanChar();
						break;
					}
					if (Character.digit(this.C, 10) < 0)
						throw new Exception();
					do
					{
						this.lbuf[this.lbufidx++] = this.C;
					}
					while (scanChar() && Character.digit(this.C, 10) >= 0);
					String number = new StringBuilder(2 + this.lbufidx).append("0x").append(this.lbuf, 0, this.lbufidx).toString();
					this.lbufidx = 0;
					if (Character.isLetter(this.C))
					{
						scanIdentifier();
						type = new String(this.lbuf, 0, this.lbufidx);
						this.lbufidx = 0;
					}
					else
					{
						type = "D";
					}
					switch (type)
					{
					case "f" : case "F" :
						this.literal = new Literal(LitType.FLOAT, Float.valueOf(number));
						return;
					case "d" : case "D" :
						this.literal = new Literal(LitType.DOUBLE, Double.valueOf(number));
						return;
					default:
						this.literal = this.factory.parseHexLiteral(number, type);
						return;
					}
				}
				reset();
			}
			String number = new String(this.lbuf, 0, this.lbufidx);
			this.lbufidx = 0;
			if (Character.isLetter(this.C))
			{
				scanIdentifier();
				type = new String(this.lbuf, 0, this.lbufidx);
				this.lbufidx = 0;
			}
			else
			{
				this.literal = new Literal(LitType.INT, Strings.parseInt2(number, 4));
				return;
			}
			switch (type)
			{
			case "l" : case "L" :
				this.literal = new Literal(LitType.LONG, Strings.parseLong2(number, 4));
				return;
			default:
				this.literal = this.factory.parseHexLiteral(number, type);
				return;
			}
		}
		catch (Exception exception)
		{
			err("number.format", err);
		}
	}
	
	/**
	 * 语法：
	 * <li> oct := '0o' {octdigit} id?
	 * </li>
	 */
	private void scanOctNumber()
	{
		final int err = this.bufidx;
		try
		{
			while (Character.digit(this.C, 8) >= 0)
			{
				this.lbuf[this.lbufidx++] = this.C;
				scanChar();
			}
			String number = new String(this.lbuf, 0, this.lbufidx);
			this.lbufidx = 0;
			if (Character.isLetter(this.C))
			{
				scanIdentifier();
				String type = new String(this.lbuf, 0, this.lbufidx);
				this.lbufidx = 0;
				switch (type)
				{
				case "l" : case "L" :
					this.literal = new Literal(LitType.LONG, Strings.parseLong2(number, 3));
					return;
				default:
					this.literal = this.factory.parseOctLiteral(number, type);
					return;
				}
			}
			else
			{
				this.literal = new Literal(LitType.INT, Strings.parseInt2(number, 3));
				return;
			}
		}
		catch (Exception exception)
		{
			err("number.format", err);
		}
	}
	
	/**
	 * 语法：
	 * <li> bin := '0b' {bindigit} id?
	 * </li>
	 */
	private void scanBinNumber()
	{
		final int err = this.bufidx;
		try
		{
			while (Character.digit(this.C, 8) >= 0)
			{
				this.lbuf[this.lbufidx++] = this.C;
				scanChar();
			}
			String number = new String(this.lbuf, 0, this.lbufidx);
			this.lbufidx = 0;
			if (Character.isLetter(this.C))
			{
				scanIdentifier();
				String type = new String(this.lbuf, 0, this.lbufidx);
				this.lbufidx = 0;
				switch (type)
				{
				case "l" : case "L" :
					this.literal = new Literal(LitType.LONG, Strings.parseLong2(number, 1));
					return;
				default:
					this.literal = this.factory.parseOctLiteral(number, type);
					return;
				}
			}
			else
			{
				this.literal = new Literal(LitType.INT, Strings.parseInt2(number, 1));
				return;
			}
		}
		catch (Exception exception)
		{
			err("number.format", err);
		}
	}
	
	/**
	 * 语法：
	 * <li> dec := int|float
	 * <li> int := 0|(nonZeroDigit [digit]) id?
	 * <li> float := (0|(nonZeroDigit [digit]))? '.' {digit} ('e'|'E' ('+'|'-')? {digit})? id?
	 * </li>
	 */
	private void scanDecNumber()
	{
		final int err = this.bufidx;
		try
		{
			while (Character.digit(this.C, 10) >= 0)
			{
				this.lbuf[this.lbufidx++] = this.C;
				scanChar();
			}
			if (this.C == '.')
			{
				mark();
				if (scanChar() && Character.digit(this.C, 10) >= 0)
				{
					this.lbuf[this.lbufidx++] = '.';
					label: {
						do
						{
							this.lbuf[this.lbufidx++] = this.C;
							if (!scanChar())
								break label;
						}
						while (Character.digit(this.C, 10) >= 0);
						switch (this.C)
						{
						case 'e' : case 'E' :
							this.lbuf[this.lbufidx++] = 'e';
							scanChar();
							switch (this.C)
							{
							case '+' :
								this.lbuf[this.lbufidx++] = '+';
								scanChar();
								break;
							case '-' :
								this.lbuf[this.lbufidx++] = '-';
								scanChar();
								break;
							}
							if (Character.digit(this.C, 10) < 0)
								throw new Exception();
							do
							{
								this.lbuf[this.lbufidx++] = this.C;
							}
							while (scanChar() && Character.digit(this.C, 10) >= 0);
							break;
						}
					}
					String number = new String(this.lbuf, 0, this.bufidx);
					this.lbufidx = 0;
					String type;
					if (Character.isLetter(this.C))
					{
						scanIdentifier();
						type = new String(this.lbuf, 0, this.lbufidx);
						this.lbufidx = 0;
					}
					else
					{
						type = "D";
					}
					switch (type)
					{
					case "f" : case "F" :
						this.literal = new Literal(LitType.FLOAT, Float.valueOf(number));
						return;
					case "d" : case "D" :
						this.literal = new Literal(LitType.DOUBLE, Double.valueOf(number));
						return;
					default:
						this.literal = this.factory.parseDecLiteral(number, type);
						return;
					}
				}
				reset();
			}
			String number = new String(this.lbuf, 0, this.lbufidx);
			this.lbufidx = 0;
			if (Character.isLetter(this.C))
			{
				scanIdentifier();
				String type = new String(this.lbuf, 0, this.lbufidx);
				this.lbufidx = 0;
				switch (type)
				{
				case "l" : case "L" :
					this.literal = new Literal(LitType.LONG, Long.valueOf(number));
					return;
				default:
					this.literal = this.factory.parseOctLiteral(number, type);
					return;
				}
			}
			else
			{
				this.literal = new Literal(LitType.INT, Integer.valueOf(number));
				return;
			}
		}
		catch (Exception exception)
		{
			err("number.format", err);
		}
	}
	
	/**
	 * 语法：
	 * <li> id := letter|'_' letter|digit|'_' ('_' op)?
	 * </li>
	 */
	private void scanIdentifier()
	{
		assert Character.isLetter(this.C);
		this.lbuf[this.lbufidx ++] = this.C;
		while (scanChar() && (Character.isLetterOrDigit(this.C) || this.C == '_'))
		{
			this.lbuf[this.lbufidx ++] = this.C;
			if (this.C == '_')
			{
				mark();
				if (scanChar() && isOperator(this.C))
				{
					scanOperator();
					return;
				}
			}
		}
	}
	
	/**
	 * 语法：
	 * <li> op := {opchar}/('<|'|'|>')
	 * </li>
	 */
	private void scanOperator()
	{
		while (true)
		{
			switch (this.C)
			{
			case '<' :
				mark();
				if (scanChar() && this.C == '|')
				{
					return;
				}
				reset();
				this.lbuf[this.lbufidx ++] = this.C;
				scanChar();
				break;
			case '|' :
				mark();
				if (scanChar() && this.C == '>')
				{
					return;
				}
				reset();
				this.lbuf[this.lbufidx ++] = this.C;
				scanChar();
				break;
			default:
				if (isOperator(this.C))
				{
					this.lbuf[this.lbufidx ++] = this.C;
					scanChar();
					break;
				}
				return;
			}
		}
	}
	
	/**
	 * 语法：
	 * <li> string := '"' [(char/('\\'))|escapeChar] '"'
	 * </li>
	 */
	private void scanString()
	{
		while (scanEscapeChar('"'))
		{
			this.lbuf[this.lbufidx++] = this.C;
		}
		scanChar();
		this.literal = new Literal(LitType.STRING, new String(this.lbuf, 0, this.lbufidx));
		this.lbufidx = 0;
	}
	
	/**
	 * 语法：
	 * <li> custom := '`' [(char/('\\'))|escapeChar] '`'
	 * </li>
	 */
	private void scanCustom()
	{
		while (scanEscapeChar('`'))
		{
			this.lbuf[this.lbufidx++] = this.C;
		}
		scanChar();
		this.literal = this.factory.parseCustomLiteral(new String(this.lbuf, 0, this.lbufidx));
		this.lbufidx = 0;
	}
	
	/**
	 * 语法：
	 * <li> char := '"' [(char/('\\'))|escapeChar] '"'
	 * </li>
	 */
	private void scanLitChar()
	{
		if (scanEscapeChar('\''))
		{
			this.lbuf[this.lbufidx++] = this.C;
		}
		char result = this.C;
		if (!scanChar() || this.C == '\'')
			err("illegal.char", this.bufidx);
		scanChar();
		this.literal = new Literal(LitType.CHAR, Character.valueOf(result));
		this.lbufidx = 0;
	}
	
	/**
	 * 语法：
	 * <li> escapeChar := '\\' 'r'|'n'|'f'|'b'|'\'|'`'|'\''|'"'|digit<1-3>
	 * </li>
	 */
	private boolean scanEscapeChar(char exclusive)
	{
		scanChar();
		if (this.C == '\\')
		{
			if (!scanChar())
				err("unexpected.ending", this.bufidx);
			switch (this.C)
			{
			case '\"' :
			case '\'' :
			case '\\' :
			case '`' :
				return true;
			case 'r' :
				this.C = '\r';
				return true;
			case 'n' :
				this.C = '\n';
				return true;
			case 'f' :
				this.C = '\f';
				return true;
			case 'b' :
				this.C = '\b';
				return true;
			case '0' : case '1' : case '2' : case '3' :
				int o = this.C - '0';
				mark();
				if (scanChar())
				{
					switch (this.C)
					{
					case '0' : case '1' : case '2' : case '3' :
					case '4' : case '5' : case '6' : case '7' :
						o = o << 3 | this.C - '0';
						mark();
						if (scanChar())
						{
							switch (this.C)
							{
							case '0' : case '1' : case '2' : case '3' :
							case '4' : case '5' : case '6' : case '7' :
								o = o << 3 | this.C - '0';
								break;
							default :
								reset();
								break;
							}
						}
						break;
					default :
						reset();
						break;
					}
				}
				this.C = (char) o;
				return true;
			case '4' : case '5' : case '6' : case '7' :
				o = this.C - '0';
				mark();
				if (scanChar())
				{
					switch (this.C)
					{
					case '0' : case '1' : case '2' : case '3' :
					case '4' : case '5' : case '6' : case '7' :
						o = o << 3 | this.C - '0';
						break;
					default :
						reset();
						break;
					}
				}
				this.C = (char) o;
				return true;
			default:
				err("unknown.escape", this.bufidx);
			}
		}
		else if (this.C == exclusive)
			return false;
		return true;
	}
	
	private static boolean isOperator(char chr)
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
	
	private boolean scanChar()
	{
		if (this.bufidx >= this.buf.length)
		{
			this.C = '\0';
			this.bufidx = this.buf.length + 1;
			return false;
		}
		this.C = this.buf[this.bufidx++];
		if (this.useUnicodeConvert &&
				this.C == '\\' && this.bufidx + 1 < this.buf.length && this.buf[this.bufidx + 1] == 'u')
		{
			this.C = (char) (
					Character.digit(this.buf[this.bufidx + 2], 16) << 12 |
					Character.digit(this.buf[this.bufidx + 3], 16) <<  8 |
					Character.digit(this.buf[this.bufidx + 4], 16) <<  4 |
					Character.digit(this.buf[this.bufidx + 5], 16)
					);
			this.bufidx += 6;
		}
		return true;
	}
	
	private void err(String cause, int pos)
	{
		
	}
	
	public Literal lit()
	{
		return this.literal;
	}
	
	public void setLiteral(Literal literal)
	{
		this.literal = literal;
	}
	
	public Name ident()
	{
		return this.identifier;
	}
	
	public void setIdentifier(Name identifier)
	{
		this.identifier = identifier;
	}
	
	private void mark()
	{
		this.bufmark = this.bufidx;
		this.Cmark = this.C;
	}
	
	private void reset()
	{
		this.bufidx = this.bufmark;
		this.C = this.Cmark;
	}
}
