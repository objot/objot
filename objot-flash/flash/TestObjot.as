package
{

import flash.display.Sprite;

import objot.Codec;

public class TestObjot extends Sprite 
{
	public function TestObjot()
	{
		var x = {};
		x.a = [];
		x.b = x.a;
		trace(new Codec().enc(x, Object));
	}
}

}