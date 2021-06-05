package com.company;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utils {

    private final static String bucketName = "xiongbin";
    private final static String accessKey = "9CC493F7E25194E921E7";
    private final static String secretKey = "WzRENEQ0RUJGRkY5NzQyMDhCQjZFMDM0QkY0NTEx";
    private final static String serviceEndpoint = "http://scut.depts.bingosoft.net:29997";

    public Utils() {

    }

    public static AmazonS3 getS3() {
        final BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        final ClientConfiguration ccfg = new ClientConfiguration().
                withUseExpectContinue(false);

        final AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, "");

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withClientConfiguration(ccfg)
                .withEndpointConfiguration(endpoint)
                .withPathStyleAccessEnabled(true)
                .build();

        return s3;
    }

    public static HashMap<String, S3ObjectSummary> getS3FilesLists() {
        final AmazonS3 s3 = getS3();
        ListObjectsV2Result result = s3.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        HashMap<String, S3ObjectSummary> lists = new HashMap();
        for (S3ObjectSummary os : objects) {
            lists.put(os.getKey(), os);
        }
        return lists;
    }

    public static List<String> getLocalFilesLists(String directoryPath, int headLength) {
        List<String> list = new ArrayList();
        File baseFile = new File(directoryPath);

        //获取到的是绝对路径，所以要把路径前缀去掉
        //headLength就是前缀路径的长度
        if (headLength == 0)
            headLength = baseFile.getAbsolutePath().length() + 1;

        if (baseFile.isFile() || !baseFile.exists()) {
            return list;
        }
        File[] files = baseFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                list.add((file.getAbsolutePath().replace('\\', '/') + "/").substring(headLength));
                list.addAll(getLocalFilesLists(file.getAbsolutePath(), headLength));
            } else {
                list.add((file.getAbsolutePath().replace('\\', '/')).substring(headLength));
            }
        }
        return list;
    }

    public static void upLoadFile(String filePath, String s3Path) throws IOException {
        String md5 = DigestUtils.md5Hex(new FileInputStream((filePath)));
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.addUserMetadata("md5", md5);
        try {
            AmazonS3 s3 = getS3();

            // Upload a file as a new object with ContentType and title specified.
            PutObjectRequest request = new PutObjectRequest(bucketName, s3Path, new File(filePath)).withMetadata(objectMetadata);
            s3.putObject(request);
        } catch (SdkClientException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        }// Amazon S3 couldn't be contacted for a response, or the client
        // couldn't parse the response from Amazon S3.

    }

    public static void multiPartUpload(String filePath, String s3Path) throws IOException {
        ArrayList<PartETag> partETags;
        File file = new File(filePath);
        long contentLength = file.length();
        String uploadId = null;
        AmazonS3 s3 = getS3();
        String md5 = DigestUtils.md5Hex(new FileInputStream((filePath)));
        UploadInfo uploadInfo;
        try {
            if (new File("t/" + md5).exists())//如果该文件记录存在
            {
                //读取实体类信息
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("t/" + md5));
                uploadInfo = (UploadInfo) objectInputStream.readObject();
                objectInputStream.close();


                uploadId = uploadInfo.getUploadId();
                System.out.format("文件续传 ID was %s\n", uploadId);

                // Step 2: Upload parts.
                long filePosition = uploadInfo.getFilePosition(); //设置位置为记录的位置
                partETags = uploadInfo.getPartETags();//设置PartETags集合
                for (int i = uploadInfo.getPartNumber() + 1; filePosition < contentLength; i++) {
                    // Last part can be less than 5 MB. Adjust part size.
                    long partSize = Math.min(1024 * 1024 * 5, contentLength - filePosition);
                    // Create request to upload a part.
                    UploadPartRequest uploadRequest = new UploadPartRequest()
                            .withBucketName(bucketName)
                            .withKey(s3Path)
                            .withUploadId(uploadId)
                            .withPartNumber(i)
                            .withFileOffset(filePosition)
                            .withFile(file)
                            .withPartSize(partSize);

                    // Upload part and add response to our list.
                    System.out.format("上传块 %d\n", i);
                    PartETag partETag = s3.uploadPart(uploadRequest).getPartETag();
                    partETags.add(partETag);
                    uploadInfo.setPartETags(partETags);//设置实体类的partETags
                    uploadInfo.setPartNumber(i);
                    ;//设置实体类的PartNumber
                    filePosition += partSize;
                    uploadInfo.setFilePosition(filePosition);//设置实体类的FilePosition
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("t/" + md5));
                    objectOutputStream.writeObject(uploadInfo);//实体类数据持久化储存到本地
                    objectOutputStream.close();
//                    if (i==3)
//                    {
//                        return;
//                    }

                }
            }
            else
                {
                //第一次上传
                uploadInfo = new UploadInfo();
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.addUserMetadata("md5", md5);
                InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, s3Path)
                        .withObjectMetadata(objectMetadata);//初始化上传请求
                uploadId = s3.initiateMultipartUpload(initRequest).getUploadId(); //得到uploadId
                uploadInfo.setUploadId(uploadId);//设置实体类的uploadID
                System.out.format("构造新上传 ID was %s\n", uploadId);

                // Step 2: Upload parts.
                long filePosition = 0; //设置位置为记录的位置
                partETags = new ArrayList<>();//设置PartETags集合
                for (int i = 1; filePosition < contentLength; i++) {
                    // Last part can be less than 5 MB. Adjust part size.
                    long partSize = Math.min(1024 * 1024 * 5, contentLength - filePosition);
                    // Create request to upload a part.
                    UploadPartRequest uploadRequest = new UploadPartRequest()
                            .withBucketName(bucketName)
                            .withKey(s3Path)
                            .withUploadId(uploadId)
                            .withPartNumber(i)
                            .withFileOffset(filePosition)
                            .withFile(file)
                            .withPartSize(partSize);

                    // Upload part and add response to our list.
                    System.out.format("上传块 %d\n", i);
                    PartETag partETag = s3.uploadPart(uploadRequest).getPartETag();
                    partETags.add(partETag);
                    filePosition += partSize;
                    uploadInfo.setPartETags(partETags);//设置实体类的partETags
                    uploadInfo.setPartNumber(i);;//设置实体类的PartNumber
                    uploadInfo.setFilePosition(filePosition);//设置实体类的FilePosition
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("t/" + md5));
                    objectOutputStream.writeObject(uploadInfo);//实体类数据持久化储存到本地
                    objectOutputStream.close();
//                    if (i == 1) {
//                        return;
//                    }

                }
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, s3Path, uploadId, partETags);
            s3.completeMultipartUpload(compRequest);//完成上传请求

            if (!new File("t/" + md5).delete())//删除记录文件
            {
                throw new Exception("删除文件失败");
            }


        } catch (Exception e) {
            e.printStackTrace();
            if (uploadId != null && !uploadId.isEmpty()) {
                // Cancel when error occurred
                System.out.println("Aborting upload");
                s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, s3Path, uploadId));
            }
            System.exit(1);
        }
    }


    public static void upLoadFileTransferManager(String filePath, String s3Path) {
        File f = new File(filePath);
        TransferManager xfer_mgr = TransferManagerBuilder.standard()
                .withS3Client(getS3())
                .build();
        try {
            Upload xfer = xfer_mgr.upload(bucketName, s3Path, f);

            xfer.addProgressListener(new ProgressListener() {
                long length = 0;

                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    length += progressEvent.getBytesTransferred();
                    System.out.println(length * 100.0 / progressEvent.getBytes());
                }
            });
            xfer.waitForCompletion();
//
//            // loop with Transfer.isDone()
//            XferMgrProgress.showTransferProgress(xfer);
//            //  or block with Transfer.waitForCompletion()
//            XferMgrProgress.waitForCompletion(xfer);
        } catch (AmazonServiceException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
        xfer_mgr.shutdownNow();
    }

    public static void multiObjectDelete(ArrayList<DeleteObjectsRequest.KeyVersion> keys) {
        try {
            AmazonS3 s3Client = getS3();


            DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucketName)
                    .withKeys(keys)
                    .withQuiet(false);

            // Verify that the objects were deleted successfully.
            DeleteObjectsResult delObjRes = s3Client.deleteObjects(multiObjectDeleteRequest);
            int successfulDeletes = delObjRes.getDeletedObjects().size();
            System.out.println(successfulDeletes + " objects successfully deleted.");
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
    }

    public static void downloadFile(String filePath, String s3Path) throws IOException {
        S3Object fullObject = null;
        try {
            AmazonS3 s3Client = getS3();
            // Get an object and print its contents.
            System.out.println("Downloading an object");
            fullObject = s3Client.getObject(new GetObjectRequest(bucketName, s3Path));
            ObjectMetadata objectMetadata = fullObject.getObjectMetadata();

            S3ObjectInputStream s3ObjectInputStream = fullObject.getObjectContent();
             File file = new File(filePath);
             if (!file.getParentFile().exists())
             {
                 file.getParentFile().mkdirs();
             }
            OutputStream outputStream = new FileOutputStream(filePath);
            int c;
            while ((c = s3ObjectInputStream.read()) != -1) {
                outputStream.write(c);
            }
            outputStream.close();
            System.out.println("下载完成");

        } catch (SdkClientException e) {
            e.printStackTrace();
        } finally {
            // To ensure that the network connection doesn't remain open, close any open input streams.
            if (fullObject != null) {
                fullObject.close();
            }
        }
    }

    public static String getS3FileMD5(String s3Path) {
        AmazonS3 s3Client = getS3();
        ObjectMetadata objectMetadata = s3Client.getObjectMetadata(bucketName, s3Path);
        return objectMetadata.getUserMetaDataOf("md5");
    }


}
