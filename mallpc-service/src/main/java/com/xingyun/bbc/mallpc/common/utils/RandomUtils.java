package com.xingyun.bbc.mallpc.common.utils;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import org.apache.commons.lang3.RegExUtils;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-17
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class RandomUtils extends RandomUtil {

    public static String getUUID() {
        TimeBasedGenerator gen = Generators.timeBasedGenerator(EthernetAddress.fromInterface());
        return RegExUtils.replaceAll(gen.generate().toString(), "-", "");
    }

}
