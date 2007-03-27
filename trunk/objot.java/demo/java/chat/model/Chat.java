//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.model;

import objot.GetSet;
import objot.Name;
import chat.service.DoChat;


/**
 * a chat message. PO as SO directly. I prefer "Chat" to "Message" just for less letters,
 * am I lazy ?
 */
@GetSet(DoChat.class)
public class Chat
{
	// @ManyToOne
	@GetSet
	public User out;

	// @ManyToOne
	@GetSet
	@Name("In")
	public User in;

	@GetSet
	public long datime;

	@GetSet
	public String text;
}
