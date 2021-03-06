//
// Copyright 2007-2015 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat.service;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import objot.container.Inject;
import objot.util.Err;
import objot.util.ErrThrow;
import objot.util.Errs;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;


/** all services must in containers */
public abstract class Do
{
	private static final Map<Class<?>, ClassValidator<?>> VS //
	= new ConcurrentHashMap<Class<?>, ClassValidator<?>>(128, 0.8f, 32);

	@Inject
	public Session sess;
	@Inject
	public Data data;

	@Inject
	public DoSign doSign;
	@Inject
	public DoUser doUser;
	@Inject
	public DoChat doChat;

	/** null for default */
	public String respType;

	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	public @interface Service
	{
	}

	public static ErrThrow err(Err e)
	{
		return new ErrThrow(e);
	}

	public static ErrThrow err(String hint)
	{
		return new ErrThrow(null, hint);
	}

	public static ErrThrow err(Throwable e)
	{
		return new ErrThrow(null, e);
	}

	public static ErrThrow err(String hint, Throwable e)
	{
		return new ErrThrow(null, hint, e);
	}

	@SuppressWarnings("unchecked")
	public static <T>T validate(T o) throws Exception
	{
		ClassValidator<T> v = (ClassValidator<T>)VS.get(o.getClass());
		if (v == null)
			VS.put(o.getClass(), v = new ClassValidator(o.getClass()));
		InvalidValue[] s = v.getInvalidValues(o);
		if (s != null && s.length > 0)
			throw err(new Errs(s));
		return o;
	}
}
