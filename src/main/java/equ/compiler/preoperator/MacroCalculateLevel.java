/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import equ.compiler.util.AbstractSourceInfo;

/**
 * @author ueyudiud
 */
public class MacroCalculateLevel extends AbstractSourceInfo implements Macro<Object>
{
	public int level;
	public String operator;
	
	public MacroCalculateLevel(int level, String operator)
	{
		this.level = level;
		this.operator = operator;
	}
	
	@Override
	public int type()
	{
		return PARSE;
	}
	
	@Override
	public void affect(Object handler) throws Exception
	{
		//TODO
	}
}
