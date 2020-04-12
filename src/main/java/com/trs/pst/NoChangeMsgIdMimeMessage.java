/**
 * 
 */
package com.trs.pst;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * <p>
 * <b>MyMimeMessage</b> 是
 * </p>
 *
 * @since 2019年11月18日
 * @author czhcc
 * @version $Id$
 *
 */
public class NoChangeMsgIdMimeMessage extends MimeMessage
{
	/**
	 * @param session
	 */
	public NoChangeMsgIdMimeMessage(Session session) {
		super(session);
	}

	/**
	 * 使用PST里的message-id值，不要自动生成
	 */
	@Override
	protected void updateMessageID() throws MessagingException
	{
	}
}
