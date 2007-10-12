//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//


/** search object.
 * @param from the index start from, 0 if missing.
 * @param propName compare this[x][propName] to object rather than this[x].
 * @return the index of the element first found, or negative if not found */
Array.prototype.indexOf = function (o, from, propName) {
	if (propName != null) {
		for (var x = from || 0; x < this.length; x++)
			if ((y = this[x]) != null && y[propName] == o)
				return x;
	} else
		for (var x = from || 0; x < this.length; x++)
			if (this[x] == o)
				return x;
	return -1;
}
/** search object backward.
 * @param from the index start from, length - 1 if missing.
 * @param propName compare this[x][propName] to object rather than this[x].
 * @return the index of the element last found, or negative if not found */
Array.prototype.lastIndexOf = function (o, from, propName) {
	if (propName != null) {
		for (var x = from || this.length - 1, y; x >= 0; x--)
			if ((y = this[x]) != null && y[propName] == o)
				return x;
	} else
		for (var x = from || this.length - 1; x >= 0; x--)
			if (this[x] == o)
				return x;
	return -1;
}
/** slight different with Firfox some() */
Array.prototype.index = function (Do, This)  {
	if (This != null) {
		for (var x = 0; x < this.length; x++)
			if (Do.call(This, this[x], x, this))
				return x;
	} else
		for (var x = 0; x < this.length; x++)
			if (Do(This, this[x], x, this))
				return x;		
	return -1;
}
/** slight different with Firefox forEach() */
Array.prototype.each = function (Do, This) {
	if (This != null)
		for (var x = 0; x < this.length; x++)
			Do.call(This, this[x], x, this);
	else
		for (var x = 0; x < this.length; x++)
			Do(this[x], x, this);
}
/** remove elements. @return this array */
Array.prototype.remove = function (from, len) {
	return this.splice(from, len), this;
}
/** construct a new array by each element's property of this array */
Array.prototype.byProp = function (propName) {
	var s = Array(this.length);
	for (var i = 0; i < s.length; i++)
		s[i] = this[i][propName];
	return s;
}


/** @param emptyName true: any query parameter without = is value, false: is name
 * @return value, or null if no such name */
location.param = function (name, emptyName) {
	var s = location.search.substr(1).split('&');
	for (var i = 0; i < s.length; i++) {
		var j = s[i].indexOf('=');
		j < 0 && !emptyName && (j = s[i].length);
		if (name == s[i].substr(0, j))
			return unescape(s[i].substr(j + 1));
	}
	return null;
}
/** @param emptyName true: any query parameter without = is value, false: is name */
location.params = function (emptyName) {
	var s = location.search.substr(1).split('&');
	var p = {};
	for (var i = 0; i < s.length; i++) {
		var j = s[i].indexOf('=');
		j < 0 && !emptyName && (j = s[i].length);
		p[s[i].substr(0, j)] = unescape(s[i].substr(j + 1));
	}
	return p;
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


/** SO for error */
Err = function (hint) {
	this.hint = $(hint);
}
/** SO for errors. sub of Err */
Errs = function (hints) {
	Err.call(this, '');
	hints && (this.hints = $.a(hints));
}
$class(true, 'Err');
$class(true, 'Errs', Err);


/** $http wrapped with hint and several callback functions.
 * @param service appended to $Do.Url as url.
 * @param this3 as "this" in done3.
 * @param done3 called after done2, skipped if this3 missing.
 * @param this2 as "this" in done2.
 * @param done2 called after done1, skipped if this2 missing.
 * @param this1 as "this" in done1.
 * @param done1 called after HTTP round end, skipped if this1 missing.
 * @return same as $http */
$Do = function (service, hint, req, this3, done3, this2, done2, this1, done1) {
	var h = $http($Do.Url + service, $Do.Timeout, req, $Do.done);
	h.$hint = hint, h.$this3 = this3, h.$done3 = done3,
		h.$this2 = this2, h.$done2 = done2, h.$this1 = this1, h.$done1 = done1;
	return h;
}
	$Do.done = function (code, res, h) {
		h.$this0 !== undefined && h.$done0.call(h.$this0);
		var ok = false, err = false;
		if (code == 0)
			(res = $dec(res)) instanceof Err ? err = res : ok = res;
		else if (code > 0)
			err = new Err('HTTP Error ' + code + ' ' + res);
		h.$this1 !== undefined && h.$done1.call(h.$this1, ok, err, h);
		h.$this2 !== undefined && h.$done2.call(h.$this2, ok, err, h);
		h.$this3 !== undefined && h.$done3.call(h.$this3, ok, err, h);
	}

/** url prefix */
$Do.Url = '';
/** default timeout milliseconds */
$Do.Timeout = 30000; 

/** default callback delay after HTTP round end */
$http.doneDelay = 300;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


$dom($D.body);

/** get/set style.cssFloat in Firefox, style.styleFloat in IE */
$.Float = $fos ? function (d, v) {
	return v === undefined ? d.style.cssFloat : (d.style.cssFloat = v, d);
} : function (d, v) {
	return v === undefined ? d.style.styleFloat : (d.style.styleFloat = v, d);
}
/** get/set style.opacity in Firefox, style.filter in IE */
$.opacity = $fos ? function (d, v) {
	return v === undefined ? d.style.opacity : (d.style.opacity = v < 1 ? v : '', d);
} : function (d, v) {
	var s = d.style, f = s.filter;
	if (v === undefined)
		return f ? f.match(/opacity=([^)]*)/)[1] /100 : 1;
	s.zoom = 1, s.filter = f.replace(/alpha\([^)]*\)/g, '')
		+ (v >= 1 ? '' : 'alpha(opacity=' + v * 100 + ')');
	return d;
}

//********************************************************************************************//

/** make a box as a HTTP widget, double click to stop http.
 * @param h return value of $Do
 * @return the box, inner des() includes http stop */
$Http = function (box, h) {
	var i = $s('c', 'HTTP-icon', 'title', h.$hint + '... Stop?', 'dblclick', h);
	i.$http = h, i.des = h.$done0 = $Http.des, h.$this0 = i;
	return box.des(0).cla(0, 'ERR').cla('HTTP').add(i);
}
	$Http.des = function () {
		if (arguments.length == 0)
			this.$http();
		return $dom.des.apply(this, arguments);
	}

/** make a box as error widget.
 * @param err Err, Errs or string.
 * @param show the widget contains err text if true,
               or a function called at double click (show err text if null or missing).
 * @return the box */
$Err = function (box, err, show, noStack) {
	err = err instanceof Errs ? err.hints.join('\n') : err instanceof Err ? err.hint : $(err);
	noStack || $fos && (err = err + '\n' + $.throwStack());
	show == null && (show = $Err.onHint);
	box.des(0), show === true && box.tx(err, true), box.add(0, $s('c', 'ERR-icon'));
	show === true || box.firstChild.att('title', err).attach('dblclick', $.f(show));
	return box.cla(0, 'HTTP').cla('ERR');
}
$Err.onHint = function () {
	alert(this.title); // popup better
	this.des();
}

/** overlay the document body with a layer containing an inner box.
 * @return the popup layer */
$Pop = function (inner) {
	var box = $d('c', 'POP',
		's', 'position:fixed; z-index:10000; width:100%; height:100%; top:0; left:0').add(
		$d('c', 'POP-back', 's', 'position:absolute; width:100%; height:100%'),
		$d('s', 'overflow:auto; position:absolute; width:100%; height:100%').add(
			$tab('s', 'width:100%; height:100%').add($tb().add($tr().add($td()
				.att('valign', 'center').att('align', 'center').add(inner)
			)))
		)
	);
	$fos || box.add(0, $.opacity(
		$dom('iframe', 's', 'position:absolute; width:100%; height:100%'), 0));
	if ($ie == 6)
		box.style.position = 'absolute',
		box.style.top = $D.documentElement.scrollTop,
		box.style.left = $D.documentElement.scrollLeft,
		$D.documentElement.style.overflow = 'hidden';
	box.des = $Pop.des;
	return $D.body.add(box), box;
}
	$Pop.des = function () {
		$ie == 6 && ($D.documentElement.style.overflow = '');
		$dom.des.apply(this, arguments);
	}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


// hints
//
// in Firefox, predefined function(){}.name can only be assigned without '.'
//
// && || ! ? if(x), 1 '0' [] are true, 0 NaN '' null undefined are false
//   do NOT use x == true/false, use Boolean(x) or !!x or x === true/false instead
//
// in IE 6&7, event handler codes may need outermost try { ... } finally {}
//   otherwise the inner finally { ... } may be ignored, stupid
//
// String(x) convert x to string (not String) unless x is already string
//
// function (a, b) { b = a; // then arguments[1] == arguments[0]
//
// while Firefox alert(), some callbacks could still be fired, such as
//   timeout, interval
// while IE 6&7 alert(), some callbacks could still be fired, such as
//   onclick, XMLHttpRequest.onreadystatechange
// Awful ...
//
// when Firefox XMLHttpRequest fails, readyState is 4 and status is 0 or unavailable
//
// \n unsupported in Firefox(not IE) element tooltip and textContent proprety, stupid
//
// in IE 6&7, (null dom node) instanceof (Object etc) causes Javascript error.
//
// in IE 6&7, $inp('name', 'a', ...).outerHTML contains no name="a", and
//   document.getElementsByName('a') returns without this input, stupid
// and <tr>.innerHTML may not be set directly, should createElement('td')
//
// in IE 6(7?), iframe's window.parent may not be the actual parent window,
//   but iframe's window.parent.document is the actual parent window document
//
// in IE 6(7?), codes in different windows may be multi-thread ??!
//
// in Firefox for Linux, onkeydown may be triggered only one key is down,
//   may use onkeypress instead
//