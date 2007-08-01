package chat.model.common;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.util.StringHelper;


public class Model
{
	public static AnnotationConfiguration init() throws Exception
	{
		AnnotationConfiguration c = new AnnotationConfiguration();
		c.setNamingStrategy(new Naming());

		String pkg = Model.class.getPackage().getName();
		// find the package directory
		URL url = Model.class.getResource("/" + Model.class.getName().replace('.', '/')
			+ ".class");
		url = new URL(url.toString().substring(0, url.toString().lastIndexOf('/') + 1));
		if (url.getProtocol().equals("file"))
		{
			File path = new File(url.getPath());
			if (! path.isDirectory())
				throw new Exception(path + " must be directory");
			// iterate on all classes in the package
			for (String _: path.list())
				if (_.endsWith(".class"))
					c.addAnnotatedClass(Class.forName(pkg + "."
						+ _.substring(0, _.lastIndexOf('.'))));
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
					c.addAnnotatedClass(Class.forName(pkg + "."
						+ name.substring(path.getName().length(), name.lastIndexOf('.'))));
		}
		else
			throw new Exception(url.getProtocol() + " will be supported soon");
		return c;
	}

	public static class Naming
		implements NamingStrategy
	{
		public String classToTableName(String entity)
		{
			return StringHelper.unqualify(entity);
		}

		public String propertyToColumnName(String property)
		{
			return StringHelper.unqualify(property);
		}

		public String tableName(String table)
		{
			return table;
		}

		public String columnName(String column)
		{
			return column;
		}

		public String collectionTableName(String ownerEntity, String ownerTable,
			String associatedEntity, String associatedTable, String property)
		{
			return ownerTable + "_" + StringHelper.unqualify(property);
		}

		public String joinKeyColumnName(String joinedColumn, String joinedTable)
		{
			return joinedColumn;
		}

		public String foreignKeyColumnName(String property, String propertyEntity,
			String propertyTable, String referencedColumn)
		{
			return property != null ? StringHelper.unqualify(property) : propertyTable;
		}

		public String logicalColumnName(String column, String property)
		{
			return column != null ? column : StringHelper.unqualify(property);
		}

		public String logicalCollectionTableName(String table, String ownerTable,
			String associatedTable, String property)
		{
			if (table != null)
				return table;
			return ownerTable + "_" + StringHelper.unqualify(property);
		}

		public String logicalCollectionColumnName(String column, String property,
			String referencedColumn)
		{
			return column != null ? column : property + "_" + referencedColumn;
		}
	}
}
