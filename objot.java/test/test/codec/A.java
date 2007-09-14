package test.codec;
import java.util.Date;

import objot.codec.Get;
import objot.codec.GetSet;
import objot.codec.Set;


public class A
{
	@Get
	@Set
	// ( { A.class })
	public Object a2;

	@GetSet
	public Date d;
}
