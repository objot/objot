import objot.Get;
import objot.Set;


public class A
{
	@Get
	@Set( { A.class })
	public Object a2;
}
