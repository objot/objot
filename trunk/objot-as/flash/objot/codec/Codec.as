//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.codec
{
import flash.utils.Dictionary;
import flash.utils.getDefinitionByName;
import flash.utils.getQualifiedClassName;
import objot.util.Class2;
import objot.util.Str;
import objot.util.Err;


public class Codec
{
	/** add encoding rules to a class. former rules are overrided by later rules.
 	 * (@param key. @param encs what to encode, all if null)... */
	public static function addRule(c:Class, key_:Class, encs:Array):void
	{
		var s:Array = c.$Codec$rule || (c.$Codec$rule = []);
		for (var x:int = 1; x < arguments.length; )
		{
			s.push(Class(arguments[x++]));
			if ((encs = arguments[x++]) === null)
				s.push(null);
			else if (encs is Array)
			{
				for (var y:int = 0; y < encs.length; y++)
					if ( !(encs[y] is String))
						throw new Error(Str.s(encs) + ' must no contain ' + Str.s(encs[y]));
				s.push(encs);
			}
			else
				throw new TypeError(Str.s(encs) + ' must be array or null');
		}
	}

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
	 * @param c the object class
	 * @return could be ''
	 */
	protected function name(o:Object, c:Class):String
	{
		if (c == Object)
			return '';
		return getQualifiedClassName(c);
	}

	private var key:Class;
	private var refX:int;
	private var encRefs:Dictionary;

	/** encode object graph to string, following the encoding rules. */
	public function enc(o:Object, ruleKey:Class):String
	{
		var s:Array =
			o == null ? [','] : o === false ? ['<'] : o === true ? ['>']
			: o is Number ? [String(o)] : o is String ? ['', o]
			: o is Date ? ['*', o.getTime()] : null;
		if ( !s)
			try
			{
				s = [o is Array ? '[' : '{'];
				key = ruleKey, refX = 0, encRefs = new Dictionary();
				encRef(o) || Err.th(Str.s(o)
					+ ' must not be null, String, Boolean, Number, Class, Function, Dictionary');
				o is Array ? encL(o as Array, s, 1) : encO(o, s, 1);
			}
			finally
			{
				key = null, encRefs = null;	
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
			G: if ( !l && (enc = o.constructor.$Codec$rule))
			{
				for (var g:int = enc.length - 2; g >= 0; g -= 2)
					if (Class2.extend(key, enc[g]))
					{
						if ((enc = enc[g + 1]))
						{
							for (var n:int = 0; n < enc.length; n++)
								if ((p = enc[n]) in o)
									if ((p = o[p]) is String)
										p.indexOf('\x10') < 0
										|| Err.th(Str.s(p) + ' must NOT contain \\x10');
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
						|| Err.th(Str.s(p) + ' must NOT contain \\x10');
					else
						encRef(p);
		}
		return true;
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
			else if (encRefs[v] is String)
				s[x++] = '=', s[x++] = encRefs[v];
			else if (v is Array)
				s[x++] = '[', x = encL(v as Array, s, x);
			else
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
			G: if ((enc = o.constructor.$Codec$rule))
			{
				for (var g:int = enc.length - 2; g >= 0; g -= 2)
					if (Class2.extend(key, enc[g]))
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
			var x:int = 1, v = s[0];
			switch (v)
			{
				case '[': x = decL(s, x); break;
				case '{': x = decO(s, x); break;
				case '': s.o = s[x++]; break;
				case ',': s.o = null; break;
				case '<': s.o = false; break;
				case '>': s.o = true; break;
				case '*': s.o = new Date(s[x++] - 0); break;
				case 'NaN': s.o = NaN; break;
				default: isNaN(s.o = Number(v)) && Err.th('illegal number ' + Str.s(v));
			}
			if (x < s.length)
				Err.th('end expected but ' + Str.s(s[x]));
			else if (x > s.length)
				Err.th('end unexpected');
		}
		finally
		{
			decRefs.length = 0;
		}
		return s.o;
	}

	private function decL(s:Array, x:int):int
	{
		var o:Array = new Array(s[x++] - 0);
		if (s[x] === ':')
			decRefs[s[++x]] = o, x++;
		for (var i:int = 0, v:Object; x >= s.length ? Err.th('] expected but end')
			: (v = s[x++]) !== ']'; i++)
			switch (v) {
				case '': o[i] = s[x++]; break;
				case ',': o[i] = null; break;
				case '<': o[i] = false; break;
				case '>': o[i] = true; break;
				case '*': o[i] = new Date(s[x++] - 0); break;
				case '[': x = decL(s, x); o[i] = s.o; break;
				case '{': x = decO(s, x); o[i] = s.o; break;
				case '=': o[i] = decRefs[s[x++]]; break;
				case 'NaN': o[i] = NaN; break;
				default: isNaN(o[i] = Number(v)) && Err.th('illegal number ' + Str.s(v));
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
		while (x >= s.length ? Err.th('} expected but end') : (p = s[x++]) !== '}')
			switch (v = s[x++]) {
				case '': o[p] = s[x++]; break;
				case ',': o[p] = null; break;
				case '<': o[p] = false; break;
				case '>': o[p] = true; break;
				case '*': o[p] = new Date(s[x++] - 0); break;
				case '[': x = decL(s, x); o[p] = s.o; break;
				case '{': x = decO(s, x); o[p] = s.o; break;
				case '=': o[p] = decRefs[s[x++]]; break;
				case 'NaN': o[p] = NaN; break;
				default: isNaN(o[p] = Number(v)) && Err.th('illegal number ' + Str.s(v));
			}
		decoded(s.o = o);
		return x;
	}
}
}
