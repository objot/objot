//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

public class Errs
	extends Err
{
	@Get
	public final String[] hints;

	public static final String[] HINTS0 = {};

	public Errs(String[] s)
	{
		hints = s != null ? s : HINTS0;
	}

	public Errs(Object[] s)
	{
		hints = s != null && s.length > 0 ? new String[s.length] : HINTS0;
		for (int x = 0; x < hints.length; x++)
			hints[x] = String.valueOf(s[x]);
	}

	private String hint_;

	@Override
	public String toString()
	{
		if (hint_ == null)
		{
			StringBuilder s = new StringBuilder(128);
			for (String h: hints)
				s.append(h);
			hint_ = s.toString();
		}
		return hint_;
	}
}
