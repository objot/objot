//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


// @Target(ElementType.PACKAGE)
public @interface Scope
{
	/** out of containers, create instance every time */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	public @interface None
	{
	}

	/** create instance in this container if not found in this container */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	public @interface Private
	{
	}

	/** create instance in this container if not found from this to top container */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	public @interface Spread
	{
	}

	/** create instance in top container if not found from this to top container */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	public @interface SpreadCreate
	{
	}
}
