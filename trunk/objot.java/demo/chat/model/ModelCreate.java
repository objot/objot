package chat.model;

import java.io.EOFException;
import java.io.File;
import java.io.FileReader;

import org.hibernate.tool.hbm2ddl.SchemaExport;


public class ModelCreate
{
	/** @param args whether to export, whether generate drop, false by default */
	public static void main(String... args) throws Exception
	{
		SchemaExport se = new SchemaExport(Model.init());
		String name = File.createTempFile(ModelCreate.class.getName(), "").getCanonicalPath();
		se.setOutputFile(name);
		se.execute(false, args.length < 1 ? false : Boolean.valueOf(args[0]), //
			false, args.length < 2 ? true : ! Boolean.valueOf(args[1]));

		File f = new File(name);
		f.deleteOnExit();
		FileReader read = new FileReader(f);
		char[] s = new char[(int)f.length()];
		for (int from = 0, done; from < s.length; from += done)
			if ((done = read.read(s, from, s.length - from)) < 0)
				throw new EOFException();
		System.out.print(s);
	}
}
