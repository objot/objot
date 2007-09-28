package test.codec;

import java.util.Date;

import objot.codec.Enc;
import objot.codec.EncDec;
import objot.codec.Dec;


public class A
	implements Cloneable
{
	@Override
	protected Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Enc
	@Dec
	// ( { A.class })
	public Object a2;

	@EncDec
	public Date d;
}
