/*
 * copyright© 2018 ueyudiud
 */
package equ.util;

/**
 * @author ueyudiud
 */
public class BoolLinkedSet
{
	private static final long ALL_TRUE = 0xFFFFFFFFFFFFFFFFl;
	
	private abstract class Chain
	{
		long bits = ALL_TRUE;
		int off = 0;
		Chain next;
		
		void append(boolean i)
		{
			if (off == Long.BYTES)
			{
				(last = next = new Chain2(this)).append(i);
			}
			else
			{
				if (i)
					bits |= 1 << off++;
				else
					bits &= ~(1 << off++);
			}
		}
		
		abstract boolean swap();
		
		abstract boolean set(boolean i);
		
		abstract boolean last();
		
		abstract boolean removeLast();
	}
	
	private class Chain1 extends Chain
	{
		@Override
		boolean swap()
		{
			if (off == 0)
				return false;
			bits ^= 1 << off - 1;
			return true;
		}
		
		@Override
		boolean set(boolean i)
		{
			if (off == 0)
				return false;
			if (i)
				bits |= 1 << off - 1;
			else
				bits &= ~(1 << off - 1);
			return true;
		}
		
		@Override
		boolean removeLast()
		{
			if (off == 0)
				return false;
			bits |= 1 << --off;
			return true;
		}
		
		@Override
		boolean last()
		{
			return off == 0 || (bits & (1 << off - 1)) != 0;
		}
	}
	
	private class Chain2 extends Chain
	{
		final Chain previous;
		
		Chain2(Chain c)
		{
			previous = c; c.next = this;
		}
		
		@Override
		boolean swap()
		{
			if (off == 0)
				return previous.swap();
			bits ^= 1 << off - 1;
			return true;
		}
		
		@Override
		boolean set(boolean i)
		{
			if (off == 0)
				return previous.set(i);
			if (i)
				bits |= 1 << off - 1;
			else
				bits &= ~(1 << off - 1);
			return true;
		}
		
		@Override
		boolean removeLast()
		{
			if (off == 0)
			{
				previous.next = null;
				last = previous;
				return previous.removeLast();
			}
			bits |= 1 << --off;
			return true;
		}
		
		@Override
		boolean last()
		{
			return (off == 0 && previous.last()) || (bits & (1 << off - 1)) != 0;
		}
	}
	
	private int size;
	private final Chain first = new Chain1();
	private Chain last = first;
	
	public int size()
	{
		return size;
	}
	
	public boolean all()
	{
		Chain chain = first;
		do
		{
			if (chain.bits != ALL_TRUE)
				return false;
		}
		while ((chain = chain.next) != null);
		return true;
	}
	
	public boolean last()
	{
		return last.last();
	}
	
	public void append(boolean i)
	{
		last.append(i);
		size ++;
	}
	
	public boolean swap()
	{
		return last.swap();
	}
	
	public boolean set(boolean i)
	{
		return last.set(i);
	}
	
	public boolean removeLast()
	{
		if (last.removeLast())
		{
			size --;
			return true;
		}
		return false;
	}
}
