package dk.hydrozoa.hydrowiki;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Used to interact with an S3 cloud drive.
 * The specific drive is determined when the object is constructed, and can't be changed further.
 */
public class S3Interactor {

    // properties
    private String endpoint;
    private String region;
    private String bucketName;

    private String accessKey;
    private String secretKey;

    // todo pass in arguments instead of entire properties file
    public S3Interactor(Properties props) {
        this.endpoint = props.getProperty("s3.endpoint");
        this.region = props.getProperty("s3.region");
        this.bucketName = props.getProperty("s3.bucket_name");
        this.accessKey = props.getProperty("s3.access_key");
        this.secretKey = props.getProperty("s3.secret_key");
    }

    private S3Client createClient() {
        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey))
                ).build();
    }

    /**
     * Uploads a file.
     */
    public void uploadFile(String filename, byte[] bytes) {
        S3Client client = createClient();
        final PutObjectRequest putObjReq =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filename)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .build();
        client.putObject(putObjReq, RequestBody.fromBytes(bytes));
        client.close();
    }

    /**
     * Deletes a file.
     */
    public void deleteFile(String filename) {
        S3Client client = createClient();
        final DeleteObjectRequest delObjReq = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();
        client.deleteObject(delObjReq);
        client.close();
    }

    /**
     * Renames a file by copying it and then removing the old file.
     * @param oldFilename
     * @param newFilename
     */
    public void renameFile(String oldFilename, String newFilename) {
        S3Client client = createClient();
        final CopyObjectRequest copyObjReq = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .destinationBucket(bucketName)
                .sourceKey(oldFilename)
                .destinationKey(newFilename)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();
        client.copyObject(copyObjReq);
        client.close();
        deleteFile(oldFilename);
    }

    /**
     * Retrieve bytes from a file.
     */
    public byte[] downloadFile(String filename) {
        S3Client client = createClient();
        final GetObjectRequest getObjReq =
                GetObjectRequest
                        .builder()
                        .key(filename)
                        .bucket(bucketName)
                        .build();
        byte[] fileContents = client.getObjectAsBytes(getObjReq).asByteArray();
        client.close();
        return fileContents;
    }

    /**
     * Check if a file exists.
     * @param filename
     * @return True if file exists, otherwise false.
     */
    public boolean fileExists(String filename) {
        S3Client client = createClient();
        try {
            HeadObjectResponse headResponse = client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(filename).build());
            client.close();
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * Performs an action on each file without returning anything.
     */
    public void forEachFile(Consumer<S3Object> handler) {
        S3Client client = createClient();
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(1)
                .build();
        ListObjectsV2Iterable listRes = client.listObjectsV2Paginator(listReq);
        listRes.stream()
                .flatMap(r -> r.contents().stream())
                .forEach(handler);
        client.close();
    }

    /**
     * Filters S3Objects in bucket, for example to perform actions
     * @param predicate todo
     * @return          todo
     */
    public List<S3Object> findByFilter(Predicate<S3Object> predicate) {
        S3Client client = createClient();
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(1)
                .build();
        ListObjectsV2Iterable listRes = client.listObjectsV2Paginator(listReq);
        List<S3Object> result = listRes.stream()
                .flatMap(r -> r.contents().stream())
                .filter(predicate)
                .collect(Collectors.toList());
        client.close();
        return result;
    }
}