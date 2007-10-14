//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package chat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	public static final String PROP_FORMAT = "servlet.log.format";
	public static final String PROP_CONSOLE_FORMAT = "servlet.log.console.format";
	public static final String FORMAT_DATE = "$date";
	public static final String FORMAT_LEVEL = "$level";
	public static final String FORMAT_LOG = "$log";
	public static final String FORMAT_CALL = "$call";
	public static final String FORMAT = "$level -- $log\t---- $call";
	public static final String FORMAT_CONSOLE = "$date $level -- $log\t---- $call";
	public static final SimpleDateFormat DATE = new SimpleDateFormat(
		"yyyy-MM-dd HH:mm:ss.SSS");

	static ServletContext logger;

	LogFactory factory;
	String propName;
	String format;
	int level;

	public ServletLog(String name_)
	{
		propName = ".".concat(name_);
	}

	public void setLogFactory(LogFactory f)
	{
		factory = f;

		setLevel(Level.valueOf(getProperty(PROP_LEVEL, Level.info.name())).ordinal());
		format = logger != null ? getProperty(PROP_FORMAT, FORMAT) //
			: getProperty(PROP_CONSOLE_FORMAT, getProperty(PROP_FORMAT, FORMAT_CONSOLE));
		propName = null;
	}

	String getProperty(String propBegin, String byDefault)
	{
		String p = (String)factory.getAttribute(propBegin + propName);
		String n = propName;
		for (int i; p == null && (i = Math.max(n.lastIndexOf("."), n.lastIndexOf("$"))) >= 0;)
		{
			n = propName.substring(0, i);
			p = (String)factory.getAttribute(propBegin + n);
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

		StringBuilder s = new StringBuilder(format);
		int i = s.indexOf(FORMAT_DATE);
		if (i >= 0)
			s.replace(i, i + FORMAT_DATE.length(), DATE.format(new Date()));
		i = s.indexOf(FORMAT_LEVEL);
		if (i >= 0)
			s.replace(i, i + FORMAT_LEVEL.length(), l.name());
		i = s.indexOf(FORMAT_LOG);
		if (i >= 0)
			s.replace(i, i + FORMAT_LOG.length(), String.valueOf(message));
		i = s.indexOf(FORMAT_CALL);
		if (i >= 0)
		{
			StackTraceElement[] stack = new Throwable().getStackTrace();
			// Caller is the third element
			String clazz = "unknown";
			String method = "unknown";
			if (stack != null && stack.length > 2)
			{
				clazz = stack[2].getClassName();
				method = stack[2].getMethodName();
			}
			s.replace(i, i + FORMAT_CALL.length(), clazz + '-' + method);
		}
		if (logger != null)
			logger.log(s.toString(), t);
		else
		{
			if (t != null)
			{
				StringWriter ts = new StringWriter(1024);
				PrintWriter tp = new PrintWriter(ts);
				t.printStackTrace(tp);
				tp.close();
				s.append("\n ").append(ts.toString());
			}
			(l.ordinal() <= Level.debug.ordinal() ? System.out : System.err).println(s);
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
