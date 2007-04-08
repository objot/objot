package chat.model;

import java.io.File;
import java.net.URL;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.util.StringHelper;


public class Model
{
	public static AnnotationConfiguration init() throws Exception
	{
		AnnotationConfiguration c = new AnnotationConfiguration();
		c.setNamingStrategy(new Naming());

		// find the package directory
		URL url = Model.class.getResource("/" + Model.class.getName().replace('.', '/')
			+ ".class");
		if (! url.getProtocol().equals("file"))
			throw new Exception(url.getProtocol() + " will be supported soon");

		File path = new File(url.getPath().substring(0, url.getPath().lastIndexOf('/') + 1));
		if (! path.isDirectory())
			throw new Exception(path + " must be directory");

		// iterate on all classes in the package
		for (String _: path.list())
			c.addAnnotatedClass(Class.forName(Model.class.getPackage().getName() + "."
				+ _.substring(0, _.lastIndexOf('.'))));

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
