package com.ccq.fastdfs;

import org.apache.commons.pool2.impl.GenericObjectPool;

import com.ccq.exception.FileSystemException;

/**
 * @author xujunping
 * @date 2019/10/31
 */
public class FastdfsConnectionFactory {
    private GenericObjectPool<FastdfsConnection> connectionPool;

    public FastdfsConnectionFactory(String configPath) {
        this(configPath, (FastdfsConnectionPoolConfig)null);
    }

    public FastdfsConnectionFactory(String configPath, FastdfsConnectionPoolConfig config) {
        if (config == null) {
            config = new FastdfsConnectionPoolConfig();
        }

        FastdfsConnectionProvider provider = new FastdfsConnectionProvider(configPath);
        FastdfsConnectionPool pool = new FastdfsConnectionPool(provider);
        this.connectionPool = new GenericObjectPool(pool, config);
    }

    public FastdfsConnection getConnection() {
        try {
            FastdfsConnection connection = (FastdfsConnection)this.connectionPool.borrowObject();
            connection.setConnectionFactory(this);
            return connection;
        } catch (Exception var2) {
            throw new FileSystemException("get connection error!", var2);
        }
    }

    public void release(FastdfsConnection connection) {
        this.connectionPool.returnObject(connection);
    }
}
