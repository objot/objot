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
	public function Class2()
	{
	}

	/** getDefinitionByName, null for void, Object for * */
	public static function byName(s:String):Class
	{
		return s == 'void' ? null : s == '*' ? Object : Class(getDefinitionByName(s));
	}

	/** init(byName()) */
	public static function initByName(s:String):Class
	{
		return init(byName(s));
	}

	public var name:String;
	/** without package */
	public var selfName:String;
	/** [class] */
	public var extens:Array;
	/** [class] */
	public var interfaces:Array;
	/** {name:class} */
	public var superz:Object;
	/** {class:name} */
	public var superNamez:Dictionary;
	/** [{name,type,on,read,write,metas,metaz}]*/
	public var props:Array;
	/** {name: {...}} */
	public var propz:Object;
	/** [{name,type,on,params,metas,metaz}]*/
	public var methods:Array;
	/** {name: {...}} */
	public var methodz:Object;
	/** [{name,type,on,read,write,metas,metaz}]*/
	public var staticProps:Array;
	/** {name: {...}} */
	public var staticPropz:Object;
	/** [{name,type,on,params,metas,metaz}]*/
	public var staticMethods:Array;
	/** {name: {...}} */
	public var staticMethodz:Object;
	
	/**
	 * run static init code, make getDefinitionByName available,
	 *  extract some info to class.$:Class2 variable
	 */
	public static function init(c:Class):Class
	{
		if (c.$)
			return c;
		var d:Class2 = c.$ = new Class2;
		d.name = getQualifiedClassName(c);
		var x:XML = describeType(c), xf:XML = x.factory[0];
		d.extens = [];
		d.interfaces = [];
		for each (var i:String in xf.extendsClass.@type)
			d.extens.push(byName(i));
		for each (i in xf.implementsInterface.@type)
			d.interfaces.push(byName(i));
		d.superz = Array2.map(Array2.map({}, d.extens, '$name'), d.interfaces, '$name');
		d.superNamez = Dictionary(Array2.map(Array2.map(new Dictionary(),
			d.extens, null, '$name'), d.interfaces, null, '$name'));
		d.props = initProps(c, xf);
		d.propz = Array2.map({}, d.props, 'name');
		d.methods = initMethods(c, xf);
		d.methodz = Array2.map({}, d.methods, 'name');
		d.staticProps = initProps(c, x);
		d.staticPropz = Array2.map({}, d.staticProps, 'name');
		d.staticMethods = initMethods(c, x);
		d.staticMethodz = Array2.map({}, d.staticMethods, 'name');
		return c;
	}

	protected static function initProps(c:Class, x:XML):Array
	{
		var s:Array = [];
		for each (x in (x.variable + x.accessor))
		{
			var p =
			{
				name: String(x.@name),
				type: byName(x.@type),
				on: c.$.superz[x.@declaredBy] || c,
				read: x.@access != 'writeonly',
				write: x.@access != 'readonly',
				metas: initMetas(x)
			};
			p.metaz = Array2.map({}, p.metas, 'name');
			s.push(p);
		}
		return s;
	}

	protected static function initMethods(c:Class, x:XML):Array
	{
		var s:Array = [];
		for each (x in x.method)
		{
			var m =
			{
				name: String(x.@name),
				type: byName(x.@returnType),
				on: c.$.superz[x.@declaredBy] || c,
				params: Array2.fromXml(x.parameter),
				metas: initMetas(x)
			};
			for (var i:int = m.params.length - 1; i >= 0; i--)
				m.params[i] =
					{
						index: i,
						type: byName(m.params[i].@type),
						option: m.params[i].@optional == 'true'
					};
			m.metaz = Array2.map({}, m.metas, 'name');
			s.push(m);
		}
		return s;
	}

	protected static function initMetas(x:XML):Array
	{
		var s:Array = [];
		for each (x in x.metadata)
		{
			var m = { name: String(x.@name), args: [] };
			for each (var y:XML in x.arg)
				m.args.push({ key: String(y.@key), value: String(y.@value) });
			m.argz = Array2.map({}, m.args, 'key', 'value');
			s.push(m);
		}
		return s;
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
		return sub == sup || init(sub).$superNamez[sup];
	}
}

}
