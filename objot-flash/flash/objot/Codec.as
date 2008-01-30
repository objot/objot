package objot
{

import flash.utils.Dictionary;
import flash.utils.getDefinitionByName;
import flash.utils.getQualifiedClassName;

public class Codec extends Object
{
	/** add encoding rules to the SO class. former rules are overrided by later rules.
 	 * (@param forClass_ key. @param encs what to encode, all if null)... */
	public static function addRule(so:Class, forClass_:Class, encs:Array):void
	{
		var s:Array = so[RULE] || (so[RULE] = []);
		for (var x:int = 1; x < arguments.length; )
		{
			s.push(Class(arguments[x++]));
			if ((encs = arguments[x++]) === null)
				s.push(null);
			else if (encs is Array)
			{
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
	private var encRefs:Dictionary;

	/** encode object graph to string, following the encoding rules.
	 * @param for_ rule key or subclass of rule key */
	public function enc(o:Object, for_:Class):String
	{
		Util.nul(o);
		var s:Array = [o is Array ? '[' : '{'];
		try
		{
			forClass = for_, refX = 0, encRefs = new Dictionary();
			encRef(o) || Util.err(Util.s(o)
				+ ' must not be null, String, Boolean, Number, Class, Function, Dictionary');
			o is Array ? encL(o as Array, s, 1) : encO(o, s, 1);
		}
		finally
		{
			forClass = null, encRefs = null;	
		}
		return s.join('\x10');
	}

	private function encRef(o:Object):Boolean
	{
		if (o == null || o is String || o is Boolean || o is Number
			|| o is Class || o is Function || o is Dictionary)
			return false;
		if (o is Date || (encRefs[o] = o in encRefs)) // check and set multi reference flag
			return true;
		var l = o is Array, enc:Array, p:Object;
		P: {
			G: if ( !l && (enc = o.constructor[RULE]))
			{
				for (var g:int = enc.length - 2; g >= 0; g -= 2)
					if (Util.extend(forClass, enc[g]))
					{
						if ((enc = enc[g + 1]))
						{
							for (var n:int = 0; n < enc.length; n++)
								if ((p = enc[n]) in o)
									if ((p = o[p]) is String)
										p.indexOf('\x10') < 0
										|| Util.err(Util.s(p) + ' must NOT contain \\x10');
									else
										encRef(p);
							break P;
						}
						break G;
					}
				break P;
			}
			for (var y in o)
				if (l || o.hasOwnProperty(y))
					if ((p = o[y]) is String)
						p.indexOf('\x10') < 0
						|| Util.err(Util.s(p) + ' must NOT contain \\x10');
					else
						encRef(p);
		}
		return true;
	}

	private function encName(c:Class):String
	{
		return Util.extend(c, String) ? "'" : Util.extend(c, Boolean) ? '<'
			: Util.extend(c, Number) ? '0' : Util.extend(c, Date) ? '*'
			: Util.extend(c, Array) ? '[' : name(null, c);
	}

	private function encL(o:Array, s:Array, x:int):int
	{
		s[x++] = String(o.length);
		if (encRefs[o])
			s[x++] = ':', s[x++] = encRefs[o] = String(++refX);
		for (var i:int = 0, v:Object; i < o.length; i++)
			if ((v = o[i]) == null)
				s[x++] = ',';
			else if (v is Boolean)
				s[x++] = v ? '>' : '<';
			else if (v is Number)
				s[x++] = String(v); 
			else if (v is String)
				s[x++] = '', s[x++] = v;
			else if (v is Date)
				s[x++] = '*', s[x++] = v.getTime();
			else if (v is Class)
				s[x++] = '/', s[x++] = encName(Class(v));
			else if (encRefs[v] is String)
				s[x++] = '=', s[x++] = encRefs[v];
			else if (v is Array)
				s[x++] = '[', x = encL(v as Array, s, x);
			else if ( !(v is Function))
				s[x++] = '{', x = encO(v, s, x);
		s[x++] = ']';
		return x;
	}

	private function encO(o:Object, s:Array, x:int):int
	{
		s[x++] = name(o, o.constructor);
		var enc:Array, p:String, v:Object;
		if (encRefs[o])
			s[x++] = ':', s[x++] = encRefs[o] = String(++refX);
		P: {
			G: if ((enc = o.constructor[RULE]))
			{
				for (var g:int = enc.length - 2; g >= 0; g -= 2)
					if (Util.extend(forClass, enc[g]))
					{
						if ((enc = enc[g + 1]))
						{
							for (var n:int = 0; n < enc.length; n++)
								if ((p = enc[n]) in o)
								if ( !((v = o[p]) is Function))
								{
									s[x++] = p;
									if (v == null)
										s[x++] = ',';
									else if (v is Boolean)
										s[x++] = v ? '>' : '<';
									else if (v is Number)
										s[x++] = String(v); 
									else if (v is String)
										s[x++] = '', s[x++] = v;
									else if (v is Date)
										s[x++] = '*', s[x++] = v.getTime();
									else if (v is Class)
										s[x++] = '/', s[x++] = encName(Class(v));
									else if (encRefs[v] is String)
										s[x++] = '=', s[x++] = encRefs[v];
									else if (v is Array)
										s[x++] = '[', x = encL(v as Array, s, x);
									else
										s[x++] = '{', x = encO(v, s, x);
								}
							break P;
						}
						break G;
					}
				break P;
			}
			for (p in o)
				if (o.hasOwnProperty(p))
				if ( !((v = o[p]) is Function))
				{
					s[x++] = p;
					if (v == null)
						s[x++] = ',';
					else if (v is Boolean)
						s[x++] = v ? '>' : '<';
					else if (v is Number)
						s[x++] = String(v); 
					else if (v is String)
						s[x++] = '', s[x++] = v;
					else if (v is Date)
						s[x++] = '*', s[x++] = v.getTime();
					else if (v is Class)
						s[x++] = '/', s[x++] = encName(Class(v));
					else if (encRefs[v] is String)
						s[x++] = '=', s[x++] = encRefs[v];
					else if (v is Array)
						s[x++] = '[', x = encL(v as Array, s, x);
					else
						s[x++] = '{', x = encO(v, s, x);
				}
		}
		s[x++] = '}';
		return x;
	}

	private var decRefs:Array = [];

	/** decode string to object graph, objects are created without constructors. */
	public function dec(str:String):Object
	{
		try
		{
			var s:Array = str.split('\x10');
			var x:int = s[0] === '[' ? decL(s, 1) : s[0] === '{' ? decO(s, 1) : -1;
			if (x < s.length)
				Util.err('termination expected but ' + Util.s(s[x]));
		}
		finally
		{
			decRefs.length = 0;
		}
		return s.o;
	}

	private function decName(s:String):Class
	{
		switch (s)
		{
			case "'" : return String;
			case '<' : return Boolean;
			case '0' : return Number;
			case '*' : return Date;
			case '[' : return Array;
		}
		var x:Object = byName(s);
		return x as Class || x.constructor;
	}

	private function decL(s:Array, x:int):int
	{
		var o:Array = new Array(s[x++] - 0);
		if (s[x] === ':')
			decRefs[s[++x]] = o, x++;
		for (var i:int = 0, v:Object; x >= s.length ? Util.err('; expected but terminated')
			: (v = s[x++]) !== ']'; i++)
			switch (v) {
				case '': o[i] = s[x++]; break;
				case ',': o[i] = null; break;
				case '<': o[i] = false; break;
				case '>': o[i] = true; break;
				case '*': o[i] = new Date(s[x++] - 0); break;
				case '/': o[i] = decName(s[x++]); break;
				case '[': x = decL(s, x); o[i] = s.o; break;
				case '{': x = decO(s, x); o[i] = s.o; break;
				case '=': o[i] = decRefs[s[x++]]; break;
				case 'NaN': o[i] = NaN; break;
				default: isNaN(o[i] = Number(v)) && Util.err('illegal number ' + Util.s(v));
			}
		s.o = o;
		return x;
	}

	private function decO(s:Array, x:int):int {
		var o:Object = byName(s[x++]), p:Object, v:Object;
		if (o is Class)
			o = new o();
		if (s[x] === ':')
			decRefs[s[++x]] = o, x++;
		while (x >= s.length ? Util.err('; expected but terminated') : (p = s[x++]) !== '}')
			switch (v = s[x++]) {
				case '': o[p] = s[x++]; break;
				case ',': o[p] = null; break;
				case '<': o[p] = false; break;
				case '>': o[p] = true; break;
				case '*': o[p] = new Date(s[x++] - 0); break;
				case '/': o[p] = decName(s[x++]); break;
				case '[': x = decL(s, x); o[p] = s.o; break;
				case '{': x = decO(s, x); o[p] = s.o; break;
				case '=': o[p] = decRefs[s[x++]]; break;
				case 'NaN': o[p] = NaN; break;
				default: isNaN(o[p] = Number(v)) && Util.err('illegal number ' + Util.s(v));
			}
		decoded(s.o = o);
		return x;
	}
}

}