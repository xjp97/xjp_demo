package com.ccq.service;



import com.ccq.exception.FileSystemException;

import java.io.File;

/**
 * @author xujunping
 * @date 2019/10/31
 */
public interface FileSystemService {
    /**
     * 上传图片
     * @param var1 文件byte
     * @param var2 后缀名
     * @return
     * @throws FileSystemException
     */
    String uploadObject(byte[] var1, String var2) throws FileSystemException;

    /**
     * 上传文件
     * @param var1 文件file
     * @param var2 后缀名
     * @return
     * @throws FileSystemException
     */
    String uploadFile(File var1, String var2) throws FileSystemException;

    /**
     * 下载文件
     * @param var1 文件名
     * @return
     * @throws FileSystemException
     */
    byte[] downloadObject(String var1) throws FileSystemException;

    /**
     * 删除文件
     * @param var1 文件名
     * @throws FileSystemException
     */
    void deleteObject(String var1) throws FileSystemException;
}