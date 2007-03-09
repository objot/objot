import objot.servlet.Service;


public class DemoService
{
	@Service
	public Object index(Object req) throws Exception
	{
		return new String[] { "Hello" };
	}

	@Service
	public Object echo(Object req) throws Exception
	{
		return req;
	}
}
