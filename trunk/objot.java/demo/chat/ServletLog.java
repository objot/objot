/*
 * Copyright 2001-2004 The Apache Software Foundation. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the
 * License.
 */

package chat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ServletLog
	implements Log
{
	enum Level
	{
		all, trace, debug, info, warn, error, fatal, off
	}

	public static final String PROP_LEVEL = "servlet.log.level";
	public static final String PROP_PREFIX = "servlet.log.prefix";
	public static final String PROP_POSTFIX = "servlet.log.postfix";

	static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
		"yyyy-MM-dd HH:mm:ss.SSS", Locale.UK);
	static ServletContext logger;

	LogFactory factory;
	String propName;
	/** prepend to the log message before logging */
	String prefix;
	/** append to the log message before logging */
	String postfix;
	int level;

	public ServletLog(String name_)
	{
		propName = ".".concat(name_);
	}

	public void setLogFactory(LogFactory f)
	{
		factory = f;

		setLevel(Level.valueOf(getProperty(PROP_LEVEL, Level.info.name())).ordinal());
		prefix = getProperty(PROP_PREFIX, " -- ");
		postfix = getProperty(PROP_POSTFIX, " -- ");
		propName = null;
	}

	String getProperty(String propBegin, String byDefault)
	{
		String p = (String)factory.getAttribute(propBegin + propName);
		for (int i = String.valueOf(propName).lastIndexOf("."); p == null && i >= 0;)
		{
			String n = propName.substring(0, i);
			p = (String)factory.getAttribute(propBegin + n);
			i = String.valueOf(n).lastIndexOf(".");
		}
		return p == null ? byDefault : p;
	}

	public void setLevel(int level_)
	{
		level = level_;
	}

	public int getLevel()
	{
		return level;
	}

	protected void log(Level l, Object message, Throwable t)
	{
		if (t == null && message instanceof Throwable)
		{
			t = (Throwable)message;
			message = "";
		}

		StackTraceElement[] stack = new Throwable().getStackTrace();
		// Caller is the third element
		String clazz = "unknown";
		String method = "unknown";
		if (stack != null && stack.length > 2)
		{
			StackTraceElement s = stack[2];
			clazz = s.getClassName();
			method = s.getMethodName();
		}
		if (logger != null)
			logger.log(l.name() + prefix + String.valueOf(message) + postfix + clazz + '-'
				+ method, t);
		else
		{
			StringBuilder s = new StringBuilder();
			s.append(l.name()).append(' ').append(DATE_FORMAT.format(new Date()));
			s.append(prefix).append(message).append(postfix);
			s.append(clazz).append('-').append(method);
			if (t != null)
			{
				StringWriter ts = new StringWriter(1024);
				PrintWriter tp = new PrintWriter(ts);
				t.printStackTrace(tp);
				tp.close();
				s.append("\n  ").append(t.toString()).append('\n').append(ts.toString());
			}
			System.err.println(s);
		}
	}

	public final void trace(Object message)
	{
		if (Level.trace.ordinal() >= level)
			log(Level.trace, message, null);
	}

	public final void trace(Object message, Throwable t)
	{
		if (Level.trace.ordinal() >= level)
			log(Level.trace, message, t);
	}

	public final void debug(Object message)
	{
		if (Level.debug.ordinal() >= level)
			log(Level.debug, message, null);
	}

	public final void debug(Object message, Throwable t)
	{
		if (Level.debug.ordinal() >= level)
			log(Level.debug, message, t);
	}

	public final void info(Object message)
	{
		if (Level.info.ordinal() >= level)
			log(Level.info, message, null);
	}

	public final void info(Object message, Throwable t)
	{
		if (Level.info.ordinal() >= level)
			log(Level.info, message, t);
	}

	public final void warn(Object message)
	{
		if (Level.warn.ordinal() >= level)
			log(Level.warn, message, null);
	}

	public final void warn(Object message, Throwable t)
	{
		if (Level.warn.ordinal() >= level)
			log(Level.warn, message, t);
	}

	public final void error(Object message)
	{
		if (Level.error.ordinal() >= level)
			log(Level.error, message, null);
	}

	public final void error(Object message, Throwable t)
	{
		if (Level.error.ordinal() >= level)
			log(Level.error, message, t);
	}

	public final void fatal(Object message)
	{
		if (Level.fatal.ordinal() >= level)
			log(Level.fatal, message, null);
	}

	public final void fatal(Object message, Throwable t)
	{
		if (Level.fatal.ordinal() >= level)
			log(Level.fatal, message, t);
	}

	public final boolean isTraceEnabled()
	{
		return Level.trace.ordinal() >= level;
	}

	public final boolean isDebugEnabled()
	{
		return Level.debug.ordinal() >= level;
	}

	public final boolean isInfoEnabled()
	{
		return Level.info.ordinal() >= level;
	}

	public final boolean isWarnEnabled()
	{
		return Level.warn.ordinal() >= level;
	}

	public final boolean isErrorEnabled()
	{
		return Level.error.ordinal() >= level;
	}

	public final boolean isFatalEnabled()
	{
		return Level.fatal.ordinal() >= level;
	}
}
