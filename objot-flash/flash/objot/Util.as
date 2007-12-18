package objot
{

import flash.utils.getDefinitionByName;
import flash.utils.getQualifiedClassName;


public class Util
{
	public function Util()
	{
		throw Error('singleton');
	}

	// ********************************************************************************

	public static function s(x):String
	{
		if (x == null)
			return 'null';
		if (x is Array)
			return x.length + '[' + s(String(x)) + '...]';
		x = "'" + String(x) + "'";
		return (x.length > 40 ? x.substring(0, 40) + '...' : x).replace(/\r?\n/g, '\\n');
	}

	public static function err(x)
	{
		throw x as Error || new Error(x);	
	}

	/** @return false if not null
	 * @throw ReferenceError if null */
	public static function nul(o = null):Boolean
	{
		if (o == null)
			throw ReferenceError('null');
		return false;
	}

	public static function not(x)
	{
		throw new TypeError('must be ' + x);
	}

	// ********************************************************************************

	public static function cinit(c:Class):Class
	{
		return getDefinitionByName(getQualifiedClassName(c)) as Class;
	}

	public static function extend(sub, sup:Class):Boolean
	{
		return sub == sup || sub.prototype instanceof sup;
	}
}

}