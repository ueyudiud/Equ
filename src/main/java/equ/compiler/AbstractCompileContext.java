/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import equ.compiler.macro.MacroBuiltin;
import equ.compiler.macro.MacroNative;

/**
 * @author ueyudiud
 */
abstract class AbstractCompileContext implements CompileContext
{
	private final ImmutableMap<String, IMacro> basicMacros;
	
	{
		ImmutableMap.Builder<String, IMacro> builder = ImmutableMap.builder();
		for (MacroBuiltin macro : MacroBuiltin.values())
		{
			builder.put(macro.name(), macro);
		}
		builder.put("__IS_DEFINED__", new IMacro()
		{
			@Override
			public List<ILexeme> transform(ILexeme header, List<ILexeme>... parameters)
			{
				List<ILexeme> lexemes = parameters[0];
				return ImmutableList.of(new LexemeBool(
						lexemes.size() == 1 && macros.containsKey(lexemes.get(0).stringValue()), header.trace()));
			}
			
			@Override
			public int parameterCount()
			{
				return 1;
			}
			
			@Override
			public String name()
			{
				return "__IS_DEFINED__";
			}
		});
		IMacro eval;
		builder.put("__EVAL__", eval = new IMacro()
		{
			@Override
			public List<ILexeme> transform(ILexeme header, List<ILexeme>... parameters)
			{
				return ImmutableList.of(eval(header.trace(), parameters[0]));
			}
			
			@Override
			public int parameterCount()
			{
				return 1;
			}
			
			@Override
			public String name()
			{
				return "__EVAL__";
			}
		});
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		try
		{
			builder.put("__STOI__", new MacroNative(eval, "__STOI__", lookup.findStatic(Integer.class, "parseInt", MethodType.methodType(int.class, String.class))));
			builder.put("__STOL__", new MacroNative(eval, "__STOL__", lookup.findStatic(Long.class, "parseLong", MethodType.methodType(long.class, String.class))));
			builder.put("__STOF__", new MacroNative(eval, "__STOF__", lookup.findStatic(Float.class, "parseFloat", MethodType.methodType(float.class, String.class))));
			builder.put("__STOD__", new MacroNative(eval, "__STOD__", lookup.findStatic(Double.class, "parseDouble", MethodType.methodType(double.class, String.class))));
		}
		catch (NoSuchMethodException | IllegalAccessException e)
		{
			throw new InternalError(e);
		}
		basicMacros = builder.build();
	}
	
	protected Map<String, IMacro> macros = new HashMap<>(basicMacros);
	
	protected void refreshContext()
	{
		clear();
	}
	
	private final MacroEvaluator evaluator = new MacroEvaluator(this);
	
	@Override
	public ILexeme eval(SourceTrace evalCaller, CompileContext context, List<ILexeme> values) throws CompileException
	{
		return new MacroEvaluator(context).eval(evalCaller, values);
	}
	
	@Override
	public ILexeme eval(SourceTrace evalCaller, List<ILexeme> values) throws CompileException
	{
		return evaluator.eval(evalCaller, values);
	}
	
	public int size() { return macros.size(); }
	public boolean isEmpty() { return macros.isEmpty(); }
	public boolean containsKey(Object key) { return macros.containsKey(key); }
	public boolean containsValue(Object value) { return macros.containsValue(value); }
	public IMacro get(Object key) { return macros.get(key); }
	public IMacro put(String key, IMacro value) { return macros.put(key, value); }
	public void putAll(Map<? extends String, ? extends IMacro> m) { macros.putAll(m); }
	public Set<String> keySet() { return Collections.unmodifiableSet(macros.keySet()); }
	public Collection<IMacro> values() { return Collections.unmodifiableCollection(macros.values()); }
	public Set<Entry<String, IMacro>> entrySet() { return Collections.unmodifiableSet(macros.entrySet()); }
	
	public IMacro remove(Object key)
	{
		IMacro macro = basicMacros.get(key);
		if (macro != null)
		{
			IMacro result = macros.put((String) key, macro);
			return result == macro ? null : result;
		}
		else
		{
			return macros.remove(key);
		}
	}
	
	public void clear()
	{
		macros.clear();
		macros.putAll(basicMacros);
	}
}
