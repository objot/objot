//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.util.HashMap;

import chat.service.Session;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;


public class Scopes
{
	static final ThreadLocal<Session> session //
	= new ThreadLocal<Session>();
	static final ThreadLocal<HashMap<Key<?>, Object>> request //
	= new ThreadLocal<HashMap<Key<?>, Object>>();

	public static Session session(Session s)
	{
		if (s == null)
			s = new Session();
		session.set(s);
		return s;
	}

	public static void request()
	{
		request.set(new HashMap<Key<?>, Object>(32));
	}

	static final Scope SESSION = new Scope()
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
	static final Scope REQUEST = new Scope()
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
