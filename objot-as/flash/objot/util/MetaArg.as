//
// Copyright 2007-2015 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util
{


public class MetaArg
{
	public var name:String;
	public var value:String;

	public function MetaArg(x:XML)
	{
		name = String(x.@key);
		value = String(x.@value);
	}

	public static function args(x:XML, s:Array, z:Object = null):Array
	{
		for each (x in x.arg)
		{
			var a:MetaArg = new MetaArg(x);
			s && (s[s.length] = a);
			z && (z[a.name] = a.value);
		}
		return s;
	}
}
}
