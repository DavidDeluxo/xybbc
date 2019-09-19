package com.xingyun.bbc.common.elasticsearch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Thstone
 * @version V1.0
 * @Title:
 * @Package com.xingyun.xyb2b.elasticsearch.config
 * @Description: (用一句话描述该文件做什么)
 * @date 2018/12/12 20:35
 */
@Component
@ConfigurationProperties(prefix = "elastic")
public class EsSettingsProperties {

    private String ip;

    private int port;

    private String cluster;

    private String index;

    private String type;

    private int pageSize;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
