//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//


Array.prototype.indexOf = function (o, from, propName) {
	if (propName != null) {
		for (var x = from; x < this.length; x++)
			if (this[x] != null && this[x][propName] == o)
				return x;
	} else
		for (var x = from; x < this.length; x++)
			if (this[x] == o)
				return x;
	return -1;
}
Array.prototype.lastIndexOf = function (o, from, propName) {
	if (propName != null) {
		for (var x = from; x >= 0; x--)
			if (this[x] != null && this[x][propName] == o)
				return x;
	} else
		for (var x = from; x >= 0; x--)
			if (this[x] == o)
				return x;
	return -1;
}
/* slight different with Firfox some() */
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
/* slight different with Firefox forEach() */
Array.prototype.each = function (Do, This) {
	if (This != null)
		for (var x = 0; x < this.length; x++)
			Do.call(This, this[x], x, this);
	else
		for (var x = 0; x < this.length; x++)
			Do(this[x], x, this);
}
Array.prototype.remove = function (from, len) {
	return this.splice(from, len), this;
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


Err = function (hint) {
	this.hint = $(hint);
}
Errs = function (hints) {
	Err.call(this, '');
	hints && (this.hints = $.a(hints));
}
$class('Err');
$class('Errs', Err);


$Do = function (service, hint, req, this3, done3, this2, done2, this1, done1) {
	var h = $http($Do.Url + service, $Do.Timeout, req, $Do.done);
	h.$hint = hint, h.$this3 = this3, h.$done3 = done3,
		h.$this2 = this2, h.$done2 = done2, h.$this1 = this1, h.$done1 = done1;
	return h;
}
	$Do.done = function (code, res, http) {
		http.$this0 !== undefined && http.$done0.call(http.$this0, code, res, http);
		var ok = false, err = false;
		if (code == 0)
			(res = $set(res)) instanceof Err ? err = res : ok = res;
		else if (code > 0)
			err = new Err('HTTP Error ' + code + ' ' + res);
		http.$this1 !== undefined && http.$done1.call(http.$this1, ok, err, http);
		http.$this2 !== undefined && http.$done2.call(http.$this2, ok, err, http);
		http.$this3 !== undefined && http.$done3.call(http.$this3, ok, err, http);
	}

$Do.Url = '';
$Do.Timeout = 30000; 

$http.doneDelay = 300;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


$dom($D.body);

/* get/set style.cssFloat in Firefox, style.styleFloat in IE */
$.Float = $fox ? function (d, v) {
	return v === undefined ? d.style.cssFloat : (d.style.cssFloat = v, d);
} : function (d, v) {
	return v === undefined ? d.style.styleFloat : (d.style.styleFloat = v, d);
}
/* get/set style.opacity in Firefox, style.filter in IE */
$.opacity = $fox ? function (d, v) {
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

/** @return the box, inner des() includes http stop */
$Http = function (box, h) {
	var img = $this($s('c', 'HTTP-img', 'title', h.$hint + '... Stop?', 'ondblclick', h), h);
	img.des = $Http.des, h.$this0 = img, h.$done0 = $Http.done;
	return box.des(0).cla(0, 'ERR').cla('HTTP').add(img);
}
	$Http.des = function () {
		if (arguments.length == 0)
			this.$this();
		return $dom.des.apply(this, arguments);
	}
	$Http.done = function () {
		this.des();
	}

/** @return the box */
$Err = function (box, err, show) {
	err = err instanceof Errs ? err.hints.join('\n') : err instanceof Err ? err.hint : $(err);
	$fox && (err = err + '\n' + $.throwStack());
	show == null && (show = $Err.onHint);
	box.des(0), show === true && box.tx(err, true), box.add(0, $s('c', 'ERR-img'));
	show === true || box.firstChild.att('title', err).attach('ondblclick', show);
	return box.cla(0, 'HTTP').cla('ERR');
}
	$Err.doHint = function () {
		alert(this.title); // popup box better
		this.des();
	}
$Err.onHint = $Err.doHint;

/** @return a box */
$Pop = function (inner) {
	var box =
	$d('c', 'POP', 's', ($ie6 ? 'position:absolute' : 'position:fixed') +
			'; z-index:10000; width:100%; height:100%; left:0; top:0').add(
		$d('c', 'POP-back', 's', 'position:absolute; width:100%; height:100%'),
		$d('s', 'overflow:auto; position:absolute; width:100%; height:100%').add(
			$tab('s', 'width:100%; height:100%').add($tb().add($tr().add($td()
				.att('valign', 'center').att('align', 'center').add(inner)
			)))
		)
	);
	$fox || box.add(0, $.opacity(
		$dom('iframe', 's', 'position:absolute; width:100%; height:100%'), 0));
	$ie6 && (box.style.top = $D.documentElement.scrollTop,
		$D.documentElement.style.overflow = 'hidden');
	box.des = $Pop.des;
	return $D.body.add(box), box;
}
	$Pop.des = function () {
		$ie6 && ($D.documentElement.style.overflow = '');
		$dom.des.apply(this, arguments);
	}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


// hints
//
// in Firefox, predefined function(){}.name can only be assigned without '.'
//
// && || ! ? if(x), 1 '0' [] are true, 0 NaN '' null undefined are false
//   do NOT use x == true/false, use x === true/false instead
//
// in IE 6(7?), event handler codes may need try { ... } finally {}
//   otherwise the finally { ... } inside the codes may be ignored, stupid
//
// String(x) convert x to string (not String) unless x is already string
//
// function (a, b) { b = a; // then arguments[1] == arguments[0]
//
// while Firefox alert(), some callbacks could still be fired, such as
//   timeout, interval
// while IE alert(), some callbacks could still be fired, such as
//   onclick, XMLHttpRequest.onreadystatechange
// Awful ...
//
// when Firefox XMLHttpRequest fails, readyState is 4 and status is 0 or unavailable
//
// \n unsupported in Firefox(not IE) element tooltip and textContent proprety, stupid
//
// in IE 6(7?), (null dom node) instanceof (Object etc) causes Javascript error.
//
