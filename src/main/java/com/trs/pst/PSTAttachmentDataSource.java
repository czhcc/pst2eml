/**
 * 
 */
package com.trs.pst;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.pff.PSTAttachment;
import com.pff.PSTException;

/**
 * <p>
 * <b>PSTAttachmentDataSource</b> 是
 * </p>
 *
 * @since 2019年11月18日
 * @author czhcc
 * @version $Id$
 *
 */
public class PSTAttachmentDataSource implements DataSource
{
	private PSTAttachment attachment;
	public PSTAttachmentDataSource(PSTAttachment attachment)
	{
		this.attachment = attachment;
	}

	@Override
	public String getContentType()
	{
		String result = this.attachment.getMimeTag();
		if(result == null || "".equals(result))
		{
			result = "text/plain";
		}
		
		return result; 
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		try {
			return this.attachment.getFileInputStream();
		}
		catch (PSTException e) {
			throw new IOException(e);
		}
	}

	@Override
	public String getName()
	{
		return this.attachment.getDisplayName();
	}

	@Override
	public OutputStream getOutputStream() throws IOException
	{
		return null;
	}
}
