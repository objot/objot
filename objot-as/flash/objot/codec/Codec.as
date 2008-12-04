//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.codec
{
import flash.utils.Dictionary;
import flash.utils.getDefinitionByName;
import flash.utils.getQualifiedClassName;

import objot.util.Class2;
import objot.util.Err;
import objot.util.String2;


public class Codec
{
	protected var rules:Dictionary;
	private var key:Object;
	private var refX:int;
	private var encRefs:Dictionary;

	public function Codec(sameRule:Codec = null)
	{
		rules = sameRule ? sameRule.rules : new Dictionary();
	}


	/**
	 * Get object by name, {} for '' by default
	 * 
	 * @param name may be ''
	 * @return object of class for the name
	 */
	protected function byName(name:String):Object
	{
		if (name == '')
			return {};
		return new (getDefinitionByName(name));
	}

	protected function decoded(o:Object):void
	{
	}

	/**
	 * Get object name, '' for Object and Function by default
	 * 
	 * @param c the object class
	 * @return could be ''
	 */
	protected function name(o:Object, c:Class):String
	{
		if (c == Object || c == null)
			return '';
		return getQualifiedClassName(c);
	}

	public function clazz(cf:Object):Rule
	{
		var r:Rule = rules[cf];
		if (r)
			return r;
		r = new Rule;
		cf is Function ? r.initFunc(Function(cf)) : r.initClass(Class(cf));
		return rules[cf] = r;
	}

	/** encode object graph to string, following the rules. */
	public function enc(o:Object, ruleKey:Object):String
	{
		var s:Array;
		if (o == null)
			s = [','];
		else if (o === false)
			s = ['<'];
		else if (o === true)
			s = ['>'];
		else if (o is Number)
			s = [String(o)];
		else if (o is String)
			s = ['', o];
		else if (o is Date)
			s = ['*', (o as Date).time];
		else
			try
			{
				s = [o is Array ? '[' : '{'];
				key = ruleKey, refX = 0, encRefs = new Dictionary();
				encRef(o) || Err.th(String2.s(o) +
					' must not be null, String, Boolean, Number, Class, Function, Dictionary');
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
		var p:String, v:Object;
		if (o is Array)
			for each (p in o as Array)
				if ((v = o[p]) is String)
					v.indexOf('\x10') < 0 || Err.th(String2.s(v) + ' must NOT contain \\x10');
				else
					encRef(v);
		else
		{
			var r:Rule = clazz(o.constructor);
			for each (p in r.encProps(key))
				if ((v = o[p]) is String)
					v.indexOf('\x10') < 0 || Err.th(String2.s(v) + ' must NOT contain \\x10');
				else
					encRef(v);
			if (r.encDynamic(key))
				for (p in o)
					if (o.hasOwnProperty(p))
					if ((v = o[p]) is String)
						v.indexOf('\x10') < 0
							|| Err.th(String2.s(v) + ' must NOT contain \\x10');
					else
						encRef(v);
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
				s[x++] = '*', s[x++] = (v as Date).time;
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
		s[x++] = name(o, o.constructor as Class);
		var s:Array, r:Rule, p:String, v:Object;
		if (encRefs[o])
			s[x++] = ':', s[x++] = encRefs[o] = String(++refX);
		r = clazz(o.constructor);
		for each (p in r.encProps(key))
			if (v = o[p], !(v is Class) && !(v is Function))
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
					s[x++] = '*', s[x++] = (v as Date).time;
				else if (encRefs[v] is String)
					s[x++] = '=', s[x++] = encRefs[v];
				else if (v is Array)
					s[x++] = '[', x = encL(v as Array, s, x);
				else
					s[x++] = '{', x = encO(v, s, x);
			}
		if (r.encDynamic(key))
			for (p in o)
				if (o.hasOwnProperty(p))
				if (v = o[p], !(v is Class) && !(v is Function))
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
						s[x++] = '*', s[x++] = (v as Date).time;
					else if (encRefs[v] is String)
						s[x++] = '=', s[x++] = encRefs[v];
					else if (v is Array)
						s[x++] = '[', x = encL(v as Array, s, x);
					else
						s[x++] = '{', x = encO(v, s, x);
				}
		s[x++] = '}';
		return x;
	}

	private var decRefs:Array = [];

	/** decode string to object graph. */
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
				default: isNaN(s.o = Number(v)) && Err.th('illegal number ' + String2.s(v));
			}
			if (x < s.length)
				Err.th('end expected but ' + String2.s(s[x]));
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
				default: isNaN(o[i] = Number(v)) && Err.th('illegal number ' + String2.s(v));
			}
		s.o = o;
		return x;
	}

	private function decO(s:Array, x:int):int {
		var o:Object = byName(s[x++]), p:Object, v:Object;
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
				default: isNaN(o[p] = Number(v)) && Err.th('illegal number ' + String2.s(v));
			}
		decoded(s.o = o);
		return x;
	}
}
}
