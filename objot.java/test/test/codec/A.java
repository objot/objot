package test.codec;

import java.util.Date;

import objot.codec.Dec;
import objot.codec.Enc;
import objot.codec.EncDec;


public class A
{
	@Enc
	@Dec
	// ( { A.class })
	public Object a2;

	@EncDec
	public Date d;
}
