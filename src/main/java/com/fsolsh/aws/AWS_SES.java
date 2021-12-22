package com.fsolsh.aws;

import com.fsolsh.aws.config.AWSProperties;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.utils.StringUtils;

/**
 * AWS_SES
 */
@Slf4j
public class AWS_SES {

    private static Region region;
    private static boolean isReady;

    public AWS_SES(AWSProperties properties) {
        if (!StringUtils.isEmpty(properties.getSes_region()) && !StringUtils.isEmpty(properties.getSes_access_key()) && !StringUtils.isEmpty(properties.getSes_secret_key())) {
            System.setProperty("aws.accessKeyId", properties.getSes_access_key());
            System.setProperty("aws.secretAccessKey", properties.getSes_secret_key());
            region = Region.of(properties.getSes_region());
            isReady = true;
        }
    }

    public boolean sendHtmlMail(
            String sender,
            String recipient,
            String subject,
            String bodyHTML
    ) {
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

    public boolean sendTextMail(
            String sender,
            String recipient,
            String subject,
            String bodyText
    ) {
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

    private boolean sendHtmlMail(SesClient client,
                                 String sender,
                                 String recipient,
                                 String subject,
                                 String bodyHTML
    ) {

        Destination destination = Destination.builder()
                .toAddresses(recipient)
                .build();

        Content content = Content.builder()
                .data(bodyHTML)
                .build();

        Content sub = Content.builder()
                .data(subject)
                .build();

        Body body = Body.builder()
                .html(content)
                .build();

        Message msg = Message.builder()
                .subject(sub)
                .body(body)
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .message(msg)
                .source(sender)
                .build();
        SendEmailResponse response = client.sendEmail(emailRequest);

        log.info("sendHtmlMail success to : {}, msgId : {}", recipient, response.messageId());

        return response.messageId() != null;

    }

    private boolean sendTextMail(SesClient client,
                                 String sender,
                                 String recipient,
                                 String subject,
                                 String bodyText
    ) {

        Destination destination = Destination.builder()
                .toAddresses(recipient)
                .build();

        Content content = Content.builder()
                .data(bodyText)
                .build();

        Content sub = Content.builder()
                .data(subject)
                .build();

        Body body = Body.builder()
                .text(content)
                .build();

        Message msg = Message.builder()
                .subject(sub)
                .body(body)
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .message(msg)
                .source(sender)
                .build();

        SendEmailResponse response = client.sendEmail(emailRequest);

        log.info("sendTextMail success to : {}, msgId : {}", recipient, response.messageId());

        return response.messageId() != null;
    }
}
