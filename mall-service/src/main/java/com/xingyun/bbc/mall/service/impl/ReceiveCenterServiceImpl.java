package com.xingyun.bbc.mall.service.impl;


import com.xingyun.bbc.core.activity.api.CouponProviderApi;
import com.xingyun.bbc.core.activity.enums.CouponScene;
import com.xingyun.bbc.core.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.core.activity.model.dto.CouponReleaseDto;

import com.xingyun.bbc.core.activity.model.vo.CouponQueryVo;
import com.xingyun.bbc.core.exception.BizException;

import com.xingyun.bbc.core.market.api.CouponCodeApi;

import com.xingyun.bbc.core.utils.Result;

import com.xingyun.bbc.mall.common.exception.MallExceptionCode;


import com.xingyun.bbc.mall.model.dto.ReceiveCouponDto;

import com.xingyun.bbc.mall.model.vo.CouponCenterVo;
import com.xingyun.bbc.mall.service.ReceiveCenterService;

import io.seata.spring.annotation.GlobalTransactional;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ReceiveCenterServiceImpl implements ReceiveCenterService {

    public static final Logger logger = LoggerFactory.getLogger(ReceiveCenterServiceImpl.class);


    @Autowired
    private CouponProviderApi couponProviderApi;

    @Autowired
    CouponCodeApi couponCodeApi;


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
        ReceiveCouponDto receiveCouponDto = new ReceiveCouponDto();
        receiveCouponDto.setFuid(fuid);
        receiveCouponDto.setFcouponCode(fcouponCode);
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
    public Result<List<CouponCenterVo>> getCoupon(CouponQueryDto couponQueryDto) {
        //校验用户id
        if(null == couponQueryDto.getUserId()){
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        Result<List<CouponQueryVo>> couponQueryVos = couponProviderApi.queryByUserId(couponQueryDto);
        return null;
    }
}
