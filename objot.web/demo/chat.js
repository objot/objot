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
chat.users = {};

//********************************************************************************************//

chat.Do = function (service, hint, doneOk, This, done, req) {
	var close = $http(chat.Url + service, chat.Timeout, req, chat.Done);
	close.hint = hint, close.doneOk = doneOk, close.This = This, close.done = done;
	return close;
}
chat.Done = function (code, res, close) {
	code == 0 && close.doneOk && close.doneOk($set(res));
	close.done.call(close.This, code == 0,
		code > 0 && new chat.Err('HTTP Error ' + code + ' : ' + res));
}

//********************************************************************************************//

chat.DoSign = function () {
}
chat.DoSign.inUp = function (name, pass, This, done) {
	return chat.Do('DoSign-inUp', 'Signing', 0, This, done,
		$get(new chat.User(0, name, pass), chat.DoSign));
}

//********************************************************************************************//

chat.DoUser = function () {
}
chat.DoUser.me = function (This, done) {
	return chat.Do('DoUser-me', 'Loading my info', this.okMe, This, done, '');
}
chat.DoUser.okMe = function (res) {
	chat.me = $.is(res, chat.User);
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


/** @return an image node with close() */
function http(close) {
	var http = $img('src', 'http.gif', 'c', 'http', 'title', close.hint + '... Stop?');
	return hint.attach('ondblclick', hint.close = close);
}
/** @return the box */
function error(box, err, hide) {
	err instanceof chat.Err && (err = err.hint);
	$fox && (err = err + '\n' + $.throwStack());
	box.className = 'error';
	hide ? box.att('title', err) : box.tx(err);
	return box.add(0, $img('src', 'error.gif', 'c', 'error'));
}

function Popup(inner) {
	$D.body.add(this.box = $d('c', 'Popup', 's', 'z-index:10000').add(
		$d('c', 'max posAbs back'),
		$d('c', 'max posAbs', 's', 'overflow:auto; z-index:10001').add(
			$tab('c', 'max').add($tb().add($tr().add($td()
				.att('valign', 'center').att('align', 'center').add(inner)
			)))
		)
	));
	this.box.className = $ie6 ? 'max0 posAbs' : 'max0 posFix';
	$fox || this.box.add(0, $.opacity($dom('iframe', 'c', 'max posAbs'), 0));
	$ie6 && (this.box.style.top = $D.documentElement.scrollTop,
		$D.documentElement.style.overflow = 'hidden');
}
Popup.prototype.close = function () {
	this.box.noleak().rem();
	$ie6 && ($D.documentElement.style.overflow = '');
	return this;
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


function SignIn(box) {
	this.box = box.add(this.err = $p(),
		$p().add($l().tx('User name'), this.name = $ln()),
		$p().add($l().tx('Password'), this.pass = $inp('type', 'password')),
		$p('s', 'text-align:center').add
			(this.submit = $this($bn('onclick', this.doSign), this).tx('Signin / Signup'))
	);
	this.name.focus();
}
SignIn.prototype.doSign = function () {
	this.err.tx('');
	this.submit.disabled = true, this.submit.blur();
	this.http = http(chat.DoSign.inUp(this.name.value, this.pass.value, this, this.doneSign));
	this.submit.parentNode.add(this.http);
}
SignIn.prototype.doneSign = function (code, res) {
	this.http.noleak().rem(), this.submit.disabled = false;
	ok && this.onOk.call(this.thisOk);
	err && error(this.err, err).show(true);
}
SignIn.prototype.onOk;
SignIn.prototype.thisOk;

//********************************************************************************************//

function Me(box) {
	this.box = box.add(
		$d('c', 'me').add(
			this.name = $d('c', 'left'), this.reload = $d('c', 'right').add(
				$this($a0('onclick', this.doReload), this).tx('Reload'))),
		this.add = $ln('c', 'left'),
			$this($a0('c', 'right', 'onclick', this.doAdd), this).tx('Add')
	);
	this.doReload();
}
Me.prototype.doReload = function () {
	this.reload.firstChild.show(false);
	this.reload.add(this.http = http(chat.DoUser.me(this, this.doneMe)));
}
Me.prototype.doneMe = function (ok, err) {
	this.http.noleak().rem(), this.http = null;
	this.reload.firstChild.show(true);
	ok && this.name.tx(chat.me.name);
	if (err instanceof chat.ErrUnsigned)
		this.onUnsigned.call(this.thisUnsigned);
	else
		err && error(this.reload, err, true);
}
Me.prototype.doAdd = function () {
}
$class('Me', chat.DoUser);

Me.prototype.onUnsigned;
Me.prototype.thisUnsigned;
