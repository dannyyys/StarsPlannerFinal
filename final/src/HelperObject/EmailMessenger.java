package HelperObject;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailMessenger implements IMessenger {
    private String recipientEmail;
    private final String username = "cz2002project@gmail.com"; // to be added
    private final String password = "cz20022020"; // to be added
    private final Properties props;

    /**
     * Creates EmailMessenger object.
     * @param recipientEmail String that stores email of recipient
     */
    public EmailMessenger(String recipientEmail) {
        this.recipientEmail = recipientEmail;
        this.props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
    }

    /**
     * Adds Recipient email. Overrides interface method.
     * @param recipientEmail String that stores email of recipient
     */
    @Override
    public void addRecipientEmail(String recipientEmail) {
        this.recipientEmail = this.recipientEmail + "," + recipientEmail;
    }

    /**
     * Sends email to recipient. Overrides interface method.
     * @param subject String that stores subject of email
     * @param text String that stores body content of email
     */
    @Override
    public void sendMessage(String subject, String text) {
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail)); // to be added an email addr
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);
            System.out.println("emails sent to " + recipientEmail);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
