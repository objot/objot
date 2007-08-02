//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.io.EOFException;
import java.io.File;
import java.io.FileReader;

import org.hibernate.tool.hbm2ddl.SchemaExport;


public class ModelCreate
{
	/** @param args whether to export, whether generate drop, false by default */
	public static void main(String... args) throws Exception
	{
		SchemaExport sch = new SchemaExport(Model.init());
		String name = File.createTempFile(ModelCreate.class.getName(), "").getCanonicalPath();
		sch.setOutputFile(name);
		sch.execute(false, args.length < 1 ? false : Boolean.valueOf(args[0]), //
			false, args.length < 2 ? true : ! Boolean.valueOf(args[1]));

		File f = new File(name);
		f.deleteOnExit();
		FileReader read = new FileReader(f);
		char[] s = new char[(int)f.length()];
		for (int from = 0, done; from < s.length; from += done)
			if ((done = read.read(s, from, s.length - from)) < 0)
				throw new EOFException();
		System.out.print(s);

		for (Object e: sch.getExceptions())
			((Exception)e).printStackTrace();
	}
}
