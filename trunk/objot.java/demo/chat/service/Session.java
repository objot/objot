//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.io.Serializable;

import chat.Scope;


@Scope.Session
public class Session
	implements Serializable
{
	private static final long serialVersionUID = 7410743024790602503L;

	/** if changed from not 0 to 0, close service session */
	public int me;
}
