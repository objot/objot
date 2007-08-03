//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import chat.model.ErrUnsigned;
import chat.service.Do;


public class Sign
{
	/** service in signed or unsigned session, and in signed session if no this annotation */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Any
	{
	}

	public static class Aspect
		implements MethodInterceptor
	{
		public Object invoke(MethodInvocation meth) throws Throwable
		{
			Do s = (Do)meth.getThis();
			if (s.sess.me == null)
				throw Do.err(new ErrUnsigned("not signed in"));
			return meth.proceed();
		}
	}
}
