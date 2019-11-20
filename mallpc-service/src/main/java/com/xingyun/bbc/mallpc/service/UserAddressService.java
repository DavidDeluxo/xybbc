package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.mallpc.model.vo.address.UserAddressDetailsVo;

/**
 * 用户地址
 * @author chenxiang
 * @version 1.0.0
 * @date 2019-11-20
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public interface UserAddressService {

    /**
     * 查询用户默认收货地址
     * @param userId
     * @return
     */
    UserAddressDetailsVo defaultAddress(Integer userId);
}
