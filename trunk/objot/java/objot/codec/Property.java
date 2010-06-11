//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.codec;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import objot.util.Class2;


public class Property
{
	protected Class<?> in;
	protected Field field;
	protected Method method;
	/** public for {@link Clazz} subclass */
	public String name;
	protected Class<?> cla;
	protected Class<?> listElem;

	/** @param fm field or method */
	public Property(AccessibleObject fm, boolean enc)
	{
		if (fm instanceof Field)
		{
			field = (Field)fm;
			in = field.getDeclaringClass();
			name = field.getName();
			cla = field.getType();
		}
		else
		{
			method = (Method)fm;
			in = method.getDeclaringClass();
			name = Class2.propertyOrName(method, enc);
			cla = enc ? method.getReturnType() : method.getParameterTypes()[0];
		}
		if (Collection.class.isAssignableFrom(cla))
			listElem = Class2.typeParamClass(field != null ? field.getGenericType() : //
				enc ? method.getGenericReturnType() : method.getGenericParameterTypes()[0],
				0, Object.class);
		else
			listElem = Object.class;
	}

	/**
	 * add this to a map
	 * 
	 * @throws RuntimeException if there is a exist property with same name
	 */
	public void into(Map<String, Property> map)
	{
		Property p = map.get(name);
		if (p != null)
			throw new RuntimeException("duplicate name " + name + ", see " + p);
		map.put(name, this);
	}

	/** @param o the object having this property to be encoded */
	public boolean encodable(Object o, Object ruleKey) throws Exception
	{
		return true;
	}

	/** @param o the object having this property to be decoded */
	public boolean decodable(Object o, Object ruleKey) throws Exception
	{
		return true;
	}

	int index;
	boolean clob;
	static final Field F_name = Class2.declaredField(Property.class, "name");
	static final Method M_encodable = Class2.declaredMethod1(Property.class, "encodable");
}
