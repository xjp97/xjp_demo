package com.ccq.fastdfs;

import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerServer;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author xujunping
 * @date 2019/10/31
 */
public class FastdfsConnection implements Closeable {
    private final StorageClient storageClient;
    private final TrackerServer trackerServer;
    private FastdfsConnectionFactory connectionFactory;

    public FastdfsConnection(StorageClient storageClient, TrackerServer trackerServer) {
        this.storageClient = storageClient;
        this.trackerServer = trackerServer;
    }

    public void close() {
        if (this.connectionFactory != null) {
            this.connectionFactory.release(this);
        } else {
            try {
                this.destroy();
            } catch (IOException var2) {
            }
        }

    }

    void destroy() throws IOException {
        if (this.trackerServer != null) {
            this.trackerServer.close();
        }

    }

    void setConnectionFactory(FastdfsConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public StorageClient getStorageClient() {
        return this.storageClient;
    }

    public TrackerServer getTrackerServer() {
        return this.trackerServer;
    }
}
