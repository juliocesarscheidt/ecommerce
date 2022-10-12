package com.github.juliocesarscheidt.ecommerce;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import jakarta.mail.Address;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

	private void sendEmail(String emailDestination, String emailBody) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "172.16.0.3");
	    // props.put("mail.smtp.socketFactory.port", "465");
	    props.put("mail.smtp.socketFactory.port", "1025");
	    // props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	    props.put("mail.smtp.auth", "false");
	    // props.put("mail.smtp.port", "465");
	    props.put("mail.smtp.port", "1025");

	    Session session = Session.getDefaultInstance(props, new Authenticator() {
	    	protected PasswordAuthentication getPasswordAuthentication() {
        	   return new PasswordAuthentication("", "");
	    	}
	    });
	    session.setDebug(true);

	    try {
	        Message message = new MimeMessage(session);
	        message.setFrom(new InternetAddress("sender@mailhog.com"));
	        Address[] destinationUser = InternetAddress.parse(emailDestination);

	        message.setRecipients(Message.RecipientType.TO, destinationUser);
	        message.setSubject("Ecommerce welcome");
	        message.setText(emailBody);

	        Transport.send(message);

	        System.out.println("Email sent");
	
	     } catch (MessagingException e) {
	    	 throw new RuntimeException(e);
	     }
	}

	private void parse(ConsumerRecord<String, Email> record) {
		System.out.println("[INFO] key " + record.key()
						  + " | value " + record.value()
						  + " | topic " + record.topic()
						  + " | partition " + record.partition()
						  + " | offset " + record.offset());
		
		Email emailContent = record.value();
		String emailDestination = emailContent.getSubject();
		String emailBody = emailContent.getBody();
		
		sendEmail(emailDestination, emailBody);
	}

	public static void main(String[] args) {
		EmailService emailService = new EmailService();		
		try (KafkaConsumerService<Email> service = new KafkaConsumerService<>("ECOMMERCE_SEND_EMAIL",
																			emailService.getClass().getSimpleName(),
																			emailService::parse,
																			Email.class)) {
			service.run();
		}
	}
}
