package com.ccq.fastdfs;

import com.ccq.exception.FileSystemException;
import com.ccq.utils.PropertiesSupport;
import org.csource.fastdfs.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

/**
 * @author xujunping
 * @date 2019/10/31
 */
class FastdfsConnectionProvider {
    public static final int DEFAULT_CONNECT_TIMEOUT = 5;
    public static final int DEFAULT_NETWORK_TIMEOUT = 30;
    private TrackerGroup trackerGroup;

    public FastdfsConnectionProvider(String configPath) {
        this.init(configPath);
    }

    public FastdfsConnection createConnection() throws IOException {
        TrackerClient trackerClient = new TrackerClient(this.trackerGroup);
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer, (StorageServer)null);
        return new FastdfsConnection(storageClient, trackerServer);
    }

    public TrackerGroup getTrackerGroup() {
        return this.trackerGroup;
    }

    private void init(String configPath) {
        File configFile = new File(configPath);
        if (configFile != null && configFile.exists() && configFile.canRead()) {
            try {
                InputStream is = new FileInputStream(configFile);
                Throwable var4 = null;

                try {
                    PropertiesSupport properties = new PropertiesSupport(is);
                    int connectiTimeOut = properties.getInteger("connect_timeout", 5);
                    if (connectiTimeOut < 0) {
                        connectiTimeOut = 5;
                    }

                    ClientGlobal.setG_connect_timeout(connectiTimeOut * 1000);
                    int networkTimeOut = properties.getInteger("network_timeout", 30);
                    if (networkTimeOut < 0) {
                        networkTimeOut = 30;
                    }

                    ClientGlobal.setG_network_timeout(networkTimeOut * 1000);
                    String charset = properties.getString("charset");
                    if (charset == null || charset.length() == 0) {
                        charset = "ISO8859-1";
                    }

                    ClientGlobal.setG_charset(charset);
                    String[] trackerServers = properties.getArrayString("tracker_server", ",");
                    if (trackerServers == null) {
                        throw new FileSystemException("item \"tracker_server\" in " + configPath + " not found");
                    }

                    InetSocketAddress[] addresses = new InetSocketAddress[trackerServers.length];

                    for(int i = 0; i < trackerServers.length; ++i) {
                        String[] parts = trackerServers[i].split("\\:", 2);
                        if (parts.length != 2) {
                            throw new FileSystemException("the value of item \"tracker_server\" is invalid, the correct format is host:port");
                        }

                        addresses[i] = new InetSocketAddress(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    }

                    TrackerGroup trackerGroup = new TrackerGroup(addresses);
                    ClientGlobal.setG_tracker_group(trackerGroup);
                    int port = properties.getInteger("http.tracker_http_port", 80);
                    ClientGlobal.setG_tracker_http_port(port);
                    boolean hasToken = properties.getBoolean("http.anti_steal_token", false);
                    if (hasToken) {
                        String secretKey = properties.getString("http.secret_key");
                        ClientGlobal.setG_anti_steal_token(hasToken);
                        ClientGlobal.setG_secret_key(secretKey);
                    }

                    this.trackerGroup = ClientGlobal.getG_tracker_group();
                } catch (Throwable var24) {
                    var4 = var24;
                    throw var24;
                } finally {
                    if (is != null) {
                        if (var4 != null) {
                            try {
                                is.close();
                            } catch (Throwable var23) {
                                var4.addSuppressed(var23);
                            }
                        } else {
                            is.close();
                        }
                    }

                }

            } catch (IOException var26) {
                throw new FileSystemException("loading fastdfs config error!", var26);
            }
        } else {
            throw new FileSystemException("can not load fastdfs config from [" + configPath + "]");
        }
    }
}
