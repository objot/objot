//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.io.Serializable;

import objot.container.Inject;


@Inject.Inherit
public class Session
	implements Serializable
{
	private static final long serialVersionUID = 7410743024790602503L;

	/** close service session if <0 */
	public int me;
}
