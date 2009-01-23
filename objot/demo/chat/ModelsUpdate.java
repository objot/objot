//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat;

import org.hibernate.tool.hbm2ddl.DatabaseMetadata;


public class ModelsUpdate
	extends Models
{

	/** @param args whether to execute, false by default */
	public static void main(String... args) throws Exception
	{
		new ModelsUpdate(args.length > 0 && Boolean.valueOf(args[0]));
	}

	public ModelsUpdate(boolean execute) throws Exception
	{
		try
		{
			init(false);
			LOG.info("\n================ update ================\n");
			start(execute);
			String[] ss = conf.generateSchemaUpdateScript(dialect, //
				new DatabaseMetadata(conn, dialect));
			System.out.println();
			for (String s: ss)
				sql(s, true);
			hib.getTransaction().commit();
			Thread.sleep(200);
			if ( !execute)
				LOG.warn("\n======== no SQL statement executed on database ========\n");
			LOG.info("\n================ end ================"
				+ "\nSomething may be ignored such as unique indices, column default values"
				+ "\nCheck them manually\n");
		}
		finally
		{
			close();
		}
	}
}
