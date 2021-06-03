package com.ccq.fastdfs;

import com.ccq.service.FileSystemService;
import com.ccq.utils.HttpUtil;
import com.google.common.io.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Base64;

/**
 * @author xujunping
 * @date 2019/7/1
 */
public class FileSystemManager {

    @Autowired
    private FileSystemService fileSystemService;

    @Value("${xjp.common.fastdfs.local.imagePrefix:}")
    private String imagePrefix;

    /**
     * 上传文件
     * @param data
     * @param fileSuffix
     * @return
     */
    public String upload(byte[] data, String fileSuffix) {
        if (data == null) {
            return null;
        }
        // 上传到FastDFS
        String imageUri = fileSystemService.uploadObject(data, fileSuffix);
        return imageUri;
    }

    /**
     * 上传文件
     * @param file
     * @param fileSuffix
     * @return
     */
    public String upload(File file, String fileSuffix) {
        if (file == null) {
            return null;
        }
        // 上传到FastDFS
        String imageUri = fileSystemService.uploadFile(file, fileSuffix);
        return imageUri;
    }


    /**
     * 上传base64文件
     * @param base64
     * @param fileName
     * @return
     */
    public String uploadByBase64(String base64, String fileName) {
        if (StringUtils.isEmpty(base64)) {
            return null;
        }
        String fileSuffix = Files.getFileExtension(fileName);
        byte[] data = Base64.getDecoder().decode(base64);
        return upload(data, fileSuffix);
    }


    /**
     * 上传base64文件
     *
     * @param url
     * @param fileName
     * @return
     */
    public String uploadByUrl(String url, String fileName) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        String fileSuffix = Files.getFileExtension(fileName.trim());
        byte[] data = HttpUtil.downloadImage(url);
        return upload(data, fileSuffix);
    }


    /**
     * 是否是fastdfs资源
     * @param url
     * @return
     */
    public boolean isFastdfsFile(String url) {
        return url != null && url.contains(imagePrefix);
    }


    public String getUrl(String uri) {
        if (StringUtils.isEmpty(uri)) {
            return uri;
        }
        return imagePrefix + uri;
    }


}
