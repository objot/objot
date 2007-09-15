package objot.bytecode;

import java.lang.reflect.Modifier;


public enum Visible
{
	/** public */
	PUBLIC(Modifier.PUBLIC),
	/** protected */
	PROTECTED(Modifier.PROTECTED),
	/** package-default */
	PACKAGE(1 << 16),
	/** private */
	PRIVATE(Modifier.PRIVATE),
	//
	/** public or protected or package-default */
	NOTPRIVATE(PUBLIC.value | PROTECTED.value | PACKAGE.value),
	/** public or protected */
	MORE(PUBLIC.value | PROTECTED.value),
	/** package or private */
	LESS(PACKAGE.value | PRIVATE.value),
	/** protected or package-default or private */
	NOTPUBLIC(PROTECTED.value | PACKAGE.value | PRIVATE.value),
	//
	/** all */
	ALL(PUBLIC.value | PROTECTED.value | PACKAGE.value | PRIVATE.value);

	public final int value;

	private Visible(int v)
	{
		value = v;
	}

	/** No multi-visible. */
	public static Visible get(int modifier)
	{
		if ((modifier & PUBLIC.value) != 0)
			return PUBLIC;
		if ((modifier & PROTECTED.value) != 0)
			return PROTECTED;
		if ((modifier & PRIVATE.value) != 0)
			return PRIVATE;
		return PACKAGE;
	}

	public boolean isSingle()
	{
		switch (this)
		{
		case PUBLIC:
		case PROTECTED:
		case PACKAGE:
		case PRIVATE:
			return true;
		default:
			return false;
		}
	}

	public static boolean isSingle(int visible)
	{
		return visible == PUBLIC.value || visible == PROTECTED.value
			|| visible == PACKAGE.value || visible == PRIVATE.value;
	}

	public boolean isMatching(Visible visible)
	{
		return visible != null && (value & visible.value) != 0;
	}

	/** @param modifier Defined in {@link java.lang.reflect.Modifier}. */
	public boolean isMatching(int modifier)
	{
		return isMatching(value, modifier);
	}

	/** @param modifier Defined in {@link java.lang.reflect.Modifier}. */
	public static boolean isMatching(int visible, int modifier)
	{
		modifier &= PUBLIC.value | PROTECTED.value | PRIVATE.value;
		return (visible & (modifier != 0 ? modifier : PACKAGE.value)) != 0;
	}
}
