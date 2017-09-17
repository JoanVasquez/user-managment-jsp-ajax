package userprocesses.controller;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//EMAIL CLASS
public class SendForgottenPassword {

	//METHOD TO SEND THE EMAIL
	public static void sendEmail(String to, String forgottenPassword) throws AddressException, MessagingException {
		//CONFIG EMAIL SERVER PROPERTIES
		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Authenticator authenticator = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("xxxx@xxxx.com", "xxxx");
			}
		};
		
		Session session = Session.getInstance(props, authenticator);

		//SEND EMAIL
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress("xxxx22@xxxx.com"));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		message.setSubject("Forgotten pass");
		message.setText("Hello, we are writing remind you that" + "\n your password is: " + forgottenPassword + ""
				+ "\n \n if you haven't clicked forgotten password, please ignore this message. Thank you.");

		Transport.send(message);
	}

}
