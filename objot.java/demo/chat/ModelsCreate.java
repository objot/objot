//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

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
		new ModelsCreate(args.length > 0 && Boolean.valueOf(args[0]), //
			args.length > 1 && Boolean.valueOf(args[1]) ? 1 : 0, false);
	}

	Data data;

	/**
	 * @param drop 0 create only, >0 drop and then create, <0 drop only
	 * @param test whether use the testing database
	 */
	public ModelsCreate(boolean execute, int drop, boolean test) throws Exception
	{
		try
		{
			start(execute, test);
			String[] cs = drop >= 0 ? conf.generateSchemaCreationScript(dialect) : null;
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
					System.out.println();
					data = new Data();
					data.hib = hib;
					init();
				}
			}
			hib.getTransaction().commit();
			Thread.sleep(200);
			if (! execute)
				LOG.warn("\n======== no SQL statement executed on database ========\n");
			LOG.info("\n================ end ================\n");
		}
		finally
		{
			close();
		}
	}

	/** @return persisted object with specified id, may be detached */
	<T extends Id<T>>T persist(T o, int id) throws Exception
	{
		if (o instanceof IdAuto || o instanceof IdAutoBean)
		{
			data.persist(o);
			String q = "update " + data.getEntityName(o) + " set id=" + id + " where id=?";
			// no evict or update query would cause HibernateException
			data.evict(o);
			if (data.query(q).setInteger(0, o.id()).executeUpdate() <= 0)
				throw new Exception("failed persist " + o + " with id = " + id);
			o.id(id);
		}
		else
			data.persist(o.id(id));
		return o;
	}

	void init() throws Exception
	{
		User foo = new User();
		foo.name = "admin";
		foo.password = "admin";
		persist(foo, 11);
	}
}
