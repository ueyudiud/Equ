/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import equ.util.GS;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
public enum MacroFactroies implements MacroFactory<Preoperator>
{
	DEF
	{
		@Override
		public Macro<?> parse(Preoperator handler, String line) throws PreoperationException
		{
			String[] split = Strings.split(line, ' ', 2);
			if (split.length == 1)
			{
				Matcher matcher = PATTERN1.matcher(split[0]);
				if (matcher.matches())
				{
					return new MacroDefine(split[0], EMPTY);
				}
			}
			else
			{
				String[] split2 = PATTERN1.split(split[0], 2);
				if (split2[0].length() == 0)
				{
					if (split2[1].length() == 0)
					{
						return new MacroDefine(split[0], new StringReplacement(0, new int[] {0x80000000}, new String[] {split[1]}));
					}
					else if (PATTERN2.matcher(split2[1]).matches())
					{
						List<String> list = new ArrayList<>(4);
						String string = split[1];
						Matcher matcher = PATTERN1.matcher(split2[1]);
						while (matcher.find())
						{
							String group = matcher.group();
							string = string.replaceAll(group, "\0" + Character.valueOf((char) (list.size() + 1)) + "\0");
							if (list.contains(group))
								throw new PreoperationException("The macro parameter '" + group + "' has defined twice!", handler);
							list.add(group);
						}
						String[] transformed = Strings.split(string, '\0');
						if ((transformed.length & 1) == 0)
							throw new PreoperationException("Illegal split.", handler);
						List<Integer> codes = new ArrayList<>(transformed.length);
						List<String> values = new ArrayList<>();
						for (int i = 0; i < transformed.length; i += 2)
						{
							if (transformed[i].length() > 0)
							{
								codes.add(0x80000000 | GS.getOrAddToList(values, transformed[i]));
							}
							if (i + 1 < transformed.length)
							{
								codes.add(transformed[i + 1].charAt(0) - 1);
							}
						}
						return new MacroDefine(split[0].substring(0, split[0].indexOf('(')),
								new StringReplacement(list.size(), codes.stream().mapToInt(Integer::intValue).toArray(),
										values.toArray(new String[values.size()])));
					}
				}
			}
			throw new PreoperationException("Fail to parse #def macro.", handler);
		}
	},
	UNDEF
	{
		@Override
		public Macro<?> parse(Preoperator handler, String line) throws PreoperationException
		{
			if (!MacroFactroies.PATTERN1.matcher(line).matches())
				throw new PreoperationException("Fail to parse #undef macro.", handler);
			return new MacroUndef(line);
		}
	},
	IFDEF
	{
		@Override
		public Macro<?> parse(Preoperator handler, String line) throws PreoperationException
		{
			if (!MacroFactroies.PATTERN1.matcher(line).matches())
				throw new PreoperationException("Fail to parse #ifdef macro.", handler);
			return new MacroIfdef(line);
		}
	},
	IFNDEF
	{
		@Override
		public Macro<?> parse(Preoperator handler, String line) throws PreoperationException
		{
			if (!MacroFactroies.PATTERN1.matcher(line).matches())
				throw new PreoperationException("Fail to parse #ifndef macro.", handler);
			return new MacroIfndef(line);
		}
	},
	ELSE
	{
		@Override
		public Macro<?> parse(Preoperator handler, String line) throws PreoperationException
		{
			if (line.length() != 0)
				throw new PreoperationException("Fail to parse #else macro.", handler);
			return MacroElse.INSTANCE;
		}
	},
	ENDIF
	{
		@Override
		public Macro<?> parse(Preoperator handler, String line) throws PreoperationException
		{
			if (line.length() != 0)
				throw new PreoperationException("Fail to parse #endif macro.", handler);
			return MacroEndif.INSTANCE;
		}
	},
	CAL
	{
		@Override
		public Macro<?> parse(Preoperator handler, String line) throws PreoperationException
		{
			String[] split = Strings.split(line, ' ');
			if (split.length != 2)
				throw new PreoperationException("Fail to parse #cal macro.", handler);
			try
			{
				return new MacroCalculateLevel(Integer.parseInt(split[0]), split[1]);
			}
			catch (NumberFormatException exception)
			{
				throw new PreoperationException("Illegal calculating level: " + exception.getMessage(), handler);
			}
		}
	};
	
	static final Pattern PATTERN1 = Pattern.compile("[a-zA-Z_]\\w*");
	static final Pattern PATTERN2 = Pattern.compile("\\([a-zA-Z_]\\w*(,[a-zA-Z_]\\w*)*\\)");
	static final StringReplacement EMPTY = new StringReplacement(0, new int[0], new String[0]);
	
	@Override public Macro<?> parse(Preoperator handler, String line) throws PreoperationException { return null; }
}
