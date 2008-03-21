//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//


/** @return x, or '' if null/undefined */
$ = function (x) {
	return x == null ? '' : String(x);
}

/* @return x, or a short string followed by ... */
$S = function (x) {
	return x === null ? 'null' // stupid IE, null COM not null
	: x instanceof Array ? x.length +'['+ $S(String(x)) +'...]' : (x = "'" + String(x) + "'",
		(x.length > 40 ? x.substr(0, 40) + '...' : x).replace(/\r?\n/g, '\\n'));
}

/** false or version number */
$ie = navigator.userAgent.search('MSIE');
$ie = $ie >= 0 && navigator.userAgent.charAt($ie + 5) - 0;
/** Gecko|Opera|Safari */
$fos = !$ie;

/** throw Error */
$throw = function (x) {
	throw $fos ? $throw.err = Error(x instanceof Error ? x.message : x)
		: x instanceof Error ? x : Error(0, x);
}

//********************************************************************************************//

/** make class. @param SO whether could $enc and $dec. @param ctor name.
 * @param sup superclass or null. @param proto own props copied to ctor prototype */
$class = function (SO, ctor, sup, proto) {
	$.s(ctor);
	var c = $.c(ctor, true);
	c != Boolean && c != String && c != Number && c != Function
		|| $throw($S(c) + ' forbidden');
	c.$name && $throw('duplicate class ' + ctor);
	if (sup) {
		$.f(sup).$name || $throw('super class ' + (sup.name || $S(sup)) + ' not ready');
		c.prototype = $.copy(new sup.$ctor, c.prototype);
		c.prototype.constructor = c;
	}
	c.$name = ctor;
	$.ctor(c);
	if (c.prototype.constructor != c)
		$throw(ctor + ' inconsistent with ' + $S(c.prototype.constructor));
	proto && $.copy(c.prototype, proto);
	SO && ($.cs[ctor] = c);
	return c;
}
/** add encoding rules to the class. former rules are overrided by later rules.
 * (@param forClass key. @param encs what to encode, all if null)... */
$class.enc = function (clazz, forClass, encs) {
	clazz.$encs || (clazz.$encs = []);
	for (var x = 1; x < arguments.length; ) {
		clazz.$encs.push($.f(arguments[x++]));
		if ((encs = arguments[x++]) === null)
			clazz.$encs.push(null);
		else if (encs instanceof Array) {
			for (var y = 0; y < encs.length; y++)
				if (typeof encs[y] != 'string')
					$throw($S(encs) + ' must not contain ' + $S(encs[y]));
			clazz.$encs.push(encs);
		}
		else
			$throw($S(encs) + ' must be array or null');
	}
}

//********************************************************************************************//

/** encode object graph to string, following the encoding rules.
 * @param forClass rule key or subclass of rule key */
$enc = function (o, forClass) {
	var t = typeof o,
	s = o == null ? [','] : o === false ? ['<'] : o === true ? ['>']
		: t == 'number' ? [String(o)] : t == 'string' ? ['', o]
		: o instanceof Date ? ['*', o - 0] : 0;
	if (!s) {
		s = [o instanceof Array ? '[' : ($.o(o), '{')];
		s.clazz = $.f(forClass), s.refX = 0;
		try {
			$enc.ref(o), o instanceof Array ? $enc.l(o, s, 1) : $enc.o(o, s, 1);
		} catch(e) {
			try { $enc.unref(o); } catch(f) {}
			throw e;
		}
		$enc.unref(o);
	}
	return s.join('\x10');
}
	$enc.ref = function (o, ox) {
		if (o[''] = '' in o)
			return;
		if (o instanceof Array)
			for (var x = 0; x < o.length; x++)
				typeof (ox = o[x]) != 'string' ?
				ox instanceof Object && (ox instanceof Date || $enc.ref(ox)) // func
				: ox.indexOf('\x10') < 0 || $throw($S(ox) + ' must NOT contain \\x10');
		else if (!o.constructor.$name)
			$throw($S(o.constructor) + ' class not ready');
		else for (var x in o)
			if (o.hasOwnProperty(x))
				typeof (ox = o[x]) != 'string' ?
				ox == null || typeof ox != 'object' || ox instanceof Date || $enc.ref(ox)
				: ox.indexOf('\x10') < 0 || $throw($S(ox) + ' must NOT contain \\x10');
	}
	$enc.unref = function (o, ox) {
		delete o[''];
		if (o instanceof Array)
			for (var x = 0; x < o.length; x++)
				o && o[''] != null && $enc.unref(ox);
		else for (var x in o)
			o.hasOwnProperty(x) && (ox = o[x]) && ox[''] != null && $enc.unref(ox);
	}
	$enc.l = function (o, s, x) {
		s[x++] = String(o.length);
		o[''] && (s[x++] = ':', s[x++] = o[''] = String(++s.refX));
		for (var i = 0, v, t; i < o.length; i++)
			v = o[i], t = typeof v,
			s[x++] = v == null ? ',' : v === false ? '<' : v === true ? '>'
				: t == 'number' ? String(v) : t == 'string' ? (s[x++] = v, '')
				: typeof v[''] == 'string' ? (s[x++] = v[''], '=')
				: v instanceof Date ? (s[x++] = v - 0, '*')
				: v instanceof Array ? (x = $enc.l(v, s, x), '[')
				: (x = $enc.o(v, s, x), '{');
		s[x++] = ']';
		return x;
	}
	$enc.o = function (o, s, x) {
		var n = o.constructor, p, v, t, enc;
		s[x++] = n == Object ? '' : n.$name;
		o[''] && (s[x++] = ':', s[x++] = o[''] = String(++s.refX));
		P: {
			G: if (enc = o.constructor.$encs) {
				for (var c = s.clazz, g = enc.length - 2; g >= 0; g -= 2)
					if (c == enc[g] || c.prototype instanceof enc[g]) {
						if (enc = enc[g + 1]) {

		for (p, n = 0; n < enc.length; n++)
			if ((p = enc[n]) in o && (t = typeof (v = o[p])) != 'function')
				s[x++] = p,
				s[x++] = v == null ? ',' : v === false ? '<' : v === true ? '>'
					: t == 'number' ? String(v) : t == 'string' ? (s[x++] = v, '')
					: typeof v[''] == 'string' ? (s[x++] = v[''], '=')
					: v instanceof Date ? (s[x++] = v - 0, '*')
					: v instanceof Array ? (x = $enc.l(v, s, x), '[')
					: (x = $enc.o(v, s, x), '{');

							break P;
						}
						break G;
					}
				break P;
			}
		for (p in o)
			if (o.hasOwnProperty(p) && p.length && (t = typeof (v = o[p])) != 'function')
				s[x++] = p,
				s[x++] = v == null ? ',' : v === false ? '<' : v === true ? '>'
					: t == 'number' ? String(v) : t == 'string' ? (s[x++] = v, '')
					: typeof v[''] == 'string' ? (s[x++] = v[''], '=')
					: v instanceof Date ? (s[x++] = v - 0, '*')
					: v instanceof Array ? (x = $enc.l(v, s, x), '[')
					: (x = $enc.o(v, s, x), '{');
		}
		s[x++] = '}';
		return x;
	}

/** decode string to object graph, objects are created without constructors.
 * @param byName function(name) { return objectByName }, null for default
 * @param ok function(objectDecoded) {} */
$dec = function (s, byName, ok) {
	try {
		s = $.s(s).split('\x10');
		s.n = byName || $dec.n, s.ok = ok;
		var x = 1;
		switch (s[0]) {
			case '[': x = $dec.l(s, x); break; case '{': x = $dec.o(s, x); break;
			case '': s.o = s[x++]; break; case ',': s.o = null; break;
			case '<': s.o = false; break; case '>': s.o = true; break;
			case '*': s.o = new Date(s[x++] - 0); break;
			case 'NaN': s.o = NaN; break;
			default: isNaN(s.o = s[0] - 0) && $throw('illegal number ' + $S(s[0]));
		}
		return x < s.length ? $throw('termination expected but ' + $S(s[x]))
			: $dec.r.length = 0, s.o;
	} catch(_) {
		throw $dec.r.length = 0, _;
	}
}
	$dec.l = function (s, x) {
		var o = Array(s[x++] - 0);
		s[x] === ':' && ($dec.r[s[++x]] = o, x++);
		for (var i = 0, v; x >= s.length ? $throw('] expected but terminated')
			: (v = s[x++]) != ']'; i++)
			switch (v) {
				case '': o[i] = s[x++]; break; case ',': o[i] = null; break;
				case '<': o[i] = false; break; case '>': o[i] = true; break;
				case '*': o[i] = new Date(s[x++] - 0); break;
				case '[': x = $dec.l(s, x); o[i] = s.o; break;
				case '{': x = $dec.o(s, x); o[i] = s.o; break;
				case '=': o[i] = $dec.r[s[x++]]; break; case 'NaN': o[i] = NaN; break;
				default: isNaN(o[i] = v - 0) && $throw('illegal number ' + $S(v));
			}
		s.o = o;
		return x;
	}
	$dec.o = function (s, x, p, v) {
		var o = s.n(s[x++]);
		s[x] === ':' && ($dec.r[s[++x]] = o, x++);
		while (x >= s.length ? $throw('} expected but terminated') : (p = s[x++]) != '}')
			switch (v = s[x++]) {
				case '': o[p] = s[x++]; break; case ',': o[p] = null; break;
				case '<': o[p] = false; break; case '>': o[p] = true; break;
				case '*': o[p] = new Date(s[x++] - 0); break;
				case '[': x = $dec.l(s, x); o[p] = s.o; break;
				case '{': x = $dec.o(s, x); o[p] = s.o; break;
				case '=': o[p] = $dec.r[s[x++]]; break; case 'NaN': o[p] = NaN; break;
				default: isNaN(o[p] = v - 0) && $throw('illegal number ' + $S(v));
			}
		s.o = o, s.ok && s.ok(o);
		return x;
	}
	$dec.n = function (n) {
		return new (($.cs[n] || $throw($S(n) + ' class not found')).$ctor);
	}
	$dec.r = [];

//********************************************************************************************//

/** start a HTTP round.
 * @param timeout milliseconds or <=0.
 * @param request string.
 * @param done function called when this round end.
 * @param data passed to done, or the return value if missing.
 * @return a function to stop this round */
$http = function (url, timeout, request, done, data) {
	$.s(url), $.s(request), $.f(done);
	window.netscape && location.protocol == 'file:'
		&& url.charCodeAt(0) == 104 && url.indexOf('http://') == 0
		&& netscape.security.PrivilegeManager.enablePrivilege('UniversalBrowserRead');
	var h = $ie == 6 ? new ActiveXObject('Msxml2.XMLHTTP.3.0') : new XMLHttpRequest;
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
				if ($ie || !onerror) // TODO opera safari ?
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
	h.onreadystatechange = $fos && $http.doneDelay <= 0 ? on : function () {
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

/** create a dom element and set props. prop 'c' for css class, 's' for css style
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
	$fos ? m.constructor.$on || delete $.copy(m.constructor.prototype, $dom).prototype
		: $.copy(m, $dom);
	for (var v, p, x = from || 0; x < props.length; x++) {
		p = $.s(props[x]), v = props[++x], v === undefined && (v = null);
		typeof v == 'function' ? m.attach(p, v, props[++x], props[++x]) :
		p == 's' ? m.style.cssText = v : p == 'c' ? m.className = v : m[p] = v;
	}
	return m;
}
	$fos && ($dom.$on = false); // for dom node's constructor, be false for event attach

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
$th = _('th');
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
$._ = function (t, m) { return function () {
	return m = $doms('input', arguments), m.type = t, m;
}}
$ln = _('text');
$chk = _('checkbox');
$rad = _('radio');
delete _;
}
/** <a href=none:>... without action */
$a0 = function () {
	return $doms('a', arguments).attr('href', 'none:').attach('click', Boolean, window);
}

/** create a text node, single line, multi whitespace reserved. */
$tx = function (singleLine) {
	return $D.createTextNode(singleLine.replace(/  /g, ' \u00a0'));
}

//********************************************************************************************//

/** append children if index is skipped or <0 or >= children length,
 * or insert before index if index >= 0.
 * @return this */
$dom.add = function (index) {
	if (typeof index == 'number')
		for (var x = 1, m = this.childNodes[index] || null; x < arguments.length; x++)
			this.insertBefore(arguments[x], m);
	else
		for (var x = 0; x < arguments.length; x++)
			this.appendChild(arguments[x]);
	return this;
}
/** remove children, or remove self if no argument,
 * or remove len children from Math.max(index, 0), or remove to last if !(len > 0) 
 * or replaced by second argument if index === true.
 * @return this */
$dom.rem = function (index, len) {
	if (arguments.length == 0)
		this.parentNode && this.parentNode.removeChild(this);
	else if (index === true)
		$.o(this.parentNode).replaceChild(len, this);
	else if (typeof index == 'number') {
		var s = this.childNodes;
		index < 0 && (index = 0), len = len > 0 ? s[index + len] : null;
		for (var x = s[index], y; x != len; x = y)
			y = x.nextSibling, this.removeChild(x);
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
		$.o(this.parentNode).replaceChild(len, this),
		this.des ? this.des() : $dom.des.call(this);
	else if (typeof index == 'number') {
		var s = this.childNodes;
		index < 0 && (index = 0), len = len > 0 ? s[index + len] : null;
		for (var x = s[index], y; x != len; x = y)
			y = x.nextSibling, x.des ? x.des() : $dom.des.call(x);
	} else
		for (var x = 0, y; x < arguments.length; x++)
			(y = arguments[x]).des ? y.des() : $dom.des.call(y);
	return this;
}

/** add css class, or remove css class if first argument === 0. @return this */
$dom.cla = function (clazz) {
	if (arguments.length == 0 || clazz === 0 && this.className.length == 0)
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
$dom.attr = function (a, v) {
	if (arguments.length <= 1)
		return this.getAttribute(a, 2/*exact value in ie*/);
	for (var x = 0; x < arguments.length; x++)
		a = arguments[x ++], v = arguments[x]
		v === null ? this.removeAttribute(a) : this.setAttribute(a, v);
	return this;
}
/** get/set textContent in Firefox, innerText in IE, or get textarea.value, spaces reserved.
 * @param multiLine if reserved. @return text if no argument, or this */
$dom.tx = $fos ? function (v, multiLine) {
	if (this.tagName.toLowerCase() == 'textarea')
		return arguments.length == 0 ? this.value
			: (this.textContent = multiLine ? v : v.replace(/\n/g, ' '), this);
	if (arguments.length == 0)
		return this.textContent;
	v = String(v).replace(/  /g, '\u00a0 ');
	if (multiLine && v.indexOf('\n') >= 0) {
		v = v.split('\n');
		this.textContent = v.length > 0 ? v[0] : '';
		for (var x = 1; x < v.length; x++)
			this.appendChild($D.createElement('br')).textContent = '\n', // \n for getting
			this.appendChild($D.createTextNode(v[x]));
	}
	else
		this.textContent = v;
	return this;
} : function (v, multiLine) {
	return arguments.length == 0 ? this.tagName.toLowerCase() == 'textarea' ?
		this.value.replace(/\r\n/g, '\n') : this.innerText
		: (this.innerText = multiLine ? String(v) : String(v).replace(/\n/g, ' '), this);
}
/** get style.display != 'none', or set style.display, or switch style.display if v === 0.
 * @return true/false if no argument, or this */
$dom.show = function (v) {
	var s = this.style.display != 'none';
	if (arguments.length == 0)
		return s;
	if (s && !v)
		this.$show = this.style.display, this.style.display = 'none';
	else if (!s && (v || v === 0))
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
	old && this.detach(type, old);
	var x, s = this.$on || (this.$on = [1, 0,,,]); // [free,next,handler,This,args...]
	if (x = s[type])
		do if (s[x + 1] === handler)
			return this;
		while (s[x] && (x = s[x]))
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
		: '@' + file + ':' + ($fos ? line : line - 1) + '\n' + s;
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
	return x !== null && typeof x == 'object' ? x
		: $throw($S(x) + ' must be not-null object');
}
/** @return x if string, or throw */
$.s = function (x) {
	return typeof x == 'string' ? x : $throw($S(x) + ' must be string');
}
/** @return x if function, or throw */
$.f = function (x) {
	return typeof x == 'function' ? x : $throw($S(x) + ' must be function');
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
$.c = function ($_$, $_$_, $_$$) {
	if ($_$$ = $.cs[$_$])
		return $_$$;
	$_$_ = $_$_ ? eval($_$) : $throw($S($_$) + ' class not found');
	return typeof $_$_ == 'function' ? $.cs[$_$] = $_$_
		: $throw($S($_$) + ' must be function');
}
	/* class cache */
	$.cs = { '':Object }
	$class(true, 'Object');

/** copy another's own props. @return to */
$.copy = function (to, from) {
	for (var x in from)
		from.hasOwnProperty(x) && (to[x] = from[x]);
	return to;
}
/** copy all another's props. @return to */
$.copyAll = function (to, from) {
	for (var x in from)
		to[x] = from[x];
	return to;
}

//********************************************************************************************//

/* event dispatcher */
$.event = function (e, s, x, r, ee) {
	if ((s = this.$on) && (x = s[(e = e || event).type])) {
		do r = (s[x + 3]
			? s[x + 1].apply(s[x + 2] || new s[x + 1].$ctor, s[x + 3])
			: s[x + 1].call (s[x + 2] || new s[x + 1].$ctor, $fos ? e : ee ||
				(e.target = e.srcElement, e.which = e.keyCode, e.stop = $.eventStop, ee = e))
			) || r;
		while (x = s[x]);
		return !r;
	}
}
	$.eventStop = function () {
		this.cancelBubble = true;
	}
	$fos && (Event.prototype.stop = Event.prototype.stopPropagation);
