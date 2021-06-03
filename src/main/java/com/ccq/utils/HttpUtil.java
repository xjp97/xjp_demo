package com.ccq.utils;

import com.google.common.primitives.Bytes;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xujunping on 16-11-28.
 *
 */
public class HttpUtil {
    private final static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    @SuppressWarnings("unchecked")
    private static void addBody(MultipartEntityBuilder builder, String key, Object value) {
        if (value instanceof List) {
            List<?> values = (List<?>) value;
            for (Object obj : values) {
                addBody(builder, key, obj);
            }
        } else if (value instanceof byte[]) {
            builder.addBinaryBody(key, (byte[]) value);
        } else if (value instanceof File) {
            builder.addBinaryBody(key, (File) value);
        } else if(value instanceof Object[]) {
            Object[] sv = (Object[]) value;
            for (int i = 0; i < sv.length; i++) {
                builder.addTextBody(key, (String)sv[i]);
            }
        } else {
            builder.addTextBody(key, value.toString());
        }
    }


    private static HttpPost buildBinaryPost(String url, Map<String, Object> params) {
        HttpPost post = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName("utf-8"));
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        if (params != null && params.size() > 0) {
            Object value;
            for (String key : params.keySet()) {
                value = params.get(key);
                addBody(builder, key, value);
            }
        }
        post.setConfig(RequestConfig.custom().setConnectTimeout(5000).build());
        post.setEntity(builder.build());
        return post;
    }

    private static HttpPost buildImagePost(String url, Map<String, Object> params) {
        HttpPost post = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        if (params != null && params.size() > 0) {
            Object value;
            for (String key : params.keySet()) {
                value = params.get(key);
                if (value instanceof byte[]) {
                    builder.addBinaryBody("imageData", (byte[])value, ContentType.DEFAULT_BINARY, "image.jpg");
                } else {
                    addBody(builder, key, value);
                }
            }
        }
        post.setConfig(RequestConfig.custom().setConnectTimeout(5000).build());
        post.setEntity(builder.build());
        return post;
    }

    private static HttpPost buildImageVerifyPost(String url, Map<String, Object> params) {
        HttpPost post = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        builder.addBinaryBody("imageOne", (byte[]) params.get("src"), ContentType.DEFAULT_BINARY, "image1.jpg");
        builder.addBinaryBody("imageTwo", (byte[]) params.get("dest"), ContentType.DEFAULT_BINARY, "image2.jpg");
        post.setConfig(RequestConfig.custom().setConnectTimeout(5000).build());
        post.setEntity(builder.build());
        return post;
    }

    public static HttpResult doVerifyPost(String url, Map<String, Object> params) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = buildImageVerifyPost(url, params);
        CloseableHttpResponse response = null;
        HttpResult result = null;
        try {
            response = client.execute(post);
            result = getHttpResult(response);
        } catch (Exception e) {
            logger.debug(">>> post error url:" + url, e);
            logger.error(">>> post error url:" + url);
        } finally {
            close(client, response);
        }
        return result;
    }

    public static HttpResult doImagePost(String url, Map<String, Object> params) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = buildImagePost(url, params);
        CloseableHttpResponse response = null;
        HttpResult result = null;
        try {
            response = client.execute(post);
            result = getHttpResult(response);
        } catch (Exception e) {
            logger.debug(">>> post error url:" + url, e);
            logger.error(">>> post error url:" + url);
        } finally {
            close(client, response);
        }
        return result;
    }

    private static HttpPost buildHttpPost(String url, Map<String, String> params) throws Exception {
        if (StringUtils.isBlank(url)) {
            logger.error(">>>构建HttpPost时,url不能为null");
            throw new Exception("url is null.");
        }
        HttpPost post = new HttpPost(url);
        HttpEntity he = null;
        if (params != null) {
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            for (String key : params.keySet()) {
                formparams.add(new BasicNameValuePair(key, params.get(key)));
            }
            post.setEntity(new UrlEncodedFormEntity(formparams));
        }
        // 在RequestContent.process中会自动写入消息体的长度，自己不用写入，写入反而检测报错
        // setContentLength(post, he);
        return post;

    }

    private static HttpDelete buildHttpDelete(String url) throws Exception {
        if (StringUtils.isBlank(url)) {
            logger.error(">>>构建HttpDelete时,url不能为null");
            throw new Exception("url is null.");
        }

        return new HttpDelete(url);

    }

    public static HttpResult doBinaryPost(String url, Map<String, Object> params) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = buildBinaryPost(url, params);
        CloseableHttpResponse response = null;
        HttpResult result = null;
        try {
            response = client.execute(post);
            result = getHttpResult(response);
        } catch (Exception e) {
            logger.debug(">>> post error url:" + url, e);
            logger.error(">>> post error url:" + url);
        } finally {
            close(client, response);
        }
        return result;
    }

    public static HttpResult doDelete(String url) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete delete = buildHttpDelete(url);
        CloseableHttpResponse response = null;
        HttpResult result = null;
        try {
            response = client.execute(delete);
            result = getHttpResult(response);
        } catch (Exception e) {
            logger.debug(">>> post error url:" + url, e);
            logger.error(">>> post error url:" + url);
        } finally {
            close(client, response);
        }
        return result;
    }

    public static HttpResult doPost(String url, Map<String, String> params) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = buildHttpPost(url, params);
        CloseableHttpResponse response = null;
        HttpResult result = null;
        try {
            response = client.execute(post);
            result = getHttpResult(response);
        } catch (Exception e) {
            logger.debug(">>> post error url:" + url, e);
            logger.error(">>> post error url:" + url);
        } finally {
            close(client, response);
        }
        return result;
    }

    public static HttpResult doPost(String url, byte[] data) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "text/xml;charset=UTF-8");
        HttpEntity entity = new ByteArrayEntity(data);
        post.setEntity(entity);
        CloseableHttpResponse response = null;
        HttpResult result = null;
        try {
            response = client.execute(post);
            result = getHttpResult(response);
        } catch (Exception e) {
            logger.debug(">>> post error url:" + url, e);
            logger.error(">>> post error url:" + url);
        } finally {
            close(client, response);
        }
        return result;
    }

    private static HttpResult getHttpResult(CloseableHttpResponse response) throws IOException {
        if (response != null) {
            HttpEntity entity = response.getEntity();
            HttpResult httpResult = new HttpResult();
            httpResult.setStatus(response.getStatusLine().getStatusCode());
            httpResult.setData(EntityUtils.toString(entity));
            EntityUtils.consume(entity);
            return httpResult;
        }
        return null;
    }

    private static void close(CloseableHttpClient client, CloseableHttpResponse response) {
        try {
            if (response != null)
                response.close();
            client.close();
        } catch (Exception e) {
            logger.debug(">>> close response or client error", e);
            logger.error(">>> close response or client error");
        }
    }

    public static HttpResult doGet(String url) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = null;
        HttpResult result = null;
        try {
            response = client.execute(get);
            result = getHttpResult(response);
        } catch (Exception e) {
            logger.debug(">>> get error url:" + url, e);
            logger.error(">>> get error url:" + url);
        } finally {
            close(client, response);
        }
        return result;
    }

    public static byte[] download(String url, String dest) {
        byte[] returnByteData = null;
        try {
            CloseableHttpClient client = getHttpClient(url);
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = entity.getContent();
                //System.out.println(vipPhoto.GetName());
                File tmp = new File(new String(dest.getBytes()));
                if (tmp.exists()) {
                    return null;
                }
                tmp.createNewFile();
                FileOutputStream out = new FileOutputStream(tmp);
                List<Byte> copyBytes = new ArrayList<>();
                byte[] buff = new byte[1024];
                int size = -1;
                while ((size = inputStream.read(buff)) != -1) {
                    for(int i = 0; i < size; ++i ) {
                        copyBytes.add(buff[i]);
                    }
                    out.write(buff, 0, size);
                }
                returnByteData = Bytes.toArray(copyBytes);
                out.close();
                inputStream.close();
            }
            response.close();
            client.close();

        } catch (Exception e) {
            logger.error("download failed",e);
        }
        return returnByteData;
    }

    /**
     * 根据URL下载图片
     *
     * @param url
     * @return
     */
    public static byte[] downloadImage(String url) {
        CloseableHttpResponse response = null;
        byte[] dataBuffer = null;
        try {
            CloseableHttpClient httpclient = getHttpClient(url);
            HttpGet httpGet = new HttpGet(url);
            response = httpclient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                InputStream input = entity.getContent();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                IOUtils.copy(input, output);
                output.flush();
                input.close();
                dataBuffer = output.toByteArray();
                output.close();
                EntityUtils.consume(entity);
            }
        } catch (Exception e) {
            logger.warn("download image failed|url:{}",url,e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    logger.warn("close io exception failed",e);
                }
            }
        }
        return dataBuffer;
    }


    /**
     * 写入输出流
     * @param url
     * @param out
     */
    public static void downloadTo(String url, OutputStream out) {
        if (out == null) {
            return;
        }

        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpclient = getHttpClient(url);
            response = httpclient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                InputStream input = entity.getContent();
                IOUtils.copy(input, out);
                out.flush();
                input.close();
                EntityUtils.consume(entity);
            }
        } catch (Exception e) {
            logger.warn("download image failed|url:{},exception:{}",url,e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    logger.warn("close io exception failed",e);
                }
            }
        }
    }


    /**
     * 下载到文件
     *
     * @param url
     * @param file
     */
    public static void downloadToFile(String url, File file) {
        if (file == null) {
            return;
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            downloadTo(url, out);
        } catch (Exception e) {
            logger.error("download image to file failed|url:{}", url, e);
        }
    }

    /**
     * 获取HttpClient，如果url以`https`开始，则需要添加相关证书信任
     * @param url url
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static CloseableHttpClient getHttpClient(String url) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String prefix = "https";
        if(url.startsWith(prefix)) {
            //信任所有
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (x509Certificates, s) -> true)
                    .build();
            SSLConnectionSocketFactory ssl = new SSLConnectionSocketFactory(sslContext);
            httpclient = HttpClients.custom().setSSLSocketFactory(ssl).build();
        }

        return httpclient;
    }
}
