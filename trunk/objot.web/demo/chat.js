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


chat = {
	Url: '/objot/service/',
	Timeout: 5000,
	MinDatime: 0
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
	pass && (this.password = pass);
	this.friends;
}

chat.Chat = function (out, In, datime, text) {
	this.out = out;
	this.In = In;
	datime != null && (this.datime = datime);
	text && (this.text = text);
}
	chat.Chat.prototype.datime = chat.MinDatime;
	chat.Chat.prototype.text = '';

$class('chat.User');
$class('chat.Chat');

//********************************************************************************************//

chat.me;
chat.ins;
chat.outs;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


chat.Do = function (service, hint, req, This3, done3, This2, done2, This1, done1) {
	var h = $http(chat.Url + service, chat.Timeout, req, chat.Done);
	h.hint = hint, h.This3 = This3, h.done3 = done3,
		h.This2 = This2, h.done2 = done2, h.This1 = This1, h.done1 = done1;
	return h;
}
	chat.Done = function (code, res, http) {
		var ok = false, err = false;
		if (code == 0)
			(res = $set(res)) instanceof chat.Err ? err = res : ok = res;
		else if (code > 0)
			err = new chat.Err('HTTP Error ' + code + ' ' + res);
		http.done1 && http.done1.call(http.This1, ok, err);
		http.done2 && http.done2.call(http.This2, ok, err);
		http.done3 && http.done3.call(http.This3, ok, err);
	}

//********************************************************************************************//

chat.DoSign = function () {
}

chat.DoSign.inUp = function (name, pass, This, done) {
	return chat.Do('DoSign-inUp', 'Signing', $get(new chat.User(null, name, pass), this),
		This, done, null, this.doneInUp);
}
	chat.DoSign.doneInUp = function (ok, err) {
		ok && (chat.me = $.is(ok, chat.User));
	}

chat.DoSign.out = function (This, done) {
	return chat.Do('DoSign-out', 'Signing out', '', This, done);
}

//********************************************************************************************//

chat.DoUser = function () {
	
}

chat.DoUser.me = function (This, done) {
	return chat.Do('DoUser-me', 'Loading my info', '', This, done, null, this.doneMe);
}
	chat.DoUser.doneMe = function (ok, err) {
		ok && (chat.me = $.is(ok, chat.User));
	}

chat.DoUser.update = function (friends, This, done) {
	chat.me.myFriends = friends;
	return chat.Do('DoUser-update', 'Updating my info', $get(chat.me, this.update),
		This, done, null, this.doneUpdate);
}
	chat.DoUser.doneUpdate = function (ok, err) {
		ok && (chat.me.friends = chat.me.myFriends);
		delete chat.me.myFriends;
	}

chat.DoUser.get = function (user, user2, This, done) {
	var x = 0, s = new Array(arguments.length - 2); // arguments.slice undefined
	for (; x < s.length; x++)
		s[x] = arguments[x];
	return chat.Do('DoUser-get', 'Getting users info', $get(s, this.get),
		arguments[x], arguments[x + 1]);
}

//********************************************************************************************//

chat.DoChat = function () {
}

chat.DoChat.read = function (chat, This, done) {
	return chat.Do('DoChat-read', 'Reading chats', $get(chat, this), This, done);
}

//********************************************************************************************//

$class('chat.DoSign');
$class('chat.DoUser');

$class.get(chat.User, Object, ['id'], chat.DoSign, ['name', 'password'],
	chat.DoUser.update, ['id', 'myFriends'], chat.DoUser.get, ['id', 'name']);
$class.get(chat.Chat, chat.DoChat, ['out', 'In', 'text']);


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//

$http.doneDelay = 300;

/** @return the box, inner des() includes http close */
function http(box, h) {
	var icon = $this($s('c', 'img', 'title', h.hint + '... Stop?', 'ondblclick', h), h);
	icon.des = http.iconDes, h.This1 = icon, h.done1 = http.iconDone;
	return box.des(-1).cla(-1, 'error').cla('http').add(icon);
}
	http.iconDes = function () {
		if (arguments.length == 0)
			this.$();
		return $dom.des.apply(this, arguments);
	}
	http.iconDone = function () {
		this.des();
	}

/** @return the box */
function error(box, err, hide) {
	err instanceof chat.Err && (err = err.hint);
	$fox && (err = err + '\n' + $.throwStack());
	box.des(-1), hide || box.tx(err), box.add(0, $s('c', 'img'));
	hide && box.firstChild.att('title', err).attach('ondblclick', error.hint);
	return box.cla(-1, 'http').cla('error');
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
	this.submit.disabled = false;
	ok && this.onOk.call(this.thisOk);
	err && error(this.err, err);
}

SignIn.prototype.onOk;
SignIn.prototype.thisOk;

//********************************************************************************************//

function Me(box) {
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

Me.prototype.doReload = function () {
	http(this.http, chat.DoUser.me(this, this.doReload.me));
}
Me.prototype.doReload.me = function (ok, err) {
	if (ok) {
		this.name.tx(chat.me.name);
		this.friends.des(-1);
		for (var s = chat.me.friends, x = 0; x < s.length; x++)
			new Me.Friend(this, x);
	}
	if (err)
		if (err instanceof chat.ErrUnsigned && this.onUnsigned)
			this.onUnsigned.call(this.thisUnsigned);
		else
			error(this.http, err, true);
}

Me.prototype.doAdd = function () {
	var u = new chat.User(null, this.add.value);
	for (var s = chat.me.friends, x = 0; x < s.length; x++)
		if (s[x].name == u.name)
			return this.add.value = '';
	http(this.http, chat.DoUser.get(u, this, this.doAdd.user))
}
Me.prototype.doAdd.user = function (ok, err) {
	if (ok)
		if (ok[0]) {
			var fs = chat.me.friends.slice();
			fs.push(ok[0]);
			http(this.http, chat.DoUser.update(fs, this, this.doneAdd));
		}
		else
			err = 'User not found';
	err && error(this.http, err, true);
}
Me.prototype.doneAdd = function (ok, err) {
	if (ok)
		new Me.Friend(this, chat.me.friends.length - 1),
		this.add.value = '';
	err && error(this.http, err, true);
}

Me.prototype.onUnsigned;
Me.prototype.thisUnsigned;

Me.Friend = function (me, x) {
	this.me = me;
	this.x = x;
	me.friends.add(this.left = $d('c', 'left').tx(chat.me.friends[x].name),
		this.right = $this($a0('c', 'right', 'onclick', this.doRem), this).tx('X'));
}
Me.Friend.prototype.doRem = function () {
	http(this.me.http, chat.DoUser.update
		(chat.me.friends.slice().remove(this.x, 1), this, this.doneRem));
}
Me.Friend.prototype.doneRem = function (ok, err) {
	ok && (this.left.des(), this.right.des());
	err && error(this.me.http, err, true);
}

//********************************************************************************************//

function Chats (box) {
	this.box = box.add(
		this.tab = $d('c', 'tab').add(
			this.refresh = $d('c', 'right').add(
				$this($a0('onclick', this.doRefresh), this).tx('Refresh'),
				this.http = $s()))
	);
	this.doRefresh();
}

Chats.prototype.doRefresh = function () {
	http(this.http, chat.DoChat.read(new chat.Chat(chat.me.id), this, this.doRefresh.out));
}
Chats.prototype.doRefresh = function () {
	http(this.http, chat.DoChat.read(new chat.Chat(null, chat.me.id), this, this.doRefresh.In));
}
