//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.util;

import java.util.Arrays;


/** could be used in java collection framework. */
public final class Objects<T>
{
	public final T[] array;

	public Objects(T[] array_)
	{
		array = array_;
	}

	/** @see Arrays#equals(Object[],Object[]) */
	@Override
	public boolean equals(Object o)
	{
		return o != null && Arrays.equals(array, ((Objects<?>)o).array);
	}

	/** @see Arrays#hashCode(Object[]) */
	@Override
	public int hashCode()
	{
		return Arrays.hashCode(array);
	}
}
