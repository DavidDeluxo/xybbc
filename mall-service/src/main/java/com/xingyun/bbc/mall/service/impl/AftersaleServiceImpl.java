package com.xingyun.bbc.mall.service.impl;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.OrderConfigApi;
import com.xingyun.bbc.core.operate.api.ShippingCompanyApi;
import com.xingyun.bbc.core.operate.po.OrderConfig;
import com.xingyun.bbc.core.operate.po.ShippingCompany;
import com.xingyun.bbc.core.order.api.*;
import com.xingyun.bbc.core.order.enums.OrderAftersaleStatus;
import com.xingyun.bbc.core.order.enums.OrderAftersaleType;
import com.xingyun.bbc.core.order.po.OrderAftersale;
import com.xingyun.bbc.core.order.po.OrderAftersaleAdjust;
import com.xingyun.bbc.core.order.po.OrderAftersaleBack;
import com.xingyun.bbc.core.order.po.OrderAftersalePic;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.GoodsSkuApi;
import com.xingyun.bbc.core.sku.po.GoodsSku;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.PageUtils;
import com.xingyun.bbc.mall.common.constans.MallConstants;
import com.xingyun.bbc.mall.model.dto.AftersaleBackDto;
import com.xingyun.bbc.mall.model.dto.AftersaleLisDto;
import com.xingyun.bbc.mall.model.dto.ShippingCompanyDto;
import com.xingyun.bbc.mall.model.vo.AftersaleBackVo;
import com.xingyun.bbc.mall.model.vo.AftersaleDetailVo;
import com.xingyun.bbc.mall.model.vo.AftersaleListVo;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.service.AftersaleService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Service
public class AftersaleServiceImpl implements AftersaleService {

    public static final Logger logger = LoggerFactory.getLogger(AftersaleService.class);

    @Autowired
    private OrderAftersaleApi orderAftersaleApi;

    @Autowired
    private OrderAftersaleAdjustApi orderAftersaleAdjustApi;

    @Autowired
    private OrderAftersaleBackApi orderAftersaleBackApi;

    @Autowired
    private OrderAftersalePicApi orderAftersalePicApi;

    @Autowired
    private GoodsSkuApi goodsSkuApi;

    @Autowired
    private OrderConfigApi orderConfigApi;

    @Autowired
    private ShippingCompanyApi shippingCompanyApi;

    @Autowired
    private PageUtils pageUtils;

    @Autowired
    private Mapper mapper;

    @Autowired
    private DozerHolder dozerHolder;


    @Override
    public Result<PageVo<AftersaleListVo>> getAftersaleLis(AftersaleLisDto aftersaleLisDto) {
        //获取售后列表信息
        Criteria<OrderAftersale, Object> criteria = Criteria.of(OrderAftersale.class)
                .andEqualTo(OrderAftersale::getFuid, aftersaleLisDto.getFuserId())
                //售后状态1待客服审核 2待采购审核 3待仓库审核 4待财务审核 5已拒绝 6待退货 7待退款 8已成功 9已撤销  列表查询不限制状态
                .fields(OrderAftersale::getForderAftersaleId, OrderAftersale::getFskuCode, OrderAftersale::getFaftersaleNum,
                        OrderAftersale::getFaftersaleStatus, OrderAftersale::getFunitPrice, OrderAftersale::getFbatchPackageNum)
                .page(aftersaleLisDto.getCurrentPage(), aftersaleLisDto.getPageSize())
                .sortDesc(OrderAftersale::getFcreateTime);
        Result<List<OrderAftersale>> listResult = orderAftersaleApi.queryByCriteria(criteria);
        if (!listResult.isSuccess()) {
            logger.info("用户user_id {}获取售后订单信息失败", aftersaleLisDto.getFuserId());
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        Result<Integer> countResult = orderAftersaleApi.countByCriteria(criteria);
        if (!listResult.isSuccess()) {
            logger.info("用户user_id {}获取售后订单信息失败", aftersaleLisDto.getFuserId());
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        PageVo<AftersaleListVo> result = pageUtils.convert(countResult.getData(), listResult.getData(), AftersaleListVo.class, aftersaleLisDto);

        //获取skuName
        for (AftersaleListVo aftersaleListVo : result.getList()) {
            aftersaleListVo.setFskuName(this.getSkuInfor(aftersaleListVo.getFskuCode()).getFskuName());
            aftersaleListVo.setFskuPic(this.getSkuInfor(aftersaleListVo.getFskuCode()).getFskuThumbImage());
            aftersaleListVo.setFbatchPackageName(aftersaleListVo.getFbatchPackageNum() + "件装");
            aftersaleListVo.setFunitPrice(aftersaleListVo.getFunitPrice().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
        }
        return Result.success(result);
    }

    @Override
    public Result<AftersaleDetailVo> getAftersaleDetail(String faftersaleId) {
        //查询售后主表基本信息
        Result<OrderAftersale> aftersaleBasicResult = orderAftersaleApi.queryOneByCriteria(Criteria.of(OrderAftersale.class)
                .andEqualTo(OrderAftersale::getForderAftersaleId, faftersaleId)
                .fields(OrderAftersale::getForderAftersaleId, OrderAftersale::getFskuCode, OrderAftersale::getFaftersaleNum,
                        OrderAftersale::getFaftersaleStatus, OrderAftersale::getFbatchPackageNum, OrderAftersale::getFunitPrice,
                        OrderAftersale::getFaftersaleReason, OrderAftersale::getFaftersaleType, OrderAftersale::getFcreateTime,
                        OrderAftersale::getFmodifyTime));
        if (!aftersaleBasicResult.isSuccess()) {
            logger.info("单号faftersaleId {}获取售后主表信息失败", faftersaleId);
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }

        //获取skuName
        AftersaleDetailVo aftersaleDetailVo = mapper.map(aftersaleBasicResult.getData(), AftersaleDetailVo.class);
        aftersaleDetailVo.setFskuName(this.getSkuInfor(aftersaleDetailVo.getFskuCode()).getFskuName());
        aftersaleDetailVo.setFskuPic(this.getSkuInfor(aftersaleDetailVo.getFskuCode()).getFskuThumbImage());
        aftersaleDetailVo.setFbatchPackageName(aftersaleDetailVo.getFbatchPackageNum() + "件装");
        aftersaleDetailVo.setFunitPrice(aftersaleDetailVo.getFunitPrice().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));

        //获取售后总金额
        Result<OrderAftersaleAdjust> aftersaleAdjustResult = orderAftersaleAdjustApi.queryOneByCriteria(Criteria.of(OrderAftersaleAdjust.class)
                .andEqualTo(OrderAftersaleAdjust::getForderAftersaleId, faftersaleId)
                .fields(OrderAftersaleAdjust::getFaftersaleTotalAmount));
        if (!aftersaleAdjustResult.isSuccess()) {
            logger.info("单号faftersaleId {}获取售后总金额信息失败", faftersaleId);
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        Long faftersaleTotalAmount = aftersaleAdjustResult.getData().getFaftersaleTotalAmount();
        aftersaleDetailVo.setFaftersaleTotalAmount(new BigDecimal(faftersaleTotalAmount).divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));

        //售后状态1待客服审核 2待采购审核 3待仓库审核 4待财务审核 5已拒绝 6待退货 7待退款 8已成功 9已撤销
        //售后类型 1 退款 2 退款退货 退货类型获取回寄信息
        if (OrderAftersaleType.RETURN_MONEY_AND_GOODS.getCode().equals(aftersaleDetailVo.getFaftersaleType())) {

            //售后状态是 6 待退货展示回寄物流信息和回寄倒计时
            if (OrderAftersaleStatus.WAIT_RETURN_GOODS.getCode().equals(aftersaleDetailVo.getFaftersaleStatus())) {
                Result<OrderAftersaleBack> aftersaleBackResult = orderAftersaleBackApi.queryOneByCriteria(Criteria.of(OrderAftersaleBack.class)
                        .andEqualTo(OrderAftersaleBack::getForderAftersaleId, faftersaleId)
                        .fields(OrderAftersaleBack::getFdeliveryName, OrderAftersaleBack::getFdeliveryMobile, OrderAftersaleBack::getFdeliveryProvince,
                                OrderAftersaleBack::getFdeliveryCity, OrderAftersaleBack::getFdeliveryArea, OrderAftersaleBack::getFdeliveryAddr));
                if (!aftersaleBackResult.isSuccess()) {
                    logger.info("单号faftersaleId {}获取售后回寄信息失败", faftersaleId);
                    throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                }
                OrderAftersaleBack aftersaleBack = aftersaleBackResult.getData();
                aftersaleDetailVo.setFdeliveryName(aftersaleBack.getFdeliveryName());
                aftersaleDetailVo.setFdeliveryMobile(aftersaleBack.getFdeliveryMobile());
                aftersaleDetailVo.setFdeliveryProvince(aftersaleBack.getFdeliveryProvince());
                aftersaleDetailVo.setFdeliveryCity(aftersaleBack.getFdeliveryCity());
                aftersaleDetailVo.setFdeliveryArea(aftersaleBack.getFdeliveryArea());
                aftersaleDetailVo.setFdeliveryAddr(aftersaleBack.getFdeliveryAddr());

                //查询限时回寄分钟数
                Result<OrderConfig> orderConfigResult = orderConfigApi.queryOneByCriteria(Criteria.of(OrderConfig.class)
                        .andEqualTo(OrderConfig::getForderConfigType, 3)
                        .fields(OrderConfig::getFminute));
                if (!orderConfigResult.isSuccess()) {
                    logger.info("查询售后详情获取限时回寄分钟数失败");
                    throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                }
                Long fminute = orderConfigResult.getData().getFminute();
                Long time = aftersaleBasicResult.getData().getFmodifyTime().getTime();
                Date reGoodsTime = new Date(fminute * 60 * 1000 + time);
                aftersaleDetailVo.setFreGoodsTime(reGoodsTime);
            }
        }
        if (aftersaleDetailVo.getFaftersaleStatus() <= OrderAftersaleStatus.WAIT_FINANCE_VERIFY.getCode()) {
            aftersaleDetailVo.setFrefundTime(aftersaleBasicResult.getData().getFcreateTime());
        }
        if (OrderAftersaleStatus.REJECTED.getCode().equals(aftersaleDetailVo.getFaftersaleStatus()) || aftersaleDetailVo.getFaftersaleStatus() > OrderAftersaleStatus.WAIT_RETURN_GOODS.getCode()) {
            aftersaleDetailVo.setFrefundTime(aftersaleBasicResult.getData().getFmodifyTime());
        }
        return Result.success(aftersaleDetailVo);
    }

    @Override
    public Result<List<ShippingCompanyDto>> getShippingCompanyLis(ShippingCompanyDto shippingCompanyDto) {
        Criteria<ShippingCompany, Object> criteria = Criteria.of(ShippingCompany.class);
        if (!StringUtils.isEmpty(shippingCompanyDto.getFshippingName())) {
            criteria.andLike(ShippingCompany::getFshippingName, shippingCompanyDto.getFshippingName() + "%");
        }
        Result<List<ShippingCompany>> listResult = shippingCompanyApi.queryByCriteria(criteria);
        List<ShippingCompanyDto> result = dozerHolder.convert(listResult.getData(), ShippingCompanyDto.class);
        return Result.success(result);
    }

    @Override
    @GlobalTransactional
    public Result modifyAftersaleBack(AftersaleBackDto aftersaleBackDto) {
        //查询售后状态
        Result<OrderAftersale> statusResult = orderAftersaleApi.queryOneByCriteria(Criteria.of(OrderAftersale.class)
                .andEqualTo(OrderAftersale::getForderAftersaleId, aftersaleBackDto.getForderAftersaleId())
                .fields(OrderAftersale::getFaftersaleStatus));
        if (!statusResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if (!OrderAftersaleStatus.WAIT_RETURN_GOODS.getCode().equals(statusResult.getData().getFaftersaleStatus())) {
            return Result.success();
        }

        Result<Integer> insResult = orderAftersaleBackApi.updateNotNull(mapper.map(aftersaleBackDto, OrderAftersaleBack.class));
        if (!insResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if (null != aftersaleBackDto.getFpicStr()) {
            String[] picStr = aftersaleBackDto.getFpicStr().split(",");
            for (String pic : picStr) {
                OrderAftersalePic orderAftersalePic = new OrderAftersalePic();
                orderAftersalePic.setForderAftersaleId(aftersaleBackDto.getForderAftersaleId());
                orderAftersalePic.setFpicType(2);
                orderAftersalePic.setFaftersalePic(pic);
                Result<Integer> picInsResult = orderAftersalePicApi.updateNotNull(orderAftersalePic);
                if (!picInsResult.isSuccess()) {
                    throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return Result.success();
    }

    @Override
    public Result<AftersaleBackVo> getAftersaleBackShipping(String faftersaleId) {
        Result<OrderAftersaleBack> aftersaleBackResult = orderAftersaleBackApi.queryOneByCriteria(Criteria.of(OrderAftersaleBack.class)
                .andEqualTo(OrderAftersaleBack::getForderAftersaleId, faftersaleId)
                .fields(OrderAftersaleBack::getForderAftersaleId, OrderAftersaleBack::getFlogisticsCompanyId, OrderAftersaleBack::getFbackLogisticsOrder,
                        OrderAftersaleBack::getFbackRemark, OrderAftersaleBack::getFbackMobile));
        if (!aftersaleBackResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        AftersaleBackVo aftersaleBackVo = mapper.map(aftersaleBackResult.getData(), AftersaleBackVo.class);
        //获取物流公司名称
        Result<ShippingCompany> shippingCompanyResult = shippingCompanyApi.queryOneByCriteria(Criteria.of(ShippingCompany.class)
                .andEqualTo(ShippingCompany::getFshippingCompanyId, aftersaleBackVo.getFlogisticsCompanyId())
                .fields(ShippingCompany::getFshippingName));
        if (!shippingCompanyResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        aftersaleBackVo.setFlogisticsCompanyName(shippingCompanyResult.getData().getFshippingName());
        Result<List<OrderAftersalePic>> picResult = orderAftersalePicApi.queryByCriteria(Criteria.of(OrderAftersalePic.class)
                .andEqualTo(OrderAftersalePic::getFpicType, 2)
                .andEqualTo(OrderAftersalePic::getForderAftersaleId, faftersaleId)
                .fields(OrderAftersalePic::getFaftersalePic));
        if (!picResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        String picUrl = "";
        if (!CollectionUtils.isEmpty(picResult.getData())) {
            StringBuffer sf = new StringBuffer();
            for (OrderAftersalePic pic : picResult.getData()) {
                sf.append(pic).append(",");
            }
            picUrl = sf.toString();
        }
        aftersaleBackVo.setFpicStr(picUrl);
        return Result.success(aftersaleBackVo);
    }

    private GoodsSku getSkuInfor(String skuCode) {
        GoodsSku goodsSku = new GoodsSku();
        String skuName = "";
        String skuPic = "";
        Result<GoodsSku> goodsSkuResult = goodsSkuApi.queryOneByCriteria(Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFskuCode, skuCode)
                .fields(GoodsSku::getFskuName, GoodsSku::getFskuThumbImage));
        if (!goodsSkuResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if (null != goodsSkuResult.getData() && null != goodsSkuResult.getData().getFskuName()) {
            skuName = goodsSkuResult.getData().getFskuName();
        }
        if (null != goodsSkuResult.getData() && null != goodsSkuResult.getData().getFskuName()) {
            skuPic = goodsSkuResult.getData().getFskuThumbImage();
        }
        goodsSku.setFskuName(skuName);
        goodsSku.setFskuThumbImage(skuPic);
        return goodsSku;
    }
}
