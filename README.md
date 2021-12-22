# aws-sdk

Amazon服务sdk，集成了以下服务组件：  
* 1.s3-文件服务
* 2.ses-邮件服务
* 3.sns-短信服务
……

_**如何使用？**_  

* 1、添加依赖  
``` 
  <dependency>
  <groupId>com.fsolsh.mining</groupId>
  <artifactId>aws-sdk</artifactId>
  <version>1.0.0</version>
  </dependency>
```
* 2、添加配置
 ``` 
aws:
  service:
    enable: true
  s3_default_bucket: #########
  s3_region: #########
  s3_access_key: #########
  s3_secret_key: #########
  
  ses_region: #########
  ses_access_key: #########
  ses_secret_key: #########
  
  sns_region: #########
  sns_access_key: #########
  sns_secret_key: #########
```

* 3、依赖注入  
 ``` 
@Autowired
private AWS_S3 aws_s3;
@Autowired
private AWS_SES aws_ses;
@Autowired
private AWS_SNS aws_sns;
```

* 4、开始使用  
```
//1、上传文件
String fileUrl = aws_s3.putObjectWithPublicRead("bucketName", "objectKey",new HashMap<>(),new byte[10]);
//2、发送邮件
boolean isSend = aws_ses.sendTextMail("f@qq.com", "s@qq.com", "subject", "bodyText");
//3、发送短信
boolean isSend = aws_sns.sendTextSMS("message", "+8615900770077");
```
