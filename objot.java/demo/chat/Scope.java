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
import java.util.HashMap;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ScopeAnnotation;


public class Scope
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

	static final ThreadLocal<chat.service.Session> session //
	= new ThreadLocal<chat.service.Session>();
	static final ThreadLocal<HashMap<Key<?>, Object>> request //
	= new ThreadLocal<HashMap<Key<?>, Object>>();

	public static chat.service.Session session(chat.service.Session s)
	{
		if (s == null)
			s = new chat.service.Session();
		session.set(s);
		return s;
	}

	public static void request()
	{
		request.set(new HashMap<Key<?>, Object>(32));
	}

	static final com.google.inject.Scope SESSION = new com.google.inject.Scope()
	{
		public <T>Provider<T> scope(final Key<T> key, final Provider<T> unscoped)
		{
			return new Provider<T>()
			{
				@SuppressWarnings("unchecked")
				public T get()
				{
					return (T)session.get();
				}

				@Override
				public String toString()
				{
					return unscoped.toString().concat("[service session]");
				}
			};
		}
	};
	static final com.google.inject.Scope REQUEST = new com.google.inject.Scope()
	{
		public <T>Provider<T> scope(final Key<T> key, final Provider<T> unscoped)
		{
			return new Provider<T>()
			{
				@SuppressWarnings("unchecked")
				public T get()
				{
					HashMap<Key<?>, Object> s = request.get();
					T t = (T)s.get(key);
					if (t == null)
						s.put(key, t = unscoped.get());
					return t;
				}

				@Override
				public String toString()
				{
					return unscoped.toString().concat("[service request]");
				}
			};
		}
	};
}
