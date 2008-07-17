//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * The annotated constructors, fields and methods need dependence injection, default is
 * {@link Single} which could be overrided by {@link Factory#Factory(Class)}
 */
@Target( { ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Inject
{
	/** inject a new instance every time */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	public @interface New
	{
	}

	/** inject the exist or a new instance in this container */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	public @interface Single
	{
	}

	/** inject the instance by {@link Container#set} in this container */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	public @interface Set
	{
	}

	/** inject from parent container */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	public @interface Parent
	{
	}
}
