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
import com.xingyun.bbc.core.supplier.api.SupplierTransportSkuApi;
import com.xingyun.bbc.core.supplier.po.SupplierTransportSku;
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
import java.util.ArrayList;
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
    private SupplierTransportSkuApi supplierTransportSkuApi;

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
                .andEqualTo(OrderAftersale::getFuid, aftersaleLisDto.getFuserId());
        Result<Integer> countResult = orderAftersaleApi.countByCriteria(criteria);
        if (!countResult.isSuccess()) {
            logger.info("用户user_id {}获取售后订单信息失败", aftersaleLisDto.getFuserId());
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }

        //售后状态1待客服审核 2待采购审核 3待仓库审核 4待财务审核 5已拒绝 6待退货 7待退款 8已成功 9已撤销  列表查询不限制状态
        criteria.fields(OrderAftersale::getForderAftersaleId, OrderAftersale::getFskuCode,
                        OrderAftersale::getFaftersaleNum, OrderAftersale::getFaftersaleStatus,
                        OrderAftersale::getFunitPrice, OrderAftersale::getFbatchPackageNum,
                        OrderAftersale::getFtransportOrderId)
                .page(aftersaleLisDto.getCurrentPage(), aftersaleLisDto.getPageSize())
                .sortDesc(OrderAftersale::getFcreateTime);

        Result<List<OrderAftersale>> listResult = orderAftersaleApi.queryByCriteria(criteria);
        if (!listResult.isSuccess()) {
            logger.info("用户user_id {}获取售后订单信息失败", aftersaleLisDto.getFuserId());
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }

        PageVo<AftersaleListVo> result = pageUtils.convert(countResult.getData(), listResult.getData(), AftersaleListVo.class, aftersaleLisDto);

        //获取skuName
        for (AftersaleListVo aftersaleListVo : result.getList()) {
            GoodsSku skuInfor = this.getSkuInfor(aftersaleListVo.getFskuCode());
            aftersaleListVo.setFskuName(skuInfor.getFskuName());
            aftersaleListVo.setFskuPic(skuInfor.getFskuThumbImage());
            aftersaleListVo.setFbatchPackageName(aftersaleListVo.getFbatchPackageNum() + "件装");
            aftersaleListVo.setFunitPrice(aftersaleListVo.getFunitPrice().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
            aftersaleListVo.setFaftersaleNumShow(this.getAftersaleNumShow(aftersaleListVo.getFaftersaleNum(), aftersaleListVo.getFtransportOrderId(), aftersaleListVo.getFskuCode()));
        }
        return Result.success(result);
    }

    private String getAftersaleNumShow (Integer faftersaleNum, String ftransportOrderId, String fskuCode) {
        //发货前直接展示 faftersaleNum 发货后(有发货单) 展示faftersaleNum/发货总数
        if (StringUtils.isEmpty(ftransportOrderId)) {
            return faftersaleNum.toString();
        } else {
            Result<SupplierTransportSku> supplierTransportSkuResult = supplierTransportSkuApi.queryOneByCriteria(Criteria.of(SupplierTransportSku.class)
                    .andEqualTo(SupplierTransportSku::getFskuCode, fskuCode)
                    .andEqualTo(SupplierTransportSku::getFtransportOrderId, ftransportOrderId)
                    .fields(SupplierTransportSku::getFbatchPackageNum, SupplierTransportSku::getFskuNum));
            if (!supplierTransportSkuResult.isSuccess()) {
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
            if (null != supplierTransportSkuResult.getData()) {
                SupplierTransportSku data = supplierTransportSkuResult.getData();
                Long fbatchPackageNum = data.getFbatchPackageNum();
                Integer fskuNum = data.getFskuNum();
                Long total = fbatchPackageNum * fskuNum;
                return new StringBuffer(faftersaleNum.toString()).append("/").append(total).toString();
            }
            return "";
        }
    }

    @Override
    public Result<AftersaleDetailVo> getAftersaleDetail(String faftersaleId) {
        //查询售后主表基本信息
        Result<OrderAftersale> aftersaleBasicResult = orderAftersaleApi.queryOneByCriteria(Criteria.of(OrderAftersale.class)
                .andEqualTo(OrderAftersale::getForderAftersaleId, faftersaleId)
                .fields(OrderAftersale::getForderAftersaleId, OrderAftersale::getFskuCode, OrderAftersale::getFaftersaleNum,
                        OrderAftersale::getFaftersaleStatus, OrderAftersale::getFbatchPackageNum, OrderAftersale::getFunitPrice,
                        OrderAftersale::getFaftersaleReason, OrderAftersale::getFaftersaleType, OrderAftersale::getFtransportOrderId,
                        OrderAftersale::getFcreateTime, OrderAftersale::getFmodifyTime));
        if (!aftersaleBasicResult.isSuccess()) {
            logger.info("单号faftersaleId {}获取售后主表信息失败", faftersaleId);
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }

        //获取skuName
        AftersaleDetailVo aftersaleDetailVo = mapper.map(aftersaleBasicResult.getData(), AftersaleDetailVo.class);
        aftersaleDetailVo.setFskuName(this.getSkuInfor(aftersaleDetailVo.getFskuCode()).getFskuName());
        aftersaleDetailVo.setFskuPic(this.getSkuInfor(aftersaleDetailVo.getFskuCode()).getFskuThumbImage());
        aftersaleDetailVo.setFbatchPackageName(aftersaleDetailVo.getFbatchPackageNum() + "件装");
        aftersaleDetailVo.setFunitPrice(aftersaleDetailVo.getFunitPrice().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
        aftersaleDetailVo.setFaftersaleNumShow(this.getAftersaleNumShow(aftersaleDetailVo.getFaftersaleNum(), aftersaleDetailVo.getFtransportOrderId(), aftersaleDetailVo.getFskuCode()));

        //获取售后总金额
        Result<OrderAftersaleAdjust> aftersaleAdjustResult = orderAftersaleAdjustApi.queryOneByCriteria(Criteria.of(OrderAftersaleAdjust.class)
                .andEqualTo(OrderAftersaleAdjust::getForderAftersaleId, faftersaleId)
                .fields(OrderAftersaleAdjust::getFaftersaleTotalAmount));
        if (!aftersaleAdjustResult.isSuccess()) {
            logger.info("单号faftersaleId {}获取售后总金额信息失败", faftersaleId);
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        Long faftersaleTotalAmount = aftersaleAdjustResult.getData().getFaftersaleTotalAmount();
        aftersaleDetailVo.setFaftersaleTotalAmount(new BigDecimal(faftersaleTotalAmount).divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));

        //售后状态faftersale_status 1待客审、2待采审、3待商审、4待财审、6待退货、7待退款、8已成功 9已撤销、10客服拒绝、11采购拒绝、12供应商拒绝、13财务拒绝、14采购拒绝收货、15逾期回寄
        //售后类型faftersale_type 1 退款 2 退款退货 退货类型获取回寄信息
        if (OrderAftersaleType.RETURN_MONEY_AND_GOODS.getCode().equals(aftersaleDetailVo.getFaftersaleType())) {

            //售后状态是 6 待退货展示回寄物流信息和回寄倒计时
            if (OrderAftersaleStatus.WAIT_RETURN_GOODS.getCode().equals(aftersaleDetailVo.getFaftersaleStatus())) {
                Result<OrderAftersaleBack> aftersaleBackResult = orderAftersaleBackApi.queryOneByCriteria(Criteria.of(OrderAftersaleBack.class)
                        .andEqualTo(OrderAftersaleBack::getForderAftersaleId, faftersaleId)
                        .fields(OrderAftersaleBack::getFdeliveryName, OrderAftersaleBack::getFdeliveryMobile, OrderAftersaleBack::getFdeliveryProvince,
                                OrderAftersaleBack::getFdeliveryCity, OrderAftersaleBack::getFdeliveryArea, OrderAftersaleBack::getFdeliveryAddr));
                if (!aftersaleBackResult.isSuccess()) {
                    logger.info("单号faftersaleId {}获取售后回寄信息失败", faftersaleId);
                    throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                }
                OrderAftersaleBack aftersaleBack = aftersaleBackResult.getData();
                if (null != aftersaleBack) {
                    aftersaleDetailVo.setFdeliveryName(aftersaleBack.getFdeliveryName());
                    aftersaleDetailVo.setFdeliveryMobile(aftersaleBack.getFdeliveryMobile());
                    aftersaleDetailVo.setFdeliveryProvince(aftersaleBack.getFdeliveryProvince());
                    aftersaleDetailVo.setFdeliveryCity(aftersaleBack.getFdeliveryCity());
                    aftersaleDetailVo.setFdeliveryArea(aftersaleBack.getFdeliveryArea());
                    aftersaleDetailVo.setFdeliveryAddr(aftersaleBack.getFdeliveryAddr());
                }

                //查询限时回寄分钟数
                Result<OrderConfig> orderConfigResult = orderConfigApi.queryOneByCriteria(Criteria.of(OrderConfig.class)
                        .andEqualTo(OrderConfig::getForderConfigType, 3)
                        .fields(OrderConfig::getFminute));
                if (!orderConfigResult.isSuccess()) {
                    logger.info("查询售后详情获取限时回寄分钟数失败");
                    throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                }
                Long fminute = orderConfigResult.getData().getFminute();
                Long time = aftersaleBasicResult.getData().getFmodifyTime().getTime();
                Date reGoodsTime = new Date(fminute * 60 * 1000 + time);
                aftersaleDetailVo.setFreGoodsTime(reGoodsTime);
            }
        }
        if (aftersaleDetailVo.getFaftersaleStatus() <= OrderAftersaleStatus.WAIT_FINANCE_VERIFY.getCode()) {
            aftersaleDetailVo.setFrefundTime(aftersaleBasicResult.getData().getFcreateTime());
        } else {
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
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (!OrderAftersaleStatus.WAIT_RETURN_GOODS.getCode().equals(statusResult.getData().getFaftersaleStatus())) {
            return Result.success();
        }
        //查询orderAftersaleBack id
        Result<OrderAftersaleBack> aftersaleBackResult = orderAftersaleBackApi.queryOneByCriteria(Criteria.of(OrderAftersaleBack.class)
                .andEqualTo(OrderAftersaleBack::getForderAftersaleId, aftersaleBackDto.getForderAftersaleId())
                .fields(OrderAftersaleBack::getFaftersaleBackId));
        if (!aftersaleBackResult.isSuccess() || null == aftersaleBackResult.getData()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        OrderAftersaleBack aftersaleBack = mapper.map(aftersaleBackDto, OrderAftersaleBack.class);
        aftersaleBack.setFaftersaleBackId(aftersaleBackResult.getData().getFaftersaleBackId());
        //更新售后回寄表
        Result<Integer> insResult = orderAftersaleBackApi.updateNotNull(aftersaleBack);
        if (!insResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (null != aftersaleBackDto.getFpicStr()) {
            String[] picStr = aftersaleBackDto.getFpicStr().split(",");
            List<OrderAftersalePic> aftersalePicLis = new ArrayList<>();
            Date nowDate = new Date();
            for (String pic : picStr) {
                OrderAftersalePic aftersalePic = new OrderAftersalePic();
                aftersalePic.setForderAftersaleId(aftersaleBackDto.getForderAftersaleId());
                aftersalePic.setFpicType(2);
                aftersalePic.setFaftersalePic(pic);
                aftersalePic.setFcreateTime(nowDate);
                aftersalePic.setFmodifyTime(nowDate);
                aftersalePicLis.add(aftersalePic);

            }
            Result<Integer> picInsResult = orderAftersalePicApi.createBatch(aftersalePicLis);
            if (!picInsResult.isSuccess()) {
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
        }
        //更新售后状态--修改时间加了乐观锁--先查询再保存
        Result<OrderAftersale> queryAfterSaleResult = orderAftersaleApi.queryOneByCriteria(Criteria.of(OrderAftersale.class)
                .andEqualTo(OrderAftersale::getForderAftersaleId, aftersaleBackDto.getForderAftersaleId())
                .fields(OrderAftersale::getFmodifyTime));
        if (!queryAfterSaleResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        OrderAftersale upAftersale = queryAfterSaleResult.getData();
        upAftersale.setForderAftersaleId(aftersaleBackDto.getForderAftersaleId());
        upAftersale.setFaftersaleStatus(OrderAftersaleStatus.WAIT_RETURN_MONEY.getCode());
        Result<Integer> aftersaleResult = orderAftersaleApi.updateNotNull(upAftersale);
        if (!aftersaleResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
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
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        AftersaleBackVo aftersaleBackVo = new AftersaleBackVo();
        if (null == aftersaleBackResult.getData()) {
            return Result.success(aftersaleBackVo);
        }
        aftersaleBackVo = mapper.map(aftersaleBackResult.getData(), AftersaleBackVo.class);
        //获取物流公司名称
        Result<ShippingCompany> shippingCompanyResult = shippingCompanyApi.queryOneByCriteria(Criteria.of(ShippingCompany.class)
                .andEqualTo(ShippingCompany::getFshippingCompanyId, aftersaleBackVo.getFlogisticsCompanyId())
                .fields(ShippingCompany::getFshippingName));
        if (!shippingCompanyResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        aftersaleBackVo.setFlogisticsCompanyName(null != shippingCompanyResult.getData() ? shippingCompanyResult.getData().getFshippingName() : "");
        Result<List<OrderAftersalePic>> picResult = orderAftersalePicApi.queryByCriteria(Criteria.of(OrderAftersalePic.class)
                .andEqualTo(OrderAftersalePic::getFpicType, 2)
                .andEqualTo(OrderAftersalePic::getForderAftersaleId, faftersaleId)
                .fields(OrderAftersalePic::getFaftersalePic));
        if (!picResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        String picUrl = "";
        if (!CollectionUtils.isEmpty(picResult.getData())) {
            StringBuffer sf = new StringBuffer();
            for (OrderAftersalePic pic : picResult.getData()) {
                sf.append(pic.getFaftersalePic()).append(",");
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
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
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
