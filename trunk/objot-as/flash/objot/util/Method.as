//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util
{


public class Method extends Metas
{
	public var cla2:Class2;

	public var name:String;

	public var type:Class;

	/** where this property is declared */
	public var on:Class;

	public var static:Boolean;

	/** [ Param ] */
	public var params:Array;

	public function Method(c:Class2, x:XML, sta:Boolean)
	{
		cla2 = c;
		name = String(x.@name),
		type = Class2.byName(x.@returnType),
		on = c.superz[x.@declaredBy] || c.cla,
		static = sta;
		Param.params(this, x, params = []);
		Meta.metas(this, x, metas = [], metaz = {});
	}

	public static function methods(c:Class2, x:XML, sta:Boolean, s:Array, z:Object = null)
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
