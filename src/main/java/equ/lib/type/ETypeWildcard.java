/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.type;

import java.util.function.Function;

/**
 * @author ueyudiud
 */
public interface ETypeWildcard extends EType
{
	EType[] upperBounds();
	
	EType lowerBound();
}

class ETypeWildcard_ implements ETypeWildcard
{
	final EType[] upper;
	final EType lower;
	
	public ETypeWildcard_(EType lower, EType... upper)
	{
		this.upper = upper;
		this.lower = lower;
	}
	
	@Override
	public EType rawType()
	{
		return upper[0];
	}
	
	@Override
	public String getName()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("_");
		if (lower != null)
		{
			builder.append(" >: ").append(lower.getName());
		}
		if (upper.length != 0 && (!(upper[0] instanceof ETypeSpecial) || ((ETypeSpecial) upper[0]).type() != EnumSpecialType.BOT))
		{
			builder.append(" <: ");
			builder.append(upper[0].getName());
			for (int i = 1; i < upper.length; ++i)
			{
				builder.append(", ").append(upper[i].getName());
			}
		}
		return builder.toString();
	}
	
	@Override
	public EType[] upperBounds()
	{
		return upper.clone();
	}
	
	@Override
	public EType lowerBound()
	{
		return lower;
	}
	
	@Override
	public EType map(Function<EType, EType> mapping)
	{
		EType lower = mapping.apply(this.lower);
		EType[] upper = new EType[this.upper.length];
		for (int i = 0; i < upper.length; ++i)
		{
			upper[i] = mapping.apply(this.upper[i]);
		}
		return new ETypeWildcard_(lower, upper);
	}
}