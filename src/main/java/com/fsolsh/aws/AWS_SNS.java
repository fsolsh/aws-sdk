package com.fsolsh.aws;

import com.fsolsh.aws.config.AWSProperties;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.utils.StringUtils;

/**
 * AWS_SNS
 */
@Slf4j
public class AWS_SNS {

    private static Region region;
    private static boolean isReady;
    private AwsCredentialsProvider provider;

    public AWS_SNS(AWSProperties properties) {
        if (!StringUtils.isEmpty(properties.getSns_region()) && !StringUtils.isEmpty(properties.getSns_access_key()) && !StringUtils.isEmpty(properties.getSns_secret_key())) {
            region = Region.of(properties.getSns_region());
            AwsCredentials awsCredentials = AwsBasicCredentials.create(properties.getSns_access_key(), properties.getSns_secret_key());
            this.provider = StaticCredentialsProvider.create(awsCredentials);
            isReady = true;
        }
    }

    public boolean sendTextSMS(String message, String phoneNumber) {

        if (!isReady) {
            log.error("sendTextSMS error : aws-sns initialization failed, to : {}", phoneNumber);
            throw new RuntimeException(" aws-sns initialization failed");
        }

        SnsClient snsClient = SnsClient.builder().credentialsProvider(provider).region(region).build();
        try {
            return this.sendTextSMS(snsClient, message, phoneNumber);
        } finally {
            if (snsClient != null) {
                snsClient.close();
            }
        }
    }

    private boolean sendTextSMS(SnsClient snsClient, String message, String phoneNumber) {
        if (StringUtils.isEmpty(phoneNumber)) {
            return false;
        }
        PublishRequest request = PublishRequest.builder()
                .message(message)
                .phoneNumber(phoneNumber.startsWith("+") ? phoneNumber : "+".concat(phoneNumber))
                .build();
        PublishResponse response = snsClient.publish(request);
        log.info("sendTextSMS success to : {}", phoneNumber);
        return response.messageId() != null;
    }
}
