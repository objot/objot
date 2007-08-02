package chat;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;


public class PackageClass
{
	/**
	 * get all classes in a package
	 * 
	 * @param cla one of classes in the package
	 */
	public static ArrayList<Class<?>> getClasses(Class<?> cla) throws Exception
	{
		if (cla.isPrimitive() || cla.isArray())
			throw new Exception("invalid class " + cla);
		String pkg = cla.getPackage().getName();
		// find the package directory
		URL url = cla.getResource("/" + cla.getName().replace('.', '/') + ".class");
		url = new URL(url.toString().substring(0, url.toString().lastIndexOf('/') + 1));

		ArrayList<Class<?>> clas = new ArrayList<Class<?>>();

		if (url.getProtocol().equals("file"))
		{
			File path = new File(url.getPath());
			if (! path.isDirectory())
				throw new Exception(path + " must be directory");
			// iterate on all classes in the package
			for (String _: path.list())
				if (_.endsWith(".class"))
					clas.add(Class.forName(pkg + "." + _.substring(0, _.lastIndexOf('.'))));
		}
		else if (url.getProtocol().equals("jar"))
		{
			JarURLConnection conn = (JarURLConnection)url.openConnection();
			JarEntry path = conn.getJarEntry();
			if (! path.isDirectory())
				throw new Exception(path + "must be directory");
			String name;
			// iterate on all classes in the package
			for (Enumeration<JarEntry> es = conn.getJarFile().entries(); es.hasMoreElements();)
				if ((name = es.nextElement().getName()).startsWith(path.getName())
					&& name.endsWith(".class"))
					clas.add(Class.forName(pkg + "."
						+ name.substring(path.getName().length(), name.lastIndexOf('.'))));
		}
		else
			throw new Exception(url.getProtocol() + " will be supported soon");
		return clas;
	}
}
