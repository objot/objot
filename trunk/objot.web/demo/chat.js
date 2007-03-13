onerror = function(m, f, l) {
	alert('Error! You could report the following details to http://objot.info\n',
		m, $.throwStack(f, l));
	return true;
}


chat = {

Ok: function () {
	this.message;
},

User: function () {
	this.id;
	this.name;
	this.password;
	this.friends;
},

Chat: function () {
	this.out;
	this.In;
	this.datime;
	this.text;
},


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
	var This = this;
	var go = function () {
		This.submit.tx('Signing ... ').add($img('src', 'loading.gif')).add($tx(' Cancel ?'))
			.retach('onclick', go, stop);
	};
	var stop = function () {
		This.submit.tx('Signin / Signup').retach('onclick', stop, go);
	};
	$dom.add.call(outer,
		$l('style', 'width:12ex').tx('User name'),
		this.name = $ln('style', 'width:20ex'),
		$p(),
		$l('style', 'width:12ex').tx('Password'),
		this.pass = $inp('type', 'password', 'style', 'width:20ex'),
		$p(),
		$d('style', 'text-align:center').add(
			this.submit = $bn('onclick', go).tx('Signin / Signup'))
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
