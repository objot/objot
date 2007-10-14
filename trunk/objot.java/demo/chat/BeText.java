//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
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


@ValidatorClass(BeText.V.class)
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Documented
public @interface BeText
{
	int min() default 1;

	int max() default Integer.MAX_VALUE;

	boolean simple() default true;

	String message() default "too short or too long or illegal format";

	public static class V
		implements Validator<BeText>, PropertyConstraint
	{
		BeText anno;

		public void initialize(BeText a)
		{
			anno = a;
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
			if (v.length() < anno.min() || v.length() > anno.max())
				return false;
			if (anno.simple() && v.length() > 0)
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
			if ( !(p.getPersistentClass() instanceof SingleTableSubclass)
				&& !(p.getPersistentClass() instanceof Collection))
				for (Iterator<Column> cs = p.getColumnIterator(); cs.hasNext();)
					cs.next().setNullable(false);
			if (anno.max() < Integer.MAX_VALUE)
				for (Iterator<Column> cs = p.getColumnIterator(); cs.hasNext();)
					cs.next().setLength(anno.max());
		}
	}
}
