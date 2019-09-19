package com.xingyun.bbc.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Thstone
 * @version V1.0
 * @Title:
 * @Package com.xingyun.bbc
 * @Description: (用一句话描述该文件做什么)
 * @date 2019/8/17 18:03
 */
@EnableAsync
@SpringCloudApplication
@EnableFeignClients("com.xingyun.bbc")
@ComponentScan(value="com.xingyun.bbc")
public class MallApplication {

	public static void main(String[] args) {
		SpringApplication.run(MallApplication.class, args);
	}


}
