//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import objot.codec.Codec;
import objot.container.Container;
import objot.util.Array2;
import objot.util.Class2;
import objot.util.Err;
import objot.util.ErrThrow;
import objot.util.Mod2;
import objot.util.String2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class ServiceHandler
{
	protected Log log;
	protected ConcurrentHashMap<String, ServiceInfo> infos //
	= new ConcurrentHashMap<String, ServiceInfo>(128, 0.8f, 32);
	protected char nameDelimiter = '-';
	/** must be set in implementation */
	protected Codec codec;

	/**
	 * @param context depending on implementation, may be null
	 * @return this one, or another which should be used to instead of this one
	 */
	public ServiceHandler init(Container context) throws Exception
	{
		log = LogFactory.getLog(toString());
		return this;
	}

	/**
	 * @return the service info
	 * @throws RequestException if not found
	 */
	public ServiceInfo getInfo(String name) throws RequestException
	{
		ServiceInfo inf = infos.get(name);
		if (inf != null)
			return inf;
		try
		{
			int x = String2.index(name, nameDelimiter, 0);
			inf = getInfo(name, name.substring(0, x), String2.sub(name, x + 1));
		}
		catch (RequestException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new RequestException("service not found : ".concat(name), e);
		}
		if (inf == null)
			throw new RequestException("service not found : ".concat(name));
		infos.put(name, inf);
		return inf;
	}

	/**
	 * must be thread safe, service not found if null or exception
	 * 
	 * @return the service info may be cached
	 */
	protected ServiceInfo getInfo(String name, String cla, String method) throws Exception
	{
		Class<?> c = Class.forName(cla);
		if (Mod2.match(c, Mod2.PUBLIC))
			for (Method m: c.getMethods())
				if (m.getName().equals(method))
					return new ServiceInfo(codec, name, m);
		return null;
	}

	/**
	 * @param context depending on implementation, may be null
	 * @return {@link CharSequence} by default, or any object depending on implementation
	 */
	public Object handle(Container context, ServiceInfo inf, char[] req, int begin, int end1,
		Object[] extraReqs) throws RequestException, Exception
	{
		try
		{
			return codec.enc(invoke(inf, null, req, begin, end1, extraReqs), inf.cla);
		}
		catch (RequestException e)
		{
			if (log.isTraceEnabled())
				log.trace(e);
			return error(e);
		}
		catch (Error e)
		{
			log.error(e);
			return error(e);
		}
		catch (Throwable e)
		{
			if (log.isDebugEnabled())
				log.debug(e);
			return error(e);
		}
	}

	/**
	 * @param obj an instance of service class
	 * @return the service result
	 */
	public Object invoke(ServiceInfo inf, Object obj, char[] req, int begin, int end1,
		Object[] extraReqs) throws RequestException, Exception
	{
		Object[] qs;
		try
		{
			if (req == null || begin >= end1)
				qs = Array2.OBJECTS0;
			else if (inf.reqClas.length == 1)
				qs = new Object[] { codec.dec(req, begin, end1, inf.reqBoxClas[0], inf.cla) };
			else
				qs = (Object[])codec.dec(req, begin, end1, Object[].class, inf.cla);
		}
		catch (Throwable e)
		{
			throw new RequestException(e);
		}
		qs = Array2.concat(qs, extraReqs);
		try
		{
			return inf.meth.invoke(obj, qs);
		}
		catch (IllegalArgumentException e)
		{
			StringBuilder s = new StringBuilder("can not apply (");
			for (int p = 0; p < qs.length; p++)
				s.append(p == 0 ? "" : ", ").append(
					qs[p] == null ? "null" : qs[p].getClass().getCanonicalName());
			s.append(") to ").append(inf.name);
			s.append(e.getMessage() != null ? " : " : "").append(
				e.getMessage() != null ? e.getMessage() : "");
			throw new RequestException(s.toString());
		}
		catch (InvocationTargetException e)
		{
			throw Class2.exception(e.getCause());
		}
		catch (Throwable e)
		{
			return Class2.exception(e);
		}
	}

	/**
	 * @return any object depending on implementation, {@link CharSequence} by default
	 * @throws Exception depending on implementation
	 */
	public Object error(Throwable e) throws Exception
	{
		return codec.enc(e instanceof ErrThrow ? ((ErrThrow)e).err : new Err(e), null);
	}
}
