//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.util
{
import flash.utils.Dictionary;
import flash.utils.describeType;
import flash.utils.getDefinitionByName;
import flash.utils.getQualifiedClassName;
import flash.utils.getQualifiedSuperclassName;


public class Class2
{
	public var cla:Class;

	public var name:String;

	/** without package */
	public var selfName:String;

	public var super0:Class;

	/** [ class ] */
	public var extens:Array;

	/** [ class ] */
	public var interfaces:Array;

	/** { name: class } */
	public var superz:Object;

	/** { class: name } */
	public var superNamez:Dictionary;

	/** [ Prop ]*/
	public var props:Array;

	/** { name: Prop } */
	public var propz:Object;

	/** [ Prop ]*/
	public var staticProps:Array;

	/** { name: Prop } */
	public var staticPropz:Object;

	/** [ Method ]*/
	public var methods:Array;

	/** { name: Method } */
	public var methodz:Object;

	/** [ Method ]*/
	public var staticMethods:Array;

	/** { name: Method } */
	public var staticMethodz:Object;

	/** [ Prop ] */
	public var allProps:Array;

	/** [ Method ] */
	public var allMethods:Array;

	/** [ Meta ]*/
	public var metas:Array;

	/** { name: Meta } */
	public var metaz:Object;

	public function Class2()
	{
	}

	/**
	 * run static init code, make getDefinitionByName available,
	 *  extract some info to class.$:Class2 variable
	 */
	public static function init(c:Class):Class2
	{
		if (c.$)
			return c.$;
		var d:Class2 = c.$ = new Class2;
		d.cla = c;
		d.name = getQualifiedClassName(c);
		d.selfName = d.name.replace(/.*::/, '');
		var x:XML = describeType(c), xf:XML = x.factory[0];
		d.extens = [];
		for each (var i:String in xf.extendsClass.@type)
			d.extens.push(byName(i));
		d.super0 = d.extens[0];
		d.interfaces = [];
		for each (i in xf.implementsInterface.@type)
			d.interfaces.push(byName(i));
		d.superz = Array2.map(Array2.map({}, d.extens, '$name'), d.interfaces, '$name');
		d.superNamez = Dictionary(Array2.map(Array2.map(new Dictionary(),
			d.extens, null, '$name'), d.interfaces, null, '$name'));
		Prop.props(c, xf, false, d.props = [], d.propz = []);
		Prop.props(c, x, true, d.staticProps = [], d.staticPropz = []);
		Method.methods(c, xf, false, d.methods = [], d.methodz = {});
		Method.methods(c, x, true, d.staticMethods = [], d.staticMethodz = {});
		d.allProps = d.props.concat(d.staticProps);
		d.allMethods = d.methods.concat(d.staticMethods);
		Meta.metas(xf, d.metas = [], d.metaz = {});
		return d;
	}

	/** getDefinitionByName, null for void, Object for * */
	public static function byName(s:String):Class
	{
		return s == 'void' ? null : s == '*' ? Object : Class(getDefinitionByName(s));
	}

	/** init(byName()) */
	public static function initByName(s:String):Class
	{
		return init(byName(s)).cla;
	}

	public static function sup(c:Object):Class
	{
		return init(c as Class || c.constructor).super0;
	}

	/** always false for interfaces */
	public static function extend(sub:Class, sup:Class):Boolean
	{
		return sub == sup || sub.prototype instanceof sup;
	}

	/** super classes or interfaces */
	public static function sub(sub:Class, sup:Class):Boolean
	{
		return sub == sup || init(sub).superNamez[sup];
	}
}

}
