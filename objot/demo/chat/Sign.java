//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import objot.aspect.Aspect;

import chat.model.ErrUnsigned;
import chat.service.Do;


public @interface Sign
{
	/** service in signed or unsigned session, and in signed session if no this annotation */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Any
	{
	}

	public static final class As
		extends Aspect
	{
		@Override
		protected void aspect() throws Throwable
		{
			Do s = Target.thiz();
			if (s.sess.me <= 0)
				throw Do.err(new ErrUnsigned("not signed in"));
			Target.invoke();
		}
	}
}
