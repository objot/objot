package test.codec;
import java.util.List;
import java.util.Map;
import java.util.Set;

import objot.codec.GetSet;


public class B
	extends A
{
	@GetSet
	public String bb;

	@GetSet
	public String a1;

	@GetSet
	public float a3;

	@GetSet
	public String a4;

	@GetSet
	public String a5;

	@GetSet
	public List<Object[]> a6;

	@GetSet
	public Map<String, A> a7;

	@GetSet
	public boolean a8;

	@GetSet
	public Set<String> a9;
}
