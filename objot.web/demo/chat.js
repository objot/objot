//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
onerror = function(m, f, l) {
	alert('Error! You could report the following details to http://objot.info\n',
		m, $.throwStack(f, l));
	return true;
}

$dom($D.body);


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


chat = {
	Url: '/objot/service/',
	Timeout: 5000
}

chat.Ok = function (hint) {
	this.hint = $(hint);
}
chat.Err = function (hint) {
	this.hint = $(hint);
}
chat.ErrUnsigned = function (hint) {
	this.hint = $(hint);
}
$class('chat.Ok');
$class('chat.Err');
$class('chat.ErrUnsigned', chat.Err);

//********************************************************************************************//

chat.User = function (id, name, pass) {
	this.id = id;
	this.name = name;
	this.password = pass;
	this.friends;
}
chat.Chat = function () {
	this.out;
	this.In;
	this.datime;
	this.text;
}
$class('chat.User');
$class('chat.Chat');

//********************************************************************************************//

chat.me;

//********************************************************************************************//

chat.Do = function (service, hint, doneOk, This, done, req) {
	var close = $http(chat.Url + service, chat.Timeout, req, chat.Done);
	close.hint = hint, close.doneOk = doneOk, close.This = This, close.done = done;
	return close;
}
	chat.Done = function (code, res, close) {
		if (code < 0)
			close.done.call(close.This, false, false);
		else if (code > 0)
			close.done.call(close.This, false, new chat.Err('HTTP Error ' + code + ' ' + res));
		else if ((res = $set(res)) instanceof chat.Err)
			close.done.call(close.This, false, res);
		else
			close.doneOk && close.doneOk(res), close.done.call(close.This, res, false);
	}

//********************************************************************************************//

chat.DoSign = function () {
}

chat.DoSign.inUp = function (name, pass, This, done) {
	return chat.Do('DoSign-inUp', 'Signing', 0, This, done,
		$get(new chat.User(0, name, pass), this));
}

chat.DoSign.out = function (This, done) {
	return chat.Do('DoSign-out', 'Signing out', 0, This, done, '');
}

//********************************************************************************************//

chat.DoUser = function () {
}

chat.DoUser.me = function (This, done) {
	return chat.Do('DoUser-me', 'Loading my info', this.me.ok, This, done, '');
}
	chat.DoUser.me.ok = function (res) {
		chat.me = $.is(res, chat.User);
	}

chat.DoUser.update = function (This, done) {
	return chat.Do('DoUser-update', 'Updating my info', 0, This, done, $get(chat.me, this));
}

chat.DoUser.getByName = function (name, name2, This, done) {
	var x = 0, s = new Array(arguments.length - 2); // arguments.slice undefined
	for (; x < s.length; x++)
		s[x] = arguments[x];
	return chat.Do('DoUser-getByName', 'Getting users info', this.getByName.ok,
		arguments[x], arguments[x + 1], $get(s, this));
}
	chat.DoUser.getByName.ok = function (res) {
		$.is(res, Array);
		for (var x = 0; x < res.length; x++)
			$.is(res[x], chat.User);
	}

//********************************************************************************************//

chat.DoChat = function () {
}
$class('chat.DoSign');
$class('chat.DoUser');

//********************************************************************************************//

$class.get(chat.User, Object, ['id'], chat.DoSign, ['name', 'password'],
	chat.DoUser, ['friends']);
$class.get(chat.Chat, chat.DoChat, ['out', 'In', 'text']);


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//

$http.doneDelay = 300;

/** @return the box, inner des() includes http close() */
function http(box, close) {
	var h = $s('c', 'img', 'title', close.hint + '... Stop?', 'ondblclick', close);
	h.des = http.des, h.close = close;
	return box.cla('http').add(h);
}
	http.des = function () {
		if (arguments.length == 0)
			this.close(), this.close = null;
		$dom.des.apply(this, arguments);
	}
 
/** @return the box */
function error(box, err, hide) {
	err instanceof chat.Err && (err = err.hint);
	$fox && (err = err + '\n' + $.throwStack());
	hide || box.tx(err);
	box.add(0, $s('c', 'img'));
	hide && box.firstChild.att('title', err).attach('ondblclick', error.hint);
	return box.cla('error');
}
	error.hint = function () {
		alert(this.title); // just for test, should use popup box
	}

/** @return a box */
function popup(inner) {
	var box = $d('s', 'z-index:10000').add(
		$d('c', 'max posAbs back'),
		$d('c', 'max posAbs', 's', 'overflow:auto; z-index:10001').add(
			$tab('c', 'max').add($tb().add($tr().add($td()
				.att('valign', 'center').att('align', 'center').add(inner)
			)))
		));
	box.className = $ie6 ? 'popup max0 posAbs' : 'popup max0 posFix';
	$fox || box.add(0, $.opacity($dom('iframe', 'c', 'max posAbs'), 0));
	$ie6 && (box.style.top = $D.documentElement.scrollTop,
		$D.documentElement.style.overflow = 'hidden');
	box.des = popup.des;
	return $D.body.add(box), box;
}
	popup.des = function () {
		$ie6 && ($D.documentElement.style.overflow = '');
		$dom.des.apply(this, arguments);
	}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


function SignIn(box) {
	this.box = box.add(this.err = $p(),
		$p().add($l().tx('User name'), this.name = $ln()),
		$p().add($l().tx('Password'), this.pass = $inp('type', 'password')),
		$p('s', 'text-align:center').add(
			this.submit = $this($bn('onclick', this.doSign), this).tx('Signin / Signup'),
			this.http = $s())
	);
	this.name.focus();
}

SignIn.prototype.doSign = function () {
	this.submit.disabled = true, this.submit.blur();
	http(this.http, chat.DoSign.inUp(this.name.value, this.pass.value, this, this.doneSign));
}
SignIn.prototype.doneSign = function (ok, err) {
	this.http.des(-1), this.submit.disabled = false;
	ok && this.onOk.call(this.thisOk);
	err && this.err.des(-1) && error(this.err, err);
}

SignIn.prototype.onOk;
SignIn.prototype.thisOk;

//********************************************************************************************//

function Me(box) {
	this.box = box.add(
		$d('c', 'me').add(this.name = $d('c', 'left'),
			this.reload = $d('c', 'right').add(
				$this($a0('onclick', this.doReload), this).tx('Reload'),
				this.http = $s(), this.err = $s())),
		this.friends = $d('c', 'friends'),
		this.add = $ln('c', 'left'),
			$this($a0('c', 'right', 'onclick', this.doAdd), this).tx('+')
	);
	this.doReload();
}

Me.prototype.friendsAdd = function (x, u) {
	this.friends.add($d('c', 'left').tx(u.name),
		$this($a0('c', 'right').tx('X'), this).attach('onclick', this.doRem, x));
}

Me.prototype.doReload = function () {
	this.http.des(-1), this.err.des(-1);
	http(this.http, chat.DoUser.me(this, this.doReload.me));
}
Me.prototype.doReload.me = function (ok, err) {
	this.http.des(-1);
	if (ok) {
		this.name.tx(chat.me.name);
		this.friends.des(-1);
		for (var s = chat.me.friends, x = 0; x < s.length; x++)
			this.friendsAdd(x, s[x]);
	}
	if (err)
		if (err instanceof chat.ErrUnsigned && this.onUnsigned)
			this.onUnsigned.call(this.thisUnsigned);
		else
			error(this.err, err, true);
}

Me.prototype.doAdd = function () {
	var u = new chat.User(0, this.add.value);
	for (var s = chat.me.friends, x = 0; x < s.length; x++)
		if (s[x].name == u.name)
			return this.doneAdd(true, false);
	this.http.des(-1), this.err.des(-1);
	http(this.http, chat.DoUser.getByName(u.name, this, this.doAdd.user))
}
Me.prototype.doAdd.user = function (ok, err) {
	this.http.des(-1);
	if (ok) {
		chat.me.friends.push(ok[0]);
		this.friendsAdd(chat.me.friends.length - 1, ok[0]);
		http(this.http, chat.DoUser.update(this, this.doneAdd));
	}
	err && error(this.err, err, true);
}
Me.prototype.doneAdd = function (ok, err) {
	this.http.des(-1);
	ok && (this.add.value = '');
	err && error(this.err, err, true);
}

Me.prototype.doRem = function (ev, x) {
	ev.target.parentNode.des();
	chat.me.friends.splice(x, 1);
	this.http.des(-1), this.err.des(-1);
	http(this.http, chat.DoUser.update(this, this.doneRem));
}
Me.prototype.doneRem = function (ok, err) {
	this.http.des(-1);
	err && error(this.err, err, true);
} 

Me.prototype.onUnsigned;
Me.prototype.thisUnsigned;
