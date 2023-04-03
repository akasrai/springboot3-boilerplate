package com.springboot3.boilerplate.miscellaneous.mail;

import com.springboot3.boilerplate.config.MailConfig;
import jakarta.activation.DataHandler;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

@Service
public class MailService {

    private final MailConfig mailConfig;

    private final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    MailService(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    public boolean sendMail(String toEmail, String subject, String body) {
        try {
            Session session = getSession(getProperties());
            MimeMessage message = getMimeMessage(session, subject, toEmail);
            message.setContent(body, "text/html");
            transport(session, message);

            return true;
        } catch (Exception ex) {
            logger.error("Error while sending mail to email: {}", toEmail, ex);
        }

        return false;
    }

    public boolean sendMail(String toEmail, String subject, String body, String attachment) {
        try {
            Session session = getSession(getProperties());
            MimeMessage message = getMimeMessage(session, subject, toEmail);
            message.setContent(getMultiPart(body, attachment));
            transport(session, message);

            return true;
        } catch (Exception ex) {
            logger.error("Error while sending mail to email: {}", toEmail, ex);
        }

        return false;
    }

    private Properties getProperties() {
        Properties properties = System.getProperties();
        properties.put("mail.transport.protocol", mailConfig.getProtocol() );
        properties.put("mail.smtp.port", mailConfig.getPort());
        properties.put("mail.smtp.starttls.enable", mailConfig.isStarttls() );
        properties.put("mail.smtp.auth", mailConfig.isAuth());
        properties.put("mail.smtp.ssl.enable", mailConfig.isSsl());

        return properties;
    }

    private Session getSession(Properties props) {
        Session session = Session.getDefaultInstance(props);
        session.setDebug(mailConfig.isDebug());

        return session;
    }

    private MimeMessage getMimeMessage(Session session, String subject, String toEmail) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(mailConfig.getUsername());
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject(subject);

        return message;
    }

    private Multipart getMultiPart(String body, String attachment) throws MessagingException
            , MalformedURLException {
        Multipart multipart = new MimeMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();

        messageBodyPart.setContent(body, "text/html");
        multipart.addBodyPart(messageBodyPart);

        URL url = new URL(attachment);
        BodyPart attachmentFile = new MimeBodyPart();
        attachmentFile.setDataHandler(new DataHandler(url));
        attachmentFile.setDisposition(Part.ATTACHMENT);
        attachmentFile.setFileName("filename");
        multipart.addBodyPart(attachmentFile);

        return multipart;
    }

    private void transport(Session session, MimeMessage message) throws MessagingException {
        Transport transport = session.getTransport();
        transport.connect(mailConfig.getHost(), mailConfig.getUsername(), mailConfig.getPassword());
        transport.sendMessage(message, message.getAllRecipients());
    }
}
