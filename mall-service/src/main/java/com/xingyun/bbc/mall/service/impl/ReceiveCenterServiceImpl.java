package com.xingyun.bbc.mall.service.impl;


import com.google.common.collect.Lists;
import com.xingyun.bbc.core.activity.api.CouponProviderApi;
import com.xingyun.bbc.core.activity.enums.CouponScene;
import com.xingyun.bbc.core.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.core.activity.model.dto.CouponReleaseDto;

import com.xingyun.bbc.core.activity.model.vo.CouponQueryVo;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;

import com.xingyun.bbc.core.market.api.CouponBindUserApi;
import com.xingyun.bbc.core.market.api.CouponCodeApi;

import com.xingyun.bbc.core.market.api.CouponReceiveApi;

import com.xingyun.bbc.core.market.po.CouponBindUser;
import com.xingyun.bbc.core.market.po.CouponCode;
import com.xingyun.bbc.core.market.po.CouponReceive;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.utils.Result;

import com.xingyun.bbc.mall.base.utils.RandomUtils;
import com.xingyun.bbc.mall.common.constans.MallConstants;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;


import com.xingyun.bbc.mall.common.lock.XybbcLock;
import com.xingyun.bbc.mall.model.dto.ReceiveCouponDto;

import com.xingyun.bbc.mall.model.vo.ReceiveCenterCouponVo;
import com.xingyun.bbc.mall.service.GoodDetailService;
import com.xingyun.bbc.mall.service.ReceiveCenterService;

import io.seata.spring.annotation.GlobalTransactional;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ReceiveCenterServiceImpl implements ReceiveCenterService {

    public static final Logger logger = LoggerFactory.getLogger(ReceiveCenterServiceImpl.class);


    @Autowired
    private CouponProviderApi couponProviderApi;

    @Autowired
    CouponCodeApi couponCodeApi;

    @Autowired
    CouponReceiveApi couponReceiveApi;

    @Autowired
    private CouponBindUserApi couponBindUserApi;

    @Autowired
    GoodDetailService goodDetailService;


    @Autowired
    private XybbcLock xybbcLock;

    /**
     * @author lll
     * @version V1.0
     * @Description: 券码兑换优惠券
     * @Param: receiveCouponDto
     * @return: Boolean                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    @Override
    public Result receiveCodeCoupon(String fcouponCode, Long fuid) {
        //校验参数
        if (null == fuid || null == fcouponCode) {
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        //通过券码查询券id
        Result<CouponCode> couponCode = couponCodeApi.queryOneByCriteria(Criteria.of(CouponCode.class)
                .andEqualTo(CouponCode::getFcouponCode, fcouponCode)
                .fields(CouponCode::getFcouponId));
        if (!couponCode.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (null == couponCode.getData()) {
            throw new BizException(MallExceptionCode.CODE_NOT_COUPON);
        }
        //校验券码对应的券和指定会员关系
        CouponQueryDto couponQueryDto = new CouponQueryDto();
        couponQueryDto.setUserId(fuid);
        List<Integer> list = new ArrayList<>();
        list.add(8);
        couponQueryDto.setReleaseTypes(list);
        Result<List<CouponQueryVo>> couponQueryVoResult = couponProviderApi.queryByUserId(couponQueryDto);
        if (!couponQueryVoResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (CollectionUtils.isEmpty(couponQueryVoResult.getData())) {
            throw new BizException(MallExceptionCode.USER_NOT_COUPON);
        }
        //判断该券是否在可领券集合中
        List<Long> couponIdList = couponQueryVoResult.getData().stream().map(s -> s.getFcouponId()).collect(Collectors.toList());
        if (!couponIdList.contains(couponCode.getData().getFcouponId())) {
            throw new BizException(MallExceptionCode.USER_NOT_RIGHT_COUPON);
        }
        ReceiveCouponDto receiveCouponDto = new ReceiveCouponDto();
        receiveCouponDto.setFuid(fuid);
        receiveCouponDto.setFcouponCode(fcouponCode);
        //调用聚合服务进行领券
        Result booleanResult = this.receiveCoupon(receiveCouponDto);
        if (!booleanResult.isSuccess()) {
            return booleanResult;
        }
        return Result.success(booleanResult.getData());
    }


    /**
     * @author lll
     * @version V1.0
     * @Description: 调用远程服务领取优惠券
     * @Param: receiveCouponDto
     * @return: Boolean                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    @GlobalTransactional
    public Result receiveCoupon(ReceiveCouponDto receiveCouponDto) {
        Long fuid = receiveCouponDto.getFuid();
        String fcouponCode = receiveCouponDto.getFcouponCode();
        try {
            //封装参数
            CouponReleaseDto couponReleaseDto = new CouponReleaseDto();
            couponReleaseDto.setCouponScene(CouponScene.COUPON_CODE_ACTIVATION);
            couponReleaseDto.setUserId(fuid);
            couponReleaseDto.setCouponCode(fcouponCode);
            //调用领券服务
            Result receiveReceive = couponProviderApi.receive(couponReleaseDto);
            if (!receiveReceive.isSuccess()) {
                return receiveReceive;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success(true);
    }


    /**
     * @author lll
     * @version V1.0
     * @Description: 查询领券中心优惠券
     * @Param: receiveCouponDto
     * @return: List<CouponCenterVo>                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    @Override
    public Result<List<ReceiveCenterCouponVo>> getCoupon(CouponQueryDto couponQueryDto) {
        //校验用户id
        if (null == couponQueryDto.getUserId()) {
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        List<Integer> list = new ArrayList<>();
        //查出发放类型为2：页面领取的数据
        list.add(2);
        couponQueryDto.setReleaseTypes(list);
        Result<List<CouponQueryVo>> couponQueryVos = couponProviderApi.queryByUserId(couponQueryDto);
        List<ReceiveCenterCouponVo> receiveCenterCouponVoList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(couponQueryVos.getData())){
            for (CouponQueryVo couponQueryVo : couponQueryVos.getData()) {
                //查询已经领到的券张数
                Result<Integer> countResult = couponReceiveApi.countByCriteria(Criteria.of(CouponReceive.class)
                        .andEqualTo(CouponReceive::getFuid, couponQueryDto.getUserId())
                        .andEqualTo(CouponReceive::getFcouponId, couponQueryVo.getFcouponId()));
                if (!countResult.isSuccess()) {
                    throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                }
                //当领券上限未达到限领次数时
                if (countResult.getData() < couponQueryVo.getFperLimit()) {
                    //封装返回对象
                    ReceiveCenterCouponVo receiveCenterCouponVo = new ReceiveCenterCouponVo();
                    receiveCenterCouponVo.setFcouponId(couponQueryVo.getFcouponId());
                    receiveCenterCouponVo.setFcouponName(couponQueryVo.getFcouponName());
                    receiveCenterCouponVo.setFcouponType(couponQueryVo.getFcouponType());
                    receiveCenterCouponVo.setFdeductionValue(BigDecimal.valueOf(couponQueryVo.getFdeductionValue()));
                    receiveCenterCouponVo.setFthresholdAmount(BigDecimal.valueOf(couponQueryVo.getFthresholdAmount()));
                    receiveCenterCouponVo.setFvalidityEnd(couponQueryVo.getFvalidityEnd());
                    receiveCenterCouponVo.setFvalidityStart(couponQueryVo.getFvalidityStart());
                    receiveCenterCouponVo.setNowDate(new Date());
                    receiveCenterCouponVo.setReceiveNum(Long.valueOf(countResult.getData()));
                    receiveCenterCouponVo.setFperLimit(couponQueryVo.getFperLimit());
                    receiveCenterCouponVo.setFvalidityType(couponQueryVo.getFvalidityType());
                    receiveCenterCouponVo.setFvalidityDays(couponQueryVo.getFvalidityDays());
                    receiveCenterCouponVoList.add(receiveCenterCouponVo);
                }
            }
        }
        return Result.success(receiveCenterCouponVoList);
    }


    /**
     * @author lll
     * @version V1.0
     * @Description: 领券中心领取优惠券
     * @Param: receiveCouponDto
     * @return: Result                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    @Override
    @GlobalTransactional
    public Result addReceiveCoupon(Long fcouponId, Long fuid) {
        //校验参数
        if (null == fcouponId || null == fuid) {
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        ReceiveCouponDto receiveCouponDto = new ReceiveCouponDto();
        receiveCouponDto.setFuid(fuid);
        receiveCouponDto.setFcouponId(fcouponId);
        //调用聚合服务进行领券操作
        Result result = this.receiveCenterCoupon(receiveCouponDto);
        if (!result.isSuccess()) {
            return result;
        }
        return Result.success(result.getData());
    }


    public Result receiveCenterCoupon(ReceiveCouponDto receiveCouponDto) {
        Long fcouponId = receiveCouponDto.getFcouponId();
        Long fuid = receiveCouponDto.getFuid();
        if (null == fcouponId || null == fuid) {
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        //对唯一的字段添加分布式锁,如果锁已存在,会立刻抛异常
        String lockKey = StringUtils.join(Lists.newArrayList(MallConstants.MALL_RECEIVE_COUPON, fcouponId, fuid), ":");
        String lockValue = RandomUtils.getUUID();
        try {
            //绑定用户和优惠券关系
            Ensure.that(xybbcLock.tryLockTimes(lockKey, lockValue, 3, 6)).isTrue(MallExceptionCode.SYSTEM_BUSY_ERROR);
            CouponBindUser couponBindUser = new CouponBindUser();
            couponBindUser.setFuid(fuid);
            couponBindUser.setFcreateTime(new Date());
            couponBindUser.setFcouponId(fcouponId);
            couponBindUser.setFisReceived(1);
            Result<Integer> insertBindResult = couponBindUserApi.create(couponBindUser);
            if (!insertBindResult.isSuccess()) {
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
            //更新优惠券发放数量
            CouponReleaseDto couponReleaseDto = new CouponReleaseDto();
            couponReleaseDto.setCouponScene(CouponScene.PAGE_RECEIVE);
            couponReleaseDto.setCouponId(fcouponId);
            couponReleaseDto.setUserId(fuid);
            couponReleaseDto.setAlreadyReceived(true);
            couponReleaseDto.setDeltaValue(-1);
            Result updateReleaseResult = couponProviderApi.updateReleaseQty(couponReleaseDto);
            if (!updateReleaseResult.isSuccess()) {
                return updateReleaseResult;
            }
            //调用领券服务
            Result receiveReceive = couponProviderApi.receive(couponReleaseDto);
            if (!receiveReceive.isSuccess()) {
                return receiveReceive;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            xybbcLock.releaseLock(lockKey, lockValue);
        }
        return Result.success(true);
    }


}
