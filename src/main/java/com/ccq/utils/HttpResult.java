package com.ccq.utils;

import com.alibaba.fastjson.JSON;

/**
 * @author xujunping
 * @date 2019/10/31
 */
public class HttpResult {
    private int status;
    private String data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    public String getErrorReason() {
        return JSON.parseObject(this.getData()).getString("error");
    }

}