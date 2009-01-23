//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


/**
 * replace {@link java.util.logging.SimpleFormatter}
 * <p>
 * for Apache Tomcat:<br>
 * add {@link SimpleFormatter}.class file to bin/bootstrap.jar, and set
 * java.util.logging.ConsoleHandler.formatter = SimpleFormatter in conf/logging.propreties
 */

public class SimpleFormatter
	extends Formatter
{
	@Override
	public String format(LogRecord log)
	{
		String m = log.getMessage() != null ? log.getMessage() : "";
		boolean sys = m.length() == 0
			|| !"log".equals(log.getSourceMethodName())
			|| !"org.apache.catalina.core.ApplicationContext".equals(log.getSourceClassName());
		StringBuilder s = new StringBuilder();
		if (sys)
			s.append(log.getLevel().getName()).append(' ');
		format(s, new Date(log.getMillis())).append("\t");
		s.append(m.length() > 0 || log.getThrown() == null ? m : log.getThrown());
		if (sys && log.getSourceClassName() != null)
			s.append("\t---- ").append(log.getSourceClassName()).append('-').append(
				log.getSourceMethodName());
		s.append("\n");
		if (log.getThrown() != null)
		{
			CharArrayWriter w = new CharArrayWriter();
			log.getThrown().printStackTrace(new PrintWriter(w));
			s.append(w.toCharArray()).append("\n");
		}
		return s.toString();
	}

	StringBuilder format(StringBuilder x, Date date)
	{
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
		c.setTime(date);
		int y = c.get(Calendar.YEAR), M = c.get(Calendar.MONTH) + 1, d = c.get(Calendar.DATE);
		int h = c.get(Calendar.HOUR), m = c.get(Calendar.MINUTE), s = c.get(Calendar.SECOND);
		int ms = c.get(Calendar.MILLISECOND);
		x.append(y).append('-').append(M / 10).append(M % 10).append('-');
		x.append(d / 10).append(d % 10).append(' ');
		x.append(h / 10).append(h % 10).append(':').append(m / 10).append(m % 10).append(':');
		x.append(s / 10).append(s % 10).append('.');
		x.append(ms / 100).append(ms % 100 / 10).append(ms % 10);
		return x;
	}
}
