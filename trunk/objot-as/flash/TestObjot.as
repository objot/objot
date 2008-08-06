package
{
import flash.display.Sprite;

import objot.codec.Codec;


public class TestObjot extends Sprite
{
	public function TestObjot()
	{
		trace(new Codec().enc(this, null));
	}
}
}
