//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.impl.SessionImpl;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import chat.model.Id;
import chat.model.IdAuto;
import chat.model.IdAutoBean;
import chat.model.User;
import chat.service.Data;


public class ModelsCreate
{
	/** @param args whether to execute, whether generate drop, false by default */
	public static void main(String... args) throws Exception
	{
		boolean execute = args.length < 1 ? false : Boolean.valueOf(args[0]);
		AnnotationConfiguration config = Models.init();
		config.setProperty("hibernate.format_sql", "true");

		SchemaExport sch = new SchemaExport(config);
		String name = File.createTempFile(ModelsCreate.class.getName(), "")
			.getCanonicalPath();
		sch.setOutputFile(name);
		sch.execute(false, execute, false, //
			args.length < 2 ? true : ! Boolean.valueOf(args[1]));

		File f = new File(name);
		f.deleteOnExit();
		FileReader read = new FileReader(f);
		char[] s = new char[(int)f.length()];
		for (int from = 0, done; from < s.length; from += done)
			if ((done = read.read(s, from, s.length - from)) < 0)
				throw new EOFException();
		Thread.sleep(300);
		System.out.println(s);
		for (Object e: sch.getExceptions())
			((Exception)e).printStackTrace();

		if (execute && sch.getExceptions().size() == 0)
		{
			Thread.sleep(300);
			config.setProperty("hibernate.hbm2ddl.auto", "false");
			config.setProperty("hibernate.cache.use_second_level_cache", "false");
			config.setProperty("hibernate.cache.use_query_cache", "false");
			config.setProperty("hibernate.format_sql", "false");
			SessionImpl hib = (SessionImpl)config.buildSessionFactory().openSession();
			try
			{
				hib.getJDBCContext().borrowConnection().setTransactionIsolation(
					Connection.TRANSACTION_SERIALIZABLE);
				hib.beginTransaction();
				ModelsCreate m = new ModelsCreate();
				m.data = new Data();
				m.data.hib = hib;
				m.init();
				hib.getTransaction().commit();
			}
			finally
			{
				if (hib.getTransaction().isActive())
					try
					{
						hib.getTransaction().rollback();
					}
					catch (Throwable e)
					{
						e.printStackTrace();
					}
				try
				{
					hib.close();
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	Data data;

	/** @return object with specified id, may be detached */
	<T extends Id<T>>T persist(T o, int id) throws Exception
	{
		if (o instanceof IdAuto || o instanceof IdAutoBean)
		{
			data.persist(o);
			String q = "update " + data.getEntityName(o) + " set id=" + id + " where id=?";
			data.evict(o);
			if (data.query(q).setInteger(0, o.id()).executeUpdate() <= 0)
				throw new Exception("failed persist " + o + " with id = " + id);
			o.id(id);
		}
		else
			data.persist(o.id(id));
		return o;
	}

	public void init() throws Exception
	{
		User foo = new User();
		foo.name = "foo";
		foo.password = "foo";
		persist(foo, 11);
	}
}
