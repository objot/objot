package chat.model.common;

import org.hibernate.tool.hbm2ddl.SchemaUpdate;


public class ModelUpdate
{
	/** @param args whether to export, false by default */
	public static void main(String... args) throws Exception
	{
		SchemaUpdate sch = new SchemaUpdate(Model.init());
		sch.execute(true, args.length < 1 ? false : Boolean.valueOf(args[0]));

		for (Object e: sch.getExceptions())
			((Exception)e).printStackTrace();
	}
}
