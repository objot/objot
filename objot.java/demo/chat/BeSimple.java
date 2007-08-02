//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SingleTableSubclass;
import org.hibernate.validator.PropertyConstraint;
import org.hibernate.validator.Validator;
import org.hibernate.validator.ValidatorClass;


@ValidatorClass(BeSimple.V.class)
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Documented
public @interface BeSimple
{
	int min() default 1;

	int max() default Integer.MAX_VALUE;

	String message() default "too short or too long or illegal format";

	public static class V
		implements Validator<BeSimple>, PropertyConstraint
	{
		int min;
		int max;

		public void initialize(BeSimple _)
		{
			min = _.min();
			max = _.max();
		}

		public boolean isValid(Object value)
		{
			String v;
			if (value instanceof String)
				v = (String)value;
			else if (value instanceof Clob)
				try
				{
					v = ((Clob)value).getSubString(1, (int)Math.min(((Clob)value).length(),
						Integer.MAX_VALUE));
				}
				catch (SQLException e)
				{
					throw new RuntimeException(e);
				}
			else
				return false; // include null
			if (v.length() < min || v.length() > max)
				return false;
			if (min > 0)
			{
				char c0 = v.charAt(0), c9 = v.charAt(v.length() - 1);
				if (c0 == ' ' || c0 == '\u00a0' || c9 == ' ' || c9 == '\u00a0')
					return false;
				if (v.indexOf('\n') >= 0 || v.indexOf('\r') >= 0)
					return false;
			}
			return true;
		}

		@SuppressWarnings("unchecked")
		public void apply(Property p)
		{
			if (! (p.getPersistentClass() instanceof SingleTableSubclass)
				&& ! (p.getPersistentClass() instanceof Collection))
				for (Iterator<Column> cs = p.getColumnIterator(); cs.hasNext();)
					cs.next().setNullable(false);
			if (max < Integer.MAX_VALUE)
				for (Iterator<Column> cs = p.getColumnIterator(); cs.hasNext();)
					cs.next().setLength(max);
		}
	}
}
