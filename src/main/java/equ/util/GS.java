/*
 * copyright© 2017 ueyudiud
 */
package equ.util;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author ueyudiud
 */
public class GS
{
	public static <K, T> T[] transform(K[] source, Function<? super K, ? extends T> function, Class<T> class1)
	{
		T[] array = (T[]) Array.newInstance(class1, source.length);
		for (int i = 0; i < array.length; array[i] = function.apply(source[i]), ++i);
		return array;
	}
	
	public static <K> Object[] transform(K[] source, Function<? super K, ?> function)
	{
		Object[] array = new Object[source.length];
		for (int i = 0; i < array.length; array[i] = function.apply(source[i]), ++i);
		return array;
	}
	
	public static <E> int getOrAddToList(List<? super E> list, E element)
	{
		int idx = list.indexOf(element);
		if (idx == -1)
		{
			idx = list.size();
			list.add(element);
		}
		return idx;
	}
	
	public static <E> boolean all(E[] elements, Predicate<E> predicate)
	{
		for (E e : elements)
			if (!predicate.test(e))
				return false;
		return true;
	}
	
	public static <E> boolean any(E[] elements, Predicate<E> predicate)
	{
		for (E e : elements)
			if (predicate.test(e))
				return true;
		return false;
	}
	
	public static <E> E of(E[] elements, Predicate<E> predicate)
	{
		for (E e : elements)
			if (predicate.test(e))
				return e;
		return null;
	}
	
	public static <E1, E2> boolean orderedAll(E1[] elements1, E2[] elements2, BiPredicate<E1, E2> predicate)
	{
		if (elements1.length != elements2.length)
			return false;
		for (int i = 0; i < elements1.length; ++i)
		{
			if (!predicate.test(elements1[i], elements2[i]))
				return false;
		}
		return true;
	}
	
	public static int lastIndexOf(Object[] array, Object arg)
	{
		int i = array.length;
		while (--i >= 0)
			if (Objects.equals(array[i], arg))
				break;
		return i;
	}
	
	public static int indexOf(char[] array, int from, int to, char arg)
	{
		int i = from;
		while (i < to)
			if (array[i] == arg)
				return i;
		return -1;
	}
}
