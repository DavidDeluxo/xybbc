package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.core.operate.enums.BooleanNum;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserDeliveryApi;
import com.xingyun.bbc.core.user.po.UserDelivery;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.vo.address.UserAddressDetailsVo;
import com.xingyun.bbc.mallpc.service.UserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Resource
    private UserDeliveryApi userDeliveryApi;
    @Autowired
    private DozerHolder dozerHolder;

    @Override
    public UserAddressDetailsVo defaultAddress(Integer userId) {
        Criteria<UserDelivery,Object> criteria = Criteria.of(UserDelivery.class)
                .andEqualTo(UserDelivery::getFuid,userId)
                .andEqualTo(UserDelivery::getFisDefualt,BooleanNum.TRUE.getCode())
                .andEqualTo(UserDelivery::getFisDelete, BooleanNum.FALSE.getCode());
        UserDelivery userDelivery = ResultUtils.getData(userDeliveryApi.queryOneByCriteria(criteria));
        if(userDelivery == null){
            return new UserAddressDetailsVo();
        }else{
            return dozerHolder.convert(userDelivery,UserAddressDetailsVo.class);
        }
    }
}
