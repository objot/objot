//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util
{
import flash.utils.getQualifiedClassName;


public class Array2
{
	public function Array2()
	{
		throw Error('singleton');
	}

	public static function fromXml(x):Array
	{
		if (x is XML)
			return new Array(x);
		var xs:XMLList = Cast.xmls(x);
		var s = new Array(xs.length());
		for (var i:int = s.length - 1; i >= 0; i--)
			s[i] = xs[i];
		return s;
	}

	public static function map(to:Object, s:Array, propKey:String, propValue:String = null)
		:Object
	{
		if (propKey != null)
			if (propValue != null)
				for (var i:int = 0; i < s.length; i++)
					to[s[i][propKey]] = s[i][propValue];
			else
				for (i = 0; i < s.length; i++)
					to[s[i][propKey]] = s[i];
		else
			if (propValue != null)
				for (i = 0; i < s.length; i++)
					to[s[i]] = s[i][propValue];
			else
				for (i = 0; i < s.length; i++)
					to[s[i]] = s[i];
		return to;
	}
}
}
