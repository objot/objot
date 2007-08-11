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
		if ("log".equals(log.getSourceMethodName()) && //
			"org.apache.catalina.core.ApplicationContext".equals(log.getSourceClassName()))
			return DATE_FORMAT.format(new Date(log.getMillis())) + "\t" + log.getMessage()
				+ "\n";
		StringBuilder s = new StringBuilder();
		s.append(log.getLevel().getName()).append(' ').append(
			DATE_FORMAT.format(new Date(log.getMillis())));
		s.append("\t").append(log.getMessage());
		if (log.getSourceClassName() != null)
			s.append("\t---- ").append(log.getSourceClassName()).append('-').append(
				log.getSourceMethodName());
		return s.append("\n").toString();
	}
}
