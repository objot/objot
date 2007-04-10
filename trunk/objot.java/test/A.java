import java.util.Date;

import objot.Get;
import objot.GetSet;
import objot.Set;


public class A
{
	@Get
	@Set
	// ( { A.class })
	public Object a2;

	@GetSet
	public Date d;
}
