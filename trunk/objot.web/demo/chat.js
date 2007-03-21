//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
onerror = function(m, f, l) {
	alert('Error! You could report the following details to http://objot.info\n\n'
		+ m + '\n' + $.throwStack(f, l));
	return true;
}

$dom($D.body);


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


Url = '/objot/service/';
Timeout = 5000;
MinDatime = 0;

Ok = function (hint) {
	this.hint = $(hint);
}
Err = function (hint) {
	this.hint = $(hint);
}
ErrUnsigned = function (hint) {
	this.hint = $(hint);
}
$class('Ok');
$class('Err');
$class('ErrUnsigned', Err);

//********************************************************************************************//

User = function (id, name, pass) {
	this.id = id;
	this.name = name;
	pass && (this.password = pass);
	this.friends;
}

Chat = function (out, In, datime, text) {
	this.out = out;
	this.In = In;
	datime != null && (this.datime = datime);
	text && (this.text = text);
}
	Chat.prototype.datime = MinDatime;
	Chat.prototype.text = '';

$class('User');
$class('Chat');

//********************************************************************************************//

_me = null;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


Do = function (service, hint, req, This3, done3, This2, done2, This1, done1) {
	var h = $http(Url + service, Timeout, req, Do.done);
	h.hint = hint, h.This3 = This3, h.done3 = done3,
		h.This2 = This2, h.done2 = done2, h.This1 = This1, h.done1 = done1;
	return h;
}
	Do.done = function (code, res, http) {
		var ok = false, err = false;
		if (code == 0)
			(res = $set(res)) instanceof Err ? err = res : ok = res;
		else if (code > 0)
			err = new Err('HTTP Error ' + code + ' ' + res);
		http.done1 && http.done1.call(http.This1, ok, err);
		http.done2 && http.done2.call(http.This2, ok, err);
		http.done3 && http.done3.call(http.This3, ok, err);
	}

//********************************************************************************************//

DoSign = function () {
}

DoSign.inUp = function (name, pass, This, done) {
	return Do('DoSign-inUp', 'Signing', $get(new User(null, name, pass), this),
		This, done, null, this.doneInUp);
}
	DoSign.doneInUp = function (ok, err) {
		ok && (_me = $.is(ok, User));
	}

DoSign.out = function (This, done) {
	return Do('DoSign-out', 'Signing out', '', This, done);
}

//********************************************************************************************//

DoUser = function () {
	
}

DoUser.me = function (This, done) {
	return Do('DoUser-me', 'Loading my info', '', This, done, null, this.doneMe);
}
	DoUser.doneMe = function (ok, err) {
		ok && (_me = $.is(ok, User));
	}

DoUser.update = function (friends, This, done) {
	_me.myFriends = friends;
	return Do('DoUser-update', 'Updating my info', $get(_me, this.update),
		This, done, null, this.doneUpdate);
}
	DoUser.doneUpdate = function (ok, err) {
		ok && (_me.friends = _me.myFriends);
		delete _me.myFriends;
	}

DoUser.get = function (user, user2, This, done) {
	var x = 0, s = new Array(arguments.length - 2); // arguments.slice undefined
	for (; x < s.length; x++)
		s[x] = arguments[x];
	return Do('DoUser-get', 'Getting users info', $get(s, this.get),
		arguments[x], arguments[x + 1]);
}

//********************************************************************************************//

DoChat = function () {
}

DoChat.read = function (chat, This, done) {
	return Do('DoChat-read', 'Reading chats', $get(chat, this), This, done);
}

//********************************************************************************************//

$class('DoSign');
$class('DoUser');

$class.get(User, Object, ['id'], DoSign, ['name', 'password'],
	DoUser.update, ['id', 'myFriends'], DoUser.get, ['id', 'name']);
$class.get(Chat, DoChat, ['out', 'In', 'text']);


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//

$http.doneDelay = 300;

/** @return the box, inner des() includes http close */
function _Http(box, h) {
	var icon = $this($s('c', 'img', 'title', h.hint + '... Stop?', 'ondblclick', h), h);
	icon.des = _Http.iconDes, h.This1 = icon, h.done1 = _Http.iconDone;
	return box.des(0).cla(0, 'error').cla('http').add(icon);
}
	_Http.iconDes = function () {
		if (arguments.length == 0)
			this.$();
		return $dom.des.apply(this, arguments);
	}
	_Http.iconDone = function () {
		this.des();
	}

/** @return the box */
function _Err(box, err, hide) {
	err instanceof Err && (err = err.hint);
	$fox && (err = err + '\n' + $.throwStack());
	box.des(0), hide || box.tx(err, true), box.add(0, $s('c', 'img'));
	hide && box.firstChild.att('title', err).attach('ondblclick', _Err.hint);
	return box.cla(0, 'http').cla('error');
}
	_Err.hint = function () {
		alert(this.title); // just for test, should use popup box
	}

/** @return a box */
function _Pop(inner) {
	var box = $d('s', 'z-index:10000').add(
		$d('c', 'max posAbs back'),
		$d('c', 'max posAbs', 's', 'overflow:auto; z-index:10001').add(
			$tab('c', 'max').add($tb().add($tr().add($td()
				.att('valign', 'center').att('align', 'center').add(inner)
			)))
		));
	box.className = $ie6 ? 'Pop max0 posAbs' : 'Pop max0 posFix';
	$fox || box.add(0, $.opacity($dom('iframe', 'c', 'max posAbs'), 0));
	$ie6 && (box.style.top = $D.documentElement.scrollTop,
		$D.documentElement.style.overflow = 'hidden');
	box.des = _Pop.des;
	return $D.body.add(box), box;
}
	_Pop.des = function () {
		$ie6 && ($D.documentElement.style.overflow = '');
		$dom.des.apply(this, arguments);
	}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


_SignIn = function (box) {
	this.box = box.add(this.err = $p(),
		$p().add($l().tx('User name'), this.name = $ln()),
		$p().add($l().tx('Password'), this.pass = $inp('type', 'password')),
		$p('s', 'text-align:center').add(
			this.submit = $this($bn('onclick', this.doSign), this).tx('Signin / Signup'),
			this.http = $s())
	);
	this.name.focus();
}

_SignIn.prototype.doSign = function () {
	this.submit.disabled = true, this.submit.blur();
	_Http(this.http, DoSign.inUp(this.name.value, this.pass.value, this, this.doneSign));
}
_SignIn.prototype.doneSign = function (ok, err) {
	this.submit.disabled = false;
	ok && this.onOk.call(this.thisOk);
	err && _Err(this.err, err);
}

_SignIn.prototype.onOk;
_SignIn.prototype.thisOk;

//********************************************************************************************//

_Me = function (box) {
	this.box = box.add(
		this.name = $d('c', 'left'),
		this.reload = $d('c', 'right').add(
			$this($a0('onclick', this.doReload), this).tx('Reload'),
			this.http = $s()),
		this.friends = $d('c', 'friends'),
		this.add = $ln('c', 'left'),
		$this($a0('c', 'right', 'onclick', this.doAdd), this).tx('+')
	);
	this.doReload();
}

_Me.prototype.doReload = function () {
	_Http(this.http, DoUser.me(this, this.doReload.me));
}
_Me.prototype.doReload.me = function (ok, err) {
	if (ok) {
		this.name.tx(_me.name);
		this.friends.des(0);
		for (var s = _me.friends, x = 0; x < s.length; x++)
			new _Me.Friend(this, x);
	}
	if (err)
		if (err instanceof ErrUnsigned && this.onUnsigned)
			this.onUnsigned.call(this.thisUnsigned);
		else
			_Err(this.http, err, true);
}

_Me.prototype.doAdd = function () {
	var u = new User(null, this.add.value);
	for (var s = _me.friends, x = 0; x < s.length; x++)
		if (s[x].name == u.name)
			return this.add.value = '';
	_Http(this.http, DoUser.get(u, this, this.doAdd.user))
}
_Me.prototype.doAdd.user = function (ok, err) {
	if (ok)
		if (ok[0]) {
			var fs = _me.friends.slice();
			fs.push(ok[0]);
			_Http(this.http, DoUser.update(fs, this, this.doneAdd));
		}
		else
			err = 'User not found';
	err && _Err(this.http, err, true);
}
_Me.prototype.doneAdd = function (ok, err) {
	if (ok)
		new _Me.Friend(this, _me.friends.length - 1),
		this.add.value = '';
	err && _Err(this.http, err, true);
}

_Me.prototype.onUnsigned;
_Me.prototype.thisUnsigned;

_Me.Friend = function (me, x) {
	this.me = me;
	this.x = x;
	me.friends.add(this.left = $d('c', 'left').tx(_me.friends[x].name),
		this.right = $this($a0('c', 'right', 'onclick', this.doRem), this).tx('X'));
}
_Me.Friend.prototype.doRem = function () {
	_Http(this.me.http, DoUser.update
		(_me.friends.slice().remove(this.x, 1), this, this.doneRem));
}
_Me.Friend.prototype.doneRem = function (ok, err) {
	ok && (this.left.des(), this.right.des());
	err && _Err(this.me.http, err, true);
}

//********************************************************************************************//

_Chats = function (box) {
	this.box = box.add(
		this.tab = $d('c', 'left'),
		this.refresh = $d('c', 'right').add(
			$this($a0('onclick', this.doRefresh), this).tx('Refresh'),
			this.http = $s()),
		this.chats = $d('c', 'chats')
	);
	this.doRefresh();
}

_Chats.prototype.doRefresh = function () {
	_Http(this.http, DoChat.read(new Chat(_me.id), this, this.doRefresh.out));
}
_Chats.prototype.doRefresh = function () {
	_Http(this.http, DoChat.read(new Chat(null, _me.id), this, this.doRefresh.In));
}
