//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.util.StringHelper;

import chat.model.Id;


public class Models
{
	public static AnnotationConfiguration init() throws Exception
	{
		AnnotationConfiguration c = new AnnotationConfiguration();

		c.setNamingStrategy(new NamingStrategy()
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
		});

		for (Class<?> cla: PackageClass.getClasses(Id.class))
			c.addAnnotatedClass(cla);
		return c;
	}
}
