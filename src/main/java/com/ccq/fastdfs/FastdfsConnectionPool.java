package com.ccq.fastdfs;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author xujunping
 * @date 2019/10/31
 */
public class FastdfsConnectionPool extends BasePooledObjectFactory<FastdfsConnection> {
    private FastdfsConnectionProvider connectionProvider;

    public FastdfsConnectionPool(FastdfsConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public FastdfsConnection create() throws Exception {
        return this.connectionProvider.createConnection();
    }

    public PooledObject<FastdfsConnection> wrap(FastdfsConnection connection) {
        return new DefaultPooledObject(connection);
    }

    public PooledObject<FastdfsConnection> makeObject() throws Exception {
        return this.wrap(this.create());
    }

    public void destroyObject(PooledObject<FastdfsConnection> pooledObject) throws Exception {
        FastdfsConnection connection = (FastdfsConnection)pooledObject.getObject();
        if (connection != null) {
            connection.destroy();
        }

    }
}
