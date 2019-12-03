package com.xingyun.bbc.mall.service.impl;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponReceiveApi;
import com.xingyun.bbc.core.market.dto.MyCoupinReceiveDto;
import com.xingyun.bbc.core.market.enums.CouponReceiveStatusEnum;
import com.xingyun.bbc.core.market.enums.CouponTypeEnum;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.market.po.CouponReceive;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.PageUtils;
import com.xingyun.bbc.mall.base.utils.PriceUtil;
import com.xingyun.bbc.mall.base.utils.ResultUtils;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.MyCouponDto;
import com.xingyun.bbc.mall.model.vo.CouponVo;
import com.xingyun.bbc.mall.model.vo.MyCouponVo;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.service.MyCouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MyCouponServiceImpl implements MyCouponService {

    private static final Logger logger = LoggerFactory.getLogger(MyCouponService.class);

    @Autowired
    private CouponReceiveApi couponReceiveApi;

    @Autowired
    private CouponApi couponApi;

    @Autowired
    private PageUtils pageUtils;

    @Override
    public Result<MyCouponVo> getMyCouponVo(MyCouponDto myCouponDto) {
        //查询已经领到的优惠券信息
        MyCoupinReceiveDto myCoupinReceiveDto = new MyCoupinReceiveDto();
        myCoupinReceiveDto.setFuid(myCouponDto.getFuid());
        myCoupinReceiveDto.setFuserCouponStatus(myCouponDto.getFuserCouponStatus());
        myCoupinReceiveDto.setCurrentPage(myCouponDto.getCurrentPage());
        myCoupinReceiveDto.setPageSize(myCouponDto.getPageSize());
        Result<Long> countResult = couponReceiveApi.selectMyCouponCount(myCoupinReceiveDto);
        if (!countResult.isSuccess()){
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (countResult.getData().intValue() == 0) {
            return Result.success(new MyCouponVo());
        }
        Result<List<CouponReceive>> listResult = couponReceiveApi.selectMyCouponList(myCoupinReceiveDto);
        if (!listResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }

        Integer count = countResult.getData().intValue();
        List<CouponReceive> couponReceives = listResult.getData();
        PageVo<CouponVo> couponPageVo = pageUtils.convert(count, couponReceives, CouponVo.class, myCouponDto);
        List<Long> couponIds = couponReceives.stream().map(CouponReceive::getFcouponId).distinct().collect(Collectors.toList());
        Result<List<Coupon>> couponResult = couponApi.queryByCriteria(Criteria.of(Coupon.class)
                .andIn(Coupon::getFcouponId, couponIds)
                .fields(Coupon::getFcouponName, Coupon::getFcouponType,
                        Coupon::getFthresholdAmount, Coupon::getFdeductionValue,
                        Coupon::getFvalidityType, Coupon::getFvalidityDays, Coupon::getFreleaseType));
        List<Coupon> couponList = ResultUtils.getListNotEmpty(couponResult, MallExceptionCode.COUPON_IS_NOT_EXIST);
        Map<Long, List<Coupon>> couponMap = couponList.stream().collect(Collectors.groupingBy(Coupon::getFcouponId));

        //查询优惠券信息
        for (CouponVo couponVo : couponPageVo.getList()) {
            Coupon coupon = couponMap.get(couponVo.getFcouponId()).get(0);
            couponVo.setFcouponName(coupon.getFcouponName());
            couponVo.setFcouponType(coupon.getFcouponType());
            couponVo.setFthresholdAmount(PriceUtil.toYuan(coupon.getFthresholdAmount()));

            //优惠券类型，1满减券、2折扣券
            if (coupon.getFcouponType().equals(CouponTypeEnum.FULL_REDUCTION.getCode())) {
                couponVo.setFdeductionValue(PriceUtil.toYuan(coupon.getFdeductionValue()));
            } else {
                couponVo.setFdeductionValue(new BigDecimal(coupon.getFdeductionValue()).divide(new BigDecimal("10"), 1, BigDecimal.ROUND_HALF_UP));
            }
            couponVo.setFvalidityType(coupon.getFvalidityType());
            couponVo.setFvalidityDays(coupon.getFvalidityDays());
            couponVo.setFreleaseType(coupon.getFreleaseType());
        }
        //查询各种优惠券数量
        Integer fuserCouponStatus = myCouponDto.getFuserCouponStatus();

        MyCouponVo myCouponVo = new MyCouponVo();
        myCouponVo.setCouponVo(couponPageVo);
        myCouponVo.setUnUsedNum(fuserCouponStatus.equals(CouponReceiveStatusEnum.NOT_USED.getCode()) ? count
                : this.getCouponByStatus(myCouponDto, CouponReceiveStatusEnum.NOT_USED.getCode()));
        myCouponVo.setUsedNum(fuserCouponStatus.equals(CouponReceiveStatusEnum.USED.getCode()) ? count
                : this.getCouponByStatus(myCouponDto, CouponReceiveStatusEnum.USED.getCode()));
        myCouponVo.setExpiredNum(fuserCouponStatus.equals(CouponReceiveStatusEnum.NULLIFY.getCode()) ? count
                : this.getCouponByStatus(myCouponDto, CouponReceiveStatusEnum.NULLIFY.getCode()));
        myCouponVo.setNowDate(new Date());

        return Result.success(myCouponVo);
    }

    private Integer getCouponByStatus (MyCouponDto myCouponDto, Integer fuserCouponStatus) {
        Criteria<CouponReceive, Object> criteriaStatus = Criteria.of(CouponReceive.class)
                .andEqualTo(CouponReceive::getFuid, myCouponDto.getFuid())
                .andEqualTo(CouponReceive::getFuserCouponStatus, fuserCouponStatus);
        Result<Integer> countResult = couponReceiveApi.countByCriteria(criteriaStatus);
        if (!countResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        return countResult.getData();
    }
}