/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.type;

/**
 * @author ueyudiud
 */
public final class ETypes
{
	public static EType transform(EType target, ETypeVariable[] source, EType[] parameters)
	{
		for (int i = 0; i < parameters.length; ++i)
		{
			if (!checkBounds(parameters[i], source[i]))
				throw new IllegalArgumentException();
		}
		return transform_(target, source, parameters);
	}
	
	public static EType transform_(EType target, ETypeVariable[] source, EType[] parameters)
	{
		if (target instanceof ETypeVariable)
		{
			for (int i = 0; i < source.length; ++i)
			{
				if (source[i].equals(target))
				{
					return parameters[i];
				}
			}
		}
		else if (target instanceof ETypeParameterized)
		{
			ETypeConstructor constructor = ((ETypeParameterized) target).constructor();
			boolean flag = false;
			EType[] parameters1 = ((ETypeParameterized) target).parameters();
			for (int i = 0; i < parameters1.length; ++i)
			{
				EType type = parameters1[i];
				if ((type = transform_(target, source, parameters1)) != parameters1[i])
				{
					flag = true;
					parameters1[i] = type;
				}
			}
			return flag ? constructor.transform_(parameters1) : target;
		}
		else if (target instanceof ETypeWildcard)
		{
			EType[] upper = ((ETypeWildcard) target).upperBounds();
			EType lower = ((ETypeWildcard) target).lowerBound();
			boolean flag = false;
			EType type;
			for (int i = 0; i < upper.length; ++i)
			{
				type = upper[i];
				if ((type = transform_(target, source, upper)) != upper[i])
				{
					flag = true;
					upper[i] = type;
				}
			}
			if (lower != null && (type = transform_(lower, source, parameters)) != lower)
			{
				lower = type;
			}
			return flag ? wildcard(lower, upper) : target;
		}
		return target;
	}
	
	public static ETypeParameterized parameterizedType(ETypeConstructor constructor, EType... parameters)
	{
		final ETypeVariable[] variables = constructor.variables();
		for (int i = 0; i < parameters.length; ++i)
		{
			if (!checkBounds(parameters[i], variables[i]))
				throw new IllegalArgumentException();
		}
		return parameterizedType_(constructor, parameters);
	}
	
	public static ETypeParameterized parameterizedType_(ETypeConstructor constructor, EType... parameters)
	{
		return constructor.transform_(parameters);
	}
	
	public static ETypeParameterized parameterizedType__(ETypeConstructor constructor, EType... parameters)
	{
		return new ETypeParameterized_(constructor, parameters);
	}
	
	public static ETypeWildcard wildcard(EType lowerBound, EType... upperBounds)
	{
		return new ETypeWildcard_(lowerBound, upperBounds);
	}
	
	public static boolean checkBounds(EType target, ETypeVariable variable)
	{
		for (EType type : variable.bounds())
		{
			if (!belong(target, type))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * =:
	 */
	public static boolean cast(EType type1, EType type2)
	{
		if (type1.equals(type2))
		{
			return true;
		}
		else if (type2 instanceof ETypeWildcard)
		{
			for (EType upper : ((ETypeWildcard) type2).upperBounds())
			{
				if (!belong(upper, type2))
				{
					return false;
				}
			}
			return ((ETypeWildcard) type2).lowerBound() == null ||
					belong(type1, ((ETypeWildcard) type2).lowerBound());
		}
		return false;
	}
	
	/**
	 *  <:
	 */
	public static boolean belong(EType type1, EType type2)
	{
		if (type1.equals(type2))
		{
			return true;
		}
		else if (type2 instanceof ETypeSpecial)
		{
			return belong_(type1, ((ETypeSpecial) type2).type());
		}
		else if (type1 instanceof ETypeSpecial)
		{
			return belong_(((ETypeSpecial) type1).type(), type2);
		}
		else if (type1 instanceof ETypeUnion)
		{
			for (EType type : ((ETypeUnion) type1).bounds())
			{
				if (!belong(type, type2))
					return false;
			}
			return true;
		}
		else if (type2 instanceof ETypeUnion)
		{
			for (EType type : ((ETypeUnion) type2).bounds())
			{
				if (belong(type1, type))
					return true;
			}
			return false;
		}
		else if (type2 instanceof ETypeWildcard)
		{
			for (EType upper : ((ETypeWildcard) type2).upperBounds())
			{
				if (!belong(upper, type1))
					return false;
			}
			return ((ETypeWildcard) type2).lowerBound() == null ||
					belong(type1, ((ETypeWildcard) type2).lowerBound());
		}
		else if (type1 instanceof ETypeWildcard)
		{
			for (EType type : ((ETypeWildcard) type1).upperBounds())
			{
				if (belong(type, type2))
					return true;
			}
			return false;
		}
		else if (type1 instanceof ETypeVariable)
		{
			for (EType type : ((ETypeVariable) type1).bounds())
			{
				if (belong(type, type2))
					return true;
			}
			return false;
		}
		//Array fast check.
		if (type2 instanceof EArrayType)
		{
			if (type1 instanceof EArrayType)
			{
				return belong(((EArrayType) type1).componentType(), ((EArrayType) type2).componentType());
			}
		}
		else if (type1 instanceof EArrayType)
		{
			return false;
		}
		if (type2 instanceof ETypeParameterized && type1 instanceof ETypeParameterized
				&& type1.rawType().equals(type2.rawType()))
		{
			ETypeVariable[] variables = ((ETypeParameterized) type1).constructor().variables();
			EType[] types1 = ((ETypeParameterized) type1).parameters();
			EType[] types2 = ((ETypeParameterized) type2).parameters();
			for (int i = 0; i < variables.length; ++i)
			{
				switch (variables[i].boundType())
				{
				default:
				case INVARIANT :
					if (!cast(types1[i], types2[i]))
						return false;
					break;
				case COVARIANT :
					if (!belong(types2[i], types1[i]))
						return false;
					break;
				case CONTRAVARIANT :
					if (!belong(types1[i], types2[i]))
						return false;
					break;
				}
			}
			return true;
		}
		if (type1 instanceof ETypeExtendable)
		{
			ETypeExtendable type = (ETypeExtendable) type1;
			do
			{
				for (EType t : type.interfaces())
				{
					if (belong(t, type2))
						return true;
				}
				type = type.zuper();
				if (type == null)
				{
					return false;
				}
				if (belong(type, type2))
				{
					return true;
				}
			}
			while (true);
		}
		return false;
	}
	
	private static boolean belong_(EnumSpecialType t1, EType t2)
	{
		switch (t1)
		{
		case TOP :
			return true;
		case NULL :
			return belong_(t2, EnumSpecialType.REF);
		default:
			return false;
		}
	}
	
	private static boolean belong_(EType t1, EnumSpecialType t2)
	{
		switch (t2)
		{
		case BOT : return true;
		case VAL :
		{
			EType type = t1.rawType();
			if (type instanceof ETypeSpecial)
			{
				switch (((ETypeSpecial) type).type())
				{
				case VAL :
				case TOP :
					return true;
				default :
					return false;
				}
			}
			else if (type instanceof EArrayType)
			{
				type = ((EArrayType) type).componentType();
				if (type instanceof EClass)
				{
					return ((EClass) type).primitive();
				}
				else
				{
					return belong_(type, t2);
				}
			}
			else if (type instanceof EClass)
			{
				return ((EClass) type).primitive();
			}
			else if (type instanceof ETypeUnion)
			{
				return false;
			}
			else
			{
				return false;
			}
		}
		case REF :
		{
			EType type = t1.rawType();
			if (type instanceof ETypeSpecial)
			{
				switch (((ETypeSpecial) type).type())
				{
				case REF :
				case NULL :
				case TOP :
					return true;
				default :
					return false;
				}
			}
			else if (type instanceof EArrayType)
			{
				type = ((EArrayType) type).componentType();
				if (type instanceof EClass)
				{
					return !((EClass) type).primitive();
				}
				else
				{
					return belong_(type, t2);
				}
			}
			else if (type instanceof EClass)
			{
				return !((EClass) type).primitive();
			}
			else if (type instanceof ETypeUnion || type instanceof ETypeVariable)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		case TOP :
		{
			return t1 instanceof ETypeSpecial ? ((ETypeSpecial) t1).type() == EnumSpecialType.TOP : false;
		}
		case NULL :
		{
			return t1 instanceof ETypeSpecial ?
					((ETypeSpecial) t1).type() == EnumSpecialType.TOP || ((ETypeSpecial) t1).type() == EnumSpecialType.NULL : false;
		}
		case VOID :
		{
			return t1 instanceof ETypeSpecial ?
					((ETypeSpecial) t1).type() == EnumSpecialType.TOP || ((ETypeSpecial) t1).type() == EnumSpecialType.VOID : false;
		}
		default:
			return false;
		}
	}
	
	private ETypes() {}
}
