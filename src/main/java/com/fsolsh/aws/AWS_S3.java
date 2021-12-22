package com.fsolsh.aws;

import com.fsolsh.aws.config.AWSProperties;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;
import software.amazon.awssdk.utils.StringUtils;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

/**
 * AWS_S3
 */
@Slf4j
public class AWS_S3 {
    public static final long MAX_PRESIGN_DURATION_IN_SECONDS = 60 * 60 * 24 * 7;
    public static final long DEFAULT_PRESIGN_DURATION_IN_SECONDS = 60 * 5;

    private static Region region;
    private static boolean isReady;
    private AwsCredentialsProvider provider;
    private String domain;

    public AWS_S3(AWSProperties properties) {
        if (!StringUtils.isEmpty(properties.getS3_access_key()) && !StringUtils.isEmpty(properties.getS3_secret_key()) && !StringUtils.isEmpty(properties.getS3_region())) {
            region = Region.of(properties.getSes_region());
            this.domain = "https://s3." + region.id() + ".amazonaws.com";
            AwsCredentials awsCredentials = AwsBasicCredentials.create(properties.getS3_access_key(), properties.getS3_secret_key());
            this.provider = StaticCredentialsProvider.create(awsCredentials);
            isReady = true;
        }
    }

    public String putObject(String bucketName, String objectKey, Map<String, String> metadata, byte[] bytesArray, ObjectCannedACL objectCannedACL) {
        return putObject(bucketName, objectKey, metadata, bytesArray, objectCannedACL, null);
    }

    public String putObject(String bucketName, String objectKey, Map<String, String> metadata, byte[] bytesArray, ObjectCannedACL objectCannedACL, String contentType) {
        if (!isReady) {
            log.error("putObject error : aws-s3 initialization failed");
            throw new RuntimeException(" aws-s3 initialization failed");
        }

        S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(provider)
                .build();

        try {
            PutObjectRequest.Builder builder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .acl(objectCannedACL)
                    .metadata(metadata);

            PutObjectRequest putOb = contentType != null ?
                    builder.contentType(contentType).build() :
                    builder.build();

            if (s3.putObject(putOb, RequestBody.fromBytes(bytesArray)).sdkHttpResponse().isSuccessful()) {
                return this.domain + "/" + bucketName + "/" + objectKey;
            }
        } finally {
            if (s3 != null) s3.close();
        }

        return null;
    }

    public String putObjectWithPublicRead(String bucketName, String objectKey, Map<String, String> metadata, byte[] bytesArray) {
        if (!isReady) {
            log.error("putObjectWithPublicRead error: aws-s3 initialization failed");
            throw new RuntimeException(" aws-s3 initialization failed");
        }

        S3Client s3 = S3Client.builder().region(region).credentialsProvider(provider).build();
        try {
            PutObjectRequest putOb = PutObjectRequest.builder().bucket(bucketName).key(objectKey).metadata(metadata).acl(ObjectCannedACL.PUBLIC_READ).build();
            if (s3.putObject(putOb, RequestBody.fromBytes(bytesArray)).sdkHttpResponse().isSuccessful()) {
                return this.domain + "/" + bucketName + "/" + objectKey;
            }
        } finally {
            if (s3 != null) {
                s3.close();
            }
        }
        return null;
    }

    public boolean deleteObjects(String bucketName, String objectName) {

        if (!isReady) {
            log.error("deleteObjects error: aws-s3 initialization failed");
            throw new RuntimeException(" aws-s3 initialization failed");
        }
        S3Client s3 = S3Client.builder().region(region).credentialsProvider(provider).build();
        ArrayList<ObjectIdentifier> toDelete = new ArrayList<>();
        toDelete.add(ObjectIdentifier.builder().key(objectName).build());
        try {
            DeleteObjectsRequest dor = DeleteObjectsRequest.builder().bucket(bucketName).delete(Delete.builder().objects(toDelete).build()).build();
            return s3.deleteObjects(dor) != null;
        } finally {
            if (s3 != null) {
                s3.close();
            }
        }
    }

    public boolean createBucket(String bucketName, BucketCannedACL bucketCannedACL) {
        if (!isReady) {
            log.error("createBucket error: aws-s3 initialization failed");
            throw new RuntimeException(" aws-s3 initialization failed");
        }
        S3Client s3 = S3Client.builder().region(region).credentialsProvider(provider).build();
        try {
            S3Waiter s3Waiter = s3.waiter();
            CreateBucketRequest bucketRequest = CreateBucketRequest.builder().bucket(bucketName).acl(bucketCannedACL).build();
            s3.createBucket(bucketRequest);
            HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder().bucket(bucketName).build();
            return s3Waiter.waitUntilBucketExists(bucketRequestWait) != null;
        } finally {
            if (s3 != null) {
                s3.close();
            }
        }
    }

    public URL presignGetObject(String bucketName, String objectKey) {
        return presignGetObject(bucketName, objectKey, DEFAULT_PRESIGN_DURATION_IN_SECONDS);
    }

    public URL presignGetObject(String bucketName, String objectKey, long durationInSeconds) {
        if (durationInSeconds > MAX_PRESIGN_DURATION_IN_SECONDS) {
            log.error("presignGetObject error : max preSign duration is 7 days");
            throw new IllegalArgumentException("max preSign duration is 7 days");
        }

        S3Presigner s3Presigner = null;
        URL url = null;
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(durationInSeconds))
                    .getObjectRequest(getObjectRequest)
                    .build();

            s3Presigner = S3Presigner.builder()
                    .credentialsProvider(provider)
                    .region(region)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);

            if (presignedGetObjectRequest != null) url = presignedGetObjectRequest.url();
        } finally {
            if (s3Presigner != null) s3Presigner.close();
        }

        return url;
    }
}
