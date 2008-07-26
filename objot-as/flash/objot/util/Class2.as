//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.util
{
import flash.utils.getDefinitionByName;
import flash.utils.getQualifiedClassName;
import flash.utils.getQualifiedSuperclassName;
import flash.utils.describeType;
import flash.utils.Dictionary;


public class Class2
{
	public function Class()
	{
		throw Error('singleton');
	}

	/** run static init code, make getDefinitionByName available, extract some info */
	public static function init(c:Class):Class
	{
		if (c.$supers)
			return c;
		var d:XML = describeType(c).factory[0], z;
		c.$supers = new Dictionary;
		for each(var i:String in (d.extendsClass + d.implementsInterface).@type)
			z = Class(getDefinitionByName(i)),
			c.$super || (c.$super = z),
			c.$supers[z] = i;
		return c;
	}

	public static function sup(c:Object):Class
	{
		return init(c as Class || c.constructor).$super;
	}

	/** always false for interfaces */
	public static function extend(sub:Class, sup:Class):Boolean
	{
		return sub == sup || sub.prototype instanceof sup;
	}

	/** super classes or interfaces */
	public static function sub(sub:Class, sup:Class):Boolean
	{
		return sub == sup || init(sub).$supers[sup];
	}
}
}
