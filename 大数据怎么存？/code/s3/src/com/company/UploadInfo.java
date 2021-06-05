package com.company;

import com.amazonaws.services.s3.model.PartETag;

import java.io.Serializable;
import java.util.ArrayList;

//上传信息实体类
public class UploadInfo implements Serializable {
    private String uploadId;
    private ArrayList<PartETag> partETags;
    private int partNumber;
    private long filePosition;

    public long getFilePosition() {
        return filePosition;
    }

    public void setFilePosition(long filePosition) {
        this.filePosition = filePosition;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public ArrayList<PartETag> getPartETags() {
        return partETags;
    }

    public void setPartETags(ArrayList<PartETag> partETags) {
        this.partETags = partETags;
    }
}
