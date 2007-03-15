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


////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\


chat = {
	Url: '/objot/service/',
	Timeout: 5000
}

chat.Ok = function (hint) {
	this.hint = hint || '';
}
chat.Err = function (hint) {
	this.hint = hint || '';
}
$class('chat.Ok');
$class('chat.Err');


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

////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\

chat.DoSign = function () {
	this.doInUpDone;
	this.doOutDone;
}
chat.DoSign.prototype.doInUp = function (name, pass) {
	this.http = http('DoSign-inUp', 'Signing ...',
		new chat.User(0, name, pass), chat.DoSign, this.doInUpDone, this);
}

chat.DoUser = function () {
}

chat.DoChat = function () {
}
$class('chat.DoSign');
$class('chat.DoUser');

////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\

$class.get(chat.User, Object, ['id'], chat.DoSign, ['name', 'password'],
	chat.DoUser, ['friends']);
$class.get(chat.Chat, chat.DoChat, ['out', 'In', 'text']);


////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\


/** @return an image node */
function http(service, hint, data, dataFor, onDone, This) {
	return $img('src', 'http.gif', 'className', 'http',
		'title', $(hint) + ' Abort?', 'ondblclick',
		$http(chat.Url + service, chat.Timeout, $get(data, dataFor), function (code, res) {
			onDone.call(This, code, code == 0 ? $set(res) : code < 0 ? null
				: new chat.Err('HTTP Error ' + code + ' : ' + res));
		}));
}
/** @return a span node */
function error(err, hide) {
	err instanceof chat.Err && (err = err.hint);
	$fox && (err = err + '\n' + $.throwStack());
	var _ = $s('className', 'error');
	hide ? _.att('title', err) : _.tx(err);
	return _.add(0, $img('src', 'error.gif', 'className', 'error'));
}


////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\////\\\\


function SignIn(outer) {
	outer.add(this.err = $s(),
		$p().add($l().tx('User name'), this.name = $ln()),
		$p().add($l().tx('Password'), this.pass = $inp('type', 'password')),
		$p('style', 'text-align:center').add
			(this.submit = $bn('onclick', this.on).tx('Signin / Signup'))
	);
	this.submit.$ = this;
}
SignIn.prototype.on = function () {
	if (this.submit.disabled)
		return;
	this.submit.disabled = true, this.submit.blur();
	this.err.add(-1, this.err = $s());
	this.doInUp(this.name.value, this.pass.value);
	this.submit.parentNode.add(this.http);
}
SignIn.prototype.doInUpDone = function (code, res) {
	this.submit.disabled = false;
	 this.http.noleak().rem();
	if (res instanceof chat.Ok)
		this.done.call(this.doneThis);
	else if (res !== null)
		this.err.add(-1, this.err = error(res));
}
$class('SignIn', chat.DoSign);

