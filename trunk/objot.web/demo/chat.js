function onerror(m, f, l) {
	alert('Error! You could report the following details to http://objot.info\n',
		m, $.throwStack(f, l));
	return true;
}

chat = {

User: function () {
	this.id;
	this.name;
	this.password;
	this.friends;
}

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
