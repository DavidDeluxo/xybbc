package com.xingyun.bbc.mallpc.config.xxl;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xxl-job config
 *
 * @author xuxueli 2017-04-28
 */

@Configuration
public class XyJobConfig {
    private Logger logger = LoggerFactory.getLogger(XyJobConfig.class);

    @Autowired
    private EurekaClient eurekaClient;

    @Value("${xxl.job.enable}")
    private boolean enable;

    @Value("${xxl.job.admin.eureka-server}")
    private String adminServer;

    @Value("${xxl.job.admin.context-path}")
    private String adminContextPath;

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.executor.appname}")
    private String appName;

    @Value("${xxl.job.executor.ip}")
    private String ip;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;

    @Bean(initMethod = "start", destroyMethod = "destroy")
    public XxlJobSpringExecutor xxlJobExecutor() {
        if (!enable) {
            return null;
        }
        logger.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        if (adminAddresses == null || adminAddresses.trim().length() == 0) {
            xxlJobSpringExecutor.setAdminAddresses(this.queryEurekaHost(adminServer) + adminContextPath);
        } else {
            xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        }
        xxlJobSpringExecutor.setAppName(appName);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }

    // 获取Eureka上Job-Manager注册的ip和端口
    private String queryEurekaHost(String eurekaServer){
        InstanceInfo instanceInfo = eurekaClient.getNextServerFromEureka(eurekaServer, false);
        return "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort();
    }

/**
     * 针对多网卡、容器内部署等情况，可借助 "spring-cloud-commons" 提供的 "InetUtils" 组件灵活定制注册IP；
     *
     *      1、引入依赖：
     *          <dependency>
     *             <groupId>org.springframework.cloud</groupId>
     *             <artifactId>spring-cloud-commons</artifactId>
     *             <version>${version}</version>
     *         </dependency>
     *
     *      2、配置文件，或者容器启动变量
     *          spring.cloud.inetutils.preferred-networks: 'xxx.xxx.xxx.'
     *
     *      3、获取IP
     *          String ip_ = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
     */


}
