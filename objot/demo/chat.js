//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//

onerror = function(m, f, l) {
	alert('Error! You could report the following details to http://objot.info\n\n'
		+ m + '\n' + $.throwStack(f, l));
	return true;
}

$Do.url = '../service/';
$Do.timeout = 10000;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


ErrUnsigned = function (hint) {
	this.hint = $(hint);
}
$class(true, 'ErrUnsigned', Err);

//********************************************************************************************//

DatimeMin = new Date(0);

User = function (id, name, pass) {
	this.id = id || 0;
	this.name = name;
	this.password = pass;
	this.friends;
}

Chat = function (out, In, datime, text) {
	this.out = out;
	this.In = In;
	this.datime = datime;
	this.text = text;
}
Smiley = function () {
}

$class(true, 'User');
$class(true, 'Chat');
$class(true, 'Smiley');


__me = null;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


DoSign = function () {
}

DoSign.inUp = function (name, pass, This, done) {
	return $Do('DoSign-inUp', 'Signing', $enc(new User(null, name, pass), this),
		This, done, null, this.doneInUp);
}
	DoSign.doneInUp = function (ok, err) {
		ok && (__me = $.is(ok, User));
	}

DoSign.out = function (This, done) {
	return $Do('DoSign-out', 'Signing out', '', This, done);
}

//********************************************************************************************//

DoUser = function () {
}

DoUser.me = function (This, done) {
	return $Do('DoUser-me', 'Loading my info', '', This, done, null, this.doneMe);
}
	DoUser.doneMe = function (ok, err) {
		ok && (__me = $.is(ok, User), __me.friends.sort(function (a, b) {
			return a.name < b.name ? -1 : a.name > b.name ? 1 : 0; 
		}));
	}

DoUser.update = function (friends, This, done) {
	var me = new User();
	me.friends_ = friends;
	var h = $Do('DoUser-update', 'Updating my info', $enc(me, this.update),
		This, done, null, this.doneUpdate);
	h.me = me;
	return h;
}
	DoUser.doneUpdate = function (ok, err, h) {
		ok && (__me.friends = h.me.friends_);
	}

DoUser.get = function (users, This, done) {
	return $Do('DoUser-get', 'Getting users info', $enc(users, this.get), This, done);
}

//********************************************************************************************//

DoChat = function () {
}

DoChat.read = function (chat, This, done) {
	return $Do('DoChat-read', 'Reading chats', $enc(chat, this), This, done);
}

DoChat.post = function (form, In, text, This, done) {
	return $Do('DoChat-post', 'Posting chat',
		$enc(new Chat(null, In, DatimeMin, text), this), This, done,
		undefined, undefined, undefined, undefined, form);
}

DoChat.smiley = function (s) {
	return $img('src', $Do.url + 'DoChat-smiley?' + $enc(s.id, this), 'border', 0);
}
DoChat.smileys = function (m, ss) {
	if (ss)
		for (var i = 0; i < ss.length; i++)
			m.add(DoChat.smiley(ss[i]));
	return m;
}

//********************************************************************************************//

$class.enc(User, Object, ['id'], DoSign, ['name', 'password']);
$class.enc(User, DoUser.update, ['id', 'friends_'], DoUser.get, ['id', 'name']);
$class.enc(Chat, Object, [], DoChat, null);


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


_SignIn = function (box, thisOk, onOk) {
	this.box = box.add(this.err = $p(),
		$p().add($l().tx('User name'), this.name = $ln()),
		$p().add($l().tx('Password'),
			this.pass = $inp('type', 'password', 'keypress', this.doKey, this)),
		$p('s', 'text-align:center').add(
			this.submit = $bn('click', this.doSign, this).tx('Signin / Signup'),
			this.http = $s())
	);
	this.name.focus();
	this.thisOk = thisOk;
	this.onOk = onOk;
}

_SignIn.prototype = {
	doKey: function (e) {
		e.which === 13 && this.doSign();
	},
	doSign: function () {
		this.submit.disabled = true, this.submit.blur();
		$Http(this.http, DoSign.inUp(this.name.value, this.pass.value, this, this.doneSign));
	},
	doneSign: function (ok, err) {
		this.submit.disabled = false;
		ok && this.onOk.call(this.thisOk);
		err && $Err(this.err, err, true);
	}
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


_Me = function (box) {
	this.box = box.add(
		this.name = $d('c', 'name'),
		this.reload = $d('c', 'do').add(
			$a0('click', this.doReload, this).tx('Reload'),
			this.http = $s()),
		this.friends = $d('c', 'friends'),
		this.add = $ln('c', 'name'),
		$s('c', 'do').add(
			$a0('title', 'Chat', 'click', this.doAddChat, this).tx('..'), $tx('  '),
			$a0('title', 'Add', 'click', this.doAdd, this).tx('+'))
	);
	this.doReload();
	var This = this;
	setInterval(function () {
		This.http.firstChild == null && This.doReload();
	}, this.reloadInterval)
}

_Me.prototype = {
	doReload: function () {
		$Http(this.http, DoUser.me(this, this.doneReload));
	},
	doneReload: function (ok, err) {
		if (ok) {
			this.name.tx(__me.name);
			this.friends.des(0);
			for (var s = __me.friends, x = 0; x < s.length; x++)
				new _Me.Friend(this, s[x]);
		}
		if (err)
			if (err instanceof ErrUnsigned && this.onUnsigned)
				this.onUnsigned.call(this.thisUnsigned);
			else
				$Err(this.http, err);
	},

	doAdd: function () {
		var u = new User(null, this.add.value);
		if (__me.friends.indexOf(u.name, 0, 'name') >= 0)
			return this.add.value = '';
		$Http(this.http, DoUser.get([u], this, this.doAdd_user));
	},
	doAdd_user: function (ok, err) {
		if (ok)
			if (ok[0]) {
				var fs = __me.friends.slice();
				fs[fs.length] = ok[0];
				$Http(this.http, DoUser.update(fs, this, this.doneAdd));
			}
			else
				err = 'User not found';
		err && $Err(this.http, err);
	},
	doneAdd: function (ok, err) {
		if (ok) {
			new _Me.Friend(this, __me.friends[__me.friends.length - 1]);
			this.add.value = '';
		}
		err && $Err(this.http, err);
	},

	doAddChat: function () {
		var u = new User(null, this.add.value);
		var x = __me.friends.indexOf(u.name, 0, 'name');
		if (x >= 0)
			return this.onChat && this.onChat.call(this.thisChat, __me.friends[x]);
		$Http(this.http, DoUser.get([u], this, this.doneAddChat));
	},
	doneAddChat: function (ok, err) {
		if (ok)
			if (ok[0])
				this.onChat && this.onChat.call(this.thisChat, ok[0]);
			else
				err = 'User not found';
		err && $Err(this.http, err);
	}
}

_Me.prototype.reloadInterval = 15000;
_Me.prototype.thisUnsigned;
_Me.prototype.onUnsigned;
_Me.prototype.thisChat;
_Me.prototype.onChat;

//********************************************************************************************//

_Me.Friend = function (me, friend) {
	this.me = me;
	this.friend = friend;
	me.friends.add(
		this.left = $a0('c', 'name', 'title', 'Chat',
			'click', me.onChat, me.thisChat, [ friend ]).tx(friend.name),
		this.right = $a0('c', 'do', 'title', 'Remove',
			'click', this.doRem, this).tx('--'));
}

_Me.Friend.prototype = {
	doRem: function () {
		$Http(this.me.http, DoUser.update(
				__me.friends.slice().remove(__me.friends.indexOf(this.friend.id, 0, 'id'), 1),
			this, this.doneRem));
	},
	doneRem: function (ok, err) {
		ok && (this.left.des(), this.right.des());
		err && $Err(this.me.http, err);
	}
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


_Chatss = function (box) {
	this.box = box.add(
		this.tabs = $d('c', 'tabs'),
		this.pull = $d('c', 'do').add(
			$a0('title', 'Any news?', 'click', this.doPull, this).tx('Pull'),
			this.http = $s()),
		this.chatss_ = $d('c', 'chatss')
	);
	this.chatss = [];
	this.active = null;
	this.datime = DatimeMin;
	this.doPull();
	var This = this;
	setInterval(function () {
		This.http.firstChild == null && This.doPull();
	}, this.interval)
}

_Chatss.prototype = {
	doPull: function () {
		$Http(this.http, DoChat.read(new Chat(null, null, this.datime), this, this.donePull));
	},
	donePull: function (ok, err) {
		if (ok) {
			for (var x = 0; x < ok.length; x++) {
				var c = ok[x];
				this.doChat(c.out.id == __me.id ? c.In : c.out, true).doRead(c);
				c.datime.getTime() > this.datime.getTime() && (this.datime = c.datime);
			}
		}
		err && $Err(this.http, err);
	},

	doChat: function (oppoUser, keepAct) {
		var c = this.chatss.indexOf(oppoUser.id, 0, 'oppoId');
		if (c >= 0)
			c = this.chatss[c]
		else
			this.chatss.push(c = new _Chats(this, oppoUser));
		return this.active != null && keepAct || c.doAct(), c;
	}
}

_Chatss.prototype.interval = 3000;

//********************************************************************************************//

_Chats = function (chatss, oppoUser) {
	this.chatss = chatss;
	this.oppo = oppoUser;
	this.oppoId = oppoUser.id;
	this.outDatime = DatimeMin;
	chatss.tabs.add(
		this.tab = $a0('c', 'tab', 'click', this.doAct, this).tx(oppoUser.name));
	chatss.chatss_.add(
		this.chats = $d('c', 'chats'),
		this._post = $lns('c', 'post'),
		this.submit = $bn('c', 'do', 'click', this.doPost, this).tx('Post'),
		this.smileys = $.form().add(
			$inp('type', 'file', 'name', 'smiley'),
			$inp('type', 'file', 'name', 'smiley'))
	);
	this.doInact();
}

_Chats.prototype = {
	doInact: function () {
		this.tab.cla(0, 'tabAct');
		this.chats.show(false), this._post.show(false);
		this.submit.show(false), this.smileys.show(false);
	},
	doAct: function () {
		var a = this.chatss.active;
		if (a != this) {
			a && a.doInact();
	 		this.tab.cla(0, 'tabNew').cla('tabAct');
	 		this.chats.show(true), this._post.show(true).focus();
	 		this.submit.show(true), this.smileys.show(true);
			this.chatss.active = this;
		}
	},

	doRead: function (chat) {
		var out = chat.out.id == __me.id;
		if (out && chat.datime.getTime() <= this.outDatime.getTime())
			return;
		var c = out ? ' out' : ' in';
		var d = chat.datime;
		d = $s('c', 'datime' + c).tx(d.getFullYear() + '-' + (d.getMonth() + 1)
			+ '-' + d.getDate() + ' ' + d.toLocaleTimeString());
		this.chats.add(d, $s('c', 'name' + c).tx(chat.out.name),
			DoChat.smileys($d('c', 'text' + c).tx(chat.text, true), chat.smileys));
		d.scrollIntoView();
		out && (this.outDatime = chat.datime);
		if (this.chatss.active != this)
			this.tab.cla('tabNew');
	},

	doPost: function () {
		this.post = this._post.tx();
		$Http(this.chatss.http,
			DoChat.post(this.smileys, this.oppo, this.post, this, this.donePost), 1, 1);
	},
	donePost: function (ok, err) {
		if (ok) {
			ok.out = __me, ok.In = this.oppo, ok.text = this.post;
			this.doRead(ok);
			this._post.tx() == this.post && this._post.tx('');
		}
		err && $Err(this.chatss.http, err);
	}
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


window.onload = function () {
	var box = $s();
	var pop = $Pop(box);
	$Http(box, DoSign.out(this, function () {
		box.des(true, box = $s('id', 'SignIn'));
		new _SignIn(box, null, function () {
			pop.des(), start();
		});
	}));
}
window.onunload = function () {
	// clean and prevent Firefox memory cache
	$dom.des.call($B, 0);
}

function start() {
	var me = new _Me($dom($id('Me')));
	var chatss = new _Chatss($dom($id('Chatss')));
	me.thisChat = chatss;
	me.onChat = chatss.doChat;
}
