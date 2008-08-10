//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util
{
import flash.utils.getQualifiedClassName;


public class Cast
{
	public function Cast()
	{
		throw Error('singleton');
	}

	public static function arr(x):Array
	{
		return x;
	}

	public static function bool(x):Boolean
	{
		return x == null ? x : x as Boolean || Err.th(new TypeError
			(getQualifiedClassName(x) + ' to Boolean'));
	}

	public static function date(x):Date
	{
		return x;
	}

	public static function num(x):Number
	{
		return x == null ? x : x as Number || Err.th(new TypeError
			(getQualifiedClassName(x) + ' to Number'));
	}

	public static function str(x):String
	{
		return x == null ? x : x as String || Err.th(new TypeError
			(getQualifiedClassName(x) + ' to String'));
	}

	public static function xml(x):XML
	{
		return x;
	}

	public static function xmls(x):XMLList
	{
		return x;
	}
}
}
