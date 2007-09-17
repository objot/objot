package test.codec;
import java.util.Date;

import objot.codec.Enc;
import objot.codec.EncDec;
import objot.codec.Dec;


public class A
{
	@Enc
	@Dec
	// ( { A.class })
	public Object a2;

	@EncDec
	public Date d;
}
