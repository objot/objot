package objot
{


public class Util
{
	protected function Util()
	{
	}

	public static function s(x):String
	{
		if (x == null)
			return 'null';
		if (x is Array)
			return x.length + '[' + s(String(x)) + '...]';
		x = "'" + String(x) + "'";
		return (x.length > 40 ? x.substring(0, 40) + '...' : x).replace(/\r?\n/g, '\\n'));
	}
}

}