package chat.model;

import objot.Get;
import objot.GetSet;
import objot.Name;
import chat.service.DoChat;


/**
 * a chat message. PO as SO directly. I prefer "Chat" to "Message" just for less letters,
 * am I lazy ?
 */
@GetSet(DoChat.class /* more clear here than User */)
public class Chat
{
	// @ManyToOne
	@GetSet
	public User out;

	// @ManyToOne
	@GetSet
	@Name("In")
	public User in;

	@Get
	public long datime;

	@GetSet
	public String text;
}