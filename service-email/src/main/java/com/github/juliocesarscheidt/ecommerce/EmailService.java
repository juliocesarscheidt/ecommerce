package com.github.juliocesarscheidt.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Properties;

import javax.mail.Address;
// import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
// import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

	private void parse(ConsumerRecord<String, com.github.juliocesarscheidt.ecommerce.Message<Email>> record) {
		System.out.println("[INFO] key " + record.key()
						  + " | value " + record.value()
						  + " | topic " + record.topic()
						  + " | partition " + record.partition()
						  + " | offset " + record.offset());

		var message = record.value();
		Email emailContent = message.getPayload();
		String emailDestination = emailContent.getSubject();
		String emailBody = emailContent.getBody();

		sendEmail(emailDestination, emailBody);
	}

	public static void main(String[] args) {
		EmailService emailService = new EmailService();
		try (KafkaConsumerService<Email> service = new KafkaConsumerService<>("ECOMMERCE_SEND_EMAIL",
																			emailService.getClass().getSimpleName(),
																			emailService::parse)) {
			service.run();
		}
	}

	private void sendEmail(String emailDestination, String emailBody) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "172.16.0.3");
	    props.put("mail.smtp.socketFactory.port", "1025");
	    // props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	    props.put("mail.smtp.auth", "false");
	    props.put("mail.smtp.port", "1025");

	    // Authenticator auth = new Authenticator() {
	    //     public PasswordAuthentication getPasswordAuthentication() {
	    //         return new PasswordAuthentication("", "");
	    //     }
	    // };
 
        // Session session = Session.getInstance(props, auth);

	    Session session = Session.getInstance(props);
	    session.setDebug(true);

	    try {
	        Message message = new MimeMessage(session);
	        message.setFrom(new InternetAddress("sender@mailhog.com"));
	        Address[] destinationUser = InternetAddress.parse(emailDestination);

	        message.setContent(emailBody, "text/html; charset=utf-8");
        
	        message.setRecipients(Message.RecipientType.TO, destinationUser);
	        message.setSubject("Ecommerce welcome email");

	        Transport.send(message);

	        System.out.println("Email sent to " + destinationUser);
	
	     } catch (MessagingException e) {
	    	 throw new RuntimeException(e);
	     }
	}
}
