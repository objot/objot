//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util
{


public class Param
{
	public var method:Method;

	public var index:int;

	public var type:Class;

	public var option:Boolean;

	public function Param(m:Method, x:XML, index_:int)
	{
		method = m;
		index = index_;
		type = Class2.byName(x.@type);
		option = x.@optional == 'true';
	}

	public static function params(m:Method, x:XML, s:Array):Array
	{
		if (!s)
			return s;
		var i:int = 0;
		for each (x in x.parameter)
			s[s.length] = new Param(m, x, i++);
		return s;
	}
}
}
