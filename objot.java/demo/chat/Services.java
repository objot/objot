//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import objot.servlet.Service;
import chat.service.Do;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletScopes;


public class Services
{
	public static Injector init() throws Exception
	{
		Guice.createInjector(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				bindScope(ScopeSession.class, ServletScopes.SESSION);
				bindScope(ScopeRequest.class, ServletScopes.REQUEST);
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Service.class).and( //
					Matchers.not(Matchers.annotatedWith(SignAny.class))), new AspectSign());
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Service.class).and(
					Matchers.annotatedWith(TransacSerial.class)), //
					new AspectTransac(dataFactory, false, false, true));
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Service.class).and(
					Matchers.annotatedWith(TransacRepeat.class)).and(
					Matchers.not(Matchers.annotatedWith(TransacSerial.class))), //
					new AspectTransac(dataFactory, false, true, false));
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Service.class).and(
					Matchers.annotatedWith(TransacReadonly.class)).and(
					Matchers.not(Matchers.annotatedWith(TransacCommit.class).or(
						Matchers.annotatedWith(TransacRepeat.class)).or(
						Matchers.annotatedWith(TransacSerial.class)))), //
					new AspectTransac(dataFactory, true, false, false));
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Service.class).and(
					Matchers.not(Matchers.annotatedWith(TransacAny.class).or(
						Matchers.annotatedWith(TransacReadonly.class)).or(
						Matchers.annotatedWith(TransacRepeat.class)).or(
						Matchers.annotatedWith(TransacSerial.class)))), //
					new AspectTransac(dataFactory, false, false, false));
				try
				{
					for (Class<?> c: PackageClass.getClasses(Do.class))
						bind(c);
				}
				catch (RuntimeException e)
				{
					throw e;
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		});
	}
}
