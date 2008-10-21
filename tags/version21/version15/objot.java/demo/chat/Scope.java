//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.google.inject.ScopeAnnotation;


@Target(ElementType.PACKAGE /* just for eclipse auto completion */)
public @interface Scope
{
	/** one instance per service session */
	@ScopeAnnotation
	@Inherited
	@Target( { ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface Session
	{
	}

	/** one instance per service request */
	@ScopeAnnotation
	@Inherited
	@Target( { ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface Request
	{
	}
}
