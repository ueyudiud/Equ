/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import equ.compiler.util.AbstractSourceInfo;

/**
 * @author ueyudiud
 */
class MacroUndef extends AbstractSourceInfo implements Macro<Preoperator>
{
	private String key;
	
	MacroUndef(String key)
	{
		this.key = key;
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
			resolver.removeReplacement(this.key);
	}
}
