//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.util
{


public class Param
{
	public var index:int;

	public var type:Class;

	public var option:Boolean;

	public function Param(x:XML, index_:int)
	{
		index = index_;
		type = Class2.byName(x.@type);
		option = x.@optional == 'true';
	}

	public static function params(x:XML, s:Array):Array
	{
		if (!s)
			return s;
		var i:int = 0;
		for each (x in x.parameter)
			s[s.length] = new Param(x, i++);
		return s;
	}
}
}
