//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//

onerror = function(m, f, l) {
	alert('Error! You could report the following details to http://objot.info\n\n'
		+ m + '\n' + $.throwStack(f, l));
	return true;
}

$Do.Url = '/objot/service/';
$Do.Timeout = 5000;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


Ok = function (hint) {
	this.hint = $(hint);
}
ErrUnsigned = function (hint) {
	this.hint = $(hint);
}
$class(true, 'Ok');
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

$class(true, 'User');
$class(true, 'Chat');


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

//********************************************************************************************//

DoUser = function () {
}

DoUser.me = function (This, done) {
	return $Do('DoUser-me', 'Loading my info', '', This, done, null, this.doneMe);
}
	DoUser.doneMe = function (ok, err) {
		ok && (_me = $.is(ok, User), _me.friends.sort(function (a, b) {
			return a.name < b.name ? -1 : a.name > b.name ? 1 : 0; 
		}));
	}

DoUser.update = function (friends, This, done) {
	var me = new User();
	me.friends_ = friends;
	var h = $Do('DoUser-update', 'Updating my info', $get(me, this.update),
		This, done, null, this.doneUpdate);
	h.me = me;
	return h;
}
	DoUser.doneUpdate = function (ok, err, h) {
		ok && (_me.friends = h.me.friends_);
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

DoChat.post = function (In, text, This, done) {
	return $Do('DoChat-post', 'Posting chat',
		$get(new Chat(null, In, DatimeMin, text), this), This, done);
}

//********************************************************************************************//

$class(false, 'DoSign');
$class(false, 'DoUser');

$class.get(User, Object, ['id'], DoSign, ['name', 'password']);
$class.get(User, DoUser.update, ['id', 'friends_'], DoUser.get, ['id', 'name']);
$class.get(Chat, DoChat, null);


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


_SignIn = function (box, thisOk, onOk) {
	this.box = box.add(this.err = $p(),
		$p().add($l().tx('User name'), this.name = $ln()),
		$p().add($l().tx('Password'),
			this.pass = $inp('type', 'password', 'keypress', this.doKey, 'this', this)),
		$p('s', 'text-align:center').add(
			this.submit = $bn('click', this.doSign, 'this', this).tx('Signin / Signup'),
			this.http = $s())
	);
	this.name.focus();
	this.thisOk = thisOk;
	this.onOk = onOk;
}

_SignIn.prototype = {
	doKey: function (e) {
		if (e.which === 13)
			this.doSign();
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
			$a0('click', this.doReload, 'this', this).tx('Reload'),
			this.http = $s()),
		this.friends = $d('c', 'friends'),
		this.add = $ln('c', 'name'),
		$s('c', 'do').add(
			$a0('title', 'Chat', 'click', this.doAddChat, 'this', this).tx('..'), $tx('  '),
			$a0('title', 'Add', 'click', this.doAdd, 'this', this).tx('+'))
	);
	this.doReload();
	var This = this;
	setInterval(function () {
		This.http.firstChild == null && This.doReload();
	}, this.ReloadInterval)
}

_Me.prototype = {
	doReload: function () {
		$Http(this.http, DoUser.me(this, this.doneReload));
	},
	doneReload: function (ok, err) {
		if (ok) {
			this.name.tx(_me.name);
			this.friends.des(0);
			for (var s = _me.friends, x = 0; x < s.length; x++)
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
		if (_me.friends.indexOf(u.name, 0, 'name') >= 0)
			return this.add.value = '';
		$Http(this.http, DoUser.get([u], this, this.doAdd_user));
	},
	doAdd_user: function (ok, err) {
		if (ok)
			if (ok[0]) {
				var fs = _me.friends.slice();
				fs[fs.length] = ok[0];
				$Http(this.http, DoUser.update(fs, this, this.doneAdd));
			}
			else
				err = 'User not found';
		err && $Err(this.http, err);
	},
	doneAdd: function (ok, err) {
		if (ok) {
			new _Me.Friend(this, _me.friends[_me.friends.length - 1]);
			this.add.value = '';
		}
		err && $Err(this.http, err);
	},

	doAddChat: function () {
		var u = new User(null, this.add.value);
		var x = _me.friends.indexOf(u.name, 0, 'name');
		if (x >= 0)
			return this.onChat && this.onChat.call(this.thisChat, _me.friends[x]);
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

_Me.prototype.ReloadInterval = 15000;
_Me.prototype.thisUnsigned;
_Me.prototype.onUnsigned;
_Me.prototype.thisChat;
_Me.prototype.onChat;

//********************************************************************************************//

_Me.Friend = function (me, friend) {
	this.Me = me;
	this.Friend = friend;
	me.friends.add(
		this.left = $a0('c', 'name', 'title', 'Chat',
			'click', this.doChat, 'this', this).tx(friend.name),
		this.right = $a0('c', 'do', 'title', 'Remove',
			'click', this.doRem, 'this', this).tx('--'));
}

_Me.Friend.prototype = {
	doRem: function () {
		$Http(this.Me.http, DoUser.update(
				_me.friends.slice().remove(_me.friends.indexOf(this.Friend.id, 0, 'id'), 1),
			this, this.doneRem));
	},
	doneRem: function (ok, err) {
		ok && (this.left.des(), this.right.des());
		err && $Err(this.Me.http, err);
	},
	
	doChat: function () {
		this.Me.onChat && this.Me.onChat.call(this.Me.thisChat, this.Friend);
	}
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


_Chatss = function (box) {
	this.box = box.add(
		this.tabs = $d('c', 'tabs'),
		this.pull = $d('c', 'do').add(
			$a0('title', 'Any news?', 'click', this.doPull, 'this', this).tx('Pull'),
			this.http = $s()),
		this.chatss = $d('c', 'chatss')
	);
	this.Chatss = [];
	this.Active = null;
	this.Datime = DatimeMin;
	this.doPull();
	var This = this;
	setInterval(function () {
		This.http.firstChild == null && This.doPull();
	}, this.Interval)
}

_Chatss.prototype = {
	doPull: function () {
		$Http(this.http, DoChat.read(new Chat(null, null, this.Datime), this, this.donePull));
	},
	donePull: function (ok, err) {
		if (ok) {
			for (var x = 0; x < ok.length; x++) {
				var c = ok[x];
				this.doChat(c.out.id == _me.id ? c.In : c.out, true).doRead(c);
				c.datime.getTime() > this.Datime.getTime() && (this.Datime = c.datime);
			}
		}
		err && $Err(this.http, err);
	},
	
	doChat: function (oppoUser, keepAct) {
		var c = this.Chatss.indexOf(oppoUser.id, 0, 'OppoId');
		if (c >= 0)
			c = this.Chatss[c]
		else
			c = new _Chats(this, oppoUser), this.Chatss.push(c);
		return this.Active != null && keepAct || c.doAct(), c;
	}
}

_Chatss.prototype.Interval = 3000;

//********************************************************************************************//

_Chats = function (chatss, oppoUser) {
	this.Chatss = chatss;
	this.OppoId = oppoUser.id;
	this.Oppo = oppoUser;
	this.OutDatime = DatimeMin;
	chatss.tabs.add(
		this.tab = $a0('c', 'tab', 'click', this.doAct, 'this', this).tx(oppoUser.name));
	chatss.chatss.add(
		this.chats = $d('c', 'chats'),
		this.post = $lns('c', 'post'),
		this.submit = $bn('c', 'do', 'click', this.doPost, 'this', this).tx('Post'));
	this.doInact();
}

_Chats.prototype = {
	doInact: function () {
		this.tab.cla(0, 'tabAct');
		this.chats.show(false), this.post.show(false), this.submit.show(false);
	},
	doAct: function () {
		var a = this.Chatss.Active;
		if (a != this) {
			a && a.doInact();
	 		this.tab.cla(0, 'tabNew').cla('tabAct'), this.chats.show(true),
			this.post.show(true).focus(), this.submit.show(true);
			this.Chatss.Active = this;
		}
	},

	doRead: function (chat) {
		var out = chat.out.id == _me.id;
		if (out && chat.datime.getTime() <= this.OutDatime.getTime())
			return;
		var c = out ? ' out' : ' in';
		var d = chat.datime;
		d = $s('c', 'datime' + c).tx(d.getFullYear() + '-' + (d.getMonth() + 1)
			+ '-' + d.getDate() + ' ' + d.toLocaleTimeString());
		this.chats.add(d, $s('c', 'name' + c).tx(chat.out.name),
			$d('c', 'text' + c).tx(chat.text, true));
		d.scrollIntoView();
		out && (this.OutDatime = chat.datime);
		if (this.Chatss.Active != this)
			this.tab.cla('tabNew');
	},
	
	doPost: function () {
		this.Post = this.post.value;
		$Http(this.Chatss.http, DoChat.post(this.Oppo, this.Post, this, this.donePost));
	},
	donePost: function (ok, err) {
		if (ok) {
			ok.out = _me, ok.In = this.Oppo, ok.text = this.Post;
			this.doRead(ok);
			this.post.value == this.Post && (this.post.value = '');
		}
		err && $Err(this.Chatss.http, err);
	}
}


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//


window.onload = function () {
	var box = $s();
	var pop = $Pop(box);
	$Http(box, DoSign.out(this, function () {
		box = $s('id', 'SignIn').des(true, box);
		new _SignIn(box, null, function () {
			pop.des(), start();
		});
	}));
}
window.onunload = function () {
	// clean and prevent Firefox memory cache
	$dom.des.call($D.body, 0);
}

function start() {
	var me = new _Me($dom($id('Me')));
	var chatss = new _Chatss($dom($id('Chatss')));
	me.thisChat = chatss;
	me.onChat = chatss.doChat;
}
