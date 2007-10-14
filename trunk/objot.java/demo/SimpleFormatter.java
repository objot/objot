//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
		"yyyy-MM-dd HH:mm:ss.SSS");

	@Override
	public String format(LogRecord log)
	{
		String m = log.getMessage() != null ? log.getMessage() : "";
		boolean sys = m.length() == 0
			|| !"log".equals(log.getSourceMethodName())
			|| !"org.apache.catalina.core.ApplicationContext"
				.equals(log.getSourceClassName());
		StringBuilder s = new StringBuilder();
		if (sys)
			s.append(log.getLevel().getName()).append(' ');
		s.append(DATE_FORMAT.format(new Date(log.getMillis()))).append("\t");
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
}
