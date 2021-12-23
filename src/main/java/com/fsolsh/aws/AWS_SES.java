package com.fsolsh.aws;

import com.fsolsh.aws.config.AWSProperties;
import com.fsolsh.aws.config.FileType;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.utils.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * aws send email service
 */
@Slf4j
public class AWS_SES {

    private Region region;
    private boolean isReady;

    public AWS_SES(AWSProperties properties) {
        if (!StringUtils.isEmpty(properties.getSes_region()) && !StringUtils.isEmpty(properties.getSes_access_key()) && !StringUtils.isEmpty(properties.getSes_secret_key())) {
            System.setProperty("aws.accessKeyId", properties.getSes_access_key());
            System.setProperty("aws.secretAccessKey", properties.getSes_secret_key());
            this.region = Region.of(properties.getSes_region());
            this.isReady = true;
        }
    }

    public boolean sendHtmlMail(String sender, String recipient, String subject, String bodyHTML) {
        if (!isReady) {
            log.error("sendHtmlMail error, aws-ses initialization failed, to : {}", recipient);
            throw new RuntimeException(" aws-ses initialization failed");
        }
        SesClient client = SesClient.builder().region(region).build();
        try {
            return sendHtmlMail(client, sender, recipient, subject, bodyHTML);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public boolean sendHtmlMailWithAttachment(String sender, String recipient, String subject, String bodyHTML, String attachmentName, FileType fileType, byte[] attachment) throws IOException, MessagingException {
        if (!isReady) {
            log.error("sendHtmlMail error, aws-ses initialization failed, to : {}", recipient);
            throw new RuntimeException(" aws-ses initialization failed");
        }
        SesClient client = SesClient.builder().region(region).build();
        try {
            return sendHtmlMailWithAttachment(client, sender, recipient, subject, bodyHTML, attachmentName, fileType, attachment);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public boolean sendTextMail(String sender, String recipient, String subject, String bodyText) {
        if (!isReady) {
            log.error("sendTextMail error, aws-ses initialization failed, to : {}", recipient);
            throw new RuntimeException(" aws-ses initialization failed");
        }
        SesClient client = SesClient.builder().region(region).build();
        try {
            return sendTextMail(client, sender, recipient, subject, bodyText);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public boolean sendTextMailWithAttachment(String sender, String recipient, String subject, String bodyText, String attachmentName, FileType fileType, byte[] attachment) throws IOException, MessagingException {
        if (!isReady) {
            log.error("sendHtmlMail error, aws-ses initialization failed, to : {}", recipient);
            throw new RuntimeException(" aws-ses initialization failed");
        }
        SesClient client = SesClient.builder().region(region).build();
        try {
            return sendTextMailWithAttachment(client, sender, recipient, subject, bodyText, attachmentName, fileType, attachment);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private boolean sendHtmlMail(SesClient client, String sender, String recipient, String subject, String bodyHTML) {

        Destination destination = Destination.builder().toAddresses(recipient).build();
        Content content = Content.builder().data(bodyHTML).build();
        Content sub = Content.builder().data(subject).build();
        Body body = Body.builder().html(content).build();
        Message msg = Message.builder().subject(sub).body(body).build();

        SendEmailRequest emailRequest = SendEmailRequest.builder().destination(destination).message(msg).source(sender).build();
        SendEmailResponse response = client.sendEmail(emailRequest);

        log.info("sendHtmlMail success to : {}, msgId : {}", recipient, response.messageId());

        return response.messageId() != null;

    }

    private boolean sendTextMail(SesClient client, String sender, String recipient, String subject, String bodyText) {

        Destination destination = Destination.builder().toAddresses(recipient).build();
        Content content = Content.builder().data(bodyText).build();
        Content sub = Content.builder().data(subject).build();
        Body body = Body.builder().text(content).build();
        Message msg = Message.builder().subject(sub).body(body).build();

        SendEmailRequest emailRequest = SendEmailRequest.builder().destination(destination).message(msg).source(sender).build();
        SendEmailResponse response = client.sendEmail(emailRequest);

        log.info("sendTextMail success to : {}, msgId : {}", recipient, response.messageId());

        return response.messageId() != null;
    }

    private boolean sendHtmlMailWithAttachment(SesClient client, String sender, String recipient, String subject, String bodyHTML, String attachmentName, FileType fileType, byte[] attachment) throws MessagingException, IOException {

        Session session = Session.getDefaultInstance(new Properties());

        //subject, from , to
        MimeMessage message = new MimeMessage(session);
        message.setSubject(subject, "UTF-8");
        message.setFrom(new InternetAddress(sender));
        message.setRecipients(javax.mail.Message.RecipientType.TO, recipient);

        //multiPart
        MimeMultipart multiPart = new MimeMultipart();

        //html part
        MimeBodyPart htmlBody = new MimeBodyPart();
        htmlBody.setContent(bodyHTML, "text/html; charset=UTF-8");
        multiPart.addBodyPart(htmlBody);

        //attachment part
        MimeBodyPart attachmentBody = new MimeBodyPart();
        DataSource dataSource = new ByteArrayDataSource(attachment, fileType.getMiniType());
        attachmentBody.setFileName(attachmentName);
        attachmentBody.setDataHandler(new DataHandler(dataSource));
        multiPart.addBodyPart(attachmentBody);

        message.setContent(multiPart);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);

        RawMessage rawMessage = RawMessage.builder().data(SdkBytes.fromByteBuffer(ByteBuffer.wrap(outputStream.toByteArray()))).build();

        SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder().rawMessage(rawMessage).build();
        SendRawEmailResponse rawEmailResponse = client.sendRawEmail(rawEmailRequest);

        log.info("sendHtmlMailWithAttachment success to : {}, msgId : {}", recipient, rawEmailResponse.messageId());

        return rawEmailResponse.messageId() != null;
    }

    private boolean sendTextMailWithAttachment(SesClient client, String sender, String recipient, String subject, String bodyText, String attachmentName, FileType fileType, byte[] attachment) throws MessagingException, IOException {
        Session session = Session.getDefaultInstance(new Properties());

        //subject, from , to
        MimeMessage message = new MimeMessage(session);
        message.setSubject(subject, "UTF-8");
        message.setFrom(new InternetAddress(sender));
        message.setRecipients(javax.mail.Message.RecipientType.TO, recipient);

        //multiPart
        MimeMultipart multiPart = new MimeMultipart();

        //html part
        MimeBodyPart textBody = new MimeBodyPart();
        textBody.setContent(bodyText, "text/plain; charset=UTF-8");
        multiPart.addBodyPart(textBody);

        //attachment part
        MimeBodyPart attachmentBody = new MimeBodyPart();
        DataSource dataSource = new ByteArrayDataSource(attachment, fileType.getMiniType());
        attachmentBody.setFileName(attachmentName);
        attachmentBody.setDataHandler(new DataHandler(dataSource));
        multiPart.addBodyPart(attachmentBody);

        message.setContent(multiPart);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);

        RawMessage rawMessage = RawMessage.builder().data(SdkBytes.fromByteBuffer(ByteBuffer.wrap(outputStream.toByteArray()))).build();

        SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder().rawMessage(rawMessage).build();
        SendRawEmailResponse rawEmailResponse = client.sendRawEmail(rawEmailRequest);

        log.info("sendTextMailWithAttachment success to : {}, msgId : {}", recipient, rawEmailResponse.messageId());

        return rawEmailResponse.messageId() != null;
    }
}

