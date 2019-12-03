package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.address.CityRegionDto;
import com.xingyun.bbc.mallpc.model.dto.address.UserAddressDetailsDto;
import com.xingyun.bbc.mallpc.model.dto.address.UserAddressDto;
import com.xingyun.bbc.mallpc.model.dto.address.UserAddressListDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.address.CityRegionVo;
import com.xingyun.bbc.mallpc.model.vo.address.UserAddressDetailsVo;
import com.xingyun.bbc.mallpc.model.vo.address.UserAddressListVo;

import java.util.List;

/**
 * @author nick
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
     * @author nick
     * @date 2019-11-20
     * @description :  收货地址列表查询
     * @version 1.0.0
     */
    Result<PageVo<UserAddressListVo>> query(UserAddressListDto userAddressListDto);

    /**
     * @author nick
     * @date 2019-11-20
     * @description :  新增或编辑收货地址
     * @version 1.0.0
     */
    Result saveOrUpdate(UserAddressDto userAddressDto);

    /**
     * @author nick
     * @date 2019-11-20
     * @description :  收货地址列表详情
     * @version 1.0.0
     */
    Result<UserAddressDetailsVo> view(UserAddressDetailsDto userAddressDetailsDto);

    /**
     * @author nick
     * @date 2019-11-20
     * @description :  删除收货地址
     * @version 1.0.0
     */
    Result del(UserAddressDetailsDto userAddressDetailsDto);

    /**
     * @author nick
     * @date 2019-11-20
     * @description : 收件地址查询区域列表
     * @version 1.0.0
     */
    Result<List<CityRegionVo>> getCityRegionLis(CityRegionDto cityRegionDto);

    /**
     * @author cx
     * @date 2019-11-20
     * @description :  查询用户默认收货地址
     * @version 1.0.0
     */
    UserAddressDetailsVo defaultAddress(Integer userId);

    /**
     * @author nick
     * @date 2019-11-20
     * @description :  查询收件地址 确认订单页
     * @version 1.0.0
     */
    Result<List<UserAddressListVo>> queryAddress(UserAddressListDto userAddressListDto);
}
