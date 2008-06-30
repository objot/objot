package test.codec;

import java.util.List;
import java.util.Map;
import java.util.Set;

import objot.codec.EncDec;


public class B
	extends A
{
	@EncDec
	public String bb;

	@EncDec
	public int a1;

	@EncDec
	public float a3;

	@EncDec
	public String a4;

	@EncDec
	public long a5;

	@EncDec
	public List<Object[]> a6;

	@EncDec
	public Map<String, A> a7;

	@EncDec
	public boolean a8;

	@EncDec
	public Set<String> a9;

	@EncDec
	public double a10;

}
