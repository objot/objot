//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util
{


public class Meta
{
	public var on:Metas;

	public var name:String;

	/** [ MetaArg ] */
	public var args:Array;

	/** { name: value } */
	public var argz:Object;

	public function Meta(o:Metas, x:XML)
	{
		on = o;
		name = String(x.@name);
		MetaArg.args(x, args = [], argz = {});
	}

	public static function metas(o:Metas, x:XML, s:Array, z:Object = null):Array
	{
		for each (x in x.metadata)
		{
			var m:Meta = new Meta(o, x);
			s && (s[s.length] = m);
			z && (z[m.name] = m);
		}
		return s;
	}
}
}
