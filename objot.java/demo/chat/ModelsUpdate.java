//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import org.hibernate.pretty.DDLFormatter;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;


public class ModelsUpdate
	extends Models
{

	/** @param args whether to execute, false by default */
	public static void main(String... args) throws Exception
	{
		new ModelsUpdate(args.length < 1 ? false : Boolean.valueOf(args[0]));
	}

	public ModelsUpdate(boolean execute) throws Exception
	{
		try
		{
			start();

			String[] ss = conf.generateSchemaUpdateScript(dialect, //
				new DatabaseMetadata(conn, dialect));
			System.out.println();
			for (String s: ss)
			{
				System.out.println(new DDLFormatter(s).format());
				if (execute)
					stat.executeUpdate(s);
			}

			hib.getTransaction().commit();
			Thread.sleep(200);
			System.err.println("\n\n---------------- end ----------------"
				+ "\nSomething may be ignored such as unique indices, column default values"
				+ "\nCheck them manually");
		}
		finally
		{
			close();
		}
	}
}
