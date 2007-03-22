//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
if (window.$ === undefined) {


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


/* return x, or '' if null/undefined */
$ = function (x) {
	return x == null ? '' : String(x);
}

/* return x, or cached {} if null/undefined */
$$ = function (x) {
	return x == null ? $$.o : x;
}
	$$.o = {};

/* return x, or a short string followed by ... */
$S = function (x) {
	return x === null ? 'null' // stupid IE
		: x instanceof Array ? x.length + '[' + $S(String(x)) + '...]' : (x = String(x),
			(x.length > 40 ? x.substring(0, 40) + '...' : x).replace(/\r?\n/g, '\\n'));
}

$fox = navigator.userAgent.indexOf('Gecko') >= 0;
$ie7 = navigator.userAgent.indexOf('MSIE 7') >= 0;
$ie6 = !$fox && !$ie7;

$throw = function (x) {
	throw $fox ? $throw.err = Error(x instanceof Error ? x.message : x)
		: x instanceof Error ? x : Error(0, x);
}

//********************************************************************************************//

/* make class with super class by prototype and interfaces by copying prototype */
$class = function (ctorName, sup, interfaces) {
	$.s(ctorName);
	var ctor = $.c(ctorName, 1);
	ctor.Name !== ctorName && (ctor.Name = ctorName);
	ctor.classed && $throw('duplicate class ' + ctor.Name);
	if (sup) {
		$.f(sup).classed || $throw('super class ' + sup.Name + ' not ready');
		var c = function () {};
		c.prototype = sup.prototype;
		ctor.prototype = $.copy(new c(), ctor.prototype);
		ctor.prototype.constructor = ctor;
	}
	if (ctor.prototype.constructor !== ctor)
		$throw(ctor.Name + ' inconsistent with ' + $S(ctor.prototype.constructor));
	for (var x = 2; x < arguments.length; x++)
		$.copy(ctor.prototype, arguments[x].prototype);
	$.cs[ctor.Name] = ctor;
	ctor.classed = true;
}
$class.get = function (clazz, forClass, gets) {
	if (arguments.length > 1)
		clazz.$get = [], clazz.$gets = [];
	for (var x = 1; x < arguments.length; ) {
		clazz.$get.push($.f(arguments[x++]));
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

/* get string from object graph, with class and reference */
$get = function (o, forClass, onlyTree) {
	var s = [o instanceof Array ? '[' : ($.o(o), '/')];
	s.clazz = $.f(forClass);
	try {
		onlyTree || ($get.refX = 0, $get.ref(o));
		o instanceof Array ? $get.l(o, s, 1) : $get.o(o, s, 1);
	} catch(_) {
		try { onlyTree || $get.unref(o); } catch(_) {}
		throw _;
	}
	onlyTree || $get.unref(o);
	return s.join('\20');
}
	$get.ref = function (o, ox) {
		if (o instanceof String || o instanceof Boolean || o instanceof Number)
			$throw($S(x) + ' must not be String or Boolean or Number');
		if (o[''] = '' in o) // whether and set multi references
			return;
		if (o instanceof Array)
			for (var x = 0; x < o.length; x ++) ox = o[x],
				typeof ox !== 'string' ? ox != null && typeof ox === 'object' && this.ref(ox)
					: ox.indexOf('\20') < 0 || $throw($S(ox) + ' must NOT contain \20 \\20');
		else for (var x in o)
			if (o.hasOwnProperty(x)) ox = o[x],
				typeof ox !== 'string' ? ox != null && typeof ox === 'object' && this.ref(ox)
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
				s[x++] = v === null || v === undefined ? '.' : t === 'number' ? String(v)
					: v === false ? '<' : v === true ? '>' : t === 'string' ? (s[x++] = v, '')
					: typeof v[''] === 'string' ? (s[x++] = v[''], '+')
					: v instanceof Array ? (x = this.l(v, s, x), '[')
					: (x = this.o(v, s, x), '/');
		s[x++] = ';';
		return x;
	}
	$get.o = function (o, s, x) {
		var v, t = o.constructor.Name || 'Object', get;
		s[x++] = t === 'Object' ? '' : t;
		o[''] && (s[x++] = '=', s[x++] = o[''] = String(++this.refX));
		P: {
			G: if (get = o.constructor.$get) {
				for (var c = s.clazz, g = get.length - 1; g >= 0; g--)
					if (c === get[g] || c.prototype instanceof get[g]) {
						if (get = o.constructor.$gets[g]) {

		for (var p, n = 0; n < get.length; n++)
			if ((p = get[n], v = o[p], t = typeof v) !== 'function')
				s[x++] = p,
				s[x++] = v === null || v === undefined ? '.' : t === 'number' ? String(v)
					: v === false ? '<' : v === true ? '>' : t === 'string' ? (s[x++] = v, '')
					: typeof v[''] === 'string' ? (s[x++] = v[''], '+')
					: v instanceof Array ? (x = this.l(v, s, x), '[')
					: (x = this.o(v, s, x), '/');
	
							break P;
						}
						break G;
					}
				break P;
			}
		for (var p in o)
			if (o.hasOwnProperty(p) && p.length && (v = o[p], t = typeof v) !== 'function')
				s[x++] = p,
				s[x++] = v === null || v === undefined ? '.' : t === 'number' ? String(v)
					: v === false ? '<' : v === true ? '>' : t === 'string' ? (s[x++] = v, '')
					: typeof v[''] === 'string' ? (s[x++] = v[''], '+')
					: v instanceof Array ? (x = this.l(v, s, x), '[')
					: (x = this.o(v, s, x), '/');
		}
		s[x++] = ';';
		return x;
	}

/* set object graph from string, with class and reference */
$set = function (s) {
	try {
		s = $.s(s).split('\20'/* Ctrl-P in vim */);
		var x = s[0] === '[' ? $set.l(s, 1) : s[0] === '/' ? $set.o(s, 1) : -1;
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
			: (v = s[x++]) !== ';'; i++)
			switch(v) {
				case '': o[i] = s[x++]; break; case '.': o[i] = null; break;
				case '<': o[i] = false; break; case '>': o[i] = true; break;
				case '[': x = this.l(s, x); o[i] = s.o; break;
				case '/': x = this.o(s, x); o[i] = s.o; break;
				case '+': o[i] = this.r[s[x++]]; break; case 'NaN': o[i] = NaN; break;
				default: (o[i] = v - 0) != NaN || $throw('illegal number ' + $S(v));
			}
		s.o = o;
		return x;
	}
	$set.o = function (s, x, p, v) {
		var c = $.c(s[x++]), o = new c;
		s[x] === '=' && (this.r[s[++x]] = o, x++);
		while (x >= s.length ? $throw('; expected but terminated') : (p = s[x++]) !== ';')
			switch (v = s[x++]) {
				case '': o[p] = s[x++]; break; case '.': o[p] = null; break;
				case '<': o[p] = false; break; case '>': o[p] = true; break;
				case '[': x = this.l(s, x); o[p] = s.o; break;
				case '/': x = this.o(s, x); o[p] = s.o; break;
				case '+': o[p] = this.r[s[x++]]; break; case 'NaN': o[i] = NaN; break;
				default: (o[p] = v - 0) != NaN || $throw('illegal number ' + $S(v));
			}
		s.o = o;
		return x;
	}
	$set.r = [];

//********************************************************************************************//

$http = function (url, timeout, request, done, data) {
	$.s(url), $.s(request), $.f(done);
	$fox && location.protocol === 'file:'
		&& url.charCodeAt(0) == 104 && url.indexOf('http://') == 0
		&& netscape.security.PrivilegeManager.enablePrivilege('UniversalBrowserRead');
	var h = $ie6 ? new ActiveXObject('Msxml2.XMLHTTP.3.0') : new XMLHttpRequest;
	h.open('POST', url, true);
	h.setRequestHeader('Content-Type', 'application/octet-stream');
	h.setRequestHeader('Cache-Control', 'no-cache');
	var on = function (s, t) {
		if (h && h.readyState == 4) {
			try {
				if ((s = h.status) == 0)
					throw 0;
				s == 200 && (s = 0);
				t = s == 0 ? h.responseText : h.statusText;
			} catch (_) { // stupid Firefox XMLHttpRequest issue
				s = 9999, t = 'Network Failed';
			}
			close(0, 0);
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
	var close = function (nil, mode) {
		if (h) {
			try { h.onreadystatechange = null; h.abort(); } catch(_) {}
			h = null, clearTimeout(timeout);
			if (mode != 0)
				done(mode ? 1 : -1, mode ? 'timeout' : 'close', data), done = data = null;
		}
	}
	h.onreadystatechange = $ie6/*7?*/ || $http.doneDelay > 0 ? function () {
		setTimeout(on, $http.doneDelay + 1);
	} : on;
	timeout = timeout > 0 && setTimeout(function () {
		close(0, 1);
	}, timeout);
	arguments.length < 5 && (data = close);
	h.send(request);
	return url = request = null, close;
}
$http.doneDelay = 0;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


$D = document;

$id = function (id) {
	return $D.getElementById(id);
}

/* create a dom element, and set properties */
$dom = function (domOrName, x_, props_) {
	var m = typeof domOrName === 'string' ? $D.createElement(domOrName) : $.o(domOrName);
	!m.constructor ? $.copy(m, $dom) // ie6(7?)
		: m.constructor[''] || delete $.copy(m.constructor.prototype, $dom).prototype;
	var x = x_, props = props_;
	x >= 0 || (x = 1, props = arguments);
	for (var v, p; x < props.length; x++)
		if ((p = props[x]) == null)
			$throw('arguments[' + x + '] must not be null');
		else if (typeof p === 'string')
			if (typeof (v = props[++x]) === 'function')
				m.attach(p, v);
			else
				p == 's' ? m.style.cssText = v : p == 'c' ? m.className = v : m[p] = v;
		else for (var pp in p)
			if (typeof (v = p[pp]) === 'function')
				m.attach(pp, v);
			else
				pp == 's' ? m.style.cssText = v : pp == 'c' ? m.className = v : m[pp] = v;
	return m;
}
$this = function (dom, o) {
	return dom.$ = o, dom;
}
	$fox && ($dom[''] = false); // for dom node's constructor, be false for event attach

	eval(function (s1, f1, s2, f2) {
		for (var x in s1)
			window[x] = f1(s1[x]);
		for (var x in s2)
			window[x] = f2(s2[x]);
	})(
{ $a:'a', $s:'span', $br:'br', $l:'label', $d:'div', $p:'p',
  $tab:'table', $tb:'tbody', $tr:'tr', $td:'td',
  $img:'img', $ul:'ul', $ol:'ol', $li:'li', $h1:'h1', $h2:'h2', $h3:'h3', $h4:'h4',
  $bn:'button', $inp:'input', $sel:'select', $opt:'option', $lns:'textarea' },
		function (g) {
			return function () {
				return $dom(g, 0, arguments);
			}
		},
{ $ln:'text', $chk:'check', $rad:'radio' },
		function (ty) {
			return function () {
				return $dom('input', 0, arguments).att('type', ty);
			}
		}
	);
$a0 = function () {
	return $dom('a', 0, arguments).att('href', 'javascript://');
}
$tx = function (singleLine) {
	return $D.createTextNode(singleLine);
}

//********************************************************************************************//

/* append children if index is skipped or >= children length,
 * or prepend children if index is 0 or <= - chilren length,
 * or insert if index >= 0 (from first) or <= -1 (from last) */
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
/* remove children, or remove self if no argument,
 * or remove len children from index, or remove from index to last
 * or replace second argument if index is true */
$dom.rem = function (index, len) {
	if (arguments.length == 0)
		this.parentNode && this.parentNode.removeChild(this);
	else if (index === true)
		$.o(len.parentNode).replaceChild(this, len);
	else if (index >= 0 || index < 0) {
		var s = this.childNodes;
		index < 0 && (index = Math.max(s.length + index, 0));
		if (index < s.length)
			for (var x = index + (len > 0 ? len : s.length) - 1; x >= index; x--) 
				this.removeChild(s[x]);
	} else
		for (var x = 0; x < arguments.length; x++)
			this.removeChild(arguments[x]);
}
/* similar to rem(), recursively detach event handlers and $ and more for no IE memory leak */
$dom.des = function (index, len) {
	this !== window || $throw('destroy window forbidden');
	if (arguments.length == 0)
		this[''] && (this[''] = null), this.$ && (this.$ = null),
		this.parentNode && this.parentNode.removeChild(this),
		index = 0;
	if (index === true)
		$.o(len.parentNode).replaceChild(this, len),
		len.des ? len.des() : $dom.des.call(len);
	else if (index >= 0 || index < 0) {
		var s = this.childNodes;
		index < 0 && (index = Math.max(s.length + index, 0));
		if (index < s.length)
			for (var x = index + (len > 0 ? len : s.length) - 1; x >= index; x--) 
				s[x].des ? s[x].des() : $dom.des.call(s[x]);
	} else
		for (var x = 0; x < arguments.length; x++)
			arguments[x].des ? arguments[x].des() : $dom.des.call(arguments[x]);
	return this;
}

/* add css class, or remove css class if first argument is 0 */
$dom.cla = function (clazz) {
	if (arguments.length < 1 || clazz === 0 && this.className.length < 1)
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
/* getAttribute, setAttribute, removeAttribute */
$dom.att = function (a, v) {
	if (arguments.length <= 1)
		return this.getAttribute(a);
	for (var x = 0; x < arguments.length; x++)
		a = arguments[x ++], v = arguments[x]
		v === null ? this.removeAttribute(a) : this.setAttribute(a, v);
	return this;
}
/* get/set textContent in Firefox, innerText in IE */
$dom.tx = $fox ? function (v, multiLine) {
	if (arguments.length == 0)
		return this.textContent; // single line for stupid Firefox
	v = String(v).replace(/  /g, ' \u00a0'); // stupid Firefox, multi whitespaces unsupported
	if (multiLine && v.indexOf('\n') >= 0) { // stupid Firefox, '\n' unsupported
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
/* get/set style.display == 'none', or switch if argument is null */
$dom.show = function (v) {
	var s = this.style.display !== 'none';
	if (arguments.length == 0)
		return s;
	if (s && !v)
		this._disp = this.style.display, this.style.display = 'none';
	else if (!s && (v || v == null))
		this.style.display = this._disp || '';
	return this;
}

/* attach event handler which 'this' will be this node.$ if available or this node
 * if oldHandler, old is detached and handler is attached */
$dom.attach = function (ontype, handler, oldHandler) {
	if (oldHandler)
		detach(ontype, oldHandler);
	var x, t, s = this[''] || (this[''] = [1, 0, 0]); // [free, next, handler, ...]
	if (x = s[t = ontype.substr(2)])
		do if (s[x + 1] === handler)
			return handler;
		while (s[x] && (x = s[x]))
// this causes window.onerror no effect for exceptions from handler
//	else if ($fox) // more events available than this[ontype] = $.event 
//		this.addEventListener(t, $.event, false);
	else // 'this' in $.event works, but it doesn't if attachEvent
		this[ontype] = $.event;
	s[x || t] = (x = s[0]), s[0] = s[x] || x + 2, s[x] = 0, s[x + 1] = handler;
	return this;
}
/* detach event handler */
$dom.detach = function (ontype, handler) {
	var s = this[''];
	if (s)
		for (var x = ontype.substr(2), y; y = s[x]; x = y)
			if (s[y + 1] === handler)
				return s[x] = s[y], s[y] = s[0], s[0] = y, s[y + 1] = null, this;
	return this;
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


$.throwStack = function (file, line) {
	var s = arguments.length == 0 ? new Error().Stack()
		: $throw.err ? $throw.err.Stack() : '';
	s = s.substr(s.indexOf('\n') + 1);
	return arguments.length == 0 || $throw.err ? s
		: '@' + file + ':' + ($ie6/*7?*/ ? line - 1 : line) + '\n' + s;
}
	Error.prototype.Stack = function (s) {
		if (!(s = this.stack))
			return '';
		(s = this.stack.split('\n')).pop();
		for (var x = s.length - 1; x >= 0; x--)
			(s[x] = s[x].substr(s[x].lastIndexOf(')') + 1)) == '@:0' && s.splice(x, 1);
		return s.join('\n');
	}

/* must be not-null object (including list, excluding function) */
$.o = function (x) {
	return x !== null && typeof x === 'object' ? x
		: $throw($S(x) + ' must be not-null object');
}
/* must be string */
$.s = function (x) {
	return typeof x === 'string' ? x : $throw($S(x) + ' must be string');
}
/* must be function */
$.f = function (x) {
	return typeof x === 'function' ? x : $throw($S(x) + ' must be function');
}
/* must not-null and instanceof the clazz */
$.is = function (x, clazz, name) {
	return x !== null && x instanceof clazz ? x
		: $throw($S(x) + ' must not-null and instanceof '
		+ (clazz.Name || clazz.name || name || $S(clazz)));
}

/* get function from class cache, or eval */
$.c = function ($_$, _$_) {
	if ($_$ in this.cs)
		return this.cs[$_$];
	_$_ || $throw($S($_$) + ' class not found');
	with(window) _$_ = eval($_$);
	return typeof _$_ === 'function' ? this.cs[$_$] = _$_
		: $throw($S($_$) + ' must be function');
}
	/* class cache */
	$.cs = { '': Object, Object: Object }

/**
 * copy another's properties
 * @return to
 */
$.copy = function (to, from) {
	for (var x in from)
		to[x] = from[x];
	return to;
}
/**
 * copy another's own properties
 * @return to
 */
$.copyOwn = function (to, from) {
	for (var x in from)
		from.hasOwnProperty(x) && (to[x] = from[x]);
	return to;
}

//********************************************************************************************//

	/* event dispatcher */
	$.event = function (e, s, x, r, $) {
		if ((s = this['']) && (x = s[(e || (e = window.event)).type])) {
			$ = this.$ || this, e.target || (e.target = e.srcElement);
			r = 0; do
				r |= !s[x + 1].call($, e);
			while (x = s[x]);
			return !r;
		}
	}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//
}
