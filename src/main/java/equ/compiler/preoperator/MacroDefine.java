/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import equ.compiler.util.AbstractSourceInfo;

/**
 * @author ueyudiud
 */
class MacroDefine extends AbstractSourceInfo implements Macro<Preoperator>
{
	private String key;
	private StringReplacement replacement;
	
	MacroDefine(String key, StringReplacement replacement)
	{
		this.key = key;
		this.replacement = replacement;
	}
	
	@Override
	public int type()
	{
		return PREOPERATE;
	}
	
	@Override
	public void affect(Preoperator resolver) throws PreoperationException
	{
		if (resolver.isLineReading())
			resolver.addReplacement(this.key, this.replacement);
	}
}
