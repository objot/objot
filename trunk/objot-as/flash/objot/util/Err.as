//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util
{


public class Err 
{
	public var hint:String;

	public function Err(hint_:String = '')
	{
		hint = hint_;
	}

	public static function th(x:Object)
	{
		throw x as Error || new Error(x);	
	}
}
}