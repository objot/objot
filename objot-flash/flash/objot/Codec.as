package objot
{

import flash.utils.Dictionary;
import flash.utils.getDefinitionByName;
import flash.utils.getQualifiedClassName;

public class Codec extends Object
{
	/** add encoding rules to the SO class. former rules are overrided by later rules.
 	 * (@param forClass key. @param encs what to encode, all if null)... */
	public static function addRule(so:Class, forClass:Class, encs:Array):void
	{
		var s:Array = so[RULE] || (so[RULE] = []);
		for (var x:int = 1; x < arguments.length; ) {
			s.push(arguments[x++] as Class || Util.not(Class));
			if ((encs = arguments[x++]) === null)
				s.push(null);
			else if (encs is Array) {
				for (var y:int = 0; y < encs.length; y++)
					if ( !(encs[y] is String))
						throw new Error(Util.s(encs) + ' must no contain ' + Util.s(encs[y]));
				s.push(encs);
			}
			else
				throw new TypeError(Util.s(encs) + ' must be array or null');
		}
	}

	private static const RULE:String = 'objot$Codec$rule';

	public function Codec()
	{
	}

	/**
	 * Get object or class by name, Object for '' by default
	 * 
	 * @param name may be ''
	 * @return class for creating, otherwise for reusing
	 */
	protected function byName(name:String):Object
	{
		if (name.length == 0)
			return Object;
		return getDefinitionByName(name);
	}

	protected function decoded(o:Object):void
	{
	}

	/**
	 * Get object or class name, '' for Object by default
	 * 
	 * @param o may be null
	 * @param c the object class
	 * @return could be ''
	 */
	protected function name(o:Object, c:Class):String
	{
		if (c == Object)
			return '';
		return getQualifiedClassName(c);
	}

	private var forClass:Class;
	private var refX:int;
	private var refs:Dictionary;

	public function enc(o:Object, for_:Class):String
	{
		var s:Array = [o is Array ? '[' : (Util.nul(o), '{')];
		forClass = for_, refX = 0, refs = new Dictionary();
		encRef(o) || Util.err(Util.s(o)
			+ ' must not be null, String, Boolean, Number, Class, Function, Dictionary');
		o is Array ? encL(o as Array, s, 1) : encO(o, s, 1);
		return s.join('\x10');
	}

	private function encRef(o:Object):Boolean
	{
		if (o == null || o is String || o is Boolean || o is Number
			|| o is Class || o is Function || o is Dictionary)
			return false;
		if (o is Date || (refs[o] = o in refs)) // check and set multi reference flag
			return true;
		var l = o is Array, p:Object;
		for (var y in o)
			if (l || o.hasOwnProperty(y)) // @todo rule
				if ((p = o[y]) is String)
					p.indexOf('\x10') < 0 || Util.err(Util.s(p) + ' must NOT contain \\x10');
				else
					encRef(p);
		return true;
	}

	private function encName(c:Class):String {
		return Util.extend(c, String) ? "'" : Util.extend(c, Boolean) ? '<'
			: Util.extend(c, Number) ? '0' : Util.extend(c, Date) ? '*'
			: Util.extend(c, Array) ? '[' : name(null, c);
	}

	private function encL(o:Array, s:Array, x:int):int {
		s[x++] = String(o.length);
		if (refs[o])
			s[x++] = ':', s[x++] = refs[o] = String(++refX);
		for (var i:int = 0, v:Object; i < o.length; i++)
			if (v = o[i], v is Class)
				s[x++] = '/', s[x++] = encName(v as Class); 
			else if (! (v is Function)) 
				s[x++] = v == null ? ',' : v === false ? '<' : v === true ? '>'
					: v is Number ? String(v) : v is String ? (s[x++] = v, '')
					: refs[v] is String ? (s[x++] = refs[v], '=')
					: v is Date ? (s[x++] = v.getTime(), '*')
					: v is Array ? (x = encL(v as Array, s, x), '[')
					: (x = encO(v, s, x), '{');
		s[x++] = ']';
		return x;
	}

	private function encO(o:Object, s:Array, x:int):int {
		s[x++] = name(o, o.constructor);
		var enc:Array, p:String, v:Object;
		refs[o] && (s[x++] = ':', s[x++] = refs[o] = String(++refX));
		P: {
			G: if ((enc = o.constructor[RULE])) {
				for (var c = s.clazz, g:int = enc.length - 2; g >= 0; g -= 2)
					if (Util.extend(c, enc[g])) {
						if ((enc = enc[g + 1])) {

		for (var n:int = 0; n < enc.length; n++)
			if ((p = enc[n]) in o)
			if (v = o[p], v is Class)
				s[x++] = p, s[x++] = '/', s[x++] = encName(v as Class); 
			else if (! (v is Function)) 
				s[x++] = p,
				s[x++] = v == null ? ',' : v === false ? '<' : v === true ? '>'
					: v is Number ? String(v) : v is String ? (s[x++] = v, '')
					: refs[v] is String ? (s[x++] = refs[v], '=')
					: v is Date ? (s[x++] = v.getTime(), '*')
					: v is Array ? (x = encL(v as Array, s, x), '[')
					: (x = encO(v, s, x), '{');

							break P;
						}
						break G;
					}
				break P;
			}
		for (p in o)
			if (o.hasOwnProperty(p))
			if (v = o[p], v is Class)
				s[x++] = p, s[x++] = '/', s[x++] = encName(v as Class); 
			else if (! (v is Function)) 
				s[x++] = p,
				s[x++] = v == null ? ',' : v === false ? '<' : v === true ? '>'
					: v is Number ? String(v) : v is String ? (s[x++] = v, '')
					: refs[v] is String ? (s[x++] = refs[v], '=')
					: v is Date ? (s[x++] = v.getTime(), '*')
					: v is Array ? (x = encL(v as Array, s, x), '[')
					: (x = encO(v, s, x), '{');
		}
		s[x++] = '}';
		return x;
	}

	public function dec(s:String, cla:Class, for_:Class):Object
	{
		return null;
	}
}

}