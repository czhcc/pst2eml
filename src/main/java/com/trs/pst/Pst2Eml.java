/**
 * 
 */
package com.trs.pst;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.pff.PSTAttachment;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;

/**
 * <p>
 * <b>Pst2Eml</b> 是
 * </p>
 *
 * @since 2019年11月18日
 * @author czhcc
 * @version $Id$
 *
 */
public class Pst2Eml
{
	private final static Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|]");
	
	private final String pstFile;
	
	private final Session session;
	
	private final List<Message> messages = new ArrayList<>();
	
	public Pst2Eml(String pstFile)
	{
		this.pstFile = pstFile;
		this.session = Session.getInstance(System.getProperties());
	}
	
	public void process() throws Exception
	{
		PSTFile pst = new PSTFile(pstFile);
		processFolder(pst.getRootFolder());
	}
	
	protected void processFolder(PSTFolder folder) throws Exception
	{
		if (folder.hasSubfolders()) {
            Vector<PSTFolder> childFolders = folder.getSubFolders();
            for (PSTFolder childFolder : childFolders) {
                processFolder(childFolder);
            }
        }
		
		if (folder.getContentCount() > 0) {
            PSTMessage email = (PSTMessage)folder.getNextChild();
            while (email != null) {
            	writeEml(email);
                email = (PSTMessage)folder.getNextChild();
            }
        }
	}
	
	private void writeEml(PSTMessage pst) throws Exception
	{
		final Message message = new NoChangeMsgIdMimeMessage(session);
		String header = pst.getTransportMessageHeaders();
        if(header == null || "".equals(header))
        {
        	return;
        }
        String[] hh = header.split("\r\n");
        String hx = "";
        String hy = "";
        for(String h1 : hh)
        {
        	if(h1.startsWith(" ") || h1.startsWith("\t"))//有些头信息比较长，有换行，换行后前面会有空格或TAB
        	{
        		hy = hy + h1;
        	}
        	else
        	{
        		int i = h1.indexOf(":");
        		if(i<=0)
        		{
        			hy = hy + h1;
        		}
        		else
        		{
        			message.addHeader(hx, hy);//直接使用pst中的头信息
        			hx = h1.substring(0, i);
        			hy = h1.substring(i+1);
        		}
        	}
        }

        Multipart multipart = new MimeMultipart();
        BodyPart bodyPart = new MimeBodyPart();
        String body = pst.getBody(); 
        bodyPart.setText(body);     
        multipart.addBodyPart(bodyPart);
        
        int nuberOfAttachments = pst.getNumberOfAttachments();
        if (nuberOfAttachments > 0) {
            for (int x = 0; x < nuberOfAttachments; x++) {
            	MimeBodyPart attaPart = new MimeBodyPart();
            	PSTAttachment attachment = pst.getAttachment(x);
            	
            	DataSource source = new PSTAttachmentDataSource(attachment);
            	attaPart.setDataHandler(new DataHandler(source));
            	attaPart.setFileName(attachment.getDisplayName());
            	multipart.addBodyPart(attaPart);
            }
        }
        
        message.setContent(multipart);
        messages.add(message);
	}
	

	/**
	 * @return the messages
	 */
	public List<Message> getMessages()
	{
		return messages;
	}
	
	public void writeEml(List<Message> emlList, String dir) throws Exception
	{
		int index = 0;
		for(Message eml : emlList)
		{
			String subject = eml.getSubject();
			subject = FilePattern.matcher(subject).replaceAll("");//去除subject名称中不适合做文件名的字符
			
			String emlfile = dir + File.separator + subject + "_" + index + ".eml";//加个序号，防止同subject的覆盖
	        eml.writeTo(new BufferedOutputStream(new FileOutputStream(emlfile)));
	        index++;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if(args.length < 1)
		{
			System.out.println("使用方式：Pst2Eml pstfile toemldir");
			System.out.println("pstfile ---- pst文件，例：d:\\dir\\abc.pst");
			System.out.println("toemldir ---- eml文件输出目录， 例： d:\\emldir");
			System.exit(0);
		}
		String pstFile = args[0];
		String emlDir = args[1];
		File f = new File(pstFile);
		if(!f.exists())
		{
			System.out.println("提定的pst文件：" + pstFile + "不存在！");
			System.exit(1);
		}
		if(!pstFile.toLowerCase().endsWith("pst"))
		{
			System.out.println("提定的pst文件：" + pstFile + "不是pst后缀！");
			System.exit(1);
		}
		
		File d = new File(emlDir);
		if(!d.exists())
		{
			System.out.println("指定的eml文件输出目录" + emlDir + "不存在");
			System.exit(1);
		}
		Pst2Eml pst2eml = new Pst2Eml(pstFile);
		try {
			pst2eml.process();
			List<Message> emlList = pst2eml.getMessages();
			pst2eml.writeEml(emlList, emlDir);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
