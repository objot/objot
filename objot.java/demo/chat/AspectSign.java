//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import chat.model.ErrUnsigned;
import chat.service.Do;


public class AspectSign
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
