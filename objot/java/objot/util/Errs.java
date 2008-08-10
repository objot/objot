//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

import objot.codec.Enc;


public class Errs
	extends Err
{
	@Enc
	public final String[] hints;

	public Errs(String[] s)
	{
		hints = s != null ? s : Array2.STRINGS0;
	}

	public Errs(Object[] s)
	{
		hints = s != null && s.length > 0 ? new String[s.length] : Array2.STRINGS0;
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
