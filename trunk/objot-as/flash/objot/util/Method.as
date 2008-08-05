//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.util
{


public class Method
{
	public var name:String;

	public var type:Class;

	/** where this property is declared */
	public var on:Class;

	public var static:Boolean;

	/** [ Param ] */
	public var params:Array;

	/** [ Meta ] */
	public var metas:Array;

	/** { name: Meta } */
	public var metaz:Object;

	public function Method(c:Class, x:XML, sta:Boolean)
	{
		name = String(x.@name),
		type = Class2.byName(x.@returnType),
		on = c.$.superz[x.@declaredBy] || c,
		static = sta;
		Param.params(x, params = []);
		Meta.metas(x, metas = [], metaz = {});
	}

	public static function methods(c:Class, x:XML, sta:Boolean, s:Array, z:Object = null)
		:Array
	{
		for each (x in x.method)
		{
			var m:Method = new Method(c, x, sta);
			s && (s[s.length] = m);
			z && (z[m.name] = m);
		}
		return s;
	}
}
}
