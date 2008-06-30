//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//


/** @return x, or '' if null/undefined */
$ = function (x) {
	return x == null ? '' : String(x)
}

/* @return x, or a short string followed by ... */
$S = function (x) {
	return x === null ? 'null' // stupid IE, null COM not null
	: x instanceof Array ? x.length +'['+ $S(String(x)) +'...]' : (x = "'" + String(x) + "'",
		(x.length > 40 ? x.substr(0, 40) + '...' : x).replace(/\r?\n/g, '\\n'))
}

/** false or version number */
$ie = navigator.userAgent.search('MSIE')
$ie = $ie >= 0 && +navigator.userAgent.charAt($ie + 5)
/** Gecko|Opera|Safari */
$fos = !$ie

/** throw Error */
$throw = function (x) {
	throw $fos ? $throw.err = Error(x instanceof Error ? x.message : x)
		: x instanceof Error ? x : Error(0, x)
}

/** make class. @param SO if could be $enc and $dec. @param ctor name
 * @param sup superclass or null. @param proto own props copied to ctor prototype */
$class = function (SO, ctor, sup, proto) {
	$.s(ctor)
	var c = $.c(ctor, true)
	c != Boolean && c != String && c != Number && c != Function
		|| $throw($S(c) + ' forbidden')
	c.$name && $throw('duplicate class ' + ctor)
	if (sup) {
		$.f(sup).$name || $throw('super class ' + (sup.name || $S(sup)) + ' not ready')
		c.prototype = $.copy(new sup.$ctor, c.prototype)
		c.prototype.constructor = c
	}
	$.ctor(c).$name = ctor
	if (c.prototype.constructor != c)
		$throw(ctor + ' inconsistent with ' + $S(c.prototype.constructor))
	proto && $.copy(c.prototype, proto)
	SO && ($.cs[ctor] = c)
	return c
}
/** add encoding rules to the class. former rules are overrided by later rules
 * (@param forClass key. @param encs what to encode, all if null)... */
$class.enc = function (clazz, forClass, encs) {
	clazz.$encs || (clazz.$encs = [])
	for (var x = 1; x < arguments.length; ) {
		clazz.$encs.push($.f(arguments[x++]))
		if ((encs = arguments[x++]) === null)
			clazz.$encs.push(null)
		else if (encs instanceof Array) {
			for (var y = 0; y < encs.length; y++)
				if (typeof encs[y] != 'string')
					$throw($S(encs) + ' must not contain ' + $S(encs[y]))
			clazz.$encs.push(encs)
		}
		else
			$throw($S(encs) + ' must be array or null')
	}
}

//********************************************************************************************//

/** encode object graph to string, following the encoding rules
 * @param forClass rule key or subclass of rule key */
$enc = function (o, forClass) {
	var t = typeof o,
	s = o == null ? [','] : o === false ? ['<'] : o === true ? ['>']
		: t == 'number' ? [String(o)] : t == 'string' ? ['', o]
		: o instanceof Date ? ['*', +o] : 0
	if (!s) {
		s = [o instanceof Array ? '[' : ($.o(o), '{')]
		s.clazz = $.f(forClass), s.refX = 0
		try {
			$enc.ref(o), o instanceof Array ? $enc.l(o, s, 1) : $enc.o(o, s, 1)
		} catch(e) {
			try { $enc.unref(o) } catch(f) {}
			throw e
		}
		$enc.unref(o)
	}
	return s.join('\x10')
}
	$enc.ref = function (o, ox) {
		if (o[''] = '' in o)
			return
		if (o instanceof Array)
			for (var x = 0; x < o.length; x++)
				typeof (ox = o[x]) != 'string' ?
				ox instanceof Object && (ox instanceof Date || $enc.ref(ox)) // func
				: ox.indexOf('\x10') < 0 || $throw($S(ox) + ' must NOT contain \\x10')
		else if (!o.constructor.$name)
			$throw($S(o.constructor) + ' class not ready')
		else for (var x in o)
			if (o.hasOwnProperty(x))
				typeof (ox = o[x]) != 'string' ?
				ox == null || typeof ox != 'object' || ox instanceof Date || $enc.ref(ox)
				: ox.indexOf('\x10') < 0 || $throw($S(ox) + ' must NOT contain \\x10')
	}
	$enc.unref = function (o, ox) {
		delete o['']
		if (o instanceof Array)
			for (var x = 0; x < o.length; x++)
				o && o[''] != null && $enc.unref(ox)
		else for (var x in o)
			o.hasOwnProperty(x) && (ox = o[x]) && ox[''] != null && $enc.unref(ox)
	}
	$enc.l = function (o, s, x) {
		s[x++] = String(o.length)
		o[''] && (s[x++] = ':', s[x++] = o[''] = String(++s.refX))
		for (var i = 0, v, t; i < o.length; i++)
			v = o[i], t = typeof v,
			s[x++] = v == null ? ',' : v === false ? '<' : v === true ? '>'
				: t == 'number' ? String(v) : t == 'string' ? (s[x++] = v, '')
				: typeof v[''] == 'string' ? (s[x++] = v[''], '=')
				: v instanceof Date ? (s[x++] = +v, '*')
				: v instanceof Array ? (x = $enc.l(v, s, x), '[')
				: (x = $enc.o(v, s, x), '{')
		s[x++] = ']'
		return x
	}
	$enc.o = function (o, s, x) {
		var n = o.constructor, p, v, t, enc
		s[x++] = n == Object ? '' : n.$name
		o[''] && (s[x++] = ':', s[x++] = o[''] = String(++s.refX))
		P: {
			if (enc = o.constructor.$encs)
				for (var c = s.clazz, g = enc.length - 2; g >= 0; g -= 2)
					if (c == enc[g] || c.prototype instanceof enc[g]) {
						if (enc = enc[g + 1]) {

		for (p, n = 0; n < enc.length; n++)
			if ((p = enc[n]) in o && (t = typeof (v = o[p])) != 'function')
				s[x++] = p,
				s[x++] = v == null ? ',' : v === false ? '<' : v === true ? '>'
					: t == 'number' ? String(v) : t == 'string' ? (s[x++] = v, '')
					: typeof v[''] == 'string' ? (s[x++] = v[''], '=')
					: v instanceof Date ? (s[x++] = +v, '*')
					: v instanceof Array ? (x = $enc.l(v, s, x), '[')
					: (x = $enc.o(v, s, x), '{')

							break P
						}
						break
					}
		for (p in o)
			if (o.hasOwnProperty(p) && p.length && (t = typeof (v = o[p])) != 'function')
				s[x++] = p,
				s[x++] = v == null ? ',' : v === false ? '<' : v === true ? '>'
					: t == 'number' ? String(v) : t == 'string' ? (s[x++] = v, '')
					: typeof v[''] == 'string' ? (s[x++] = v[''], '=')
					: v instanceof Date ? (s[x++] = +v, '*')
					: v instanceof Array ? (x = $enc.l(v, s, x), '[')
					: (x = $enc.o(v, s, x), '{')
		}
		s[x++] = '}'
		return x
	}

/** decode string to object graph, objects are created without ctors
 * @param byName function(name) { return objectByName }, null for default
 * @param ok function(objectDecoded) {} */
$dec = function (s, byName, ok) {
	try {
		s = $.s(s).split('\x10')
		s.n = byName || $dec.n, s.ok = ok
		var x = 1
		switch (s[0]) {
			case '[': x = $dec.l(s, x); break; case '{': x = $dec.o(s, x); break
			case '': s.o = s[x++]; break; case ',': s.o = null; break
			case '<': s.o = false; break; case '>': s.o = true; break
			case '*': s.o = new Date(+s[x++]); break
			case 'NaN': s.o = NaN; break
			default: isNaN(s.o = +s[0]) && $throw('illegal number ' + $S(s[0]))
		}
		return x < s.length ? $throw('end expected but ' + $S(s[x]))
			: x > s.length ? $throw('end unexpected') : $dec.r.length = 0, s.o
	} catch(_) {
		throw $dec.r.length = 0, _
	}
}
	$dec.l = function (s, x) {
		var o = Array(+s[x++])
		s[x] === ':' && ($dec.r[s[++x]] = o, x++)
		for (var i = 0, v; x >= s.length ? $throw('] expected but end')
			: (v = s[x++]) != ']'; i++)
			switch (v) {
				case '': o[i] = s[x++]; break; case ',': o[i] = null; break
				case '<': o[i] = false; break; case '>': o[i] = true; break
				case '*': o[i] = new Date(+s[x++]); break
				case '[': x = $dec.l(s, x); o[i] = s.o; break
				case '{': x = $dec.o(s, x); o[i] = s.o; break
				case '=': o[i] = $dec.r[s[x++]]; break; case 'NaN': o[i] = NaN; break
				default: isNaN(o[i] = +v) && $throw('illegal number ' + $S(v))
			}
		s.o = o
		return x
	}
	$dec.o = function (s, x, p, v) {
		var o = s.n(s[x++])
		s[x] === ':' && ($dec.r[s[++x]] = o, x++)
		while (x >= s.length ? $throw('} expected but end') : (p = s[x++]) != '}')
			switch (v = s[x++]) {
				case '': o[p] = s[x++]; break; case ',': o[p] = null; break
				case '<': o[p] = false; break; case '>': o[p] = true; break
				case '*': o[p] = new Date(+s[x++]); break
				case '[': x = $dec.l(s, x); o[p] = s.o; break
				case '{': x = $dec.o(s, x); o[p] = s.o; break
				case '=': o[p] = $dec.r[s[x++]]; break; case 'NaN': o[p] = NaN; break
				default: isNaN(o[p] = +v) && $throw('illegal number ' + $S(v))
			}
		s.o = o, s.ok && s.ok(o)
		return x
	}
	$dec.n = function (n) {
		return new (($.cs[n] || $throw($S(n) + ' class not found')).$ctor)
	}
	$dec.r = []

//********************************************************************************************//

/** start a HTTP round
 * @param time timeout ms or <=0
 * @param req string
 * @param done function called when this round ends
 * @param data passed to done, or this return value if undefined
 * @return a function to stop this round */
$http = function (url, time, req, done, data) {
	$.s(url), $.s(req), $.f(done)
	var r = $ie == 6 ? new ActiveXObject('Msxml2.XMLHTTP.3.0') : new XMLHttpRequest
	r.open('POST', url, true)
	r.setRequestHeader('Content-Type', 'text/plain; charset=UTF-8')
	r.setRequestHeader('Cache-Control', 'no-cache')
	r.onreadystatechange = function () {
		r && r.readyState == 4 && setTimeout(on, $http.doneDelay) // must timeout on IE
	}
	function on() {
		if (!r) return
		try { var s = r.status, t } catch (_) {} // Firefox issue
		s = s == 200 ? (t = r.responseText) ? 0 : 1000 : s || 1000
		stop(on, s, s ? s == 1000 ? 'Network Failure' : r.statusText : t)
	}
	function stop(o, a, b) {
		if (!r) return
		try { r.onreadystatechange = null, r.abort() } catch(_) {}
		r = null, clearTimeout(time)
		try {
			done(o == on ? a : -1, o == on ? b : 'stop', data), done = data = null
		} catch(_) {
			done = data = null
			if ($ie || !onerror) // TODO opera safari ?
				throw _
			_ instanceof Error ? onerror(_.message, _.fileName, _.lineNumber)
				: onerror(_, 0, 0)
		}
	}
	time = time > 0 && setTimeout(function () { stop(on, 1, 'timeout') }, time)
	data === undefined && (data = stop)
	return r.send(req), url = req = null, stop
}
$http.doneDelay = 0


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


$D = document

$id = function (id) {
	return $D.getElementById(id)
}

/** create a dom element and set props. prop 'c' for css class, 's' for css style
 * function value followed by "this" and "arguments" is for event handler (see $dom.attach)
 * @param domOrName dom object or tag name. (@param prop. @param value)... */
$dom = function (domOrName, prop, value) {
	return $doms(domOrName, arguments, 1)
}
/** similar to $dom
 * @param props array of prop and value
 * @param from the index props start from, 0 if missing */
$doms = function (domOrName, props, from) {
	var m = typeof domOrName == 'string' ? $D.createElement(domOrName) : $.o(domOrName)
	m !== window || $throw('apply $dom or $doms to window forbidden')
	$fos ? m.constructor.$on || delete $.copy(m.constructor.prototype, $dom).prototype
		: $.copy(m, $dom)
	for (var v, p, x = from || 0; x < props.length; x++) {
		p = $.s(props[x]), v = props[++x], v === undefined && (v = null)
		typeof v == 'function' ? m.attach(p, v, props[++x], props[++x]) :
		p == 's' ? m.style.cssText = v : p == 'c' ? m.className = v : m[p] = v
	}
	return m
}
	$fos && ($dom.$on = false) // for dom node's constructor, be false for event attach

with ($)
{
$._ = function (n) { return function () {
	return $doms(n, arguments)
}}
$a = _('a')
$s = _('span')
$br = _('br')
$l = _('label')
$d = _('div')
$p = _('p')
$tab = _('table')
$tb = _('tbody')
$tr = _('tr')
$th = _('th')
$td = _('td')
$img = _('img')
$ul = _('ul')
$ol = _('ol')
$li = _('li')
$h1 = _('h1')
$h2 = _('h2')
$h3 = _('h3')
$h4 = _('h4')
$bn = _('button')
$inp = _('input')
$sel = _('select')
$opt = _('option')
$lns = _('textarea')
$._ = function (t) { return function () {
	return $dom($doms('input', arguments), 'type', t)
}}
$ln = _('text')
$chk = _('checkbox')
$rad = _('radio')
delete _
}
/** <a>... without action */
$a0 = function () {
	return $dom($doms('a', arguments), 'href', 'ob:'+Math.random(), 'click', Boolean, window)
}

/** create a text node, single line, multi whitespace reserved. */
$tx = function (singleLine) {
	return $D.createTextNode(singleLine.replace(/  /g, ' \u00a0'))
}

//********************************************************************************************//

/** append children if index is skipped or <0 or >= children length,
 * or insert before index if index >= 0
 * @return this */
$dom.add = function (index) {
	if (typeof index == 'number')
		for (var x = 1, m = this.childNodes[index] || null; x < arguments.length; x++)
			this.insertBefore(arguments[x], m)
	else
		for (var x = 0; x < arguments.length; x++)
			this.appendChild(arguments[x])
	return this
}
/** remove children, or remove self if no argument,
 * or remove len children from Math.max(index, 0), or remove to last if !(len > 0) 
 * or replaced by second argument if index === true
 * @return this */
$dom.rem = function (index, len) {
	if (arguments.length == 0)
		this.parentNode && this.parentNode.removeChild(this)
	else if (index === true)
		$.o(this.parentNode).replaceChild(len, this)
	else if (typeof index == 'number') {
		var s = this.childNodes
		index < 0 && (index = 0), len = len > 0 ? s[index + len] : null
		for (var x = s[index], y; x != len; x = y)
			y = x.nextSibling, this.removeChild(x)
	} else
		for (var x = 0; x < arguments.length; x++)
			this.removeChild(arguments[x])
	return this
}
/** like rem() and recursively trigger 'des' event then detach all handlers
 * @return this */
$dom.des = function (index, len) {
	if (arguments.length == 0)
		this.nodeType == 1 && (index = 0,
			this.ondes && this.ondes({type:'des', target:this, stop:$}), this.$on = 0),
		this.parentNode && this.parentNode.removeChild(this)
	if (index === true)
		$.o(this.parentNode).replaceChild(len, this),
		this.des ? this.des() : $dom.des.call(this)
	else if (typeof index == 'number') {
		var s = this.childNodes
		index < 0 && (index = 0), len = len > 0 ? s[index + len] : null
		for (var x = s[index], y; x != len; x = y)
			y = x.nextSibling, x.des ? x.des() : $dom.des.call(x)
	} else
		for (var x = 0, y; x < arguments.length; x++)
			(y = arguments[x]).des ? y.des() : $dom.des.call(y)
	return this
}

/** add css class, or remove css class if first argument === 0. @return this */
$dom.cla = function (clazz) {
	if (arguments.length == 0 || clazz === 0 && this.className.length == 0)
		return this;
	var cs = this.className.split(' '), c
	X:for (var x = clazz === 0 ? 1 : 0; x < arguments.length; x++)
		if (c = $.s(arguments[x])) {
			for (var y = cs.length - 1; y >= 0; y--)
				if (cs[y] == c)
					if (clazz === 0)
						cs.splice(y, 1)
					else
						continue X
			clazz === 0 || (cs[cs.length] = c)
		}
	this.className = cs.join(' ')
	return this
}
/** getAttribute if no argument, removeAttribute if v === null, or setAttribute
 * @return attribute if no argument, or this */
$dom.attr = function (a, v) {
	if (arguments.length <= 1)
		return this.getAttribute(a, 2/*exact value in ie*/)
	for (var x = 0; x < arguments.length; x++)
		a = arguments[x ++], v = arguments[x]
		v === null ? this.removeAttribute(a) : this.setAttribute(a, v)
	return this
}
/** get/set textContent in Firefox, innerText in IE, or get textarea.value, spaces reserved
 * @param multiLine if reserved. @return text if no argument, or this */
$dom.tx = $fos ? function (v, multiLine) {
	if (this.tagName.toLowerCase() == 'textarea')
		return arguments.length == 0 ? this.value :
			(this.textContent = multiLine ? String(v) : String(v).replace(/\n/g, ' '), this)
	if (arguments.length == 0)
		return this.textContent
	v = String(v).replace(/  /g, '\u00a0 ')
	if (multiLine && v.indexOf('\n') >= 0) {
		v = v.split('\n')
		this.textContent = v.length > 0 ? v[0] : ''
		for (var x = 1; x < v.length; x++)
			this.appendChild($D.createElement('br')).textContent = '\n', // \n for getting
			this.appendChild($D.createTextNode(v[x]))
	} else
		this.textContent = v
	return this
} : function (v, multiLine) {
	return arguments.length == 0 ? this.tagName.toLowerCase() == 'textarea' ?
		this.value.replace(/\r\n/g, '\n') : this.innerText :
		(this.innerText = multiLine ? String(v) : String(v).replace(/\n/g, ' '), this)
}
/** get style.display != 'none', or set style.display, or switch style.display if v === 0
 * @return true/false if no argument, or this */
$dom.show = function (v) {
	var s = this.style.display != 'none'
	if (arguments.length == 0)
		return s
	if (s && !v)
		this.$show = this.style.display, this.style.display = 'none'
	else if (!s && (v || v === 0))
		this.style.display = this.$show || ''
	return this
}

/** attach event handler
 * @param handler ignored if already attached, return true to cancel default action
 * @param This the "this" in handler, false for this dom element, === true for (new handler)
 * @param args the arguments for handler, false for the event object which
 *   "target" is source dom element, "which" is key code, "stop()" to cancel bubble
 * @param old replaced by handler
 * @return this */
$dom.attach = function (type, handler, This, args, old) {
	old && this.detach(type, old)
	var x, s = this.$on || (this.$on = [1, 0,,,]) // [free,next,handler,This,args...]
	if (x = s[type])
		do if (s[x + 1] === handler)
			return this
		while (s[x] && (x = s[x]))
	else
		this['on' + type] = $.event
	return s[x || type] = (x = s[0]), s[0] = s[x] || x + 4, s[x] = 0, s[++x] = $.f(handler),
		s[++x] = This === true ? ($.ctor(handler), 0) : This || this, s[++x] = args, this
}
/** detach event handler. @return this */
$dom.detach = function (type, handler) {
	var s = this.$on
	if ($.f(handler), s)
		for (var x = type, y; y = s[x]; x = y)
			if (s[y + 1] === handler)
				return s[x] = s[y], s[y] = s[0], s[0] = y,
					s[++y] = s[++y] = s[++y] = null, this
	return this
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


$.throwStack = function (file, line) {
	var s = arguments.length == 0 ? new Error().Stack()
		: $throw.err ? $throw.err.Stack() : ''
	s = s.substr(s.indexOf('\n') + 1)
	return arguments.length == 0 || $throw.err ? s
		: '@' + file + ':' + ($fos ? line : line - 1) + '\n' + s
}
	Error.prototype.Stack = function (s) {
		if (!(s = this.stack))
			return ''
		(s = this.stack.split('\n')).pop()
		for (var x = s.length - 1; x >= 0; x--)
			(s[x] = s[x].substr(s[x].lastIndexOf(')') + 1)) == '@:0' && s.splice(x, 1)
		return s.join('\n')
	}

/** @return x if not-null object (including list, excluding function), or throw */
$.o = function (x) {
	return x !== null && typeof x == 'object' ? x
		: $throw($S(x) + ' must be not-null object')
}
/** @return x if string, or throw */
$.s = function (x) {
	return typeof x == 'string' ? x : $throw($S(x) + ' must be string')
}
/** @return x if function, or throw */
$.f = function (x) {
	return typeof x == 'function' ? x : $throw($S(x) + ' must be function')
}
/** @return x if Array, or throw */
$.a = function (x) {
	return x !== null && x instanceof Array ? x : $throw($S(x) + ' must be Array')
}
/** @return x if not-null and instanceof the class, or throw */
$.is = function (x, clazz, name) {
	return x !== null && x instanceof clazz ? x
		: $throw($S(x) + ' must not-null and instanceof '
		+ (clazz.$name || clazz.name || name || $S(clazz)))
}

/** @return c which $ctor = an empty constructor */
$.ctor = function (c) {
	c.$ctor || ((c.$ctor = function () {}).prototype = c.prototype)
	return c
}
/** @return class (constructor) from class cache, or if $_$_ is true, eval() */
$.c = function ($_$, $_$_, $_$$) {
	if ($_$$ = $.cs[$_$])
		return $_$$
	$_$_ = $_$_ ? eval($_$) : $throw($S($_$) + ' class not found')
	return typeof $_$_ == 'function' ? $.cs[$_$] = $_$_
		: $throw($S($_$) + ' must be function')
}
	/* class cache */
	$.cs = { '':Object }
	$class(true, 'Object')

/** copy another's own props. @return to */
$.copy = function (to, from) {
	for (var x in from)
		from.hasOwnProperty(x) && (to[x] = from[x])
	$fos || from.hasOwnProperty(x = 'toString') && (to[x] = from[x])
	return to
}
/** copy all another's props. @return to */
$.copyAll = function (to, from) {
	for (var x in from)
		to[x] = from[x]
	$ie && from[x = 'toString'] != Object.prototype.toString && (to[x] = from[x])
	return to
}

/* event dispatcher */
$.event = function (e, s, x, r) {
	if ((s = this.$on) && (x = s[(e || event).type])) {
		do r = (s[x + 3]
			? s[x + 1].apply(s[x + 2] || new s[x + 1].$ctor, s[x + 3])
			: s[x + 1].call (s[x + 2] || new s[x + 1].$ctor, e || (e = event,
				e.target = e.srcElement, e.which = e.keyCode, e.stop = $.eventStop, e))
			) || r
		while (x = s[x])
		return !r
	}
}
	$fos ? Event.prototype.stop = Event.prototype.stopPropagation
		: $.eventStop = function () { this.cancelBubble = true }


//@@@@@@@@@@@@@@@@ UTILITIES @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


/** @return equal, or if both NaN */
Number.prototype.equal = function (n) {
	return this == n || isNaN(this) && isNaN(n)
}

/** @return indexOf(), or length if not found */
String.prototype.indexEnd =
Array.prototype.indexEnd = function (a, b, c) {
	var i = this.indexOf(a, b, c)
	return i >= 0 ? i : this.length
}

/** search object
 * @param from the index start from, 0 if missing
 * @param propName compare this[x][propName] to object rather than this[x]
 * @return the index of the element first found, or negative if not found */
Array.prototype.indexOf = function (o, from, propName) {
	if (propName != null) {
		for (var x = from || 0; x < this.length; x++)
			if ((y = this[x]) != null && y[propName] == o)
				return x
	} else
		for (var x = from || 0; x < this.length; x++)
			if (this[x] == o)
				return x
	return -1
}
/** search object backward
 * @param from the index start from, length - 1 if missing
 * @param propName compare this[x][propName] to object rather than this[x]
 * @return the index of the element last found, or negative if not found */
Array.prototype.lastIndexOf = function (o, from, propName) {
	if (propName != null) {
		for (var x = from || this.length - 1, y; x >= 0; x--)
			if ((y = this[x]) != null && y[propName] == o)
				return x
	} else
		for (var x = from || this.length - 1; x >= 0; x--)
			if (this[x] == o)
				return x
	return -1
}
/** slight different with Firfox some() */
Array.prototype.index = function (Do, This)  {
	if (This != null) {
		for (var x = 0; x < this.length; x++)
			if (Do.call(This, this[x], x, this))
				return x
	} else
		for (var x = 0; x < this.length; x++)
			if (Do(This, this[x], x, this))
				return x		
	return -1
}
/** slight different with Firefox forEach() */
Array.prototype.each = function (Do, This) {
	if (This != null)
		for (var x = 0; x < this.length; x++)
			Do.call(This, this[x], x, this)
	else
		for (var x = 0; x < this.length; x++)
			Do(this[x], x, this)
}
/** remove elements. @return this array */
Array.prototype.remove = function (from, len) {
	return this.splice(from, len), this
}
/** construct a new array by each element's property of this array */
Array.prototype.byProp = function (propName) {
	var s = Array(this.length)
	for (var i = 0; i < s.length; i++)
		s[i] = this[i][propName]
	return s
}
/** @return whether equal length and equal elements */ 
Array.prototype.equal = function (a) {
	if (a == null || this.length != a.length)
		return false
	for (var i = 0; i < this.length; i++)
		if (this[i] != a[i])
			return false
	return true
}

/** @param emptyName true: any query parameter without = is value, false: is name
 * @return value, or null if no such name */
location.param = function (name, emptyName) {
	var s = location.search.substr(1).split('&')
	for (var i = 0; i < s.length; i++) {
		var j = emptyName ? s[i].indexOf('=') : s[i].indexEnd('=')
		if (name == decodeURIComponent(s[i].substr(0, j)))
			return decodeURIComponent(s[i].substr(j + 1))
	}
	return null
}
/** @param emptyName true: any query parameter without = is value, false: is name */
location.params = function (emptyName) {
	var s = location.search.substr(1).split('&'), p = {}
	for (var i = 0; i < s.length; i++) {
		var j = emptyName ? s[i].indexOf('=') : s[i].indexEnd('=')
		p[decodeURIComponent(s[i].substr(0, j))] = decodeURIComponent(s[i].substr(j + 1))
	}
	return p
}
$ie && (location.$load = location.reload, location.reload = function () { location.$load() })


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


/** SO for error */
Err = function (hint) {
	this.hint = $(hint)
}
/** SO for errors. sub of Err */
Errs = function (hints) {
	Err.call(this, '')
	hints && (this.hints = $.a(hints))
}
$class(true, 'Err')
$class(true, 'Errs', Err)

/** start a HTTP round using form, see $http */
$http.form = function (url, time, req, done, data, form, prog) {
	$.s(req), $.f(done), form.action = $.s(url)
	form.firstChild.$form && form.firstChild.des()
	form.firstChild.tx(req, true)
	var r = $.iframe(form, '$form', 1, 'des', stop).show(false), tm = new Date()
	var on = setInterval(function (t) {
		if (!r) return
		try {
			if (!(t = r.contentWindow.document.body) || !(t = t.lastChild)
				|| t.$ || !t.tagName)
				return time > 0 && new Date() - tm > time && stop(on, 1, 'timeout')
			t.$ = 1, t = t.previousSibling
		} catch (_) { // denied
			return stop(on, 2000, 'Network or Server Failure')
		}
		t.tagName ? stop(on, 0, $dom(t).tx())
			: (tm = new Date()) && prog && prog(t.nodeValue, data)
	}, 300)
	function stop(o, a, b, R) {
		if (!r) return
		r.nextSibling.tx(''), clearInterval(on)
		try { R = r, r = null, R.src = 'about:blank', R.des() } catch(_) {}
		try {
			done(o == on ? a : -1, o == on ? b : 'stop', data), done = data = null
		} catch(_) {
			done = data = null
			if ($ie || !onerror) // TODO opera safari ?
				throw _
			_ instanceof Error ? onerror(_.message, _.fileName, _.lineNumber)
				: onerror(_, 0, 0)
		}
	}
	data === undefined && (data = stop)
	try { form.submit() } catch(_) { $.defer(0, stop, [on, 1000, 'Network Failure']) } // IE
	return stop
}
$http.cross = function () {
	$fos && location.protocol == 'file:' &&
		netscape.security.PrivilegeManager.enablePrivilege('UniversalBrowserRead')
}

/** $http wrapped with hint and several callback functions
 * @param url prepended by $Do.Url
 * @param thisX as "this" in doneX
 * @param doneX called after done(X-1), skipped if thisX missing
 * @return same as $http */
$Do = function (url, hint, req, this3, done3, this2, done2, this1, done1, form, thisP, doneP) {
	var h = (form ? $http.form : $http)
		($Do.url + url, $Do.timeout, req, $Do.done, undefined, form, $Do.prog)
	h.$hint = hint, h.$t3 = this3, h.$3 = done3, h.$t2 = this2, h.$2 = done2,
		h.$t1 = this1, h.$1 = done1, h.$tp = thisP, h.$p = doneP
	return h
}
	$Do.done = function (code, res, h) {
		h.$t0 !== undefined && h.$0.call(h.$t0)
		var ok, err // undefined
		if (code == 0)
			(res = $dec(res, $Do.byName, $Do.decoded)) instanceof Err ? err = res : ok = res
		else if (code > 0)
			err = new Err('HTTP Error ' + code + ' ' + res)
		h.$t1 !== undefined && h.$1.call(h.$t1, ok, err, h)
		h.$t2 !== undefined && h.$2.call(h.$t2, ok, err, h)
		h.$t3 !== undefined && h.$3.call(h.$t3, ok, err, h)
	}
	$Do.prog = function (p, h) {
		h.$tp !== undefined && h.$p.call(h.$tp, p, h)
	}

/** url prefix */
$Do.url = ''
/** default timeout ms */
$Do.timeout = 30000
/** @see $dec */
$Do.byName = null
/** @see $dec */
$Do.decoded = null

/** default delay after HTTP round end */
$http.doneDelay = 300


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


$B = $D.body
$dom($B)

$.proxy = function (This, go) {
	return function () { return go.apply(This, arguments) }
}
$.defer = function (This, Do, args) {
	return setTimeout(function () { args ? Do.apply(This, args) : Do.call(This) }, 0), This
}
$.deferDom = $fos ? $dom : function (m) {
	var s = arguments
	return setTimeout(function(){ $doms(m, s, 1) }, 0), m
}

/** for upload, var args */
$.form = function () {
	return $doms($dom($fos ? 'form' : '<form enctype="multipart/form-data">',
		'method', 'post', 'enctype', 'multipart/form-data'), arguments, 0)
		.add($lns('name', '$$').show(false))
}
$.iframe = function (form) {
	var n = '$$' + Math.random(), i = $doms(
		$dom($fos ? 'iframe' : '<iframe name="' + n + '">', 'name', n), arguments, 1)
	return form && (form.add(0, i).target = n), i
}
/** get/set style.cssFloat in Firefox, style.styleFloat in IE */
$.Float = $fos ? function (d, v) {
	return v === undefined ? d.style.cssFloat : (d.style.cssFloat = v, d)
} : function (d, v) {
	return v === undefined ? d.style.styleFloat : (d.style.styleFloat = v, d)
}
/** get/set style.opacity in Firefox, style.filter in IE */
$.opacity = $fos ? function (d, v) {
	return v === undefined ? d.style.opacity : (d.style.opacity = v < 1 ? v : '', d)
} : function (d, v) {
	var s = d.style, f = s.filter
	if (v === undefined)
		return f ? f.match(/opacity=([^)]*)/)[1] /100 : 1
	s.zoom = 1, s.filter = f.replace(/alpha\([^)]*\)/g, '')
		+ (v >= 1 ? '' : 'alpha(opacity=' + v * 100 + ')')
	return d
}
/** get disabled/readOnly, or set disabled/readOnly, or switch disabled/readOnly if v === 0
 * @return true/false if no argument, or this */
$.disable = function (d, v) {
	var r = d.disabled || d.readOnly
	if (arguments.length == 1)
		return r
	r = v === 0 ? !r : v
	var rr = d.tagName.toLowerCase()
	rr = rr == 'textarea' || rr == 'input' && d.type == 'text'
		? (d.disabled = false, d.readOnly = r) : d.disabled = r 
	return d
}
/** add css rule. @param selector, style... */
$.css = function () {
	$D.styleSheets.length || $throw('no stylesheet found')
	var sh = $D.styleSheets[0], e, t
	for (var as = arguments, x = 0; x < as.length; x += 2)
		if (e = as[x], t = as[x + 1], $fos)
			sh.insertRule(e + '{' + t + '}', sh.cssRules.length)
		else if (e.indexOf(',') < 0)
			sh.addRule(e, t)
		else
			for (var es = e.split(','), y = 0; y < es.length; y++)
				sh.addRule(es[y], t)
}

//********************************************************************************************//

/** make a box as a HTTP widget, double click or des() to stop http
 * @param h return value of $Do
 * @param show the widget contains http hint text if true
 * @return the box */
$Http = function (box, h, show) {
	h.$t0 = box, h.$0 = $Http.done
	h.$tp === undefined && (h.$tp = box, h.$p = $Http.prog, box.$hint = h.$hint)
	return box.des(0).cla(0, 'Err').cla('Http').add(
		$s('c', 'Http-icon', 'title', h.$hint + '. stop?', 'dblclick', h, 0, 0, 'des', h))
		.add(show ? $s('c', 'Http-text').tx(h.$hint) : 0)
}
	$Http.done = function () {
		this.des(0).cla(0, 'Http')
	}
	$Http.prog = function (p) {
		this.firstChild.title = this.$hint + '... ' + p + '. stop?'
		this.firstChild.nextSibling && this.lastChild.tx(this.$hint + ' ' + p)
	}

/** make a box as error widget
 * @param err Err, Errs or string
 * @param show the widget contains err text if true,
               or a function called at double click (show err text if null or missing)
 * @return the box */
$Err = function (box, err, show, noStack) {
	err = err instanceof Errs ? err.hints.join('\n') : err instanceof Err ? err.hint : $(err)
	noStack || $Err.noStack && noStack === undefined  
		|| $fos && (err = err + '\n' + $.throwStack())
	show == null && (show = $Err.onHint)
	box.des(0).add($s('c', 'Err-icon'))
	show === true ? box.add($s('c', 'Err-text').tx(err, true))
		: box.firstChild.attr('title', err).attach('dblclick', $.f(show))
	return box.cla(0, 'Http').cla('Err')
}
$Err.onHint = function () {
	alert(this.title) // popup better
	this.des()
}
$Err.noStack = false

/** overlay document with a layer containing an inner box
 * @return the popup layer */
$Pop = function (inner) {
	var box = $d('c', 'Pop',
		's', 'position:fixed; z-index:10000; width:100%; height:100%; top:0; left:0').add(
		$d('c', 'Pop-back', 's', 'position:absolute; width:100%; height:100%'),
		$d('s', 'overflow:auto; position:absolute; width:100%; height:100%').add(
			$tab('s', 'width:100%; height:100%').add($tb().add($tr().add(
				$td('s', 'vertical-align:middle').attr('align', 'center').add(inner)
			)))
		)
	)
	$fos || box.add(0, $.opacity(
		$dom('iframe', 's', 'position:absolute; width:100%; height:100%'), 0))
	if ($ie == 6)
		box.style.position = 'absolute',
		box.style.top = $D.documentElement.scrollTop,
		box.style.left = $D.documentElement.scrollLeft,
		$D.documentElement.style.overflow = 'hidden'
	box.des = $Pop.des
	return $B.add(box), box
}
	$Pop.des = function () {
		$ie == 6 && ($D.documentElement.style.overflow = '')
		$dom.des.apply(this, arguments)
	}
