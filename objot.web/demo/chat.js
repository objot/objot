function onerror(m, f, l) {
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
		
	},
},

DoUser: {
}

}


function signIn(outer) {
	var This = this;
	$dom.add.call(outer, this.shell = $tab().add($tb().add(
		$tr().add($td().tx('User name'), $td(), $td().add(
			this.name = $ln('style', 'width:100%'))),
		$tr().add($td()),
		$tr().add($td().tx('Password'), $td(), $td().add(
			this.pass = $inp('type', 'password', 'style', 'width:100%'))),
		$tr().add($td()),
		$tr().add($td().att('colspan', 99, 'align', 'center').add(
			$bn('value', 'Signin / Signup', 'onclick', function () {
			})))
	)));
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
