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
Timeout: 1,

DoSign: {
	
	inUp: function () {
	},

	out: function () {
		
	}
},

DoUser: {
}

}


function signIn(outer) {
	var _ = this;
	var click = function () {
		_.submit.retach('onclick', click, stop).att('title', 'Cancel').tx('Signing ')
			.ins($img('src', 'loading.gif', 'style', 'vertical-align:middle'));
		$http(chat.Url + 'DoSign-inUp', chat.Timeout,
			new chat.User(0, _.name.value, _.pass.value),
			done, undone);
	}
	var stop = function () {
		_.submit.retach('onclick', stop, click).att('title', null).tx('Signin / Signup');
	}
	var done = function (code, data, http) {
		if (data instanceof chat.Ok)
			location.href = location.href.replace(/[^\/]*$/, 'chat.html');
		else
			stop(), $throw(data.message);
	}
	var undone = function (code, http) {
		stop(), $throw('HTTP ' + code);
	}
	$dom.ins.call(outer,
		$l('style', 'width:12ex').tx('User name'),
		this.name = $ln('style', 'width:20ex'),
		$p(),
		$l('style', 'width:12ex').tx('Password'),
		this.pass = $inp('type', 'password', 'style', 'width:20ex'),
		$p(),
		$d('style', 'text-align:center').ins
			(this.submit = $bn('onclick', click).tx('Signin / Signup'))
	);
	return this;
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
