package com.fsolsh.aws.config;

import com.fsolsh.aws.AWS_S3;
import com.fsolsh.aws.AWS_SES;
import com.fsolsh.aws.AWS_SNS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({AWS_S3.class, AWS_SES.class, AWS_SNS.class})
@ConditionalOnProperty(prefix = "aws", name = {"service.enable"})
@EnableConfigurationProperties(AWSProperties.class)
public class AWSAutoConfiguration {

    @Autowired
    private AWSProperties awsProperties;

    @Bean
    public AWS_S3 aws_s3() {
        return new AWS_S3(awsProperties);
    }

    @Bean
    public AWS_SES aws_ses() {
        return new AWS_SES(awsProperties);
    }

    @Bean
    public AWS_SNS aws_sns() {
        return new AWS_SNS(awsProperties);
    }

}
