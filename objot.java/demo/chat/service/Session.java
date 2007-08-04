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

	/** if changed from non null to null, close service session */
	public Integer me;
}
