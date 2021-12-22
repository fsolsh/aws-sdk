package com.fsolsh.aws.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aws")
public class AWSProperties {

    private String s3_access_key;
    private String s3_secret_key;
    private String s3_region;

    private String ses_access_key;
    private String ses_secret_key;
    private String ses_region;

    private String sns_access_key;
    private String sns_secret_key;
    private String sns_region;

}
