//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat;

import objot.util.Class2;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import chat.model.Id;
import chat.model.IdAuto;
import chat.model.IdAutoBean;
import chat.model.User;
import chat.service.Data;


public class ModelsCreate
	extends Models
{
	/** @param args whether to execute, whether drop, false by default */
	public static void main(String... args) throws Exception
	{
		new ModelsCreate(false).create(args.length > 0 && Boolean.valueOf(args[0]),
			args.length > 1 && Boolean.valueOf(args[1]) ? 1 : 0);
	}

	Data data;

	/** @param test whether use the testing database */
	public ModelsCreate(boolean test) throws Exception
	{
		init(test);
	}

	public static final Method CREATE = Class2.method1(ModelsCreate.class, "create");

	/** @param drop 0 create only, >0 drop and then create, <0 drop only */
	public void create(boolean execute, int drop) throws Exception
	{
		try
		{
			LOG.info("\n================ "
				+ (drop > 0 ? "drop and create" : drop == 0 ? "create" : "drop")
				+ " ================\n");
			start(execute);
			String[] cs = drop >= 0 ? conf.generateSchemaCreationScript(dialect) : null;
			if (print)
				System.out.println();
			if (drop != 0)
			{
				DatabaseMetaData m = conn.getMetaData();
				ResultSet t = m.getTables(null, null, "%", new String[] { "TABLE" });
				for (String name; t.next() && (name = t.getString("TABLE_NAME")) != null;)
					for (ResultSet k = m.getImportedKeys(null, null, name); k.next();)
						sql("alter table " + name + " drop constraint "
							+ k.getString("FK_NAME"), false);
				for (t.beforeFirst(); t.next();)
					sql("drop table " + t.getString("TABLE_NAME"), false);
			}
			if (drop >= 0)
			{
				for (String s: cs)
					sql(s, true);
				if (execute)
				{
					if (print)
						System.out.println();
					data = new Data();
					data.hib = hib;
					init();
				}
			}
			hib.getTransaction().commit();
			Thread.sleep(200);
			if ( !execute)
				LOG.warn("\n======== no SQL statement executed on database ========\n");
			LOG.info("\n================ end ================\n");
		}
		finally
		{
			close();
		}
	}

	/** @return persisted object with specified id, may be detached */
	<T extends Id<T>>T save(T o, int id) throws Exception
	{
		if (o instanceof IdAuto || o instanceof IdAutoBean)
		{
			data.save(o.id(0));
			data.flush();
			if (o.id() == id)
				data.evict(o);
			else
			{
				// before evict, or HibernateException
				String q = "update " + data.getEntityName(o) + " set id=" + id
					+ " where id=?";
				data.evict(o);
				// after evict, or HibernateException
				if (data.query(q).setInteger(0, o.id()).executeUpdate() <= 0)
					throw new Exception("failed persist " + o + " with id = " + id);
				o.id(id);
			}
		}
		else
		{
			data.persist(o.id(id));
			data.flush();
			data.evict(o);
		}
		return o;
	}

	void init() throws Exception
	{
		User foo = new User();
		foo.name = "admin";
		foo.password = "admin";
		save(foo, 11);
	}
}
