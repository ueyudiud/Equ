/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import equ.compiler.util.AbstractSourceInfo;
import equ.compiler.util.ISourceInfo;
import equ.compiler.util.ISourceInfoProvider;
import equ.util.Dictionary;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
public class ReaderPreoperator extends AbstractSourceInfo implements ISourceInfoProvider, Preoperator
{
	BufferedReader reader;
	Map<String, MacroFactory<? super ReaderPreoperator>> macroFactories;
	TreeMap<Integer, Macro<?>> macros = new TreeMap<>();
	Dictionary<StringReplacement> replacement = new Dictionary<>();
	LinkedList<Object> transformed = new LinkedList<>();
	LinkedList<IfNestedLabel> ifnested = new LinkedList<>();
	boolean readln = true;
	boolean escape = true;
	
	{
		this.macroFactories = new HashMap<>();
		for (MacroFactroies factroies : MacroFactroies.values())
		{
			this.macroFactories.put(factroies.name().toLowerCase(), factroies);
		}
	}
	
	public ReaderPreoperator(Reader reader)
	{
		this.reader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
	}
	
	public ReaderPreoperator(BufferedReader reader)
	{
		this.reader = reader;
	}
	
	public Stream<?> preoperate() throws IOException, PreoperationException
	{
		while ((this.line = this.reader.readLine()) != null)
		{
			this.ln ++;
			if (this.line.length() > 0)
			{
				if (this.line.charAt(0) == '#')//Macro predicated.
				{
					int idx = this.line.indexOf(' ');
					if (idx == -1) idx = this.line.length();
					MacroFactory<? super ReaderPreoperator> factory = this.macroFactories.get(this.line.substring(1, idx));
					if (factory == null)
						throw new PreoperationException("Unknown macro got.", (ISourceInfo) this);
					Macro<?> macro = factory.parse(this, this.line.length() == idx ? Strings.EMPTY : this.line.substring(idx + 1));
					switch (macro.type())
					{
					case Macro.PREOPERATE :
						try
						{
							((Macro<? super ReaderPreoperator>) macro).affect(this);
						}
						catch (PreoperationException exception)
						{
							throw exception;
						}
						catch (Exception exception)
						{
							throw new InternalError(exception);
						}
						break;
					default:
						this.transformed.addLast(macro);
						break;
					}
				}
				else if (this.readln)
				{
					this.transformed.addLast(new Object[] { translateLine(), this.source, this.ln });
				}
			}
		}
		return this.transformed.stream();
	}
	
	private int checkHex(String line, int pos, int off) throws PreoperationException
	{
		int i = Character.digit(line.charAt(pos), 16);
		if (i == -1)
			throw new PreoperationException("Fail to match the hex number.", (ISourceInfo) this);
		return i << off;
	}
	
	@Override
	public void appendTransformed(Object value)
	{
		this.transformed.addLast(value);
	}
	
	private String translateLine() throws PreoperationException
	{
		this.line = this.line.replace('\t', ' ');
		final int length = this.line.length();
		char[] buf;
		int len;
		if (this.escape)
		{
			buf = new char[length];
			len = 0;
			for (int i = 0; i < length; ++i)
			{
				this.col = i;
				if (this.line.charAt(i) == '\\' && this.line.charAt(i + 1) == 'u')
				{
					if (i + 5 >= length)
						throw new PreoperationException("Illegal escape char.", (ISourceInfo) this);
					buf[len ++] = (char) (
							checkHex(this.line, i + 2, 12) |
							checkHex(this.line, i + 3,  8) |
							checkHex(this.line, i + 4,  4) |
							checkHex(this.line, i + 5,  0) );
					i += 5;
				}
				else
				{
					buf[len ++] = this.line.charAt(i);
				}
			}
		}
		else
		{
			buf = this.line.toCharArray();
			len = buf.length;
		}
		StringBuilder builder = new StringBuilder();
		transform(buf, 0, len, builder);
		return builder.toString();
	}
	
	private int transformDepth;
	
	void transform(char[] array, int from, int to, StringBuilder builder) throws PreoperationException
	{
		if (this.transformDepth++ >= 16)
		{
			this.col = from;
			throw new PreoperationException("Too many macro nested, the compiler only accept less than 16 size of macro nested.", (ISourceInfo) this);
		}
		int c = from;
		for (int i = from; i < to; ++i)
		{
			this.col = i;
			if (!(Character.isLetter(array[i]) || array[i] == '_') || !this.replacement.containsPrefix(array, c, i + 1))
			{
				builder.append(array, c, i + 1 - c);
				c = i + 1;
			}
			else
			{
				StringReplacement replacement = this.replacement.get(array, null, c, i + 1);
				if (replacement != null && (!Character.isLetter(array[i + 1]) && array[i + 1] != '_' || i + 1 == to))
				{
					if (replacement.parameterCount > 0)
					{
						if (i + 1 >= to || array[i + 1] != '(')
						{
							this.col = i + 1;
							throw new PreoperationException("The macro is missing parameters.", (ISourceInfo) this);
						}
						int rcount = 0;
						String[] replacements = new String[replacement.parameterCount];
						i += 2;
						int j = i;
						int c1 = j;
						int depth = 0;
						label: for (; j < to; ++j)
						{
							this.col = j;
							switch (array[j])
							{
							case '(' : ++depth; break;
							case ')' :
								if (depth-- == 0)
								{
									StringBuilder builder2 = new StringBuilder();
									transform(array, c1, j, builder2);
									replacements[rcount++] = builder2.toString();
									c1 = j + 1;
									break label;
								}
								break;
							case ',' :
								if (depth == 0)
								{
									StringBuilder builder2 = new StringBuilder();
									transform(array, c1, j, builder2);
									replacements[rcount++] = builder2.toString();
									c1 = j + 1;
								}
								break;
							}
						}
						c = (i = j) + 1;
						if (depth != -1)
							throw new PreoperationException("Wrong bracket nested.", (ISourceInfo) this);
						if (rcount != replacements.length)
							throw new PreoperationException("Wrong macro paramater count.", (ISourceInfo) this);
						String transformed = replacement.transform(replacements);
						transform(transformed.toCharArray(), 0, transformed.length(), builder);
					}
					else
					{
						c = i + 1;
						String transformed = replacement.transform();
						transform(transformed.toCharArray(), 0, transformed.length(), builder);
					}
				}
			}
		}
		builder.append(array, c, to - c);
		this.transformDepth --;
	}
	
	@Override
	public final ISourceInfo applyInfo()
	{
		return this;
	}
	
	@Override
	public void addReplacement(String key, StringReplacement replacement) throws PreoperationException
	{
		if (this.replacement.put(key, replacement) != null)
			throw new PreoperationException("The macro " + key + " has defined twice!", (ISourceInfo) this);
	}
	
	@Override
	public void removeReplacement(String key) throws PreoperationException
	{
		this.replacement.remove(key);
	}
	
	@Override
	public boolean isLineReading()
	{
		return this.readln;
	}
}
