package com.xingyun.bbc.mall.service.impl;

import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.CityRegionApi;
import com.xingyun.bbc.core.operate.api.CountryApi;
import com.xingyun.bbc.core.operate.po.CityRegion;
import com.xingyun.bbc.core.operate.po.Country;
import com.xingyun.bbc.core.order.api.RegularListApi;
import com.xingyun.bbc.core.order.po.RegularList;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.*;
import com.xingyun.bbc.core.sku.enums.GoodsSkuEnums;
import com.xingyun.bbc.core.sku.enums.SkuBatchEnums;
import com.xingyun.bbc.core.sku.po.*;
import com.xingyun.bbc.core.supplier.enums.TradeTypeEnums;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.api.UserDeliveryApi;
import com.xingyun.bbc.core.user.enums.UserVerifyStatusEnum;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.user.po.UserDelivery;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.PriceUtil;
import com.xingyun.bbc.mall.common.constans.MallConstants;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.GoodsDetailDto;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.GoodDetailService;
import com.xingyun.bbc.order.api.FreightApi;
import com.xingyun.bbc.order.model.dto.freight.FreightDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class GoodDetailServiceImpl implements GoodDetailService {

    public static final Logger logger = LoggerFactory.getLogger(GoodDetailService.class);

    @Autowired
    private UserApi userApi;

    @Autowired
    private CityRegionApi cityRegionApi;

    @Autowired
    private GoodsApi goodsApi;

    @Autowired
    private GoodsBrandApi goodsBrandApi;

    @Autowired
    private CountryApi countryApi;

    @Autowired
    private GoodsThumbImageApi goodsThumbImageApi;

    @Autowired
    private GoodsSkuApi goodsSkuApi;

    @Autowired
    private GoodsAttributeApi goodsAttributeApi;

    @Autowired
    private SkuBatchApi skuBatchApi;

    @Autowired
    private SkuBatchPackageApi skuBatchPackageApi;

    @Autowired
    private GoodsSkuBatchPriceApi goodsSkuBatchPriceApi;

    @Autowired
    private SkuBatchUserPriceApi skuBatchUserPriceApi;

    @Autowired
    private SkuUserDiscountConfigApi skuUserDiscountConfigApi;

    @Autowired
    private UserDeliveryApi userDeliveryApi;

    @Autowired
    private FreightApi freightApi;

    @Autowired
    private RegularListApi regularListApi;

    @Autowired
    private Mapper dozerMapper;

    @Autowired
    private DozerHolder dozerHolder;

    @Override
    public Result<List<String>> getGoodDetailPic(Long fgoodsId, Long fskuId) {
        List<String> result = new ArrayList<>();
        //查询sku图片
        if (null != fskuId) {
            Result<GoodsSku> skuPic = goodsSkuApi.queryOneByCriteria(Criteria.of(GoodsSku.class)
                    .andEqualTo(GoodsSku::getFskuId, fskuId)
                    .fields(GoodsSku::getFskuThumbImage));
            if (null != skuPic.getData()) {
                if (StringUtils.isNotBlank(skuPic.getData().getFskuThumbImage())) {
                    result.add(skuPic.getData().getFskuThumbImage());
                }
            }
        }
        //查询spu图片
        Result<List<GoodsThumbImage>> spuLis = goodsThumbImageApi.queryByCriteria(Criteria.of(GoodsThumbImage.class)
                .andEqualTo(GoodsThumbImage::getFgoodsId, fgoodsId)
                .fields(GoodsThumbImage::getFimgUrl));
        List<String> spuPic = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(spuLis.getData())) {
            spuPic = spuLis.getData().stream().filter(goodsThumbImage ->
                    StringUtils.isNotBlank(goodsThumbImage.getFimgUrl())).map(goodsThumbImage ->
                    goodsThumbImage.getFimgUrl()).collect(Collectors.toList());
        }
        result.addAll(spuPic);
        return Result.success(result);
    }

    @Override
    public Result<GoodsVo> getGoodDetailBasic(Long fgoodsId, Long fskuId) {
        //获取商品spu基本信息
        Result<Goods> goodsBasic = goodsApi.queryById(fgoodsId);
        if (!goodsBasic.isSuccess()) {
            logger.info("商品spu id {}获取商品基本信息调用远程服务失败", fgoodsId);
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (null == goodsBasic.getData()) {
            return Result.success(null);
        }
        GoodsVo goodsVo = dozerMapper.map(goodsBasic.getData(), GoodsVo.class);

        //获取贸易类型名称
        String tradeType = TradeTypeEnums.getTradeType(goodsVo.getFtradeId().toString());
        if (null == tradeType) {
            logger.info("商品spu id {}获取商品贸易类型枚举失败", fgoodsId);
            throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        }
        goodsVo.setFtradeType(tradeType);

        //获取sku商品描述和商品主图
        goodsVo.setFskuDesc("");
        if (null != fskuId) {
            GoodsSku goodSkuDesc = goodsSkuApi.queryOneByCriteria(Criteria.of(GoodsSku.class)
                    .andEqualTo(GoodsSku::getFskuId, fskuId)
                    .fields(GoodsSku::getFskuDesc, GoodsSku::getFskuThumbImage)).getData();
            if (null != goodSkuDesc && null != goodSkuDesc.getFskuDesc()) {
                goodsVo.setFskuDesc(goodSkuDesc.getFskuDesc());
            }
            //之前取spu表列表缩略图后改成sku表主图
            if (null != goodSkuDesc && null != goodSkuDesc.getFskuThumbImage()) {
                goodsVo.setFgoodsImgUrl(goodSkuDesc.getFskuThumbImage());
            }
        }

        //获取品牌名称和国旗icon
        goodsVo.setFbrandName("");
        goodsVo.setFcountryIcon("");
        if (null != goodsVo.getFbrandId()) {
            GoodsBrand goodsBrand = goodsBrandApi.queryOneByCriteria(Criteria.of(GoodsBrand.class)
                    .andEqualTo(GoodsBrand::getFbrandId, goodsVo.getFbrandId())
                    .fields(GoodsBrand::getFbrandName, GoodsBrand::getFbrandLogo, GoodsBrand::getFcountryName)).getData();
            if (null != goodsBrand) {
                goodsVo.setFbrandName(goodsBrand.getFbrandName());
                goodsVo.setFbrandLogo(goodsBrand.getFbrandLogo() == null ? "" : goodsBrand.getFbrandLogo());
                Country country = countryApi.queryOneByCriteria(Criteria.of(Country.class)
                        .andEqualTo(Country::getFcountryName, goodsBrand.getFcountryName())
                        .fields(Country::getFcountryIcon)).getData();
                if (null != country) {
                    goodsVo.setFcountryIcon(country.getFcountryIcon());
                }
            }
        }

        //获取商品原产地名称
        goodsVo.setFgoodsOrigin("");
        if (null != goodsVo.getForiginId()) {
            CityRegion cityRegion = cityRegionApi.queryOneByCriteria(Criteria.of(CityRegion.class)
                    .andEqualTo(CityRegion::getFregionId, goodsVo.getForiginId())
                    .fields(CityRegion::getFcrName)).getData();
            if (null != cityRegion && null != cityRegion.getFcrName()) {
                goodsVo.setFgoodsOrigin(cityRegion.getFcrName());
            }
        }

//        //商品规格
//        Result<List<GoodsSku>> goodsSkuResult = goodsSkuApi.queryByCriteria(Criteria.of(GoodsSku.class)
//                .andEqualTo(GoodsSku::getFgoodsId, fgoodsId)
//                .fields(GoodsSku::getFskuId, GoodsSku::getFgoodsId, GoodsSku::getFskuSpecValue));
//        List<GoodsSkuVo> convert = dozerHolder.convert(goodsSkuResult.getData(), GoodsSkuVo.class);
//        goodsVo.setFgoodsSkuVo(convert);
        return Result.success(goodsVo);
    }


    @Override
    public Result<GoodspecificationVo> getGoodsSpecifi(Long fgoodsId) {
        //商品各种规格
        GoodspecificationVo result = new GoodspecificationVo();

        //sku规格
        Result<List<GoodsSku>> goodsSkuResult = goodsSkuApi.queryByCriteria(Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFgoodsId, fgoodsId)
                .andEqualTo(GoodsSku::getFskuStatus, GoodsSkuEnums.Status.OnShelves.getValue())
                .andEqualTo(GoodsSku::getFisDelete, "0")
                .fields(GoodsSku::getFskuId, GoodsSku::getFskuCode, GoodsSku::getFskuSpecValue));
        List<GoodsSkuVo> skuRes = dozerHolder.convert(goodsSkuResult.getData(), GoodsSkuVo.class);
        //批次效期
        List<GoodsSkuBatchVo> batchRes = new ArrayList<>();
        //包装规格
        List<GoodsSkuBatchPackageVo> packageRes = new ArrayList<>();
        //商品各种规格详情
        List<GoodspecificationDetailVo> detailRes = new ArrayList<>();

        for (GoodsSkuVo skuVo : skuRes) {
            Result<List<SkuBatch>> skuBatchResult = skuBatchApi.queryByCriteria(Criteria.of(SkuBatch.class)
                    .andEqualTo(SkuBatch::getFskuId, skuVo.getFskuId())
                    .andEqualTo(SkuBatch::getFbatchStatus, SkuBatchEnums.Status.OnShelves.getValue())
                    .andEqualTo(SkuBatch::getFbatchPutwaySort, 1)//只用取上架排序为1的
                    .fields(SkuBatch::getFqualityEndDate, SkuBatch::getFsupplierSkuBatchId));
            List<GoodsSkuBatchVo> batchVert = dozerHolder.convert(skuBatchResult.getData(), GoodsSkuBatchVo.class);
            batchRes.addAll(batchVert);
            for (GoodsSkuBatchVo batchVo : batchVert) {
                Result<List<SkuBatchPackage>> skuBatchPackageResult = skuBatchPackageApi.queryByCriteria(Criteria.of(SkuBatchPackage.class)
                        .andEqualTo(SkuBatchPackage::getFsupplierSkuBatchId, batchVo.getFsupplierSkuBatchId())
                        .fields(SkuBatchPackage::getFbatchPackageId, SkuBatchPackage::getFbatchPackageNum, SkuBatchPackage::getFbatchStartNum));
                List<GoodsSkuBatchPackageVo> packageVert = dozerHolder.convert(skuBatchPackageResult.getData(), GoodsSkuBatchPackageVo.class);
                packageRes.addAll(packageVert);
                for (GoodsSkuBatchPackageVo packageVo : packageVert) {
                    GoodspecificationDetailVo detailVo = new GoodspecificationDetailVo();
                    detailVo.setFskuId(skuVo.getFskuId());
                    detailVo.setFskuCode(skuVo.getFskuCode());
                    detailVo.setFskuSpecValue(skuVo.getFskuSpecValue());
                    detailVo.setFskuBatchId(batchVo.getFsupplierSkuBatchId());
                    detailVo.setFqualityEndDate(batchVo.getFqualityEndDate());
                    detailVo.setFbatchPackageId(packageVo.getFbatchPackageId());
                    detailVo.setFbatchPackageNum(packageVo.getFbatchPackageNum());
                    detailVo.setFbatchStartNum(packageVo.getFbatchStartNum());
                    detailRes.add(detailVo);
                }
            }
        }

        skuRes.stream().filter(distinctByKey(b -> b.getFskuId()));
        batchRes.stream().filter(distinctByKey(b -> b.getFsupplierSkuBatchId()));
        packageRes.stream().filter(distinctByKey(b -> b.getFbatchPackageId()));

        //移动端老哥一定要拼成这样 一定要灵活避免经常发前端版本 ^_^！#
        List<MallTVo> skuMall = new ArrayList<>();
        List<MallTVo> batchMall = new ArrayList<>();
        List<MallTVo> packageMall = new ArrayList<>();
        for (GoodsSkuVo skuRe : skuRes) {
            MallTVo tVoSku = new MallTVo();
            tVoSku.setTId(skuRe.getFskuId());
            tVoSku.setTName(skuRe.getFskuSpecValue());
            skuMall.add(tVoSku);
        }
        GoodspecificationExVo skuEx = new GoodspecificationExVo();
        skuEx.setIdType(1);
        skuEx.setKeyType("goodsSkuVoLis");
        skuEx.setTitle("规格");
        skuEx.setItem(skuMall);

        for (GoodsSkuBatchVo batchRe : batchRes) {
            MallTVo tVoBatch = new MallTVo();
            tVoBatch.setTId(batchRe.getFsupplierSkuBatchId());
            tVoBatch.setTName(new SimpleDateFormat("yyyy-MM-dd").format(batchRe.getFqualityEndDate()));
            batchMall.add(tVoBatch);
        }
        GoodspecificationExVo batchEx = new GoodspecificationExVo();
        batchEx.setIdType(2);
        batchEx.setKeyType("goodsSkuBatchVoLis");
        batchEx.setTitle("效期");
        batchEx.setItem(batchMall);

        for (GoodsSkuBatchPackageVo packageRe : packageRes) {
            MallTVo tVoPackage = new MallTVo();
            tVoPackage.setTId(packageRe.getFbatchPackageId());
            tVoPackage.setTName(packageRe.getFbatchPackageNum().toString());
            packageMall.add(tVoPackage);
        }
        GoodspecificationExVo packageEx = new GoodspecificationExVo();
        packageEx.setIdType(1);
        packageEx.setKeyType("goodsSkuBatchPackageVoLis");
        packageEx.setTitle("件装");
        packageEx.setItem(packageMall);

        List<GoodspecificationExVo> items = new ArrayList<>();
        items.add(skuEx);
        items.add(batchEx);
        items.add(packageEx);

        result.setItems(items);
        result.setDetailLis(detailRes);
        return Result.success(result);
    }


    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    @Override
    public Result<Map<String, List<GoodsAttributeVo>>> getGoodsAttribute(Long fgoodsId) {
        //获取商品属性
        Result<List<GoodsAttribute>> goodsAttributeRes = goodsAttributeApi.queryByCriteria(Criteria.of(GoodsAttribute.class)
                .andEqualTo(GoodsAttribute::getFgoodsId, fgoodsId)
                .fields(GoodsAttribute::getFclassAttributeItemVal, GoodsAttribute::getFclassAttributeId, GoodsAttribute::getFclassAttributeName));
        List<GoodsAttributeVo> convert = dozerHolder.convert(goodsAttributeRes.getData(), GoodsAttributeVo.class);

        Map<String, List<GoodsAttributeVo>> collect = convert.stream().collect(Collectors.groupingBy(GoodsAttributeVo::getFclassAttributeName, Collectors.toList()));
        return Result.success(collect);
    }

    @Override
    public Result<GoodsPriceVo> getGoodPrice(GoodsDetailDto goodsDetailDto) {
        //获取价格地址
        GoodsPriceVo priceResult = new GoodsPriceVo();
        //到规格
        if (null != goodsDetailDto.getFbatchPackageId()) {
            Long packagePrice = this.getPackagePrice(goodsDetailDto);
            priceResult.setRealPrice(PriceUtil.toYuan(new BigDecimal(packagePrice)));
            priceResult.setPriceStart(PriceUtil.toYuan(new BigDecimal(packagePrice)));
        }
        //到批次
        if (null != goodsDetailDto.getFsupplierSkuBatchId() && null == goodsDetailDto.getFbatchPackageId()) {
            GoodsPriceVo batchPrice = this.getBatchPrice(goodsDetailDto);
            priceResult.setPriceStart(PriceUtil.toYuan(batchPrice.getPriceStart()));
            priceResult.setPriceEnd(PriceUtil.toYuan(batchPrice.getPriceEnd()));
        }
        //到sku
        if (null != goodsDetailDto.getFskuId() && null == goodsDetailDto.getFsupplierSkuBatchId() && null == goodsDetailDto.getFbatchPackageId()) {
            GoodsPriceVo skuPrice = this.getSkuPrice(goodsDetailDto);
            priceResult.setPriceStart(PriceUtil.toYuan(skuPrice.getPriceStart()));
            priceResult.setPriceEnd(PriceUtil.toYuan(skuPrice.getPriceEnd()));
        }
        //到spu
        if (null != goodsDetailDto.getFgoodsId() && null == goodsDetailDto.getFskuId() && null == goodsDetailDto.getFsupplierSkuBatchId() && null == goodsDetailDto.getFbatchPackageId()) {
            GoodsPriceVo skuPrice = this.getSpuPrice(goodsDetailDto);
            priceResult.setPriceStart(PriceUtil.toYuan(skuPrice.getPriceStart()));
            priceResult.setPriceEnd(PriceUtil.toYuan(skuPrice.getPriceEnd()));
        }
        //起始区间价 只有是单一价格PriceStart才计算运费、税费、折合单价
        if (null == priceResult.getPriceEnd()) {
            //查询批次价格类型 1.含邮含税 2.含邮不含税 3.不含邮含税 4.不含邮不含税
            SkuBatch fskuBatch = skuBatchApi.queryOneByCriteria(Criteria.of(SkuBatch.class)
                    .andEqualTo(SkuBatch::getFsupplierSkuBatchId, goodsDetailDto.getFsupplierSkuBatchId())
                    .fields(SkuBatch::getFbatchPriceType, SkuBatch::getFfreightId, SkuBatch::getFsupplierSkuBatchId)).getData();
            Integer fbatchPriceType = fskuBatch.getFbatchPriceType();
            //运费不管价格类型
            BigDecimal freightPrice = BigDecimal.ZERO;
            // 判断是默认地址还是前端选中的地址
            if (null == goodsDetailDto.getFdeliveryCityId()) {
                UserDelivery defautDelivery = userDeliveryApi.queryOneByCriteria(Criteria.of(UserDelivery.class)
                        .andEqualTo(UserDelivery::getFuid, goodsDetailDto.getFuid())
                        .andEqualTo(UserDelivery::getFisDefualt, 1)
                        .andEqualTo(UserDelivery::getFisDelete, 0)
                        .fields(UserDelivery::getFuid, UserDelivery::getFdeliveryAddr,
                                UserDelivery::getFdeliveryProvinceId, UserDelivery::getFdeliveryProvinceName,
                                UserDelivery::getFdeliveryCityId, UserDelivery::getFdeliveryCityName,
                                UserDelivery::getFdeliveryAreaId, UserDelivery::getFdeliveryAreaName)).getData();
                if (null != defautDelivery) {
                    //使用默认地址计算运费
                    if (null != goodsDetailDto.getFnum()) {
                        freightPrice = this.getFreight(goodsDetailDto.getFbatchPackageId(), fskuBatch.getFfreightId(),
                                defautDelivery.getFdeliveryCityId(), fskuBatch.getFsupplierSkuBatchId(), goodsDetailDto.getFnum());
                    }
                    priceResult.setFdeliveryAddr(defautDelivery.getFdeliveryAddr() == null ? "" : defautDelivery.getFdeliveryAddr());
                    priceResult.setFdeliveryProvinceName(defautDelivery.getFdeliveryProvinceName() == null ? "" : defautDelivery.getFdeliveryProvinceName());
                    priceResult.setFdeliveryCityName(defautDelivery.getFdeliveryCityName() == null ? "" : defautDelivery.getFdeliveryCityName());
                    priceResult.setFdeliveryAreaName(defautDelivery.getFdeliveryAreaName() == null ? "" : defautDelivery.getFdeliveryAreaName());
                }
            } else {
                //使用前端传参计算运费
                if (null != goodsDetailDto.getFdeliveryCityId() && null != goodsDetailDto.getFnum()) {
                    freightPrice = this.getFreight(goodsDetailDto.getFbatchPackageId(), fskuBatch.getFfreightId(),
                            goodsDetailDto.getFdeliveryCityId(), fskuBatch.getFsupplierSkuBatchId(), goodsDetailDto.getFnum());
                }
            }

            //查询税率
            Long fskuTaxRate = goodsSkuApi.queryOneByCriteria(Criteria.of(GoodsSku.class)
                    .andEqualTo(GoodsSku::getFskuId, goodsDetailDto.getFskuId())
                    .fields(GoodsSku::getFskuTaxRate)).getData().getFskuTaxRate();
            //(原始价*购买数量)
            BigDecimal orgPrice = priceResult.getPriceStart().multiply(new BigDecimal(goodsDetailDto.getFnum()));
            //税费 = (原始价*购买数量 + 运费) * 税率
            BigDecimal taxPrice = BigDecimal.ZERO;

            if (new Integer(2).equals(fbatchPriceType) || new Integer(4).equals(fbatchPriceType)) {
                taxPrice = orgPrice.add(freightPrice).multiply(new BigDecimal(fskuTaxRate))
                        .divide(MallConstants.TEN_THOUSAND, 2, BigDecimal.ROUND_HALF_UP);
            }
            //总价 = (原始价*购买数量) + 运费 + 税费
            BigDecimal priceTotal = orgPrice.add(freightPrice).add(taxPrice);
            //折合单价 = 总价 / 数量 /包装规格数量
            BigDecimal dealUnitPrice = BigDecimal.ZERO;
            if (null != goodsDetailDto.getFbatchPackageNum()) {
                dealUnitPrice = priceTotal.divide(new BigDecimal(goodsDetailDto.getFnum()).multiply(new BigDecimal(goodsDetailDto.getFbatchPackageNum())), 2, BigDecimal.ROUND_HALF_UP);
            }

            priceResult.setPriceStart(priceTotal);
            priceResult.setFreightPrice(freightPrice);
            priceResult.setTaxPrice(taxPrice);
            priceResult.setDealUnitPrice(dealUnitPrice);
        }
        return Result.success(priceResult);
    }

    private BigDecimal getFreight(Long fbatchPackageId, Long ffreightId, Long fdeliveryCityId, String fsupplierSkuBatchId, Long fnum) {
        BigDecimal freightPrice = BigDecimal.ZERO;
        //查询相应规格的件装数
        Result<SkuBatchPackage> skuBatchPackageResult = skuBatchPackageApi.queryOneByCriteria(Criteria.of(SkuBatchPackage.class)
                .andEqualTo(SkuBatchPackage::getFbatchPackageId, fbatchPackageId)
                .fields(SkuBatchPackage::getFbatchPackageNum));
        if (!skuBatchPackageResult.isSuccess()) {
            logger.info("批次包装规格fbatchPackageId {}获取包装规格值失败", fbatchPackageId);
            throw new BizException(MallExceptionCode.BATCH_PACKAGE_NUM_NOT_EXIST);
        }
        Long fbatchPackageNum = skuBatchPackageResult.getData().getFbatchPackageNum();
        FreightDto freightDto = new FreightDto();
        freightDto.setFfreightId(ffreightId);
        freightDto.setFregionId(fdeliveryCityId);
        freightDto.setFbatchId(fsupplierSkuBatchId);
        freightDto.setFbuyNum(fnum*fbatchPackageNum);
        logger.info("商品详情--查询运费入参{}", JSON.toJSONString(freightDto));
        Result<BigDecimal> bigDecimalResult = freightApi.queryFreight(freightDto);
        if (bigDecimalResult.isSuccess() && null != bigDecimalResult.getData()) {
            freightPrice = bigDecimalResult.getData().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP);
        }
        return freightPrice;
    }

    @Override
    public Result<GoodStockSellVo> getGoodStockSell(GoodsDetailDto goodsDetailDto) {
        //获取库存和销量
        GoodStockSellVo result = new GoodStockSellVo();
        //到批次
        if (null != goodsDetailDto.getFsupplierSkuBatchId()) {
            result = this.getBatchStockSell(goodsDetailDto);
        }
        //到sku
        if (null != goodsDetailDto.getFskuId() && null == goodsDetailDto.getFsupplierSkuBatchId()) {
            result = this.getSkuStockSell(goodsDetailDto);
        }
        //到spu
        if (null != goodsDetailDto.getFgoodsId() && null == goodsDetailDto.getFskuId() && null == goodsDetailDto.getFsupplierSkuBatchId()) {
            result = this.getSpuStockSell(goodsDetailDto);
        }
        return Result.success(result);
    }

    //获取批次的库存和销量
    private GoodStockSellVo getBatchStockSell(GoodsDetailDto goodsDetailDto) {
        GoodStockSellVo result = new GoodStockSellVo();
        result.setFsellNum(0l);
        result.setFstockRemianNum(0l);
        Result<SkuBatch> stockSell = skuBatchApi.queryOneByCriteria(Criteria.of(SkuBatch.class)
                .andEqualTo(SkuBatch::getFsupplierSkuBatchId, goodsDetailDto.getFsupplierSkuBatchId())
                .fields(SkuBatch::getFsellNum, SkuBatch::getFstockRemianNum));
        if (!stockSell.isSuccess()) {
            logger.info("商品fsupplierSkuBatchId {}获取该批次库存和销量失败", goodsDetailDto.getFsupplierSkuBatchId());
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        SkuBatch stockSellResult = stockSell.getData();
        if (null != stockSellResult) {
            if (null != stockSellResult.getFsellNum()) {
                result.setFsellNum(stockSellResult.getFsellNum());
            }
            if (null != stockSellResult.getFstockRemianNum()) {
                result.setFstockRemianNum(stockSellResult.getFstockRemianNum());
            }
        }
        return result;
    }

    //获取sku的库存和销量
    private GoodStockSellVo getSkuStockSell(GoodsDetailDto goodsDetailDto) {
        GoodStockSellVo result = new GoodStockSellVo();
        result.setFsellNum(0l);
        result.setFstockRemianNum(0l);
        Result<List<SkuBatch>> skuStockSell = skuBatchApi.queryByCriteria(Criteria.of(SkuBatch.class)
                .andEqualTo(SkuBatch::getFskuId, goodsDetailDto.getFskuId())
                .andEqualTo(SkuBatch::getFbatchStatus, SkuBatchEnums.Status.OnShelves.getValue())
                .fields(SkuBatch::getFsupplierSkuBatchId));
        if (!skuStockSell.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        Result<GoodsSku> goodsSkuResult = goodsSkuApi.queryOneByCriteria(Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFskuId, goodsDetailDto.getFskuId()).fields(GoodsSku::getFsellNum));
        if (!goodsSkuResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (null != goodsSkuResult.getData()) {
            result.setFsellNum(goodsSkuResult.getData().getFsellNum());
        }
        List<SkuBatch> skuStockSellResult = skuStockSell.getData();
        if (!CollectionUtils.isEmpty(skuStockSellResult)) {
            for (SkuBatch skuBatch : skuStockSellResult) {
                GoodsDetailDto param = new GoodsDetailDto();
                param.setFsupplierSkuBatchId(skuBatch.getFsupplierSkuBatchId());
                GoodStockSellVo batchStockSell = this.getBatchStockSell(param);
                if (null != batchStockSell.getFstockRemianNum()) {
                    result.setFstockRemianNum(result.getFstockRemianNum() + batchStockSell.getFstockRemianNum());
                }
            }
        }
        return result;
    }

    //获取spu的库存和销量
    private GoodStockSellVo getSpuStockSell(GoodsDetailDto goodsDetailDto) {
        GoodStockSellVo result = new GoodStockSellVo();
        result.setFsellNum(0l);
        result.setFstockRemianNum(0l);
        Result<List<GoodsSku>> spuStockSell = goodsSkuApi.queryByCriteria(Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFgoodsId, goodsDetailDto.getFgoodsId())
                .andEqualTo(GoodsSku::getFskuStatus, GoodsSkuEnums.Status.OnShelves.getValue())
                .andEqualTo(GoodsSku::getFisDelete, "0")
                .fields(GoodsSku::getFskuId));
        if (!spuStockSell.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        List<GoodsSku> spuStockSellResult = spuStockSell.getData();
        if (!CollectionUtils.isEmpty(spuStockSellResult)) {
            for (GoodsSku goodsSku : spuStockSellResult) {
                GoodsDetailDto param = new GoodsDetailDto();
                param.setFskuId(goodsSku.getFskuId());
                GoodStockSellVo batchStockSell = this.getSkuStockSell(param);
                if (null != batchStockSell.getFsellNum()) {
                    result.setFsellNum(result.getFsellNum() + batchStockSell.getFsellNum());
                }
                if (null != batchStockSell.getFstockRemianNum()) {
                    result.setFstockRemianNum(result.getFstockRemianNum() + batchStockSell.getFstockRemianNum());
                }
            }
        }
        return result;
    }

    //获取到规格的价格
    private Long getPackagePrice(GoodsDetailDto goodsDetailDto) {
        Long price = 0l;
        //是否支持平台会员折扣
        if (new Integer(1).equals(this.getIsUserDiscount(goodsDetailDto.getFskuId()))) {
            Result<User> userResult = userApi.queryOneByCriteria(Criteria.of(User.class)
                    .andEqualTo(User::getFuid, goodsDetailDto.getFuid())
                    .fields(User::getFoperateType, User::getFverifyStatus));
            if (!userResult.isSuccess()) {
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
            //如果用户未认证直接查基础价格表
            if (null == userResult.getData() || !UserVerifyStatusEnum.AUTHENTICATED.getCode().equals(userResult.getData().getFverifyStatus())) {
                return this.getGeneralPrice(goodsDetailDto.getFbatchPackageId());
            }

            Integer foperateType = userResult.getData().getFoperateType();
            Result<List<SkuUserDiscountConfig>> skuUserDiscountResult = skuUserDiscountConfigApi.queryByCriteria(Criteria.of(SkuUserDiscountConfig.class)
                    .andEqualTo(SkuUserDiscountConfig::getFskuId, goodsDetailDto.getFskuId())
                    .andEqualTo(SkuUserDiscountConfig::getFuserTypeId, foperateType.longValue())
                    .andEqualTo(SkuUserDiscountConfig::getFisDelete, 0)
                    .fields(SkuUserDiscountConfig::getFdiscountId));
            if (!skuUserDiscountResult.isSuccess()) {
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
            if (CollectionUtils.isEmpty(skuUserDiscountResult.getData())) {
                return this.getGeneralPrice(goodsDetailDto.getFbatchPackageId());
            }

            Result<SkuBatchUserPrice> skuBatchUserPriceResult = skuBatchUserPriceApi.queryOneByCriteria(Criteria.of(SkuBatchUserPrice.class)
                    .andEqualTo(SkuBatchUserPrice::getFbatchPackageId, goodsDetailDto.getFbatchPackageId())
                    .andEqualTo(SkuBatchUserPrice::getFuserTypeId, foperateType)
                    .fields(SkuBatchUserPrice::getFbatchSellPrice));
            if (!skuBatchUserPriceResult.isSuccess()) {
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
            if (null != skuBatchUserPriceResult.getData()) {
                if (null != skuBatchUserPriceResult.getData().getFbatchSellPrice()) {
                    price = skuBatchUserPriceResult.getData().getFbatchSellPrice();
                }
            } else {
                price = this.getGeneralPrice(goodsDetailDto.getFbatchPackageId());
            }
        } else {
            price = this.getGeneralPrice(goodsDetailDto.getFbatchPackageId());
        }
        return price;
    }

    //获取非折扣价格
    private Long getGeneralPrice(Long fbatchPackageId) {
        Long price = 0L;
        Result<GoodsSkuBatchPrice> goodsSkuBatchPriceResult = goodsSkuBatchPriceApi.queryOneByCriteria(Criteria.of(GoodsSkuBatchPrice.class)
                .andEqualTo(GoodsSkuBatchPrice::getFbatchPackageId, fbatchPackageId)
                .fields(GoodsSkuBatchPrice::getFbatchSellPrice));
        if (goodsSkuBatchPriceResult.isSuccess() && null != goodsSkuBatchPriceResult.getData()) {
            if (null != goodsSkuBatchPriceResult.getData().getFbatchSellPrice()) {
                price = goodsSkuBatchPriceResult.getData().getFbatchSellPrice();
            }
        }
        return price;
    }

    //获取到批次的价格
    private GoodsPriceVo getBatchPrice(GoodsDetailDto goodsDetailDto) {
        //到批次
        GoodsPriceVo priceVo = new GoodsPriceVo();
        priceVo.setPriceStart(BigDecimal.ZERO);
        priceVo.setPriceEnd(BigDecimal.ZERO);
        Result<List<SkuBatchPackage>> batch = skuBatchPackageApi.queryByCriteria(Criteria.of(SkuBatchPackage.class)
                .andEqualTo(SkuBatchPackage::getFsupplierSkuBatchId, goodsDetailDto.getFsupplierSkuBatchId())
                .fields(SkuBatchPackage::getFbatchPackageId));
        if (!batch.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        List<SkuBatchPackage> batchResult = batch.getData();
        if (!CollectionUtils.isEmpty(batchResult)) {
            for (int i = 0; i < batchResult.size(); i++) {
                GoodsDetailDto param = new GoodsDetailDto();
                param.setFuid(goodsDetailDto.getFuid());
                param.setFskuId(goodsDetailDto.getFskuId());
                param.setFbatchPackageId(batchResult.get(i).getFbatchPackageId());
                Long packagePrice = this.getPackagePrice(param);
                if (i == 0) {
                    if (null != packagePrice) {
                        priceVo.setPriceStart(new BigDecimal(packagePrice));
                        priceVo.setPriceEnd(new BigDecimal(packagePrice));
                    }
                } else {
                    if (null != packagePrice) {
                        if (packagePrice < priceVo.getPriceStart().longValue()) {
                            priceVo.setPriceStart(new BigDecimal(packagePrice));
                        }
                    }
                    if (null != packagePrice) {
                        if (packagePrice > priceVo.getPriceEnd().longValue()) {
                            priceVo.setPriceEnd(new BigDecimal(packagePrice));
                        }
                    }
                }
            }
        }
        return priceVo;
    }

    //获取到sku的价格
    private GoodsPriceVo getSkuPrice(GoodsDetailDto goodsDetailDto) {
        GoodsPriceVo priceVo = new GoodsPriceVo();
        priceVo.setPriceStart(BigDecimal.ZERO);
        priceVo.setPriceEnd(BigDecimal.ZERO);
        Result<List<SkuBatch>> skuBatche = skuBatchApi.queryByCriteria(Criteria.of(SkuBatch.class)
                .andEqualTo(SkuBatch::getFskuId, goodsDetailDto.getFskuId())
                .andEqualTo(SkuBatch::getFbatchStatus, SkuBatchEnums.Status.OnShelves.getValue())
                .fields(SkuBatch::getFsupplierSkuBatchId));
        if (!skuBatche.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        List<SkuBatch> skuBatcheResult = skuBatche.getData();
        if (!CollectionUtils.isEmpty(skuBatcheResult)) {
            for (int i = 0; i < skuBatcheResult.size(); i++) {
                GoodsDetailDto param = new GoodsDetailDto();
                param.setFuid(goodsDetailDto.getFuid());
                param.setFskuId(goodsDetailDto.getFskuId());
                param.setFsupplierSkuBatchId(skuBatcheResult.get(i).getFsupplierSkuBatchId());
                GoodsPriceVo batchPrice = this.getBatchPrice(param);
                if (i == 0) {
                    if (null != batchPrice.getPriceStart()) {
                        priceVo.setPriceStart(batchPrice.getPriceStart());
                    }
                    if (null != batchPrice.getPriceEnd()) {
                        priceVo.setPriceEnd(batchPrice.getPriceEnd());
                    }
                } else {
                    if (null != batchPrice.getPriceStart()) {
                        if (batchPrice.getPriceStart().longValue() < priceVo.getPriceStart().longValue()) {
                            priceVo.setPriceStart(batchPrice.getPriceStart());
                        }
                    }
                    if (null != batchPrice.getPriceEnd()) {
                        if (batchPrice.getPriceEnd().longValue() > priceVo.getPriceEnd().longValue()) {
                            priceVo.setPriceEnd(batchPrice.getPriceEnd());
                        }
                    }
                }
            }
        }
        return priceVo;
    }

    //获取到spu的价格
    private GoodsPriceVo getSpuPrice(GoodsDetailDto goodsDetailDto) {
        GoodsPriceVo priceVo = new GoodsPriceVo();
        priceVo.setPriceStart(BigDecimal.ZERO);
        priceVo.setPriceEnd(BigDecimal.ZERO);
        Result<List<GoodsSku>> listResult = goodsSkuApi.queryByCriteria(Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFgoodsId, goodsDetailDto.getFgoodsId())
                .andEqualTo(GoodsSku::getFskuStatus, GoodsSkuEnums.Status.OnShelves.getValue())
                .andEqualTo(GoodsSku::getFisDelete, "0")
                .fields(GoodsSku::getFskuId));
        if (!listResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        List<GoodsSku> skuResult = listResult.getData();
        if (!CollectionUtils.isEmpty(skuResult)) {
            for (int i = 0; i < skuResult.size(); i++) {
                GoodsDetailDto param = new GoodsDetailDto();
                param.setFuid(goodsDetailDto.getFuid());
                param.setFskuId(skuResult.get(i).getFskuId());
                GoodsPriceVo skuPrice = this.getSkuPrice(param);
                if (i == 0) {
                    if (null != skuPrice.getPriceStart()) {
                        priceVo.setPriceStart(skuPrice.getPriceStart());
                    }
                    if (null != skuPrice.getPriceEnd()) {
                        priceVo.setPriceEnd(skuPrice.getPriceEnd());
                    }
                } else {
                    if (null != skuPrice.getPriceStart()) {
                        if (skuPrice.getPriceStart().longValue() < priceVo.getPriceStart().longValue()) {
                            priceVo.setPriceStart(skuPrice.getPriceStart());
                        }
                    }
                    if (null != skuPrice.getPriceEnd()) {
                        if (skuPrice.getPriceEnd().longValue() > priceVo.getPriceEnd().longValue()) {
                            priceVo.setPriceEnd(skuPrice.getPriceEnd());
                        }
                    }
                }
            }
        }
        return priceVo;
    }

    //获取是否支持平台会员折扣 0 取 GoodsSkuBatchPrice 1 取 SkuBatchUserPrice
    private Integer getIsUserDiscount(Long skuId) {
        Result<GoodsSku> goodsSkuResult = goodsSkuApi.queryOneByCriteria(Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFskuId, skuId)
                .fields(GoodsSku::getFisUserTypeDiscount));
        return goodsSkuResult.getData().getFisUserTypeDiscount();
    }

    @Override
    public Result<Integer> getIsRegular(Long fgoodsId, Long fuid) {
        //是否已经加入常购清单 1是 0否
        Integer fisRegular = 0;
        //查询是否已经加入常购清单
        Result<Integer> isRegularResult = regularListApi.countByCriteria(Criteria.of(RegularList.class)
                .andEqualTo(RegularList::getFgoodsId, fgoodsId)
                .andEqualTo(RegularList::getFuid, fuid));
        if (isRegularResult.isSuccess() && isRegularResult.getData() > 0) {
            fisRegular = 1;
        }
        return Result.success(fisRegular);
    }

    //    @Override
//    public Result<List<GoodsSkuBatchVo>> getSkuBatchSpecifi(Long fskuId) {
//        Result<List<SkuBatch>> skuBatchResult = skuBatchApi.queryByCriteria(Criteria.of(SkuBatch.class)
//                .andEqualTo(SkuBatch::getFskuId, fskuId)
//                .fields(SkuBatch::getFvalidityStartDate, SkuBatch::getFvalidityEndDate, SkuBatch::getFsupplierSkuBatchId, SkuBatch::getFskuId));
//        List<GoodsSkuBatchVo> convert = dozerHolder.convert(skuBatchResult.getData(), GoodsSkuBatchVo.class);
//        return Result.success(convert);
//    }
//
//    @Override
//    public Result<List<GoodsSkuBatchPackageVo>> getSkuBatchPackageSpecifi(Long fskuBatchId) {
//        Result<List<SkuBatchPackage>> skuBatchPackageResult = skuBatchPackageApi.queryByCriteria(Criteria.of(SkuBatchPackage.class)
//                .andEqualTo(SkuBatchPackage::getFsupplierSkuBatchId, fskuBatchId)
//                .fields(SkuBatchPackage::getFbatchPackageId, SkuBatchPackage::getFsupplierSkuBatchId, SkuBatchPackage::getFbatchPackageNum, SkuBatchPackage::getFbatchStartNum));
//        List<GoodsSkuBatchPackageVo> convert = dozerHolder.convert(skuBatchPackageResult.getData(), GoodsSkuBatchPackageVo.class);
//        return Result.success(convert);
//    }

}
