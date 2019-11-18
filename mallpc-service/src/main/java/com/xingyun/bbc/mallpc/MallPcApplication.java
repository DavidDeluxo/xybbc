/**
 *
 */
package com.xingyun.bbc.mallpc;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@SpringCloudApplication
@EnableFeignClients("com.xingyun.bbc.*")
@ComponentScan("com.xingyun.bbc.*")
public class MallPcApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPcApplication.class, args);
    }

}
