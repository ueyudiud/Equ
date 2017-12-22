/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.base;

import equ.lib.type.SType;
import equ.lib.type.SVariableType;
import equ.util.GS;

/**
 * @author ueyudiud
 */
public interface SParameterizedDeclearation<D extends SGenericDeclearation, D1 extends SDeclearation> extends SDeclearation<D1>
{
	SDeclearation getGenericOwner();
	
	D getRawDeclearation();
	
	SType[] getActualTypeArguments();
	
	@Override
	default boolean equal(D1 declearation)
	{
		if (declearation == this)
			return true;
		else if (declearation instanceof SParameterizedDeclearation<?, ?>)
		{
			SParameterizedDeclearation<?, ?> declearation2 = (SParameterizedDeclearation<?, ?>) declearation;
			return getRawDeclearation().equal(declearation2.getRawDeclearation()) &&
					(getGenericOwner() == null || getGenericOwner().equal(declearation2.getRawDeclearation())) &&
					GS.orderedAll(getActualTypeArguments(), declearation2.getActualTypeArguments(), SDeclearation::equal);
		}
		else
			return false;
	}
	
	@Override
	default boolean convert(D1 declearation)
	{
		if (declearation == this || getRawDeclearation().equal(declearation))
			return true;
		else if (declearation instanceof SParameterizedDeclearation<?, ?>)
		{
			SParameterizedDeclearation<?, ?> declearation2 = (SParameterizedDeclearation<?, ?>) declearation;
			return getRawDeclearation().convert(declearation2.getRawDeclearation()) &&
					(getGenericOwner() == null || getGenericOwner().convert(declearation2.getRawDeclearation())) &&
					$checkTypeArguments(declearation2.getActualTypeArguments());
		}
		else
			return false;
	}
	
	/*protected*/default boolean $checkTypeArguments(SType[] types)
	{
		SVariableType<?>[] ts1 = getRawDeclearation().getTypeParameters();
		SType[] ts2 = getActualTypeArguments();
		assert types.length == ts1.length;
		for (int i = 0; i < ts1.length; ++i)
		{
			switch (ts1[i].getVariableType())
			{
			case NORMAL :
				if (!ts2[i].cast(types[i]))
					return false;
				break;
			case CONTRAVARIANT :
				if (!ts2[i].convert(types[i]))
					return false;
				break;
			case COVARIANCE :
				if (!types[i].convertFrom(ts2[i]))
					return false;
				break;
			}
		}
		return true;
	}
	
	@Override
	default SType $transformVariableType(SVariableType<?> type)
	{
		SVariableType<?>[] types = getRawDeclearation().getTypeParameters();
		int i = GS.indexOf(types, type);
		if (i != -1)
		{
			return getActualTypeArguments()[i];
		}
		else
		{
			SDeclearation owner = getGenericOwner();
			return owner != null ? owner.$transformVariableType(type) : type;
		}
	}
}
