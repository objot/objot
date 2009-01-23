//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util
{


public class Prop extends Metas
{
	public var cla2:Class2;

	public var name:String;

	public var type:Class;

	/** where this property is declared */
	public var on:Class;

	public var static:Boolean;

	/** readable */
	public var read:Boolean;

	/** writeable */
	public var write:Boolean;

	public function Prop(c:Class2, x:XML, sta:Boolean)
	{
		cla2 = c;
		name = String(x.@name),
		type = Class2.byName(x.@type),
		on = c.superz[x.@declaredBy] || c.cla,
		static = sta;
		read = x.@access != 'writeonly',
		write = x.@access != 'readonly',
		Meta.metas(this, x, metas = [], metaz = {});
	}

	public static function props(c:Class2, x:XML, sta:Boolean, s:Array, z:Object = null)
		:Array
	{
		for each (x in (x.variable + x.accessor))
		{
			var p:Prop = new Prop(c, x, sta);
			s && (s[s.length] = p);
			z && (z[p.name] = p);
		}
		return s;
	}
}
}
