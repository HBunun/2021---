package com.company;

import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.util.*;

public class Main {
    static String path = "a";

    public static void main(String[] args) throws IOException {


        sync(1);//首次启动 冲突取s3

        System.out.println("---输入sync开始同步---");
        String line;
        while (true) {
            Scanner scanner = new Scanner(System.in);
            line = scanner.nextLine();
            if (line.equals("sync")) {
                System.out.println("---输入数字选择冲突模式 0=冲突取本地 1=冲突取s3 2=冲突询问---");
                int t = scanner.nextInt();
                if (t == 0 || t == 1 || t == 2) {
                    sync(t);
                } else {
                    System.out.println("---输入格式有误---");
                }
            } else {
                System.out.println("---输入格式有误---");
            }
            System.out.println("---输入sync开始同步---");
        }
    }

    public static void sync(int clashMode) throws IOException //clashMode冲突模式 0=冲突取本地 1 = 冲突取s3 2 = 冲突询问
    {
        ArrayList<String> uploadList = new ArrayList<>();
        ArrayList<String> downloadList = new ArrayList<>();
        ArrayList<String> delS3List = new ArrayList<>();

        Map<String, S3ObjectSummary> s3Map = Utils.getS3FilesLists();
        List<String> list = Utils.getLocalFilesLists(path, 0);

        for (String s : list) {
            if (s.endsWith("/")) {
                continue;
            }

            if (s3Map.containsKey(s))//如果s3有本地的文件
            {
                String localMd5 = DigestUtils.md5Hex(new FileInputStream(path + "/" + s));
                String s3Md5 = Utils.getS3FileMD5(s);

                if (!localMd5.equals(s3Md5))//如果本地文件md5与s3文件md5不一致
                {
                    if (clashMode == 0) {
                        System.out.println("本地更新 --> S3 |" + s);
                        uploadList.add(s);//放到上传列表
                    }
                    if (clashMode == 1) {
                        System.out.println("本地 <-- S3更新 |" + s);
                        downloadList.add(s);//放到下载列表
                    }
                    if (clashMode == 2) {
                        if (clashInfo(new File(s), s3Map.get(s)) == 0) {
                            System.out.println("本地更新 --> S3 |" + s);
                            uploadList.add(s);//放到上传列表
                        } else {
                            System.out.println("本地 <-- S3更新 |" + s);
                            downloadList.add(s);//放到下载列表
                        }
                    }
                }
                s3Map.remove(s);//删除s3map文件
            } else //s3没有本地有的文件
            {
                System.out.println("本地新增 --> S3 |" + s);
                uploadList.add(s);//放到上传列表
            }
        }

        //s3map剩下的是s3有本地没有的，遍历他，然后依次询问
        for (String s : s3Map.keySet()) {
            if (s.endsWith("/")) {
                continue;
            }

            File file = new File(s);
            S3ObjectSummary s3ObjectSummary = s3Map.get(s);
            System.out.println("发现S3存在但本地没有的文件| " + file.getPath() + " |本地文件大小 " + file.length() / 1024 + " kb S3文件大小 " + s3ObjectSummary.getSize() / 1024 + " 输入'0'下载到本地，输入'1'删除s3文件");
            Scanner scanner = new Scanner(System.in);
            int t = scanner.nextInt();
            if (t == 0) {
                System.out.println("本地 <-- S3新增 |" + s);
                downloadList.add(s);//放到下载列表
            } else {
                System.out.println("S3文件删除 ~~~~ |" + s);
                delS3List.add(s);
            }
        }

        System.out.println("------------开始同步------------");//开始同步三个列表
        System.out.println("----------开始上传列表----------");
        for (String s : uploadList) {
            if (!s.endsWith("/")) //非目录则上传
            {
                System.out.println("开始上传| " + path + "/" + s);
                File file = new File(path + "/" + s);
                if (file.length() > 1024 * 1024 * 20) {
                    System.out.println("文件超过20M，使用分块上传");
                    Utils.multiPartUpload(path + "/" + s, s);
                } else {
                    System.out.println("pt");
                    Utils.upLoadFile(path + "/" + s, s);
                }

                System.out.println("上传完毕| " + path + "/" + s);
            }
        }

        System.out.println("----------开始下载列表----------");
        for (String s : downloadList) {
            if (!s.endsWith("/")) //非目录则上传
            {
                System.out.println("开始下载| " + path + "/" + s);
                Utils.downloadFile(path + "/" + s, s);
                System.out.println("下载完毕| " + path + "/" + s);
            } else {
                File file = new File(path + "/" + s);
                file.mkdir();
            }
        }

        System.out.println("--------开始删除S3多余文件--------");
        ArrayList<DeleteObjectsRequest.KeyVersion> arrayList = new ArrayList<>();
        for (String s : delS3List) {
            arrayList.add(new DeleteObjectsRequest.KeyVersion(s));
        }
        Utils.multiObjectDelete(arrayList);

        System.out.println("------------同步完成------------");

    }

    public static int clashInfo(File file, S3ObjectSummary s3ObjectSummary) {
        System.out.println("发现文件冲突| " + file.getPath() + " |本地文件大小 " + file.length() / 1024 + " kb S3文件大小 " + s3ObjectSummary.getSize() / 1024 + " 输入0保留本地文件，输入1保留s3文件");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }


}
