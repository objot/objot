onerror = function(m, f, l) {
	alert('Error! You could report the following details to http://objot.info\n',
		m, $.throwStack(f, l));
	return true;
}


chat = {
}

chat.Ok = function () {
	this.message;
}
$class('chat.Ok');

chat.User = function (id, name, pass) {
	this.id = id;
	this.name = name;
	this.password = pass;
	this.friends;
}
$class('chat.User');

chat.Chat = function () {
	this.out;
	this.In;
	this.datime;
	this.text;
}
$class('chat.Chat');

chat.Url = '/objot/service/';
chat.Timeout = 5000;

chat.DoSign = function () {
}
$class('chat.DoSign');

chat.DoUser = function () {
}
$class('chat.DoUser');


$class.get(chat.User, Object, ['id'], chat.DoSign, ['name', 'password'],
	chat.DoUser, ['friends']);


/** @return an image node */
function http(outer, service, hint, data, dataFor, onDone, This) {
	outer.ins(outer = $img('src', 'loading.gif', 'className', 'http',
		'title', $(hint) + ' Abort?', 'ondblclick',
		$http(chat.Url + service, chat.Timeout, $get(data, dataFor), onDone, This)));
	return outer;
}
function done(code, res) {
	return code == 0 ? $set(res) : code < 0 ? null : new Error('HTTP ' + code + ' : ' + res);
}

function SignIn(outer) {
	$dom.ins.call(outer,
		this.nameL = $l().tx('User name'), this.name = $ln(),
		$p(),
		this.passL = $l().tx('Password'), this.pass = $inp('type', 'password'),
		$p(),
		$d('style', 'text-align:center').ins
			(this.submit = $bn('onclick', this.on).tx('Signin / Signup'))
	);
	this.submit.$ = this;
}
$class('SignIn', chat.DoSign);
SignIn.prototype.on = function () {
	if (this.submit.disabled)
		return;
	this.submit.disabled = true, this.submit.blur();
	this.http = http(this.submit.parentNode, 'DoSign-inUp', 'Signing ...',
		new chat.User(0, this.name.value, this.pass.value), chat.DoSign,
		function (code, res, This) {
			res = done(code, res);
			if (res instanceof chat.Ok)
				location.href = location.href.replace(/[^\/]*$/, 'chat.html');
			else
				This.submit.disabled = false, This.http.noleak().ins(),
				res === null || $throw(res);
		}, this);
}

