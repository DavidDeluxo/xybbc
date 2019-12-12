package com.xingyun.bbc.common.elasticsearch.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;


/**
 * @author Thstone
 * @version V1.0
 * @Title:
 * @Package com.xingyun.xyb2b.elasticsearch.config
 * @Description: (用一句话描述该文件做什么)
 * @date 2018/12/12 20:43
 */
@Configuration
public class EsConfig {

    @Bean
    @Autowired
    public RestHighLevelClient getClient(EsSettingsProperties esSettingsProperties){

        RestClientBuilder restClientBuilder = null;

        if(esSettingsProperties.getAuthRequired()){
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            if(StringUtils.isEmpty(esSettingsProperties.getAccount())){
                throw new RuntimeException("认证开启, 用户名不能为空");
            }
            if(StringUtils.isEmpty(esSettingsProperties.getPassword())){
                throw new RuntimeException("认证开启, 密码不能为空");
            }
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esSettingsProperties.getAccount(), esSettingsProperties.getPassword()));
            restClientBuilder = RestClient.builder(
                    HttpHost.create(esSettingsProperties.getIp())).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    httpClientBuilder.disableAuthCaching();
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });
        }else {
            restClientBuilder = RestClient.builder(
    	        		new HttpHost(esSettingsProperties.getIp(), esSettingsProperties.getPort(), "http"));
        }
        RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);
        return  client;
    }
}
