package com.xingyun.bbc.mall.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.xingyun.bbc.activity.api.CouponProviderApi;
import com.xingyun.bbc.activity.enums.CouponScene;
import com.xingyun.bbc.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.activity.model.dto.CouponReleaseDto;
import com.xingyun.bbc.activity.model.vo.CouponQueryVo;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponCodeApi;
import com.xingyun.bbc.core.market.api.CouponReceiveApi;
import com.xingyun.bbc.core.market.enums.CouponReleaseTypeEnum;
import com.xingyun.bbc.core.market.enums.CouponStatusEnum;
import com.xingyun.bbc.core.market.enums.CouponTypeEnum;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.market.po.CouponCode;
import com.xingyun.bbc.core.market.po.CouponReceive;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.PriceUtil;
import com.xingyun.bbc.mall.base.utils.RandomUtils;
import com.xingyun.bbc.mall.common.constans.MallConstants;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.common.lock.XybbcLock;
import com.xingyun.bbc.mall.model.dto.QueryCouponDto;
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
import java.util.*;
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
    GoodDetailService goodDetailService;

    @Autowired
    private CouponApi couponApi;

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
    @GlobalTransactional
    @Override
    public Result receiveCodeCoupon(String fcouponCode, Long fuid) {
        //校验参数
        if (null == fuid || null == fcouponCode) {
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        //通过券码查询券id
        Result<CouponCode> couponCode = couponCodeApi.queryOneByCriteria(Criteria.of(CouponCode.class)
                .andEqualTo(CouponCode::getFcouponCode, fcouponCode)
                .fields(CouponCode::getFcouponId, CouponCode::getFisUsed));
        if (!couponCode.isSuccess()) {
            logger.error("通过券码查询券id失败，fcouponCode{}", fcouponCode);
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (null == couponCode.getData()) {
            throw new BizException(MallExceptionCode.CODE_NOT_COUPON);
        }
        if (couponCode.getData().getFisUsed() == 1) {
            throw new BizException(MallExceptionCode.CODE_IS_USED);
        }
        //查询优惠券--状态（已发布）--类型（页面领取）--剩余数量--领取上限--有效期结束时间--发放结束时间
        Result<Coupon> couponResult = couponApi.queryOneByCriteria(Criteria.of(Coupon.class)
                .andEqualTo(Coupon::getFcouponId, couponCode.getData().getFcouponId())
                .andEqualTo(Coupon::getFcouponStatus, CouponStatusEnum.PUSHED.getCode())
                .andEqualTo(Coupon::getFreleaseType, CouponReleaseTypeEnum.COUPON_CODE_ACTIVATION.getCode())
                .fields(Coupon::getFperLimit, Coupon::getFsurplusReleaseQty, Coupon::getFvalidityType,
                        Coupon::getFvalidityEnd, Coupon::getFreleaseTimeEnd, Coupon::getFreleaseTimeType, Coupon::getFcouponId
                ));
        if (!couponResult.isSuccess()) {
            logger.error("查询优惠券失败，fcouponId{}", couponCode.getData().getFcouponId());
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        Coupon coupon = couponResult.getData();
        //校验该券是否存在
        if (null == coupon) {
            throw new BizException(MallExceptionCode.COUPON_IS_NOT_EXIST);
        }
        //校验该券库存
        if (coupon.getFsurplusReleaseQty() <= 0) {
            throw new BizException(MallExceptionCode.COUPON_IS_PAID_OUT);
        }
        Date now = new Date();
        //校验该券有效期时间
        if (coupon.getFvalidityType() == 1 && now.after(coupon.getFvalidityEnd())) {
            return Result.failure(MallExceptionCode.COUPON_IS_INVALID);
        }
        //校验该券领取时间
        if (coupon.getFreleaseTimeType() == 2 && (now.after(coupon.getFreleaseTimeEnd()) || now.before(coupon.getFreleaseTimeStart()))) {
            throw new BizException(MallExceptionCode.COUPON_IS_NOT_TIME);
        }
        //查询用户已经领到的券张数
        Result<Integer> countResult = couponReceiveApi.countByCriteria(Criteria.of(CouponReceive.class)
                .andEqualTo(CouponReceive::getFuid, fuid)
                .andEqualTo(CouponReceive::getFcouponId, couponResult.getData().getFcouponId()));
        if (!couponResult.isSuccess()) {
            logger.error("查询用户已经领到的券张数失败，fcouponId{} fuid{}", couponCode.getData().getFcouponId(), fuid);
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        //校验该券领取上限
        if (null != countResult.getData() && countResult.getData().equals(coupon.getFperLimit())) {
            throw new BizException(MallExceptionCode.COUPON_IS_OUT_OF_LIMIT);
        }
        //校验券码对应的券和指定会员关系
        CouponQueryDto couponQueryDto = new CouponQueryDto();
        couponQueryDto.setUserId(fuid);
        List<Integer> list = new ArrayList<>();
        list.add(8);
        couponQueryDto.setReleaseTypes(list);
        //查询用户可用优惠券
        Result<List<CouponQueryVo>> couponQueryVoResult = couponProviderApi.queryByUserId(couponQueryDto);
        if (!couponQueryVoResult.isSuccess()) {
            logger.error("查询用户可用优惠券失败，请求参数{}", JSONObject.toJSONString(couponQueryDto));
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
        Result result = this.receiveCoupon(receiveCouponDto);
        return result;
    }


    /**
     * @author lll
     * @version V1.0
     * @Description: 调用远程服务领取优惠券
     * @Param: receiveCouponDto
     * @return: Result                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    public Result receiveCoupon(ReceiveCouponDto receiveCouponDto) {
        Long fuid = receiveCouponDto.getFuid();
        String fcouponCode = receiveCouponDto.getFcouponCode();
        try {
            //封装参数
            CouponReleaseDto couponReleaseDto = new CouponReleaseDto();
            couponReleaseDto.setCouponScene(CouponScene.COUPON_CODE_ACTIVATION);
            couponReleaseDto.setUserId(fuid);
            couponReleaseDto.setCouponCode(fcouponCode);
            couponReleaseDto.setDeltaValue(-1);
            //调用领券服务
            Result receiveReceive = couponProviderApi.receive(couponReleaseDto);
            if (!receiveReceive.isSuccess()) {
                logger.error("调用领券服务失败，请求参数{}", JSONObject.toJSONString(couponReleaseDto));
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
    public Result<List<ReceiveCenterCouponVo>> getCoupon(QueryCouponDto queryCouponDto) {
        //校验用户id
        if (null == queryCouponDto.getUserId()) {
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        List<Integer> list = new ArrayList<>();
        CouponQueryDto couponQueryDto = new CouponQueryDto();
        //查出发放类型为2：页面领取的数据
        list.add(2);
        couponQueryDto.setReleaseTypes(list);
        couponQueryDto.setUserId(queryCouponDto.getUserId());
        Result<List<CouponQueryVo>> couponQueryVos = couponProviderApi.queryByUserId(couponQueryDto);
        List<ReceiveCenterCouponVo> receiveCenterCouponVoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(couponQueryVos.getData())) {
            //按时间倒序
            Collections.sort(couponQueryVos.getData(), new Comparator<CouponQueryVo>() {
                @Override
                public int compare(CouponQueryVo o1, CouponQueryVo o2) {
                    return o2.getFmodifyTime().compareTo(o1.getFmodifyTime());
                }
            });
            for (CouponQueryVo couponQueryVo : couponQueryVos.getData()) {
                //查询已经领到的券张数
                Result<Integer> countResult = couponReceiveApi.countByCriteria(Criteria.of(CouponReceive.class)
                        .fields(CouponReceive::getFcouponId)
                        .andEqualTo(CouponReceive::getFuid, couponQueryDto.getUserId())
                        .andEqualTo(CouponReceive::getFcouponId, couponQueryVo.getFcouponId()));
                if (!countResult.isSuccess()) {
                    logger.error("查询已经领到的券张数失败，userid{} couponId{}", couponQueryDto.getUserId(), couponQueryVo.getFcouponId());
                    throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                }
                //当领券上限未达到限领次数时
                if (countResult.getData() < couponQueryVo.getFperLimit()) {
                    //封装返回对象
                    ReceiveCenterCouponVo receiveCenterCouponVo = new ReceiveCenterCouponVo();
                    receiveCenterCouponVo.setFcouponId(couponQueryVo.getFcouponId());
                    receiveCenterCouponVo.setFcouponName(couponQueryVo.getFcouponName());
                    receiveCenterCouponVo.setFcouponType(couponQueryVo.getFcouponType());
                    receiveCenterCouponVo.setFvalidityEnd(couponQueryVo.getFvalidityEnd());
                    receiveCenterCouponVo.setFvalidityStart(couponQueryVo.getFvalidityStart());
                    receiveCenterCouponVo.setNowDate(new Date());
                    receiveCenterCouponVo.setReceiveNum(Long.valueOf(countResult.getData()));
                    receiveCenterCouponVo.setFperLimit(couponQueryVo.getFperLimit());
                    receiveCenterCouponVo.setFvalidityType(couponQueryVo.getFvalidityType());
                    receiveCenterCouponVo.setFvalidityDays(couponQueryVo.getFvalidityDays());
                    receiveCenterCouponVo.setFthresholdAmount(PriceUtil.toYuan(couponQueryVo.getFthresholdAmount()));
                    //优惠券类型，1满减券需要除以100、2折扣券需要除以10
                    if (couponQueryVo.getFcouponType().equals(CouponTypeEnum.FULL_REDUCTION.getCode())) {
                        receiveCenterCouponVo.setFdeductionValue(PriceUtil.toYuan(couponQueryVo.getFdeductionValue()));
                    } else {
                        receiveCenterCouponVo.setFdeductionValue(new BigDecimal(couponQueryVo.getFdeductionValue()).divide(new BigDecimal("10"), 1, BigDecimal.ROUND_HALF_UP));
                    }
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
        //校验入参
        if (null == fuid || null == fcouponId) {
            return Result.failure(MallExceptionCode.PARAM_ERROR);
        }
        //查询优惠券--状态（已发布）--类型（页面领取）--剩余数量--领取上限--有效期结束时间--发放结束时间
        Result<Coupon> couponResult = couponApi.queryOneByCriteria(Criteria.of(Coupon.class)
                .andEqualTo(Coupon::getFcouponId, fcouponId)
                .andEqualTo(Coupon::getFcouponStatus, CouponStatusEnum.PUSHED.getCode())
                .andEqualTo(Coupon::getFreleaseType, CouponReleaseTypeEnum.PAGE_RECEIVE.getCode())
                .fields(Coupon::getFperLimit, Coupon::getFsurplusReleaseQty, Coupon::getFvalidityType,
                        Coupon::getFvalidityEnd, Coupon::getFreleaseTimeEnd, Coupon::getFreleaseTimeStart,
                        Coupon::getFreleaseTimeType
                ));
        if (!couponResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        Coupon coupon = couponResult.getData();
        //校验该券是否存在
        if (null == coupon) {
            throw new BizException(MallExceptionCode.COUPON_IS_NOT_EXIST);
        }
        //校验该券库存
        if (coupon.getFsurplusReleaseQty() <= 0) {
            throw new BizException(MallExceptionCode.COUPON_IS_PAID_OUT);
        }
        Date now = new Date();
        //校验该券有效期时间
        if (coupon.getFvalidityType() == 1 && now.after(coupon.getFvalidityEnd())) {
            return Result.failure(MallExceptionCode.COUPON_IS_INVALID);
        }
        //校验该券领取时间
        if (coupon.getFreleaseTimeType() == 2 && (now.after(coupon.getFreleaseTimeEnd()) || now.before(coupon.getFreleaseTimeStart()))) {
            throw new BizException(MallExceptionCode.COUPON_IS_NOT_TIME);
        }
        //查询已经领到的券张数
        Result<Integer> countResult = couponReceiveApi.countByCriteria(Criteria.of(CouponReceive.class)
                .andEqualTo(CouponReceive::getFuid, fuid)
                .andEqualTo(CouponReceive::getFcouponId, fcouponId));
        if (!couponResult.isSuccess()) {
            logger.error("查询已经领到的券张数失败，userid{} couponId{}", fuid, fcouponId);
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        //校验该券领取上限
        if (null != countResult.getData()) {
            if (countResult.getData().equals(coupon.getFperLimit())) {
                throw new BizException(MallExceptionCode.COUPON_IS_MAX);
            }
        }
        ReceiveCouponDto receiveCouponDto = new ReceiveCouponDto();
        receiveCouponDto.setFuid(fuid);
        receiveCouponDto.setFcouponId(fcouponId);
        Result result = this.receiveCenterCoupon(receiveCouponDto);
        return result;
    }


    /**
     * @author lll
     * @version V1.0
     * @Description: 调用远程服务领取优惠券
     * @Param: receiveCouponDto
     * @return: Result                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    public Result receiveCenterCoupon(ReceiveCouponDto receiveCouponDto) {
        Long fcouponId = receiveCouponDto.getFcouponId();
        Long fuid = receiveCouponDto.getFuid();
        //加分布式锁
        String lockKey = StringUtils.join(Lists.newArrayList(MallConstants.MALL_RECEIVE_COUPON, fcouponId, fuid), ":");
        String lockValue = RandomUtils.getUUID();
        try {
            Ensure.that(xybbcLock.tryLockTimes(lockKey, lockValue, 3, 6)).isTrue(MallExceptionCode.SYSTEM_BUSY_ERROR);
            //更新优惠券发放数量
            CouponReleaseDto couponReleaseDto = new CouponReleaseDto();
            couponReleaseDto.setCouponScene(CouponScene.PAGE_RECEIVE);
            couponReleaseDto.setCouponId(fcouponId);
            couponReleaseDto.setUserId(fuid);
            couponReleaseDto.setAlreadyReceived(true);
            couponReleaseDto.setDeltaValue(-1);
            Result updateReleaseResult = couponProviderApi.updateReleaseQty(couponReleaseDto);
            Ensure.that(updateReleaseResult.isSuccess()).isTrue(new MallExceptionCode(updateReleaseResult.getCode(), updateReleaseResult.getMsg()));
            //调用领券服务
            Result receiveReceive = couponProviderApi.receive(couponReleaseDto);
            Ensure.that(receiveReceive.isSuccess()).isTrue(new MallExceptionCode(receiveReceive.getCode(), receiveReceive.getMsg()));
            return receiveReceive;
        } catch (Exception e) {
            e.printStackTrace();
            BizException be = (BizException) e;
            return Result.failure(be.getStatus());
        } finally {
            xybbcLock.releaseLock(lockKey, lockValue);
        }

    }


}
