/*
 * Objot 11a
 *
 * Copyright 2007 Qianyan Cai
 * Under the terms of The GNU General Public License version 2
 */
if (window.$ === undefined) {

////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\

$ = function () {
}

/* return x, or a short string followed by ... */
$$ = function (x) {
	return typeof x === 'string' || typeof x === 'function' ? (x = String(x)
		, (x.length > 40 ? x.substring(0, 40) + '...' : x).replace(/\r?\n/g, '\\n'))
		: x instanceof Array ? x.length + '[' + $$(String(x)) + '...]' : x;
}

$fox = navigator.userAgent.indexOf('Gecko') >= 0;
$ie7 = navigator.userAgent.indexOf('MSIE 7') >= 0;
$ie6 = !$fox && !$ie7;

$throw = function (x) {
	throw $fox ? $throw.err = Error(x) : Error(0, x);
}

/* make class with super class by prototype and interfaces by copying prototype */
$class = function (ctorName, sup, interfaces) {
	$.s(ctorName);
	var ctor = $.c(ctorName, 1);
	ctor.name !== ctorName && (ctor.name = ctorName);
	ctor.classed && $throw('duplicate class ' + ctor.name);
	if (sup) {
		$.f(sup).classed || $throw('super class ' + sup.name + ' not ready');
		var c = function () {};
		c.prototype = sup.prototype;
		ctor.prototype = new c();
		ctor.prototype.constructor = ctor;
	}
	if (ctor.prototype.constructor !== ctor)
		$throw(ctor.name + ' inconsistent with ' + $$(ctor.prototype.constructor));
	for (var x = 2; x < arguments.length; x++)
		$.copy(ctor.prototype, arguments[x].prototype);
	$.cs[ctor.name] = ctor;
	ctor.classed = true;
}
$class.get = function (clazz, forClass, gets) {
	if (arguments.length > 1)
		clazz.$get = [], clazz.$gets = [];
	for (var x = 1; x < arguments.length; ) {
		typeof (forClass = arguments[x++]) === 'function' ? clazz.$get.push(forClass)
			: $throw($$(forClass) + ' must be function');
		if ((gets = arguments[x++]) === null)
			clazz.$gets.push(null);
		else if (gets instanceof Array) {
			for (var y = 0; y < gets.length; y++)
				if (typeof gets[y] !== 'string')
					$throw($$(gets) + ' must not contain ' + $$(gets[y]));
			clazz.$gets.push(gets);
		}
		else
			$throw($$(gets) + ' must be array or null');
	}
}

/* get string from object graph, with class and reference */
$get = function (o, forClass, onlyTree) {
	var s = [o instanceof Array ? '[' : '/'];
	s.clazz = typeof forClass === 'function' ? forClass
		: $throw($$(forClass) + ' must be function');
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
		$throw($$(x) + ' must be not-String-Boolean-Number object');
	if (o[''] = '' in o) // whether and set multi references
		return;
	if (o instanceof Array)
		for (var x = 0; x < o.length; x ++) ox = o[x],
			typeof ox !== 'string' ? ox != null && typeof ox === 'object' && this.ref(ox)
				: ox.indexOf('\20') < 0 || $throw($$(ox) + ' must NOT contain \20 \\20');
	else for (var x in o)
		if (o.hasOwnProperty(x)) ox = o[x],
			typeof ox !== 'string' ? ox != null && typeof ox === 'object' && this.ref(ox)
				: ox.indexOf('\20') < 0 || $throw($$(ox) + ' must NOT contain \20 \\20');
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
	var v, t = o.constructor.name || 'Object', get;
	s[x++] = t === 'Object' ? '' : t;
	o[''] && (s[x++] = '=', s[x++] = o[''] = String(++this.refX));
	G: {
		if (get = o.constructor.$get) {
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

						break G;
					}
					break;
				}
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
		return x < s.length ? $throw('termination expected but ' + $$(s[x]))
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
			default: (o[i] = v - 0) != NaN || $throw('illegal number ' + $$(v));
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
			default: (o[p] = v - 0) != NaN || $throw('illegal number ' + $$(v));
		}
	c === Error && (o.description = o.message);
	s.o = o;
	return x;
}
$set.r = [];


$http = function (url, data, timeout, onOk, onOther) {
	if ($fox && location.protocol === 'file:'
		&& url.charCodeAt(0) == 104 && url.indexOf('http://') == 0)
		netscape.security.PrivilegeManager.enablePrivilege('UniversalBrowserRead');
	var h = $ie6 ? new ActiveXObject('Microsoft.XMLHTTP') : new XMLHttpRequest;
	h.open('POST', url, true);
	h.setRequestHeader('Content-Type', 'application/octet-stream');
	h.setRequestHeader('Cache-Control', 'no-cache');
	h.onreadystatechange = function () {
		if (h && h.readyState === 4)
			try {
				if (timeout > 0)
					clearTimeout(timeout);
				if (h.status === 200 || h.status === 0 &&
						url.charCodeAt(0) == 102 && url.indexOf('file://') == 0)
					onOk(h.status, h.responseText, h);
				else
					onOther(h.status, h);
				h.abort(), h = null;
			} catch(_) {
				try { h.abort(); } catch(_) {}
				throw h = null, _;
			}
	};
	if (timeout > 0)
		timeout = setTimeout(function () {
			if (h)
				try {
					onOther(-1, h);
					h.abort(), h = null;
				} catch(_) {
					try { h.abort(); } catch(_) {}
					throw h = null, _;
				}
		}, timeout);
	h.send(data != null ? data : '');
	return h;
}


////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\

$d = document;

$id = function (id) {
	return $d.getElementById(id);
}

/* create a dom element, and set properties */
$tag = function (tagName, x, props) {
	var g = $d.createElement(tagName);
	g.constructor ? g.constructor[''] || $.copy(g.constructor.prototype, $dom)
		: $.copy(g, $dom);
	x >= 0 || (x = 1, props = arguments);
	for (var v, p; x < props.length; x++)
		if (typeof (p = props[x]) === 'string')
			if (typeof (v = props[++x]) === 'function')
				g.attach(p, v);
			else
				p === 'style' ? g.style.cssText = v : g[p] = v;
		else for (var pp in p)
			if (typeof (v = p[pp]) === 'function')
				g.attach(pp, v);
			else
				pp === 'style' ? g.style.cssText = v : g[pp] = v;
	return g;
}
$tag.s1 = {
	$div:'div', $tab:'table', $tr:'tr', $td:'td', $inp:'input', $sel:'select', $opt:'option',
	$lines:'textarea'
}
$tag.s2 = { $button:'button', $submit:'submit', $line:'text', $check:'check', $radio:'radio'
}
eval(function (f1, f2) {
	for (var x in $tag.s1)
		window[x] = f1($tag.s1[x]);
	for (var x in $tag.s2)
		window[x] = f2($tag.s2[x]);
})(function (g) {
	return function () {
		return $tag(g, 0, arguments);
	}
}, function (ty) {
	return function () {
		return $tag('input', 0, arguments).attr('type', ty);
	}
});

/* something added into dom element created by $tag */
$dom = {
	/* appendChild */
	add: function (child) {
		return this.appendChild(child), this;
	},
	/* removeChild */
	rem: function (child) {
		return this.removeChild(child), this;
	},
	/* getAttribute, setAttribute, removeAttribute */
	attr: function (a, v) {
		return v === undefined ? this.getAttribute(a)
			: (v === null ? this.removeAttribute(a) : this.setAttribute(a, v), this);
	},
	/* get/set textContent for Firefox, innerText for IE */
	text: $fox ? function (v) {
		return v === undefined ? this.textContent : (this.textContent = v, this);
	} : function (v) {
		return v === undefined ? this.innerText : (this.innerText = v, this);
	},
	/* get/set style.display == 'none' */
	disp: function (v) {
		var s = this.style, d = s.display !== 'none';
		return v === undefined ? d : !v !== d ? this
			: (s.display = v ? this._disp || '' : (this._disp = s.display, 'none'), this);
	},
	/* get/set style.cssFloat for Firefox, style.styleFloat for IE */
	Float: $fox ? function (v) {
		return v === undefined ? this.style.cssFloat : (this.style.cssFloat = v, this);
	} : function (v) {
		return v === undefined ? this.style.styleFloat : (this.style.styleFloat = v, this);
	},
	/* get/set style.opacity for Firefox, style.filter for IE */
	opacity: $fox ? function (v) {
		return v === undefined ? this.style.opacity
			: (this.style.opacity = v < 1 ? v : '', this);
	} : function (v) {
		var s = this.style, f = s.filter;
		return v === undefined ? f ? f.match(/opacity=([^)]*)/)[1] /100 : 1
			: (s.zoom = 1, s.filter = v >= 1 ? f.replace(/alpha\([^)]*\)/g, '')
				: f.replace(/alpha\([^)]*\)/g, 'alpha(opacity=' + v * 100 + ')'), this);
	},
	/* attach event handler */
	attach: function (ontype, handler) {
		var x, t, s = m[''] || (m[''] = [1, 0, 9]); // [free, next, handler, ... ]
		if (x = s[t = ontype.substr(2)])
			do if (s[x + 1] === handler)
				return handler;
			while (s[x] && (x = s[x]))
		else
			$fox ? m.addEventListener(t, $.event, false) : m.attachEvent(ontype, $.event);
		s[x || t] = x = s[0], s[0] = s[x] || x + 2, s[x] = 0, s[x + 1] = handler;
		return this;
	},
	/* detach event handler */
	detach: function (ontype, handler) {
		if (s = m[''])
			for (var x = ontype.substr(2), y; y = s[x]; x = y)
				if (s[y + 1] !== handler)
					return s[x] = s[y], s[y] = s[0], s[0] = y, s[y + 1] = null, this;
		return this;
	},
	/* detach event handlers and some references for no IE memory leak, nothing for Firefox */
	noleak: $ie6 ? function (m) {
		(m[''] && (m[''] =null), m.$ && (m.$ =null), m._ && (m._ =null), m.o && (m.o =null));
		for (m = m.firstChild; m !== null; m = m.nextSibling)
			$noleak(m);
		return this;
	} : function () {
		return this;
	}
}
if ($fox)
	$dom[''] = true;


////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\

$.alert = window.alert;
/* alert multi lines */
window.alert = function (s) {
	for (var x = 1; x < arguments.length; x++)
		s += '\n' + arguments[x];
	return $fox ? $.alert.call(window, s) : $.alert(s), s;
}

if ($fox)
	$.throwStack = function (file, line) {
		if (! $throw.err)
			return file + ':' + line;
		var s = $throw.err.stack;
		s = s.substr(s.indexOf('\n') + 1);
		return s.substr(s.indexOf('\n') + 1);
	}

/* must be not-null object (including list, excluding function) */
$.o = function (x) {
	return x !== null && typeof x === 'object' ? x
		: $throw($$(x) + ' must be not-null object');
}
/* must be string */
$.s = function (x) {
	return typeof x === 'string' ? x : $throw($$(x) + ' must be string');
}
/* must be function */
$.f = function (x) {
	return typeof x === 'function' ? x : $throw($$(x) + ' must be function');
}

/* get function from class cache, or eval */
$.c = function ($_$, _$_) {
	if ($_$ in this.cs)
		return this.cs[$_$];
	_$_ || $throw($$($_$) + ' class not found');
	with(window) _$_ = eval($_$);
	return typeof _$_ === 'function' ? this.cs[$_$] = _$_
		: $throw($$($_$) + ' must be function');
}
/* class cache */
$.cs = { '': Object, Object: Object, Error:Error }

/* copy another's properties */
$.copy = function (to, from) {
	for (var x in from)
		to[x] = from[x];
	return to;
}
/* copy another's own properties */
$.copyOwn = function (to, from) {
	for (var x in from)
		from.hasOwnProperty(x) && (to[x] = from[x]);
	return to;
}

/* event dispatcher */
$.event = function (e, s, x) {
	if ((s = this['']) && (x = s[e.type])) {
		e = e;
		var r = 0;
		do r |= !s[x + 1].call(this, e);
		while (x = s[x]);
		return !r;
	}
}


////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\

// hints
//
// && || ! ? if(x), 1 '0' [] are true, 0 NaN '' null undefined are false
//   do NOT use x == true/false, sometimes String(x) sometimes not
//
// for IE 6(7?), event handler codes may need try { ... } finally {}
//   otherwise the finally { ... } inside the codes may be ignored.
//
// String(x) convert x to string (not String) unless x is already string
//
}
