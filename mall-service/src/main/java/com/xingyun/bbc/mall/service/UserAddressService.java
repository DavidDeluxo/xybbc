package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.*;
import com.xingyun.bbc.mall.model.vo.CityRegionVo;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.model.vo.UserDeliveryVo;

import java.util.List;

/**
 * @author lll
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
public interface UserAddressService {
   PageVo<UserDeliveryVo> getUserAddress(UserDeliveryDto userDeliveryDto);

    Result addUserAddress(UserDeliveryAddDto userDeliveryDto);

    Result modifyUserAddress(UserDeliveryUpdateDto userDeliveryDto);

    Result deleteUserAddress(UserDeliveryDeleteDto userDeliveryDto);

    Result<List<CityRegionVo>> getCityRegionLis(CityRegionDto cityRegionDto);
}
