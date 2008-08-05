//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.util
{


public class Prop
{
	public var name:String;

	public var type:Class;

	/** where this property is declared */
	public var on:Class;

	public var static:Boolean;

	/** readable */
	public var read:Boolean;

	/** writeable */
	public var write:Boolean;

	/** [ Meta ] */
	public var metas:Array;

	/** { name: Meta } */
	public var metaz:Object;

	public function Prop(c:Class, x:XML, sta:Boolean)
	{
		name = String(x.@name),
		type = Class2.byName(x.@type),
		on = c.$.superz[x.@declaredBy] || c,
		static = sta;
		read = x.@access != 'writeonly',
		write = x.@access != 'readonly',
		Meta.metas(x, metas = [], metaz = {});
	}

	public static function props(c:Class, x:XML, sta:Boolean, s:Array, z:Object = null)
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
