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
    /**
     * @author lll
     * @version V1.0
     * @Description: 查询用户收货地址列表
     * @Param: [userDeliveryDto]
     * @return: PageVo<UserDeliveryVo>
     * @date 2019/9/20 13:49
     */
    PageVo<UserDeliveryVo> getUserAddress(UserDeliveryDto userDeliveryDto);

    /**
     * @author lll
     * @version V1.0
     * @Description: 新增用户收货地址
     * @Param: [userDeliveryDto]
     * @return: Result
     * @date 2019/9/20 13:49
     */
    Result addUserAddress(UserDeliveryAddDto userDeliveryDto);

    /**
     * @author lll
     * @version V1.0
     * @Description: 编辑用户收货地址
     * @Param: [userDeliveryDto]
     * @return: Result
     * @date 2019/9/20 13:49
     */
    Result modifyUserAddress(UserDeliveryUpdateDto userDeliveryDto);

    /**
     * @author lll
     * @version V1.0
     * @Description: 删除用户收货地址
     * @Param: [userDeliveryDto]
     * @return: Result
     * @date 2019/9/20 13:49
     */
    Result deleteUserAddress(UserDeliveryDeleteDto userDeliveryDto);

    /**
     * @author lll
     * @version V1.0
     * @Description: 收件地址查询区域列表
     * @Param: [cityRegionDto]
     * @return: Result<List < CityRegionVo>>
     * @date 2019/9/20 13:49
     */
    Result<List<CityRegionVo>> getCityRegionLis(CityRegionDto cityRegionDto);
}
