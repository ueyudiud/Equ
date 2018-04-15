/*
 * copyright© 2017 ueyudiud
 */
package equ.util;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author ueyudiud
 */
public final class Spliterators1
{
	public static <T> Spliterator<T> single(T value)
	{
		return new Spliterator<T>()
		{
			boolean used = false;
			
			@Override
			public boolean tryAdvance(Consumer<? super T> action)
			{
				if (!this.used)
				{
					action.accept(value);
					this.used = true;
					return true;
				}
				return false;
			}
			
			@Override
			public void forEachRemaining(Consumer<? super T> action)
			{
				if (!this.used)
				{
					action.accept(value);
					this.used = true;
				}
			}
			
			@Override
			public Spliterator<T> trySplit()
			{
				return null;
			}
			
			@Override
			public long estimateSize()
			{
				return 1;
			}
			
			@Override
			public int characteristics()
			{
				return DISTINCT | SIZED | IMMUTABLE;
			}
		};
	}
}
