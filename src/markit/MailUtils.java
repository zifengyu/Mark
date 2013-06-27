package markit;

import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import markit.MarkItDb;

public class MailUtils {

	public static void sendMail(String status_id) throws Exception {
		MarkItDb db = MarkItDb.getInstance();
		String htmlContent = db.getStatusHTML(status_id);
		String to = db.getUserTarget(status_id);
		//String to = "biaojiwei@yeah.net";
		//System.out.println(db.getUserTarget(status_id));
		String from = "biaojiwei@yeah.net";
		String host = "smtp.yeah.net";
		boolean debug = false;

		// create some properties and get the default Session
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", 25);
		props.put("mail.smtp.auth", false);

		Session session = Session.getInstance(props, null);
		session.setDebug(debug);

		try {
			// create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			InternetAddress[] address = {new InternetAddress(to)};
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(db.getTitle(status_id));
			msg.setSentDate(new Date());
			msg.setHeader("X-Mailer", "sendhtml");
			BodyPart messageBodyPart = new MimeBodyPart(); 
			messageBodyPart.setContent(htmlContent, "text/html; charset=utf-8" );
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(messageBodyPart);
			msg.setContent(mp);
			
			msg.saveChanges();

			Transport trans = null;

			// get the smtp transport for the address
			trans = session.getTransport("smtp");

			// register ourselves as listener for ConnectionEvents 
			// and TransportEvents

			trans.connect(host, "biaojiwei@yeah.net", "54746181");


			// connect the transport
			//trans.connect();
			
			// send the message
			trans.sendMessage(msg, msg.getAllRecipients());
			trans.close();
		} catch (MessagingException mex) {
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {


		//String to = "yuzifeng.27ed70a@m.evernote.com";
		try {
			sendMail("3556172848084658");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}

