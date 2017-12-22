/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.lexer1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

import equ.compiler.preoperator.Macro;
import equ.compiler.util.AbstractSourceInfo;
import equ.compiler.util.ISourceInfo;
import equ.compiler.util.Lexeme;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
public class StreamLexer extends AbstractSourceInfo implements Lexer
{
	Stream<?> stream;
	int maxExceptionSize = 0;
	List<LexeralizeException> exceptions = new ArrayList<>();
	List<Lexeme<?>> lexemes = new ArrayList<>();
	boolean readingAnnotation = false;
	
	public StreamLexer(Stream<?> stream)
	{
		this.stream = stream;
	}
	
	public List<Lexeme<?>> lexeralize() throws LexeralizeException
	{
		this.stream.forEach(this::parse);
		return this.lexemes;
	}
	
	void parse(Object value)
	{
		try
		{
			if (value instanceof Object[])
			{
				parseLine((Object[]) value);
			}
			else if (value instanceof Macro<?>)
			{
				parseMacro((Macro<?>) value);
			}
		}
		catch (LexeralizeException exception)
		{
			this.exceptions.add(exception);
			if (this.exceptions.size() >= this.maxExceptionSize)
			{
				throw exception;
			}
		}
	}
	
	void parseLine(Object[] value) throws LexeralizeException
	{
		this.line = (String) value[0];
		this.source = (String) value[1];
		this.ln = (int) value[2];
		
		if (this.readingAnnotation && parseMultiAnnotation(this.line, 0) == -1)
			return;
		
		for (this.col = 0; this.col < this.line.length();)
		{
			char chr = this.line.charAt(this.col++);
			switch (chr)
			{
			case ' ' :
			case '\t':
				continue;
			case '(' :
			case '[' :
			case '{' :
			case '}' :
			case ']' :
			case ')' :
			case '.' :
			case ';' :
			case ',' :
				this.lexemes.add(Lexeme.lexeme(Lexeme.SEPERATOR, chr, this));
				break;
			case '\"' :
				try
				{
					StringBuilder builder = new StringBuilder();
					int i;
					while ((i = pollEscapedChar(this.line, '"')) != -1)
					{
						builder.append((char) i);
					}
					this.lexemes.add(Lexeme.lexeme(Lexeme.LITERAL_STRING, builder.toString(), this));
				}
				catch (StringIndexOutOfBoundsException exception)
				{
					throw new LexeralizeException("Unexpected ending of string.", this);
				}
				break;
			case '\'' :
				try
				{
					int i = pollEscapedChar(this.line, '\'');
					if (i == -1 || this.line.charAt(this.col) != '\'')
						throw new LexeralizeException("Illegal character format.", this);
					this.lexemes.add(Lexeme.lexeme(Lexeme.LITERAL_CHAR, Character.valueOf((char) i), this));
				}
				catch (StringIndexOutOfBoundsException exception)
				{
					throw new LexeralizeException("Unexpected ending of escape char.", this);
				}
				break;
			case '`' :
				try
				{
					StringBuilder builder = new StringBuilder();
					int i;
					while ((i = pollEscapedChar(this.line, '`')) != -1)
					{
						builder.append((char) i);
					}
					this.lexemes.add(Lexeme.lexeme(Lexeme.LITERAL_CUSTOM, builder.toString(), this));
				}
				catch (StringIndexOutOfBoundsException exception)
				{
					throw new LexeralizeException("Unexpected ending of custom literal.", this);
				}
				break;
			case '/':
				if (this.col < this.line.length())
				{
					switch (this.line.charAt(this.col))
					{
					case '*' :
						this.readingAnnotation = true;
						if ((this.col = parseMultiAnnotation(this.line, this.col)) == -1)
							return;
						break;
					case '/' :
						return;//Annotation start, all code in same line skipped.
					}
				}
			default:
				if (Character.isDigit(chr))
				{
					this.lexemes.add(readNumber(this.line, chr));
				}
				else if (Character.isLetterOrDigit(chr))
				{
					this.lexemes.add(readSymbol(this.line, chr));
				}
				else if (!Character.isWhitespace(chr))
				{
					this.lexemes.add(readOperatorSymbol(this.line, chr, new StringBuilder()));
				}
				break;
			}
		}
	}
	
	private int parseMultiAnnotation(String line, int start)
	{
		int i = line.indexOf("*/", start);
		if (i == -1)
			return -1;
		this.readingAnnotation = false;
		return i + 2;
	}
	
	private int pollEscapedChar(String line, char exclude) throws LexeralizeException
	{
		try
		{
			char chr = line.charAt(this.col++);
			if (chr == exclude) return -1;
			else if (chr == '\\')
			{
				chr = line.charAt(this.col++);
				switch (chr)
				{
				case '\\': return '\\';
				case '\'': return '\'';
				case '\"': return '\"';
				case '`' : return  '`';//For Custom Literal.
				case 'b' : return '\b';
				case 'r' : return '\r';
				case 'f' : return '\f';
				case 't' : return '\t';
				case 'n' : return '\n';
				case 'u' :
				{
					int i, j;
					j = Character.digit(line.charAt(this.col++), 16);
					if (j == -1)
						throw new LexeralizeException("Invalid unicode escape data character got.", this);
					i  = j << 12;
					j = Character.digit(line.charAt(this.col++), 16);
					if (j == -1)
						throw new LexeralizeException("Invalid unicode escape data character got.", this);
					i |= j <<  8;
					j = Character.digit(line.charAt(this.col++), 16);
					if (j == -1)
						throw new LexeralizeException("Invalid unicode escape data character got.", this);
					i |= j <<  4;
					j = Character.digit(line.charAt(this.col++), 16);
					if (j == -1)
						throw new LexeralizeException("Invalid unicode escape data character got.", this);
					i |= j      ;
					return (char) i;
				}
				default : if (Character.digit(chr, 8) != -1)
				{
					int i = Character.digit(chr, 8);
					chr = line.charAt(this.col);
					int x;
					if ((x = Character.digit(chr, 8)) != -1)
					{
						i = i << 3 | x;
						++this.col;
					}
					return (char) i;
				}
				throw new LexeralizeException("Unknown escape character got: " + chr, this);
				}
			}
			else return chr;
		}
		catch (StringIndexOutOfBoundsException exception)
		{
			this.col = line.length() - 1;
			throw new LexeralizeException("Illegal escape character parse.", this);
		}
	}
	
	private Lexeme readNumber(String line, char current) throws LexeralizeException
	{
		StringBuilder builder = new StringBuilder();
		if (current == '0')
			switch (line.charAt(this.col))
			{
			case 'x' :
			{
				while (++this.col < line.length() && Character.digit(line.charAt(this.col), 16) != -1)
					builder.append(line.charAt(this.col));
				try
				{
					if (this.col + 1 < line.length() && line.charAt(this.col) == '.' &&
							Character.digit(line.charAt(this.col + 1), 16) != -1)
					{
						builder.append('.');
						this.col++;
						while (Character.digit(line.charAt(this.col++), 16) != -1)
							builder.append(line.charAt(this.col - 1));
						if (Character.toLowerCase(line.charAt(this.col)) == 'p')
						{
							builder.append('p');
							switch (line.charAt(++this.col))
							{
							case '+' :
							case '-' :
								builder.append(line.charAt(this.col++));
								break;
							}
							while (Character.digit(line.charAt(this.col++), 10) != -1)
								builder.append(line.charAt(this.col - 1));
						}
						String value = builder.toString();
						builder = new StringBuilder();
						while (this.col < line.length() && Character.isLetter(line.charAt(this.col++)))
						{
							builder.append(line.charAt(this.col - 1));
						}
						String value1 = builder.toString();
						switch (value1)
						{
						case Strings.EMPTY :
						case "D" :
						case "d" :
							return Lexeme.lexeme(Lexeme.LITERAL_INT, Double.valueOf(value), this);
						case "F" :
						case "f" :
							return Lexeme.lexeme(Lexeme.LITERAL_INT, Float.valueOf(value), this);
						default:
							throw new LexeralizeException("Hex point number literal only supported with float or double value.", this);
						}
					}
					String value = builder.toString();
					builder = new StringBuilder();
					while (this.col < line.length() && Character.isLetter(line.charAt(this.col++)))
					{
						builder.append(line.charAt(this.col - 1));
					}
					String value1 = builder.toString();
					switch (value1)
					{
					case Strings.EMPTY :
						return Lexeme.lexeme(Lexeme.LITERAL_INT, Integer.valueOf(value, 16), this);
					case "L" :
					case "l" :
						return Lexeme.lexeme(Lexeme.LITERAL_INT, Long.valueOf(value, 16), this);
					default:
						return Lexeme.lexeme(Lexeme.LITERAL_CUSTOM_NUM,
								new Object[] {new BigInteger(value, 16).toString(), value1.toUpperCase()}, this);
					}
				}
				catch (NumberFormatException exception)
				{
					throw new LexeralizeException(exception, this);
				}
			}
			case 'b' :
			case 'B' :
			{
				BitSet set = new BitSet();
				while (++this.col < line.length() && (line.charAt(this.col) >>> 1) == 0x18/* chr == '0' || chr == '1' */)
					if (line.charAt(this.col) == '1')
						set.set(set.length());
				BitSet set2 = new BitSet(set.length());
				for (int i = set.length() - 1; i >= 0; set2.set(set.length() - i - 1, set.get(i)), --i);
				try
				{
					builder = new StringBuilder();
					while (this.col < line.length() && Character.isLetter(line.charAt(this.col++)))
					{
						builder.append(line.charAt(this.col - 1));
					}
					String value1 = builder.toString();
					switch (value1)
					{
					case Strings.EMPTY :
						if (set2.length() > 32)
							throw new NumberFormatException("Number is out of range.");
						return Lexeme.lexeme(Lexeme.LITERAL_INT, (int) (set2.toLongArray()[0] & 0xFFFFFFFFL), this);
					case "L" :
					case "l" :
						if (set2.length() > 64)
							throw new NumberFormatException("Number is out of range.");
						return Lexeme.lexeme(Lexeme.LITERAL_INT, set2.toLongArray()[0], this);
					default:
						return Lexeme.lexeme(Lexeme.LITERAL_CUSTOM_NUM,
								new Object[] {new BigInteger(set2.toByteArray()).toString(), value1.toUpperCase()}, this);
					}
				}
				catch (NumberFormatException exception)
				{
					throw new LexeralizeException(exception, this);
				}
			}
			default :
				builder.append('0');
			}
		else
			builder.append(current);
		while (this.col < line.length() && Character.digit(line.charAt(this.col++), 10) != -1)
			builder.append(line.charAt(this.col - 1));
		if (line.charAt(this.col) == '.' && this.col < line.length()
				&& Character.digit(line.charAt(this.col + 1), 10) != -1)
		{
			builder.append('.');
			while (this.col < line.length() && Character.digit(line.charAt(++this.col), 10) != -1)
				builder.append(line.charAt(this.col));
			if (Character.toLowerCase(line.charAt(this.col)) == 'e')
			{
				builder.append('e');
				switch (line.charAt(++this.col))
				{
				case '+' :
				case '-' :
					builder.append(line.charAt(this.col++));
					break;
				}
				while (Character.digit(line.charAt(this.col++), 10) != -1)
					builder.append(line.charAt(this.col - 1));
			}
			String value = builder.toString();
			builder = new StringBuilder();
			while (this.col < line.length() && Character.isLetter(line.charAt(this.col++)))
			{
				builder.append(line.charAt(this.col - 1));
			}
			String value1 = builder.toString();
			try
			{
				switch (value1)
				{
				case Strings.EMPTY :
				case "d" :
				case "D" :
					return Lexeme.lexeme(Lexeme.LITERAL_FLOAT, Double.valueOf(value), this);
				case "f" :
				case "F" :
					return Lexeme.lexeme(Lexeme.LITERAL_INT, Float.valueOf(value), this);
				default:
					return Lexeme.lexeme(Lexeme.LITERAL_CUSTOM_NUM, new Object[] {value, value1.toUpperCase()}, this);
				}
			}
			catch (NumberFormatException exception)
			{
				throw new LexeralizeException(exception, this);
			}
		}
		else try
		{
			String value = builder.toString();
			builder = new StringBuilder();
			while (this.col < line.length() && Character.isLetter(line.charAt(this.col++)))
			{
				builder.append(line.charAt(this.col - 1));
			}
			String value1 = builder.toString();
			switch (value1)
			{
			case "d" :
			case "D" :
				return Lexeme.lexeme(Lexeme.LITERAL_FLOAT, Double.valueOf(value), this);
			case "f" :
			case "F" :
				return Lexeme.lexeme(Lexeme.LITERAL_FLOAT, Float.valueOf(value), this);
			case "l" :
			case "L" :
				return Lexeme.lexeme(Lexeme.LITERAL_INT, Long.valueOf(value), this);
			case Strings.EMPTY :
				return Lexeme.lexeme(Lexeme.LITERAL_INT, Integer.valueOf(value), this);
			default:
				return Lexeme.lexeme(Lexeme.LITERAL_CUSTOM_NUM, new Object[] {value, value1.toUpperCase()}, this);
			}
		}
		catch (NumberFormatException exception)
		{
			throw new LexeralizeException(exception.getMessage(), this);
		}
	}
	
	private Lexeme readSymbol(String line, char current) throws LexeralizeException
	{
		StringBuilder builder = new StringBuilder().append(current);
		while (this.col < line.length())
		{
			char value = line.charAt(this.col++);
			if (value == '_')
			{
				builder.append('_');
				if (this.col + 1 < line.length() && !Character.isLetterOrDigit(value))
				{
					this.col += 2;
					return readOperatorSymbol(line, value, builder);
				}
			}
			else if (Character.isLetterOrDigit(value))
				builder.append(value);
			else break;
		}
		this.col --;
		return Lexeme.lexeme(Lexeme.SYMBOL, builder.toString(), this);
	}
	
	private Lexeme readOperatorSymbol(String line, char current, StringBuilder builder) throws LexeralizeException
	{
		builder.append(current);
		while (this.col < line.length())
		{
			char value;
			if (!Character.isLetterOrDigit(value = line.charAt(this.col++)) && !Character.isWhitespace(value) &&
					"([{}]).,;".indexOf(value) == -1)
				builder.append(value);
			else break;
		}
		this.col --;
		return Lexeme.lexeme(Lexeme.SYMBOL, builder.toString(), this);
	}
	
	private void parseMacro(Macro<?> macro) throws LexeralizeException
	{
		if (macro.type() == Macro.LEXERALIZE)
		{
			try
			{
				((Macro<? super StreamLexer>) macro).affect(this);
			}
			catch (LexeralizeException exception)
			{
				throw exception;
			}
			catch (Exception exception)
			{
				throw new LexeralizeException(exception, this);
			}
		}
		else
		{
			setSource(macro);
			this.lexemes.add(Lexeme.lexeme(Lexeme.MACRO, macro, this));
		}
	}
	
	@Override
	public ISourceInfo applyInfo()
	{
		return this;
	}
}
