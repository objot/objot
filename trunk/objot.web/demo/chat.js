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

$Do.Url = '/objot/service/';
$Do.Timeout = 5000;
$http.doneDelay = 300;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


Ok = function (hint) {
	this.hint = $(hint);
}
ErrUnsigned = function (hint) {
	this.hint = $(hint);
}
$class('Ok');
$class('ErrUnsigned', Err);

//********************************************************************************************//

User = function (id, name, pass) {
	id && (this.id = id);
	name && (this.name = name);
	pass && (this.password = pass);
}
	User.prototype.id = -1;
	User.prototype.name = '';
	User.prototype.password = '';
	User.prototype.friends = null;

Chat = function (out, In, datime, text) {
	this.out = out;
	this.In = In;
	datime != null && (this.datime = datime);
	text && (this.text = text);
}
	Chat.prototype.datime = 0;
	Chat.prototype.text = '';

$class('User');
$class('Chat');


_me = null;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


DoSign = function () {
}

DoSign.inUp = function (name, pass, This, done) {
	return $Do('DoSign-inUp', 'Signing', $get(new User(null, name, pass), this),
		This, done, null, this.doneInUp);
}
	DoSign.doneInUp = function (ok, err) {
		ok && (_me = $.is(ok, User));
	}

DoSign.out = function (This, done) {
	return $Do('DoSign-out', 'Signing out', '', This, done);
}

DoSign.signed = function (ids, This, done) {
	return $Do('DoSign-signed', 'Getting signed users', $get(ids, this), This, done);
}

//********************************************************************************************//

DoUser = function () {
}

DoUser.me = function (This, done) {
	return $Do('DoUser-me', 'Loading my info', '', This, done, null, this.doneMe);
}
	DoUser.doneMe = function (ok, err) {
		ok && (_me = $.is(ok, User));
	}

DoUser.update = function (friends, This, done) {
	var me = new User();
	me.myFriends = friends;
	var h = $Do('DoUser-update', 'Updating my info', $get(me, this.update),
		This, done, null, this.doneUpdate);
	h.me = me;
	return h;
}
	DoUser.doneUpdate = function (ok, err, h) {
		ok && (_me.friends = h.me.myFriends);
	}

DoUser.get = function (users, This, done) {
	return $Do('DoUser-get', 'Getting users info', $get(users, this.get), This, done);
}

//********************************************************************************************//

DoChat = function () {
}

DoChat.read = function (chat, This, done) {
	return $Do('DoChat-read', 'Reading chats', $get(chat, this), This, done);
}

//********************************************************************************************//

$class('DoSign');
$class('DoUser');

$class.get(User, Object, ['id'], DoSign, ['name', 'password'],
	DoUser.update, ['id', 'myFriends'], DoUser.get, ['id', 'name']);
$class.get(Chat, DoChat, null);


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
	$Http(this.http, DoSign.inUp(this.name.value, this.pass.value, this, this.doneSign));
}
_SignIn.prototype.doneSign = function (ok, err) {
	this.submit.disabled = false;
	ok && (_me = ok, this.onOk.call(this.thisOk));
	err && $Err(this.err, err);
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
	$Http(this.http, DoUser.me(this, this.doneReload));
}
_Me.prototype.doneReload = function (ok, err) {
	if (ok) {
		this.name.tx(_me.name);
		this.friends.des(0);
		for (var s = _me.friends, x = 0; x < s.length; x++)
			new _Me.Friend(this, x);
		this.doSigned();
	}
	if (err)
		if (err instanceof ErrUnsigned && this.onUnsigned)
			this.onUnsigned.call(this.thisUnsigned);
		else
			$Err(this.http, err, true);
}

_Me.prototype.doSigned = function () {
	var ids = _me.friends.slice();
	for (var x = 0; x < ids.length; x++)
		ids[x] = _me.friends[x].id;
	$Http(this.http, DoSign.signed(ids, this, this.doneSigned));
}
_Me.prototype.doneSigned = function (ok, err) {
	if (ok)
		for (var x = 0; x < ok.length; x++)
			if (ok[x])
				this.friends.childNodes[x + x].cla('signed'),
				this.friends.childNodes[x + x + 1].cla('signed');
	err && $Err(this.http, err, true);
}

_Me.prototype.doAdd = function () {
	var u = new User(null, this.add.value);
	for (var s = _me.friends, x = 0; x < s.length; x++)
		if (s[x] === u.name)
			return this.add.value = '';
	$Http(this.http, DoUser.get([u], this, this.doAdd.user))
}
_Me.prototype.doAdd.user = function (ok, err) {
	if (ok)
		if (ok[0]) {
			var fs = _me.friends.slice();
			fs[fs.length] = ok[0];
			$Http(this.http, DoUser.update(fs, this, this.doneAdd));
		}
		else
			err = 'User not found';
	err && $Err(this.http, err, true);
}
_Me.prototype.doneAdd = function (ok, err) {
	if (ok) {
		new _Me.Friend(this, _me.friends.length - 1),
		this.add.value = '';
		this.doSigned();
	}
	err && $Err(this.http, err, true);
}

_Me.prototype.onUnsigned;
_Me.prototype.thisUnsigned;


_Me.Friend = function (me, x) {
	this.me = me;
	this.X = x;
	me.friends.add(this.left = $d('c', 'left').tx(_me.friends[x].name),
		this.right = $this($a0('c', 'right', 'onclick', this.doRem), this).tx('X'));
}
_Me.Friend.prototype.doRem = function () {
	$Http(this.me.http, DoUser.update
		(_me.friends.slice().remove(this.X, 1), this, this.doneRem));
}
_Me.Friend.prototype.doneRem = function (ok, err) {
	ok && (this.left.des(), this.right.des());
	err && $Err(this.me.http, err, true);
}

//********************************************************************************************//

_Chats = function (box) {
	this.box = box.add(
		this.tabs = $d('c', 'left'),
		this.refresh = $d('c', 'right').add(
			$this($a0('onclick', this.doRefresh), this).tx('Refresh'),
			this.http = $s()),
		this.chats = $d('c', 'chats').tx(0)
	);
	this.Chats = [];
	this.Datime = 0;
	this.doRefresh();
}

_Chats.prototype.doRefresh = function () {
	$Http(this.http, DoChat.read(new Chat(_me, null, this.datime),
		this, this.doRefresh.out));
}
_Chats.prototype.doRefresh.out = function (ok, err) {
	if (ok) {
		for (var x = 0; x < ok.length; x++) {
			var c = ok[x];
			this._Chat(c.In).doRead(c);
			this.Datime = Math.max(this.Datime, c.datime + 1);
		}
		$Http(this.http, DoChat.read(new Chat(null, _me, this.datime),
			this, this.doRefresh.In));
	}
	err && $Err(this.http, err, true);
}

_Chats.prototype._Chat = function (user) {
	for (var s = this.Chats, x = 0; x < s.length; x++)
		if (s[x].User.id == user.id)
			return s[x];
	var chat = new _Chat(this, this.Chats.length);
	return this.Chats.push(chat), chat;
}


_Chat = function (chats, x) {
	this.chats = chats;
	this.X = x;

	chats.chats.add()
}