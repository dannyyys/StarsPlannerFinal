package HelperObject;

public interface IMessenger {
    /**
     * Sends email to recipient.
     * @param subject String that stores subject of email
     * @param text String that stores body content of email
     */
    void sendMessage(String subject, String text);

    /**
     * Adds Recipient email.
     * @param recipientEmail String that stores email of recipient
     */
    void addRecipientEmail(String recipientEmail);
}
