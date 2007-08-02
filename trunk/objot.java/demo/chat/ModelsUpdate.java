//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import org.hibernate.tool.hbm2ddl.SchemaUpdate;


public class ModelsUpdate
{
	/** @param args whether to export, false by default */
	public static void main(String... args) throws Exception
	{
		SchemaUpdate sch = new SchemaUpdate(Models.init());
		sch.execute(true, args.length < 1 ? false : Boolean.valueOf(args[0]));

		for (Object e: sch.getExceptions())
			((Exception)e).printStackTrace();
	}
}
