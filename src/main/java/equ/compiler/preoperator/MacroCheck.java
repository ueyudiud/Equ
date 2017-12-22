/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import equ.compiler.util.AbstractSourceInfo;
import equ.compiler.util.ISourceInfo;

/**
 * @author ueyudiud
 */
abstract class MacroCheck extends AbstractSourceInfo implements Macro<ReaderPreoperator>
{
	String key;
	
	@Override
	public int type()
	{
		return PREOPERATE;
	}
}

class MacroIfdef extends MacroCheck
{
	MacroIfdef(String key)
	{
		this.key = key;
	}
	
	@Override
	public void affect(ReaderPreoperator handler)
	{
		handler.ifnested.addLast(new IfNestedLabel(handler.replacement.containsKey(this.key)));
		handler.readln = handler.ifnested.stream().allMatch(l->l.enable);
	}
}

class MacroIfndef extends MacroCheck
{
	MacroIfndef(String key)
	{
		this.key = key;
	}
	
	@Override
	public void affect(ReaderPreoperator handler)
	{
		handler.ifnested.addLast(new IfNestedLabel(!handler.replacement.containsKey(this.key)));
		handler.readln = handler.ifnested.stream().allMatch(l->l.enable);
	}
}

class MacroElse extends MacroCheck
{
	public static final MacroElse INSTANCE = new MacroElse();
	
	@Override
	public void affect(ReaderPreoperator handler) throws PreoperationException
	{
		if (handler.ifnested.isEmpty())
			throw new PreoperationException("Wrong nested for #else.", (ISourceInfo) handler);
		IfNestedLabel label = handler.ifnested.getLast();
		label.enable = !label.enable;
		handler.readln = handler.ifnested.stream().allMatch(l->l.enable);
	}
}

class MacroEndif extends MacroCheck
{
	public static final MacroEndif INSTANCE = new MacroEndif();
	
	@Override
	public void affect(ReaderPreoperator handler) throws PreoperationException
	{
		if (handler.ifnested.isEmpty())
			throw new PreoperationException("Wrong nested for #endif.", (ISourceInfo) handler);
		handler.ifnested.removeLast();
		handler.readln = handler.ifnested.stream().allMatch(l->l.enable);
	}
}