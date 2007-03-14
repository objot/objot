onerror = function(m, f, l) {
	alert('Error! You could report the following details to http://objot.info\n',
		m, $.throwStack(f, l));
	return true;
}


chat = {

Ok: function () {
	this.message;
},

User: function (id, name, pass) {
	this.id = id;
	this.name = name;
	this.password = pass;
	this.friends;
},

Chat: function () {
	this.out;
	this.In;
	this.datime;
	this.text;
},

Url: '/objot/service/',
Timeout: 5000,

DoSign: function () {},
DoUser: function () {}


}


/** @return an image node */
function http(outer, service, hint, data, dataFor, onDone, This) {
	outer.ins(outer = $img('src', 'loading.gif', 'className', 'http',
		'title', $(hint) + ' Abort?', 'ondblclick',
		$http(chat.Url + service, chat.Timeout, $get(data, dataFor), onDone, This)));
	return outer;
}
function done(code, res) {
	return code === 0 || code === 500 ? $set(res) :
		code < 0 ? null : new Error('HTTP ' + code + ' : ' + res); 
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
SignIn.prototype.on = function () {
	this.submit.disabled = true;
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


function demoH(){
	$http(location.protocol + '//' + location.host + url.value, null, -1
	, function (code, data, http){
		$d.title = code + ' ' + data.length;
		text.value = U = data;
	}, function (code, http){
		$d.title = code;
	});
}

//$class('A');
//$class('B', A);
//$class.get(B, demoG, ['a1'], Object, null);
