package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.CityRegionApi;
import com.xingyun.bbc.core.operate.enums.BooleanNum;
import com.xingyun.bbc.core.operate.po.CityRegion;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserDeliveryApi;
import com.xingyun.bbc.core.user.po.UserDelivery;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.core.utils.StringUtil;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.FileUtils;
import com.xingyun.bbc.mallpc.common.utils.PageHelper;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.common.utils.StringUtilExtention;
import com.xingyun.bbc.mallpc.model.dto.address.CityRegionDto;
import com.xingyun.bbc.mallpc.model.dto.address.UserAddressDetailsDto;
import com.xingyun.bbc.mallpc.model.dto.address.UserAddressDto;
import com.xingyun.bbc.mallpc.model.dto.address.UserAddressListDto;
import com.xingyun.bbc.mallpc.model.vo.ImageVo;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.address.CityRegionVo;
import com.xingyun.bbc.mallpc.model.vo.address.UserAddressDetailsVo;
import com.xingyun.bbc.mallpc.model.vo.address.UserAddressListVo;
import com.xingyun.bbc.mallpc.service.UserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-20
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */

@Slf4j
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Resource
    private UserDeliveryApi userDeliveryApi;

    @Resource
    private DozerHolder convertor;

    @Resource
    private PageHelper pageHelper;

    @Resource
    private CityRegionApi cityRegionApi;

    /**
     * @author nick
     * @date 2019-11-20
     * @description :  收货地址列表查询
     * @version 1.0.0
     */
    @Override
    public Result<PageVo<UserAddressListVo>> query(UserAddressListDto userAddressListDto) {
        String fdeliveryCardid = userAddressListDto.getFdeliveryCardid();
        String fdeliveryMobile = userAddressListDto.getFdeliveryMobile();
        String fdeliveryName = userAddressListDto.getFdeliveryName();
        Criteria<UserDelivery, Object> deliveryCondition = Criteria.of(UserDelivery.class).andEqualTo(UserDelivery::getFisDelete, 0);
        if (StringUtil.isNotBlank(fdeliveryCardid)) {
            Ensure.that(StringUtilExtention.idCardCheck(fdeliveryCardid)).isTrue(MallPcExceptionCode.ID_CARD_NUMBER_ILLEGAL);
            deliveryCondition.andEqualTo(UserDelivery::getFdeliveryCardid, fdeliveryCardid);
        }
        if (StringUtil.isNotBlank(fdeliveryMobile)) {
            Ensure.that(StringUtilExtention.mobileCheck(fdeliveryMobile)).isTrue(MallPcExceptionCode.BIND_MOBILE_ERROR);
            deliveryCondition.andEqualTo(UserDelivery::getFdeliveryMobile, fdeliveryMobile);
        }
        if (StringUtil.isNotBlank(fdeliveryName)) {
            deliveryCondition.andEqualTo(UserDelivery::getFdeliveryName, fdeliveryName);
        }
        Result<Integer> integerResult = userDeliveryApi.countByCriteria(deliveryCondition);
        Ensure.that(integerResult).isNotNull(MallPcExceptionCode.SYSTEM_ERROR);
        Integer totalCount = integerResult.getData();
        if (totalCount < 1) {
            return pageHelper.emptyResult(userAddressListDto, UserAddressListVo.class);
        }
        // 分页
        Result<List<UserDelivery>> userDeliveryResult = userDeliveryApi.queryByCriteria(deliveryCondition.page(userAddressListDto.getCurrentPage(), userAddressListDto.getPageSize()));
        Ensure.that(userDeliveryResult.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        List<UserDelivery> userDeliveryList = userDeliveryResult.getData();
        List<UserAddressListVo> vos = userDeliveryList.stream().map(userDelivery -> {
            UserAddressListVo vo = convertor.convert(userDelivery, UserAddressListVo.class);
            String deliveryArea = StringUtils.join(userDelivery.getFdeliveryProvinceName(), userDelivery.getFdeliveryCityName(), userDelivery.getFdeliveryAreaName());
            if (Objects.equals(userDelivery.getFisDefualt(), 0)) {
                vo.setIsDefualt("/");
            } else if (Objects.equals(userDelivery.getFisDefualt(), 1)) {
                vo.setIsDefualt("默认地址");
            }
            vo.setFdeliveryCardid(StringUtils.overlay(userDelivery.getFdeliveryCardid(), "****", 4, 7));
            if (StringUtils.isNotBlank(userDelivery.getFdeliveryCardUrlBack()) && StringUtils.isNotBlank(userDelivery.getFdeliveryCardUrlFront())) {
                vo.setIsCardUpload("已上传");
            } else {
                vo.setIsCardUpload("未上传");
            }
            vo.setDeliveryArea(deliveryArea);
            return vo;
        }).collect(toList());
        return Result.success(new PageVo<>(totalCount, userAddressListDto.getCurrentPage(), userAddressListDto.getPageSize(), vos));
    }

    /**
     * @author nick
     * @date 2019-11-20
     * @description :  新增或编辑收货地址
     * @version 1.0.0
     */
    @Override
    public Result saveOrUpdate(UserAddressDto userAddressDto) {
        //校验身份证 手机号
        Ensure.that(StringUtilExtention.mobileCheck(userAddressDto.getFdeliveryMobile())).isTrue(MallPcExceptionCode.BIND_MOBILE_ERROR);
        Ensure.that(StringUtilExtention.idCardCheck(userAddressDto.getFdeliveryCardid())).isTrue(MallPcExceptionCode.ID_CARD_NUMBER_ILLEGAL);
        UserDelivery userDelivery = convertor.convert(userAddressDto, UserDelivery.class);
        if (StringUtil.isBlank(userAddressDto.getFdeliveryUserId())) {
            Ensure.that(userDeliveryApi.create(userDelivery).isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        } else {
            Ensure.that(userDeliveryApi.updateNotNull(userDelivery).isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        }
        return Result.success();
    }

    /**
     * @author nick
     * @date 2019-11-20
     * @description :  收货地址详情
     * @version 1.0.0
     */
    @Override
    public Result<UserAddressDetailsVo> view(UserAddressDetailsDto userAddressDetailsDto) {
        Result<UserDelivery> userDeliveryResult = userDeliveryApi.queryOneByCriteria(Criteria.of(UserDelivery.class).andEqualTo(UserDelivery::getFdeliveryUserId, userAddressDetailsDto.getFdeliveryUserId()));
        Ensure.that(userDeliveryResult).isNotNull(MallPcExceptionCode.USER_DELIVERY_ADDRESS_NOT_EXISTS);
        UserDelivery userDelivery = userDeliveryResult.getData();
        UserAddressDetailsVo vo = convertor.convert(userDelivery, UserAddressDetailsVo.class);
        String deliveryArea = StringUtils.join(userDelivery.getFdeliveryProvinceName(), userDelivery.getFdeliveryCityName(), userDelivery.getFdeliveryAreaName());
        vo.setDeliveryArea(deliveryArea);
        vo.setCardUrlFront(new ImageVo(userDelivery.getFdeliveryCardUrlFront()));
        vo.setCardUrlBack(new ImageVo(userDelivery.getFdeliveryCardUrlBack()));
        return Result.success(vo);
    }

    /**
     * @author nick
     * @date 2019-11-20
     * @description :  删除收货地址
     * @version 1.0.0
     */
    @Override
    public Result del(UserAddressDetailsDto userAddressDetailsDto) {
        UserDelivery userDelivery = new UserDelivery();
        userDelivery.setFdeliveryUserId(userAddressDetailsDto.getFdeliveryUserId());
        userDelivery.setFisDelete(1);
        Ensure.that(userDeliveryApi.updateNotNull(userDelivery).isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        return Result.success();
    }

    /**
     * @author nick
     * @date 2019-11-20
     * @description : 收件地址查询区域列表
     * @version 1.0.0
     */
    @Override
    public Result<List<CityRegionVo>> getCityRegionLis(CityRegionDto cityRegionDto) {
        Result<List<CityRegion>> list = cityRegionApi.queryByCriteria(Criteria.of(CityRegion.class)
                .andEqualTo(CityRegion::getFpRegionId, cityRegionDto.getFpRegionId())
                .andEqualTo(CityRegion::getFregionType, cityRegionDto.getFRegionType())
                .fields(CityRegion::getFregionId, CityRegion::getFcrName));
        if (!list.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        List<CityRegionVo> listResult = convertor.convert(list.getData(), CityRegionVo.class);
        return Result.success(listResult);
    }

    @Override
    public UserAddressDetailsVo defaultAddress(Integer userId) {
        Criteria<UserDelivery, Object> criteria = Criteria.of(UserDelivery.class)
                .andEqualTo(UserDelivery::getFuid, userId)
                .andEqualTo(UserDelivery::getFisDefualt, BooleanNum.TRUE.getCode())
                .andEqualTo(UserDelivery::getFisDelete, BooleanNum.FALSE.getCode());
        UserDelivery userDelivery = ResultUtils.getData(userDeliveryApi.queryOneByCriteria(criteria));
        if (userDelivery == null) {
            return new UserAddressDetailsVo();
        } else {
            if (!StringUtils.isEmpty(userDelivery.getFdeliveryCardUrlFront())) {
                userDelivery.setFdeliveryCardUrlFront(FileUtils.getFileUrl(userDelivery.getFdeliveryCardUrlFront()));
            }
            if (!StringUtils.isEmpty(userDelivery.getFdeliveryCardUrlBack())) {
                userDelivery.setFdeliveryCardUrlBack(FileUtils.getFileUrl(userDelivery.getFdeliveryCardUrlBack()));
            }
            return convertor.convert(userDelivery, UserAddressDetailsVo.class);
        }
    }
}
