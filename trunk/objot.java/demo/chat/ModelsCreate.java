//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import org.hibernate.pretty.DDLFormatter;

import chat.model.Id;
import chat.model.IdAuto;
import chat.model.IdAutoBean;
import chat.model.User;
import chat.service.Data;


public class ModelsCreate
	extends Models
{
	/** @param args whether to execute, whether generate drop, false by default */
	public static void main(String... args) throws Exception
	{
		new ModelsCreate(args.length < 1 ? false : Boolean.valueOf(args[0]), //
			args.length < 2 ? false : Boolean.valueOf(args[1]));
	}

	Data data;

	public ModelsCreate(boolean execute, boolean drop) throws Exception
	{
		try
		{
			start();

			String[] cs = conf.generateSchemaCreationScript(dialect);
			System.out.println();
			if (drop)
			{
				DatabaseMetaData meta = conn.getMetaData();
				ResultSet t = meta.getTables(null, null, "%", new String[] { "TABLE" });
				while (t.next())
				{
					String name = t.getString("TABLE_NAME");
					for (ResultSet k = meta.getImportedKeys(null, null, name); k.next();)
					{
						String s = "alter table " + name + " drop constraint "
							+ k.getString("FK_NAME");
						System.out.println(s);
						if (execute)
							stat.executeUpdate(s);
					}
				}
				t.beforeFirst();
				while (t.next())
				{
					String s = "drop table " + t.getString("TABLE_NAME");
					System.out.println(s);
					if (execute)
						stat.executeUpdate(s);
				}
			}
			for (String s: cs)
			{
				System.out.println(new DDLFormatter(s).format());
				if (execute)
					stat.executeUpdate(s);
			}

			if (execute)
			{
				System.out.println();
				data = new Data();
				data.hib = hib;
				init();
			}

			hib.getTransaction().commit();
			Thread.sleep(200);
			System.err.println("\n\n---------------- end ----------------");
		}
		finally
		{
			close();
		}
	}

	/** @return object with specified id, may be detached */
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
		foo.name = "foo";
		foo.password = "foo";
		persist(foo, 11);
	}
}
