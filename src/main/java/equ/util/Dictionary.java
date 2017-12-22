/*
 * copyright© 2017 ueyudiud
 */
package equ.util;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author ueyudiud
 */
public class Dictionary<E> extends AbstractMap<String, E>
{
	class DictionaryEntry implements Entry<String, E>
	{
		DictionaryEntry parent;
		
		char current;
		E value;
		
		DictionaryEntry next;
		
		int size;
		DictionaryEntry[] entries = new Dictionary.DictionaryEntry[4];
		
		DictionaryEntry find(char value)
		{
			DictionaryEntry entry = this.entries[value % this.entries.length];
			while (entry != null && entry.current != value)
				entry = entry.next;
			return entry;
		}
		
		@Override
		public String getKey()
		{
			return this.parent == null ? Strings.EMPTY : this.parent.getKey() + this.current;
		}
		
		@Override
		public E getValue()
		{
			return this.value;
		}
		
		@Override
		public E setValue(E value)
		{
			if (value == null)
			{
				Dictionary.this.set.remove(this);
			}
			E old = this.value;
			this.value = value;
			return old;
		}
		
		E remove(char[] array, int i, int end)
		{
			char value = array[i];
			int idx = value % this.entries.length;
			DictionaryEntry entry = this.entries[idx];
			DictionaryEntry last = null;
			while (entry != null && entry.current != value)
			{
				last = entry;
				entry = entry.next;
			}
			if (entry != null)
			{
				--this.size;
				if (end == i + 1)
				{
					Dictionary.this.set.remove(entry);
					if (entry.size == 0)
					{
						if (last == null)
							this.entries[idx] = null;
						else
							last.next = entry.next;
						return entry.value;
					}
					else
						return entry.setValue(null);
				}
				else
				{
					E result = entry.remove(array, i + 1, end);
					if (entry.size == 0 && entry.value == null)
					{
						if (last == null)
							this.entries[idx] = null;
						else
							last.next = entry.next;
					}
					return result;
				}
			}
			else
			{
				return null;
			}
		}
		
		E remove(char value)
		{
			DictionaryEntry entry = this.entries[value % this.entries.length];
			DictionaryEntry last = entry;
			while (entry != null && entry.current != value)
			{
				last = entry;
				entry = entry.next;
			}
			if (entry != null)
			{
				last.next = entry.next;
				--this.size;
				return entry.value;
			}
			else
			{
				return null;
			}
		}
		
		DictionaryEntry childNonnull(char chr)
		{
			DictionaryEntry entry = find(chr);
			if (entry == null)
			{
				if (this.size < this.entries.length * Dictionary.this.loadFactor)
				{
					int i = chr % this.entries.length;
					entry = new DictionaryEntry();
					entry.current = chr;
					entry.parent = this;
					entry.next = this.entries[i];
					this.entries[i] = entry;
					++this.size;
				}
				else
				{
					DictionaryEntry[] entries1 =
							new Dictionary.DictionaryEntry[this.entries.length + (this.entries.length >> 1)];
					for (DictionaryEntry entry2 : this.entries)
					{
						while (entry2 != null)
						{
							DictionaryEntry entry3 = entry2.next;
							
							int i = entry2.current % entries1.length;
							entry2.next = entries1[i];
							entries1[i] = entry2;
							
							entry2 = entry3;
						}
					}
					entry = new DictionaryEntry();
					entry.current = chr;
					entry.parent = this;
					
					int i = entry.current % entries1.length;
					entry.next = entries1[i];
					entries1[i] = entry;
					
					++this.size;
				}
			}
			return entry;
		}
		
		@Override
		public String toString()
		{
			return this.parent == null ? "root" : "... " + this.current + "=" + this.value;
		}
	}
	
	private int size;
	private float loadFactor;
	private final DictionaryEntry root = new DictionaryEntry();
	private Set<Entry<String, E>> set = new HashSet<>();
	
	public Dictionary()
	{
		this(0.75F);
	}
	
	public Dictionary(float loadFactor)
	{
		this.loadFactor = loadFactor;
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		return get(key) != null;
	}
	
	public boolean containsPrefix(Object key)
	{
		return key instanceof String && containsPrefix(((String) key).toCharArray());
	}
	
	public boolean containsPrefix(char[] key, int from, int to)
	{
		DictionaryEntry entry = this.root;
		for (int i = from; i < to; ++i)
		{
			if ((entry = entry.find(key[i])) == null)
				return false;
		}
		return true;
	}
	
	public E get(char[] key, E def)
	{
		return get(key, def, 0, key.length);
	}
	
	public E get(char[] key, E def, int from, int to)
	{
		DictionaryEntry entry = this.root;
		for (int i = from; i < to; ++i)
		{
			if ((entry = entry.find(key[i])) == null)
				return def;
		}
		return entry.value;
	}
	
	/**
	 * 返回Optional如果查找的字符串存在或存在这一前缀，否则返回null
	 * @param key
	 * @param from
	 * @param to
	 * @return
	 */
	public Optional<E> getOrContainPrefix(char[] key, int from, int to)
	{
		DictionaryEntry entry = this.root;
		for (int i = from; i < to; ++i)
		{
			if ((entry = entry.find(key[i])) == null)
				return null;
		}
		return Optional.ofNullable(entry.value);
	}
	
	@Override
	public E get(Object key)
	{
		return key instanceof String ? get(((String) key).toCharArray(), null) : null;
	}
	
	@Override
	public E getOrDefault(Object key, E defaultValue)
	{
		return key instanceof String ? get(((String) key).toCharArray(), defaultValue) : null;
	}
	
	@Override
	public E put(String key, E value)
	{
		DictionaryEntry entry = this.root;
		for (int i = 0; i < key.length(); ++i)
		{
			entry = entry.childNonnull(key.charAt(i));
		}
		this.set.add(entry);
		return entry.setValue(value);
	}
	
	public E put(char[] key, E value, int from, int to)
	{
		if (from < 0 || to >= key.length)
			throw new StringIndexOutOfBoundsException();
		DictionaryEntry entry = this.root;
		for (int i = from; i < to; ++i)
		{
			entry = entry.childNonnull(key[i]);
		}
		this.set.add(entry);
		return entry.setValue(value);
	}
	
	@Override
	public E remove(Object key)
	{
		return key instanceof String ? remove(((String) key).toCharArray(), 0, ((String) key).length()) : null;
	}
	
	public E remove(char[] key)
	{
		return remove(key, 0, key.length);
	}
	
	public E remove(char[] key, int from, int to)
	{
		DictionaryEntry entry = this.root;
		return entry.remove(key, from, to);
	}
	
	@Override
	public void clear()
	{
		this.root.size = 0;
		Arrays.fill(this.root.entries, null);
	}
	
	@Override
	public Set<Entry<String, E>> entrySet()
	{
		return this.set;
	}
}
