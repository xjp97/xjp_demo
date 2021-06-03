package com.ccq.service.impl;


import com.ccq.exception.FileSystemException;
import com.ccq.fastdfs.FastdfsConnection;
import com.ccq.fastdfs.FastdfsConnectionFactory;
import com.ccq.service.FileSystemService;
import org.csource.fastdfs.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * fastDfs file system service
 *
 * @author xujunping
 * @since 0.0.1
 * @date 2017-07-01
 *
 */
public class FastDfsFileSystemService implements FileSystemService {

    private static final Logger logger = LoggerFactory.getLogger(FastDfsFileSystemService.class);

    private FastdfsConnectionFactory connectionFactory;

    public FastDfsFileSystemService(FastdfsConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }


    /**
     * 上传文件
     */
    @Override
    public String uploadObject(byte[] filedata, String extName) throws FileSystemException {
        String sFilePath = "";
        try(FastdfsConnection connection = connectionFactory.getConnection()) {
            StorageClient storageClient = connection.getStorageClient();
            String[] sResultStrings = storageClient.upload_file(filedata, extName, null);
            sFilePath = sResultStrings[0] + "/" + sResultStrings[1];
        } catch (Exception e) {
            logger.error(">>>upload file error." + e.getMessage());
            throw new FileSystemException(e);
        }
        return sFilePath;
    }

    @Override
    public String uploadFile(File file, String extName) throws FileSystemException {
        String sFilePath = "";
        try(FastdfsConnection connection = connectionFactory.getConnection()) {
            StorageClient storageClient = connection.getStorageClient();
            String[] sResultStrings = storageClient.upload_file(file.getAbsolutePath(), extName, null);
            sFilePath = sResultStrings[0] + "/" + sResultStrings[1];
        } catch (Exception e) {
            logger.error(">>>upload file error." + e.getMessage());
            throw new FileSystemException(e);
        }
        return sFilePath;
    }

    @Override
    public byte[] downloadObject(String fileName) throws FileSystemException {
        byte[] filedata = null;
        try(FastdfsConnection connection = connectionFactory.getConnection()) {
            StorageClient storageClient = connection.getStorageClient();
            String sGroup = fileName.substring(0, fileName.indexOf("/"));
            String sName = fileName.substring(fileName.indexOf("/") + 1);
            filedata = storageClient.download_file(sGroup, sName);
        } catch (Exception e) {
            logger.error(">>>download file byte error.");
            throw new FileSystemException(e);
        }
        return filedata;
    }

    @Override
    public void deleteObject(String fileName) throws FileSystemException {
        try(FastdfsConnection connection = connectionFactory.getConnection()) {
            StorageClient storageClient = connection.getStorageClient();
            String sGroup = fileName.substring(0, fileName.indexOf("/"));
            String sName = fileName.substring(fileName.indexOf("/") + 1);
            storageClient.delete_file(sGroup, sName);
        } catch (Exception e) {
            logger.error(">>>delete file error.");
            throw new FileSystemException(e);
        }
    }

}
