package com.xingyun.bbc.mall.service.impl;


import com.xingyun.bbc.core.activity.api.CouponProviderApi;
import com.xingyun.bbc.core.activity.enums.CouponScene;
import com.xingyun.bbc.core.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.core.activity.model.dto.CouponReleaseDto;

import com.xingyun.bbc.core.activity.model.vo.CouponQueryVo;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;

import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponCodeApi;

import com.xingyun.bbc.core.market.api.CouponReceiveApi;
import com.xingyun.bbc.core.market.enums.CouponReleaseTypeEnum;
import com.xingyun.bbc.core.market.enums.CouponStatusEnum;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.market.po.CouponCode;
import com.xingyun.bbc.core.market.po.CouponReceive;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.utils.Result;

import com.xingyun.bbc.mall.common.exception.MallExceptionCode;


import com.xingyun.bbc.mall.model.dto.ReceiveCouponDto;

import com.xingyun.bbc.mall.model.vo.ReceiveCenterCoupon;
import com.xingyun.bbc.mall.service.GoodDetailService;
import com.xingyun.bbc.mall.service.ReceiveCenterService;

import io.seata.spring.annotation.GlobalTransactional;


import org.apache.commons.collections.CollectionUtils;
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
    private CouponApi couponApi;

    @Autowired
    GoodDetailService goodDetailService;

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
       Result<CouponCode>  couponCode = couponCodeApi.queryOneByCriteria(Criteria.of(CouponCode.class)
               .andEqualTo(CouponCode::getFcouponCode,fcouponCode)
               .fields(CouponCode::getFcouponId));
        if (!couponCode.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if(null == couponCode.getData()){
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
        if(CollectionUtils.isEmpty(couponQueryVoResult.getData())){
            throw new BizException(MallExceptionCode.USER_NOT_COUPON);
        }
        //判断该券是否在可领券集合中
        List<Long> couponIdList = couponQueryVoResult.getData().stream().map(s -> s.getFcouponId()).collect(Collectors.toList());
        if(!couponIdList.contains(couponCode.getData().getFcouponId())){
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
    public Result<List<ReceiveCenterCoupon>> getCoupon(CouponQueryDto couponQueryDto) {
        //校验用户id
        if(null == couponQueryDto.getUserId()){
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        List<Integer> list = new ArrayList<>();
        //查出发放类型为2：页面领取的数据
        list.add(2);
        couponQueryDto.setReleaseTypes(list);
        Result<List<CouponQueryVo>> couponQueryVos = couponProviderApi.queryByUserId(couponQueryDto);
        List<ReceiveCenterCoupon> receiveCenterCouponList = new ArrayList<>();
        for (CouponQueryVo couponQueryVo:couponQueryVos.getData()) {
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
                ReceiveCenterCoupon receiveCenterCoupon = new ReceiveCenterCoupon();
                receiveCenterCoupon.setFcouponId(couponQueryVo.getFcouponId());
                receiveCenterCoupon.setFcouponName(couponQueryVo.getFcouponName());
                receiveCenterCoupon.setFcouponType(couponQueryVo.getFcouponType());
                receiveCenterCoupon.setFdeductionValue(BigDecimal.valueOf(couponQueryVo.getFdeductionValue()));
                receiveCenterCoupon.setFthresholdAmount(BigDecimal.valueOf(couponQueryVo.getFthresholdAmount()));
                receiveCenterCoupon.setFvalidityEnd(couponQueryVo.getFvalidityEnd());
                receiveCenterCoupon.setFvalidityStart(couponQueryVo.getFvalidityStart());
                receiveCenterCoupon.setNowDate(new Date());
                receiveCenterCoupon.setReceiveNum(Long.valueOf(countResult.getData()));
                receiveCenterCoupon.setFperLimit(couponQueryVo.getFperLimit());
                receiveCenterCouponList.add(receiveCenterCoupon);
            }
        }
        return Result.success(receiveCenterCouponList);
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
    public Result addReceiveCoupon(Long fcouponId, Long fuid) {
        if (null == fcouponId || null == fuid) {
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        ReceiveCouponDto receiveCouponDto = new ReceiveCouponDto();
        receiveCouponDto.setFuid(fuid);
        receiveCouponDto.setFcouponId(fcouponId);
        Result result = goodDetailService.receiveCoupon(receiveCouponDto);
        return Result.success(result.getData());
        }



}
