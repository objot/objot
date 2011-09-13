
$tochar = String.fromCharCode

/** encode data graph to string, following the encoding rules
 * @param ruleKey a rule key or its subclass */
$enc = function (o, ruleKey) {
	var t = typeof o, S,
	s = o == null ? ['p'] : o === false ? ['P'] : o === true ? ['Q']
		: t == 'number' ? (S = String(o), [$tochar(S.length), S])
		: t == 'string' ? [$tochar(2<<4|S.length), o] : 0
	if (!s) {
		s = []
		s.clazz = $.f(ruleKey), s.refX = 0
		try {
			$enc.ref(o, s.clazz), $enc.o(o, s, 0)
		} finally {
			try { $enc.unref(o) } catch(f) {}
		}
		$enc.unref(o)
	}
	return s.join('')
}
	$enc.ref = function (o, k, v, n, enc, g) {
		if (o[''] = '' in o)
			return
		if ( !(o instanceof Array) && !(n = o.constructor).$name)
			$throw($S(n) + ' class not ready')
		else
		P: {
			if (enc = n && n.$encs)
				for (g = enc.length - 2; g >= 0; g -= 2)
					if (k == enc[g] || k.prototype instanceof enc[g]) {
						if (enc = enc[g + 1]) {

		for (n = 0; n < enc.length; n++)
			if ((x = enc[n]) in o)
				typeof (v = o[x]) != 'string' ?
				v == null || typeof v != 'object' || $enc.ref(v, k)
				: v.indexOf('\0') < 0 || $throw($S(v) + ' must NOT contain \\0')

							break P
						}
						break
					}
		for (var x in o)
			if (o.hasOwnProperty(x))
				typeof (v = o[x]) != 'string' ?
				v == null || typeof v != 'object' || $enc.ref(v, k)
				: v.indexOf('\0') < 0 || $throw($S(v) + ' must NOT contain \\0')
		}
	}
	$enc.unref = function (o, v) {
		delete o['']
		for (var x in o)
			(v = o[x]) && v[''] != null && $enc.unref(v)
	}
	$enc.o = function (o, s, x, S) {
		o[''] && (o[''] = String(++s.refX))
		s[x++] = 'A0'
		var n = o.constructor, p, v, t, enc
		P: {
			if (enc = o.constructor.$encs)
				for (var c = s.clazz, g = enc.length - 2; g >= 0; g -= 2)
					if (c == enc[g] || c.prototype instanceof enc[g]) {
						if (enc = enc[g + 1]) {

		for (n = 0; n < enc.length; n++)
			if ((p = enc[n]) in o && (t = typeof (v = o[p])) != 'function')
				(S = String(p|0))==p ? (s[x++] = $tochar(S.length), s[x++] = S)
					: (S = String(p.length), s[x++] = $tochar(2<<4|S.length), s[x++] = S, s[x++] = p),
				v == null ? s[x++] = 'p' : v === false ? s[x++] = 'P' : v === true ? s[x++] = 'Q'
				: t == 'number' ? (S = String(v), s[x++] = $tochar(S.length), s[x++] = S)
				: t == 'string' ? (S = String(v.length), s[x++] = $tochar(2<<4|S.length), s[x++] = S, s[x++] = v)
				: typeof (S=v['']) == 'string' ? (s[x++] = s[x++] = S, $tochar(3<<4|S.length))
				: x = $enc.o(v, s, x)

							break P
						}
						break
					}
		for (p in o)
			if (o.hasOwnProperty(p) && p.length && (t = typeof (v = o[p])) != 'function')
				(S = String(p|0))==p ? (s[x++] = $tochar(S.length), s[x++] = S)
					: (S = String(p.length), s[x++] = $tochar(2<<4|S.length), s[x++] = S, s[x++] = p),
				v == null ? s[x++] = 'p' : v === false ? s[x++] = 'P' : v === true ? s[x++] = 'Q'
				: t == 'number' ? (S = String(v), s[x++] = $tochar(S.length), s[x++] = S)
				: t == 'string' ? (S = String(v.length), s[x++] = $tochar(2<<4|S.length), s[x++] = S, s[x++] = v)
				: typeof (S=v['']) == 'string' ? (s[x++] = s[x++] = S, $tochar(3<<4|S.length))
				: x = $enc.o(v, s, x)
		}
		s[x++] = 'p'
		return x
	}

/** decode string to data graph, objects are created without ctors
 * @param byName function(name) { return objectByName }, null for default
 * @param ok function(objectDecoded) {} */
$dec = function (s, byName, ok) {
	try {
		s = new String(s), s.ok = ok
		$dec.v(s, 0)
		return s.o
	} catch(_) {
		throw $dec.r.length = 1, _
	}
}
	$dec.v = function (s, x) {
		var v = s.charCodeAt(x++)
		if (v != v)
			throw 'unexpected end'
		else if (v>>5 == 0)
			v = v&31, s.o = +s.substring(x, x += v)
		else if (v>>4 == 2)
			v = v&15, v = s.substring(x, x += v), $dec.r.push(s.o = s.substring(x, x += v))
		else if (v>>4 == 3)
			v = v&15, s.o = $dec.r[s.substring(x, x += v)]
		else if (v>>4 == 5)
			s.o = v&1 ? true : false
		else if (v>>4 == 4)
			$dec.o(s, --x)
		else if (v == 7<<4)
			s.o = null
		else
			throw 'invalid data'
		return x;
	}
	$dec.o = function (s, x) {
		var v = s.charCodeAt(x++)&15, n = +s.substring(x, x += v)
		var o = {}, p; $dec.r.push(o)
		for (;;)
		{
			p = n > 0 ? n-- : (x = $dec.v(s, x), s.o)
			if (p == null)
				break
			x = $dec.v(s, x), o[p] = s.o
		}
		s.o = o, s.ok && s.ok(o)
		return x
	}
	$dec.r = [ null ]
