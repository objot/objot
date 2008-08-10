//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.codec
{
import flash.utils.Dictionary;

import objot.util.Cast;
import objot.util.Class2;
import objot.util.Err;
import objot.util.Meta;
import objot.util.MetaArg;
import objot.util.Prop;


public class Rule
{
	public var func:Function;
	public var cla:Class;
	protected var encsn:Boolean;
	protected var encs:Dictionary;
	protected var encDsn:Boolean;
	protected var encDs:Dictionary;

	public function initFunc(f:Function):Rule
	{
		func = f;
		encsn = false;
		(encs = new Dictionary)[null] = [];
		encDsn = false;
		(encDs = new Dictionary)[null] = -1;
		return this;
	}

	public function initClass(c:Class):Rule
	{
		cla = c;
		encsn = false;
		(encs = new Dictionary)[null] = [];
		encDsn = false;
		(encDs = new Dictionary)[null] = -1;
		var d:Class2 = Class2.init(c), a0:Array, m:Meta, a:MetaArg, p:Prop;

		if ((m = d.metaz.EncDynamic))
			if (m.args.length)
				for each (a in encDsn = true, m.args)
					encDs[a.value] = 1;
			else
				encDs[null] = 1;
		if ((m = d.metaz.Enc) && m.args.length)
			for each (a in a0 = [], m.args)
				a0[a0.length] = a;

		for each (p in d.allProps)
			if (p.metaz.EncDynamic)
				throw new Error('EncDynamic on ' + d.name + ' ' + p.name + ' forbidden');
			else if ((m = p.metaz.Enc))
			{
				p.static && Err.th('encoding ' + d.name + ' static ' + p.name + ' forbidden'); 
				p.read || Err.th('encoding ' + d.name + ' writeonly ' + p.name + ' forbidden');
				if ( !a0 && m.args.length == 0)
					(encs[null] as Array).push(p.name);
				for each (a in a0)
					(encs[a.value] as Array || (encs[a.value] = [])).push(p.name);
				for each (a in m.args)
					(encs[a.value] as Array || (encs[a.value] = [])).push(p.name);
			}
		a0 = encs[null];
		for each (var e:Array in encs)
			e != a0 && (e.splice(e.length, 0, a0), encsn = true);
		return this;
	}

	/** @return [ property name ] */
	public function encProps(ruleKey:Object):Array
	{
		return (encsn
			&& encs[ruleKey is Class ? Class2.init(Class(ruleKey)).selfName : ruleKey]
			|| encs[null]) as Array;
	}

	public function encDynamic(ruleKey:Object):Boolean
	{
		return (encDsn
			&& encDs[ruleKey is Class ? Class2.init(Class(ruleKey)).selfName : ruleKey]
			|| encDs[null]) == 1;
	}
}
}
