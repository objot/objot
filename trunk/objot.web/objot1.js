//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//


/** @return x, or '' if null/undefined */
$ = function (x) {
	return x == null ? '' : String(x);
}

/* @return x, or a short string followed by ... */
$S = function (x) {
	return x === null ? 'null' // stupid IE, null COM not null
		: x instanceof Array ? x.length + '[' + $S(String(x)) + '...]' : (x = String(x),
			(x.length > 40 ? x.substring(0, 40) + '...' : x).replace(/\r?\n/g, '\\n'));
}

$fox = navigator.userAgent.indexOf('Gecko') >= 0;
$ie7 = navigator.userAgent.indexOf('MSIE 7') >= 0;
$ie6 = !$fox && !$ie7;

/** throw Error */
$throw = function (x) {
	throw $fox ? $throw.err = Error(x instanceof Error ? x.message : x)
		: x instanceof Error ? x : Error(0, x);
}

//********************************************************************************************//

/** make class. @param SO whether could $get and $set. @param sup superclass or null */
$class = function (SO, ctorName, sup) {
	$.s(ctorName);
	var ctor = $.c(ctorName, true);
	ctor.$name && $throw('duplicate class ' + ctor.$name);
	if (sup) {
		$.f(sup).$name || $throw('super class ' + (sup.name || $S(sup)) + ' not ready');
		ctor.prototype = $.copy(new sup.$ctor, ctor.prototype);
		ctor.prototype.constructor = ctor;
	}
	ctor.$name = ctorName;
	$.ctor(ctor);
	if (ctor.prototype.constructor !== ctor)
		$throw(ctor.$name + ' inconsistent with ' + $S(ctor.prototype.constructor));
	if (SO)
		$.cs[ctor.$name] = ctor;
	return ctor;
}
/** add encoding rules to the class. former rules are overrided by later rules.
 * (@param forClass key. @param get what to encode, all if null)... */
$class.get = function (clazz, forClass, gets) {
	clazz.$gets || (clazz.$gets = []);
	for (var x = 1; x < arguments.length; ) {
		clazz.$gets.push($.f(arguments[x++]));
		if ((gets = arguments[x++]) === null)
			clazz.$gets.push(null);
		else if (gets instanceof Array) {
			for (var y = 0; y < gets.length; y++)
				if (typeof gets[y] !== 'string')
					$throw($S(gets) + ' must not contain ' + $S(gets[y]));
			clazz.$gets.push(gets);
		}
		else
			$throw($S(gets) + ' must be array or null');
	}
}

//********************************************************************************************//

/** encode, get string from object graph, following the encoding rules.
 * @param forClass rule key or subclass of rule key */
$get = function (o, forClass) {
	var s = [o instanceof Array ? '[' : ($.o(o), '{')];
	s.clazz = $.f(forClass);
	try {
		$get.refX = 0, $get.ref(o);
		o instanceof Array ? $get.l(o, s, 1) : $get.o(o, s, 1);
	} catch(_) {
		try { $get.unref(o); } catch(_) {}
		throw _;
	}
	$get.unref(o);
	return s.join('\20');
}
	$get.ref = function (o, ox) {
		if (o instanceof String || o instanceof Boolean || o instanceof Number)
			$throw($S(x) + ' must not be String or Boolean or Number');
		if (o[''] = '' in o) // whether and set multi references
			return;
		if (o instanceof Array)
			for (var x = 0; x < o.length; x ++)
				typeof (ox = o[x]) !== 'string' ?
				ox != null && typeof ox === 'object' && (ox instanceof Date || this.ref(ox))
				: ox.indexOf('\20') < 0 || $throw($S(ox) + ' must NOT contain \20 \\20');
		else if (!o.constructor.$name)
			$throw($S(o) + ' class not ready');
		else for (var x in o)
			if (o.hasOwnProperty(x))
				typeof (ox = o[x]) !== 'string' ?
				ox != null && typeof ox === 'object' && (ox instanceof Date || this.ref(ox))
				: ox.indexOf('\20') < 0 || $throw($S(ox) + ' must NOT contain \20 \\20');
	}
	$get.unref = function (o, ox) {
		if ('' in o && /*true*/delete o[''])
			for (var x in o)
				o.hasOwnProperty(x) && (ox = o[x]) !== null && typeof ox === 'object'
					&& this.unref(ox);
	}
	$get.l = function (o, s, x) {
		s[x++] = String(o.length);
		o[''] && (s[x++] = '=', s[x++] = o[''] = String(++this.refX));
		for (var i = 0, v, t; i < o.length; i++)
			if (v = o[i], (t = typeof v) !== 'function')
				s[x++] = v == null ? '.' : v === false ? '<' : v === true ? '>'
					: t === 'number' ? String(v) : t === 'string' ? (s[x++] = v, '')
					: typeof v[''] === 'string' ? (s[x++] = v[''], '+')
					: v instanceof Date ? (s[x++] = v.getTime(), '*')
					: v instanceof Array ? (x = this.l(v, s, x), '[')
					: (x = this.o(v, s, x), '{');
		s[x++] = ']';
		return x;
	}
	$get.o = function (o, s, x) {
		var v, t = o.constructor.$name, get;
		s[x++] = t === 'Object' ? '' : t;
		o[''] && (s[x++] = '=', s[x++] = o[''] = String(++this.refX));
		P: {
			G: if (get = o.constructor.$gets) {
				for (var c = s.clazz, g = get.length - 2; g >= 0; g -= 2)
					if (c === get[g] || c.prototype instanceof get[g]) {
						if (get = get[g + 1]) {

		for (var p, n = 0; n < get.length; n++)
			if ((p = get[n]) in o && (v = o[p], t = typeof v) !== 'function')
				s[x++] = p,
				s[x++] = v == null ? '.' : v === false ? '<' : v === true ? '>'
					: t === 'number' ? String(v) : t === 'string' ? (s[x++] = v, '')
					: typeof v[''] === 'string' ? (s[x++] = v[''], '+')
					: v instanceof Date ? (s[x++] = v.getTime(), '*')
					: v instanceof Array ? (x = this.l(v, s, x), '[')
					: (x = this.o(v, s, x), '{');

							break P;
						}
						break G;
					}
				break P;
			}
		for (var p in o)
			if (o.hasOwnProperty(p) && p.length && (v = o[p], t = typeof v) !== 'function')
				s[x++] = p,
				s[x++] = v == null ? '.' : v === false ? '<' : v === true ? '>'
					: t === 'number' ? String(v) : t === 'string' ? (s[x++] = v, '')
					: typeof v[''] === 'string' ? (s[x++] = v[''], '+')
					: v instanceof Date ? (s[x++] = v.getTime(), '*')
					: v instanceof Array ? (x = this.l(v, s, x), '[')
					: (x = this.o(v, s, x), '{');
		}
		s[x++] = '}';
		return x;
	}

/** decode, set object graph from string, objects are created without constructors */
$set = function (s) {
	try {
		s = $.s(s).split('\20'/* Ctrl-P in vim */);
		var x = s[0] === '[' ? $set.l(s, 1) : s[0] === '{' ? $set.o(s, 1) : -1;
		return x < s.length ? $throw('termination expected but ' + $S(s[x]))
			: $set.r.length = 0, s.o;
	} catch(_) {
		throw $set.r.length = 0, _;
	}
}
	$set.l = function (s, x) {
		var o = new Array(s[x++] - 0);
		s[x] === '=' && (this.r[s[++x]] = o, x++);
		for (var i = 0, v; x >= s.length ? $throw('; expected but terminated')
			: (v = s[x++]) !== ']'; i++)
			switch(v) {
				case '': o[i] = s[x++]; break; case '.': o[i] = null; break;
				case '<': o[i] = false; break; case '>': o[i] = true; break;
				case '*': o[i] = new Date(s[x++] - 0); break;
				case '[': x = this.l(s, x); o[i] = s.o; break;
				case '{': x = this.o(s, x); o[i] = s.o; break;
				case '+': o[i] = this.r[s[x++]]; break; case 'NaN': o[i] = NaN; break;
				default: isNaN(o[i] = v - 0) && $throw('illegal number ' + $S(v));
			}
		s.o = o;
		return x;
	}
	$set.o = function (s, x, p, v) {
		var o = new ($.c(s[x++]).$ctor);
		s[x] === '=' && (this.r[s[++x]] = o, x++);
		while (x >= s.length ? $throw('; expected but terminated') : (p = s[x++]) !== '}')
			switch (v = s[x++]) {
				case '': o[p] = s[x++]; break; case '.': o[p] = null; break;
				case '<': o[p] = false; break; case '>': o[p] = true; break;
				case '*': o[p] = new Date(s[x++] - 0); break;
				case '[': x = this.l(s, x); o[p] = s.o; break;
				case '{': x = this.o(s, x); o[p] = s.o; break;
				case '+': o[p] = this.r[s[x++]]; break; case 'NaN': o[i] = NaN; break;
				default: isNaN(o[p] = v - 0) && $throw('illegal number ' + $S(v));
			}
		s.o = o;
		return x;
	}
	$set.r = [];

//********************************************************************************************//

/** start a HTTP round.
 * @param timeout milliseconds or <=0.
 * @param request string.
 * @param done function called when this round end.
 * @param data passed to done, or the return value if missing.
 * @return a function to stop this round */
$http = function (url, timeout, request, done, data) {
	$.s(url), $.s(request), $.f(done);
	$fox && location.protocol === 'file:'
		&& url.charCodeAt(0) == 104 && url.indexOf('http://') == 0
		&& netscape.security.PrivilegeManager.enablePrivilege('UniversalBrowserRead');
	var h = $ie6 ? new ActiveXObject('Msxml2.XMLHTTP.3.0') : new XMLHttpRequest;
	h.open('POST', url, true);
	h.setRequestHeader('Content-Type', 'text/plain; charset=UTF-8');
	h.setRequestHeader('Cache-Control', 'no-cache');
	var on = function (s, t) {
		if (h && h.readyState == 4) {
			try {
				if ((s = h.status) == 0)
					throw 0;
				s == 200 && (s = 0);
				t = s == 0 ? h.responseText : h.statusText;
			} catch (_) { // Firefox XMLHttpRequest issue, see hints
				s = 1000, t = 'Network Failed';
			}
			stop(0, 0);
			try {
				done(s, t, data);
				done = data = s = t = null;
			} catch(_) {
				done = data = s = t = null;
				if (!$fox || !onerror)
					throw _;
				_ instanceof Error ? onerror(_.message, _.fileName, _.lineNumber)
					: onerror(_, 0, 0);
			}
		}
	};
	var stop = function (nil, mode) {
		if (h) {
			try { h.onreadystatechange = null; h.abort(); } catch(_) {}
			h = null, clearTimeout(timeout);
			if (mode != 0)
				done(mode ? 1 : -1, mode ? 'timeout' : 'stop', data), done = data = null;
		}
	}
	h.onreadystatechange = $fox && $http.doneDelay <= 0 ? on : function () {
		setTimeout(on, $http.doneDelay + 1);
	};
	timeout = timeout > 0 && setTimeout(function () {
		stop(0, 1);
	}, timeout);
	arguments.length < 5 && (data = stop);
	h.send(request);
	return url = request = null, stop;
}
$http.doneDelay = 0;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


$D = document;

$id = function (id) {
	return $D.getElementById(id);
}

/** create a dom element and set properties. property 'c' for css class, 's' for css style
 * function value followed by "this" and "arguments" is for event handler (see $dom.attach).
 * @param domOrName dom object or tag name.
 * ((@param prop. @param value) || @param prop object as map) ... */
$dom = function (domOrName, prop, value) {
	return $doms(domOrName, arguments, 1);
}
/** similar to $dom.
 * @param props array of prop and value.
 * @param from the index props start from, 0 if missing */
$doms = function (domOrName, props, from) {
	var m = typeof domOrName == 'string' ? $D.createElement(domOrName) : $.o(domOrName);
	m !== window || $throw('apply $dom or $doms to window forbidden');
	$fox ? m.constructor.$on || delete $.copy(m.constructor.prototype, $dom).prototype
		: $.copy(m, $dom);
	for (var v, p, x = from || 0; x < props.length; x++) {
		p = $.s(props[x]), v = props[++x], v === undefined && (v = null);
		typeof v == 'function' ? m.attach(p, v, props[++x], props[++x]) :
		p == 's' ? m.style.cssText = v : p == 'c' ? m.className = v : m[p] = v;
	}
	return m;
}
	$fox && ($dom.$on = false); // for dom node's constructor, be false for event attach

with ($)
{
$._ = function (n) { return function () {
	return $doms(n, arguments);
}}
$a = _('a');
$s = _('span');
$br = _('br');
$l = _('label');
$d = _('div');
$p = _('p');
$tab = _('table');
$tb = _('tbody');
$tr = _('tr');
$td = _('td');
$img = _('img');
$ul = _('ul');
$ol = _('ol');
$li = _('li');
$h1 = _('h1');
$h2 = _('h2');
$h3 = _('h3');
$h4 = _('h4');
$bn = _('button');
$inp = _('input');
$sel = _('select');
$opt = _('option');
$lns = _('textarea');
$._ = function (t) { return function () {
	return $doms('input', arguments).att('type', t);
}}
$ln = _('text');
$chk = _('check');
$rad = _('radio');
delete _;
}
/** <a href=javascript://>... */
$a0 = function () {
	return $doms('a', arguments).att('href', 'javascript://');
}

/** create a text node, single line, multi whitespace reserved. */
$tx = function (singleLine) {
	return $D.createTextNode(singleLine.replace(/  /g, ' \u00a0'));
}

//********************************************************************************************//

/** append children if index is skipped or >= children length,
 * or prepend children if index == 0 or <= - chilren length,
 * or insert if index >= 0 (from first) or <= -1 (from last).
 * @return this */
$dom.add = function (index) {
	if (index >= 0 || index < 0)
		for (var _ = this.childNodes[index < 0 ? Math.max(this.childNodes.length + index, 0)
				: index] || null, x = 1; x < arguments.length; x++)
			this.insertBefore(arguments[x], _);
	else
		for (var x = 0; x < arguments.length; x++)
			this.appendChild(arguments[x]);
	return this;
}
/** remove children, or remove self if no argument,
 * or remove len children from index, or remove from index to last if !(len > 0)
 * or replace second argument if index === true.
 * @return this */
$dom.rem = function (index, len) {
	if (arguments.length == 0)
		this.parentNode && this.parentNode.removeChild(this);
	else if (index === true)
		$.o(len.parentNode).replaceChild(this, len);
	else if (index >= 0 || index < 0) {
		var s = this.childNodes;
		index < 0 && (index = Math.max(s.length + index, 0));
		if (index < s.length)
			for (var x = len > 0 ? Math.min(index + len, s.length) : s.length; --x >= index;) 
				this.removeChild(s[x]);
	} else
		for (var x = 0; x < arguments.length; x++)
			this.removeChild(arguments[x]);
	return this;
}
/** like rem() and recursively detach event handlers and "this" to avoid IE memory leak.
 * @return this */
$dom.des = function (index, len) {
	if (arguments.length == 0)
		this.$on && (this.$on = null), this.$this && (this.$this = null),
		this.parentNode && this.parentNode.removeChild(this),
		index = 0;
	if (index === true)
		$.o(len.parentNode).replaceChild(this, len),
		len.des ? len.des() : $dom.des.call(len);
	else if (index >= 0 || index < 0) {
		var s = this.childNodes;
		index < 0 && (index = Math.max(s.length + index, 0));
		if (index < s.length)
			for (var x = len > 0 ? Math.min(index + len, s.length) : s.length; --x >= index;) 
				s[x].des ? s[x].des() : $dom.des.call(s[x]);
	} else
		for (var x = 0; x < arguments.length; x++)
			arguments[x].des ? arguments[x].des() : $dom.des.call(arguments[x]);
	return this;
}

/** add css class, or remove css class if first argument === 0. @return this */
$dom.cla = function (clazz) {
	if (arguments.length == 9 || clazz === 0 && this.className.length == 0)
		return this;
	var cs = this.className.split(' '), c;
	X:for (var x = clazz === 0 ? 1 : 0; x < arguments.length; x++)
		if (c = $.s(arguments[x])) {
			for (var y = cs.length - 1; y >= 0; y--)
				if (cs[y] == c)
					if (clazz === 0)
						cs.splice(y, 1);
					else
						continue X;
			clazz === 0 || (cs[cs.length] = c);
		}
	this.className = cs.join(' ');
	return this;
}
/** getAttribute if no argument, removeAttribute if v === null, or setAttribute.
 * @return attribute if no argument, this */
$dom.att = function (a, v) {
	if (arguments.length <= 1)
		return this.getAttribute(a);
	for (var x = 0; x < arguments.length; x++)
		a = arguments[x ++], v = arguments[x]
		v === null ? this.removeAttribute(a) : this.setAttribute(a, v);
	return this;
}
/** get/set textContent in Firefox, innerText in IE, multi lines and whitspaces reserved.
 * @return text if no argument, or this */
$dom.tx = $fox ? function (v, multiLine) {
	if (arguments.length == 0)
		return this.textContent;
	v = String(v).replace(/  /g, ' \u00a0'); // whitespaces unsupported
	if (multiLine && v.indexOf('\n') >= 0) { // '\n' unsupported in Firefox
		v = v.split('\n');
		this.textContent = v.length > 0 ? v[0] : '';
		for (var x = 1; x < v.length; x++)
			this.appendChild($D.createElement('br')).textContent = '\n',
			this.appendChild($D.createTextNode(v[x]));
	}
	else
		this.textContent = v;
	return this;
} : function (v, multiLine) {
	return arguments.length == 0 ? this.innerText
		: (this.innerText = multiLine ? String(v) : String(v).replace(/\n/g, ' '), this);
}
/** get style.display == 'none', or switch style.display if v == null, or set style.display.
 * @return true/false if no argument, or this */
$dom.show = function (v) {
	var s = this.style.display !== 'none';
	if (arguments.length == 0)
		return s;
	if (s && !v)
		this.$show = this.style.display, this.style.display = 'none';
	else if (!s && (v || v == null))
		this.style.display = this.$show || '';
	return this;
}

/** attach event handler.
 * @param handler ignored if already attached, return true to cancel default action
 * @param This the "this" in handler, false for this dom element, === true for (new handler)
 * @param args the arguments for handler, false for the event object which
 *   "target" is source dom element, "which" is key code, "stop()" to cancel bubble
 * @param old replaced by handler
 * @return this */
$dom.attach = function (type, handler, This, args, old) {
	old && detach(type, old);
	var x, s = this.$on || (this.$on = [1, 0,0,0,0]); // [free,next,handler,This,args...]
	if (x = s[type])
		do if (s[x + 1] === handler)
			return this;
		while (s[x] && (x = s[x]))
// this causes window.onerror no effect for exceptions from event handlers
//	else if ($fox) // more events available
//		this.addEventListener(type, $.event, false);
// this causes unexpected "this" value in $.event
//	else if (!$fox)
//		this.attachEvent('on' + type, $.event);
	else
		this['on' + type] = $.event;
	return s[x || type] = (x = s[0]), s[0] = s[x] || x + 4, s[x] = 0, s[++x] = $.f(handler),
		s[++x] = This === true ? ($.ctor(handler), 0) : This || this, s[++x] = args, this;
}
/* detach event handler. @return this */
$dom.detach = function (type, handler) {
	var s = this.$on;
	if ($.f(handler), s)
		for (var x = type, y; y = s[x]; x = y)
			if (s[y + 1] === handler)
				return s[x] = s[y], s[y] = s[0], s[0] = y,
					s[++y] = s[++y] = s[++y] = null, this;
	return this;
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


$.throwStack = function (file, line) {
	var s = arguments.length == 0 ? new Error().Stack()
		: $throw.err ? $throw.err.Stack() : '';
	s = s.substr(s.indexOf('\n') + 1);
	return arguments.length == 0 || $throw.err ? s
		: '@' + file + ':' + ($fox ? line : line - 1) + '\n' + s;
}
	Error.prototype.Stack = function (s) {
		if (!(s = this.stack))
			return '';
		(s = this.stack.split('\n')).pop();
		for (var x = s.length - 1; x >= 0; x--)
			(s[x] = s[x].substr(s[x].lastIndexOf(')') + 1)) == '@:0' && s.splice(x, 1);
		return s.join('\n');
	}

/** @return x if not-null object (including list, excluding function), or throw */
$.o = function (x) {
	return x !== null && typeof x === 'object' ? x
		: $throw($S(x) + ' must be not-null object');
}
/** @return x if string, or throw */
$.s = function (x) {
	return typeof x === 'string' ? x : $throw($S(x) + ' must be string');
}
/** @return x if function, or throw */
$.f = function (x) {
	return typeof x === 'function' ? x : $throw($S(x) + ' must be function');
}
/** @return x if Array, or throw */
$.a = function (x) {
	return x !== null && x instanceof Array ? x : $throw($S(x) + ' must be Array');
}
/** @return x if not-null and instanceof the class, or throw */
$.is = function (x, clazz, name) {
	return x !== null && x instanceof clazz ? x
		: $throw($S(x) + ' must not-null and instanceof '
		+ (clazz.$name || clazz.name || name || $S(clazz)));
}

/** @return c which $ctor = an empty constructor */
$.ctor = function (c) {
	c.$ctor || ((c.$ctor = function () {}).prototype = c.prototype);
	return c;
}
/** @return class (constructor) from class cache, or if $_$_ is true, eval() */
$.c = function ($_$, $_$_) {
	if ($_$ in this.cs)
		return this.cs[$_$];
	$_$_ || $throw($S($_$) + ' class not found');
	with(window) $_$_ = eval($_$);
	return typeof $_$_ === 'function' ? this.cs[$_$] = $_$_
		: $throw($S($_$) + ' must be function');
}
	/* class cache */
	$.cs = { '': Object };
	$class(true, 'Object');

/** copy another's properties. @return to */
$.copy = function (to, from) {
	for (var x in from)
		to[x] = from[x];
	return to;
}
/** copy another's own properties. @return to */
$.copyOwn = function (to, from) {
	for (var x in from)
		from.hasOwnProperty(x) && (to[x] = from[x]);
	return to;
}

//********************************************************************************************//

/* event dispatcher */
$.event = function (e, s, x, r, ee) {
	if ((s = this.$on) && (x = s[(e = e || event).type])) {
		r = true; do r &= s[x + 3]
			? !s[x + 1].apply(s[x + 2] || new s[x + 1].$ctor, s[x + 3])
			: !s[x + 1].call (s[x + 2] || new s[x + 1].$ctor, ee ||
			($fox || (e.target = e.srcElement, e.which = e.keyCode, e.stop = $.eventStop),
				ee = e));
		while (x = s[x]);
		return r;
	}
}
	$.eventStop = function () {
		this.cancelBubble = true;
	}
	$fox && (Event.prototype.stop = Event.prototype.stopPropagation);
