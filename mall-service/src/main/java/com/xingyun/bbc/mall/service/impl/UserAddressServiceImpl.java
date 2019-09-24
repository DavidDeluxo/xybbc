package com.xingyun.bbc.mall.service.impl;


import com.google.common.collect.Lists;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.CityRegionApi;
import com.xingyun.bbc.core.operate.po.CityRegion;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserDeliveryApi;
import com.xingyun.bbc.core.user.po.UserDelivery;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.PageUtils;
import com.xingyun.bbc.mall.model.dto.*;
import com.xingyun.bbc.mall.model.vo.CityRegionVo;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.model.vo.UserDeliveryVo;
import com.xingyun.bbc.mall.service.UserAddressService;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author lll
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {

    public static final Logger logger = LoggerFactory.getLogger(UserAddressServiceImpl.class);

    @Autowired
    private UserDeliveryApi userDeliveryApi;

    @Autowired
    private Mapper dozerMapper;

    @Autowired
    private DozerHolder dozerHolder;

    @Autowired
    private CityRegionApi cityRegionApi;

    @Autowired
    private PageUtils pageUtils;

    @Override
    public PageVo<UserDeliveryVo> getUserAddress(UserDeliveryDto userDeliveryDto) {
        Criteria<UserDelivery, Object> criteria = Criteria.of(UserDelivery.class);
        if(!StringUtils.isEmpty(userDeliveryDto.getFuid())){
            criteria.andEqualTo(UserDelivery::getFuid,userDeliveryDto.getFuid())
                    .andEqualTo(UserDelivery::getFisDelete,0).sortDesc(UserDelivery::getFmodifyTime);
            }
        Result<Integer>  totalResult = userDeliveryApi.countByCriteria(criteria);
        if(!totalResult.isSuccess()){
            logger.info("查询收货地址信息失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if(0 == totalResult.getData() || Objects.isNull(totalResult.getData())){
            return new  PageVo<>(0, userDeliveryDto.getCurrentPage(), userDeliveryDto.getPageSize(), Lists.newArrayList());
        }
        Result<List<UserDelivery>> userDeliveryList = userDeliveryApi.queryByCriteria(criteria);
        if (!userDeliveryList.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if(CollectionUtils.isEmpty(userDeliveryList.getData())){
            return new  PageVo<>(0, userDeliveryDto.getCurrentPage(), userDeliveryDto.getPageSize(), Lists.newArrayList());
        }
        List<UserDeliveryVo> userDeliveryVoList = dozerHolder.convert(userDeliveryList.getData(),UserDeliveryVo.class);
        return pageUtils.convert(totalResult.getData(), userDeliveryVoList, UserDeliveryVo.class, userDeliveryDto);
        }


      public Result<List<UserDeliveryVo>> getUserAddressList(UserDeliveryDto userDeliveryDto) {
        Criteria<UserDelivery, Object> criteria = Criteria.of(UserDelivery.class);
        if(!StringUtils.isEmpty(userDeliveryDto.getFuid())){
            criteria.andEqualTo(UserDelivery::getFuid,userDeliveryDto.getFuid()).andEqualTo(UserDelivery::getFisDelete,0);
        }
          Result<List<UserDelivery>> userDeliveryList = userDeliveryApi.queryByCriteria(criteria.page(userDeliveryDto.getCurrentPage(), userDeliveryDto.getPageSize()));
          if (!userDeliveryList.isSuccess()) {
              throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
          }
        List<UserDeliveryVo> userDeliveryVoList = dozerHolder.convert(userDeliveryList.getData(),UserDeliveryVo.class);
        return Result.success(userDeliveryVoList);
    }


    @Override
    public Result addUserAddress(UserDeliveryAddDto userDeliveryDto) {
        Integer isDefualt = userDeliveryDto.getFisDefualt();
        UserDeliveryDto userDelivery = new UserDeliveryDto();
        userDelivery.setFuid(userDeliveryDto.getFuid());
        if(!StringUtils.isEmpty(isDefualt)){
            if(isDefualt == 1){
                List<UserDeliveryVo> userDeliveryVoList= this.getUserAddressList(userDelivery).getData();
                UserDelivery update = new UserDelivery();
                for (UserDeliveryVo userDeliveryVo:userDeliveryVoList) {
                    Integer defualt = userDeliveryVo.getFisDefualt();
                    if(defualt == 1){
                        update.setFdeliveryUserId(userDeliveryVo.getFdeliveryUserId());
                        update.setFisDefualt(0);
                        Result<Integer> result = userDeliveryApi.updateNotNull(update);
                        if (!result.isSuccess()) {
                            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                        }
                    }
                }
            }
        }
        UserDelivery user= dozerMapper.map(userDeliveryDto, UserDelivery.class);
        Result<Integer> result = userDeliveryApi.create(user);
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        return Result.success();
    }

    @Override
    public Result modifyUserAddress(UserDeliveryUpdateDto userDeliveryDto) {
        Integer isDefualt = userDeliveryDto.getFisDefualt();
        UserDeliveryDto userDelivery = new UserDeliveryDto();
        userDelivery.setFuid(userDeliveryDto.getFuid());
        if(!StringUtils.isEmpty(isDefualt)){
            if(isDefualt == 1){
                List<UserDeliveryVo> userDeliveryVoList= this.getUserAddressList(userDelivery).getData();
                UserDelivery update = new UserDelivery();
                for (UserDeliveryVo userDeliveryVo:userDeliveryVoList) {
                    Integer defualt = userDeliveryVo.getFisDefualt();
                    if(defualt == 1){
                        update.setFdeliveryUserId(userDeliveryVo.getFdeliveryUserId());
                        update.setFisDefualt(0);
                        Result<Integer> result = userDeliveryApi.updateNotNull(update);
                        if (!result.isSuccess()) {
                            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                        }
                    }
                }
            }
        }
        Result<Integer> resultTwo = userDeliveryApi.updateNotNull(dozerMapper.map(userDeliveryDto, UserDelivery.class));
        if (!resultTwo.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        return Result.success();
    }

    @Override
    public Result deleteUserAddress(UserDeliveryDeleteDto userDeliveryDto) {
        if(!StringUtils.isEmpty(userDeliveryDto.getFdeliveryUserIds())){
            String[] fuidsStr = userDeliveryDto.getFdeliveryUserIds().split(",");
            Integer[] fuids = (Integer[]) ConvertUtils.convert(fuidsStr,Integer.class);
            List<Integer> fuidList = Arrays.asList(fuids);
            UserDelivery userDelivery = new UserDelivery();
            for (Integer item : fuidList) {
                userDelivery.setFdeliveryUserId(Long.valueOf(item));
                userDelivery.setFisDelete(1);
                Result<Integer> result = userDeliveryApi.updateNotNull(userDelivery);
                if (!result.isSuccess()) {
                    throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return Result.success();
        }


    @Override
    public Result<List<CityRegionVo>> getCityRegionLis(CityRegionDto cityRegionDto) {
        Criteria<CityRegion, Object> criteria = Criteria.of(CityRegion.class);
        if (null != cityRegionDto.getFRegionType()){
            criteria.andEqualTo(CityRegion::getFregionType, cityRegionDto.getFRegionType());
        }
        if (null != cityRegionDto.getFpRegionId()){
            criteria.andEqualTo(CityRegion::getFpRegionId, cityRegionDto.getFpRegionId());
        }
        criteria.fields(CityRegion::getFregionId, CityRegion::getFcrName);
        Result<List<CityRegion>> list = cityRegionApi.queryByCriteria(criteria);
        if (!list.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        List<CityRegionVo> listResult = dozerHolder.convert(list.getData(), CityRegionVo.class);
        return Result.success(listResult);
    }


}
