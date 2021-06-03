package com.ccq.fastdfs;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author xujunping
 * @date 2019/10/31
 */
public class FastdfsConnectionPoolConfig extends GenericObjectPoolConfig {
    public FastdfsConnectionPoolConfig() {
        this.setMaxIdle(10);
        this.setMinIdle(0);
        this.setMaxTotal(10);
        this.setTestOnBorrow(false);
    }
}