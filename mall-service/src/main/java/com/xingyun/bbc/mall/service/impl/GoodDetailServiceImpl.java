package com.xingyun.bbc.mall.service.impl;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.CityRegionApi;
import com.xingyun.bbc.core.operate.po.CityRegion;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.*;
import com.xingyun.bbc.core.sku.po.*;
import com.xingyun.bbc.core.sku.po.Goods;
import com.xingyun.bbc.core.sku.po.GoodsSku;
import com.xingyun.bbc.core.sku.po.GoodsSkuBatchPrice;
import com.xingyun.bbc.core.sku.po.SkuBatch;
import com.xingyun.bbc.core.sku.po.SkuBatchPackage;
import com.xingyun.bbc.core.sku.po.SkuBatchUserPrice;
import com.xingyun.bbc.core.supplier.enums.TradeTypeEnums;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.api.UserDeliveryApi;
import com.xingyun.bbc.core.user.po.UserDelivery;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.common.constans.MallConstants;
import com.xingyun.bbc.mall.common.enums.GoodsEnums;
import com.xingyun.bbc.mall.model.dto.GoodsDetailDto;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.GoodDetailService;
import com.xingyun.bbc.order.api.FavoritesApi;
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
    private UserDeliveryApi userDeliveryApi;

    @Autowired
    private FreightApi freightApi;

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
            if (StringUtils.isNotBlank(skuPic.getData().getFskuThumbImage())) {
                result.add(skuPic.getData().getFskuThumbImage());
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
            logger.info("商品spu id {}获取商品基本信息失败", fgoodsId);
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        GoodsVo goodsVo = dozerMapper.map(goodsBasic.getData(), GoodsVo.class);

        //获取贸易类型名称
        String tradeType = TradeTypeEnums.getTradeType(goodsVo.getFtradeId().toString());
        if (null == tradeType) {
            logger.info("商品spu id {}获取商品贸易类型枚举失败", fgoodsId);
            throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        }
        goodsVo.setFtradeType(tradeType);

        //获取sku商品描述
        goodsVo.setFskuDesc("");
        if (null != fskuId) {
            GoodsSku goodSkuDesc = goodsSkuApi.queryOneByCriteria(Criteria.of(GoodsSku.class)
                    .andEqualTo(GoodsSku::getFskuId, fskuId)
                    .fields(GoodsSku::getFskuDesc)).getData();
            if (null != goodSkuDesc && null != goodSkuDesc.getFskuDesc()) {
                goodsVo.setFskuDesc(goodSkuDesc.getFskuDesc());
            }
        }

        //获取品牌名称
        goodsVo.setFbrandName("");
        if (null != goodsVo.getFbrandId()) {
            GoodsBrand goodsBrand = goodsBrandApi.queryOneByCriteria(Criteria.of(GoodsBrand.class)
                    .andEqualTo(GoodsBrand::getFbrandId, goodsVo.getFbrandId())
                    .fields(GoodsBrand::getFbrandName, GoodsBrand::getFbrandLogo)).getData();
            if (null != goodsBrand && null != goodsBrand.getFbrandName()) {
                goodsVo.setFbrandName(goodsBrand.getFbrandName());
                goodsVo.setFbrandLogo(goodsBrand.getFbrandLogo() == null ? "" : goodsBrand.getFbrandLogo());
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
                    .fields(SkuBatch::getFvalidityEndDate, SkuBatch::getFsupplierSkuBatchId));
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
                    detailVo.setFvalidityEndDate(batchVo.getFvalidityEndDate());
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
            tVoBatch.setTName(new SimpleDateFormat("yyyy-MM-dd").format(batchRe.getFvalidityEndDate()));
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
            priceResult.setPriceStart(new BigDecimal(this.getPackagePrice(goodsDetailDto)).divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
        }
        //到批次
        if (null != goodsDetailDto.getFsupplierSkuBatchId() && null == goodsDetailDto.getFbatchPackageId()) {
            GoodsPriceVo batchPrice = this.getBatchPrice(goodsDetailDto);
            priceResult.setPriceStart(batchPrice.getPriceStart().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
            priceResult.setPriceEnd(batchPrice.getPriceEnd().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
        }
        //到sku
        if (null != goodsDetailDto.getFskuId() && null == goodsDetailDto.getFsupplierSkuBatchId() && null == goodsDetailDto.getFbatchPackageId()) {
            GoodsPriceVo skuPrice = this.getSkuPrice(goodsDetailDto);
            priceResult.setPriceStart(skuPrice.getPriceStart().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
            priceResult.setPriceEnd(skuPrice.getPriceEnd().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
        }
        //到spu
        if (null != goodsDetailDto.getFgoodsId() && null == goodsDetailDto.getFskuId() && null == goodsDetailDto.getFsupplierSkuBatchId() && null == goodsDetailDto.getFbatchPackageId()) {
            GoodsPriceVo skuPrice = this.getSpuPrice(goodsDetailDto);
            priceResult.setPriceStart(skuPrice.getPriceStart().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
            priceResult.setPriceEnd(skuPrice.getPriceEnd().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
        }
        //起始区间价 只有是单一价格才计算运费、税费、折合单价
        if (null == priceResult.getPriceEnd()) {
            //查询批次价格类型 1.含邮含税 2.含邮不含税 3.不含邮含税 4.不含邮不含税
            SkuBatch fskuBatch = skuBatchApi.queryOneByCriteria(Criteria.of(SkuBatch.class)
                    .andEqualTo(SkuBatch::getFsupplierSkuBatchId, goodsDetailDto.getFsupplierSkuBatchId())
                    .fields(SkuBatch::getFbatchPriceType, SkuBatch::getFfreightId, SkuBatch::getFsupplierSkuBatchId)).getData();
            Integer fbatchPriceType = fskuBatch.getFbatchPriceType();
            //运费
            BigDecimal freightPrice = BigDecimal.ZERO;
            if (new Integer(3).equals(fbatchPriceType) || new Integer(4).equals(fbatchPriceType)) {

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
                            FreightDto freightDto = new FreightDto();
                            freightDto.setFfreightId(fskuBatch.getFfreightId());
                            freightDto.setFregionId(defautDelivery.getFdeliveryCityId());
                            freightDto.setFbatchId(fskuBatch.getFsupplierSkuBatchId());
                            freightDto.setFbuyNum(goodsDetailDto.getFnum());
                            Result<BigDecimal> bigDecimalResult = freightApi.queryFreight(freightDto);
                            if (!bigDecimalResult.isSuccess()) {
                                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                            }
                            freightPrice = bigDecimalResult.getData().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP);
                        }
                        priceResult.setFdeliveryAddr(defautDelivery.getFdeliveryAddr() == null ? "" : defautDelivery.getFdeliveryAddr());
                        priceResult.setFdeliveryProvinceName(defautDelivery.getFdeliveryProvinceName() == null ? "" : defautDelivery.getFdeliveryProvinceName());
                        priceResult.setFdeliveryCityName(defautDelivery.getFdeliveryCityName() == null ? "" : defautDelivery.getFdeliveryCityName());
                        priceResult.setFdeliveryAreaName(defautDelivery.getFdeliveryAreaName() == null ? "" : defautDelivery.getFdeliveryAreaName());
                    }
                } else {
                    //使用前端传参计算运费
                    if (null != goodsDetailDto.getFdeliveryCityId() && null != goodsDetailDto.getFnum()) {
                        FreightDto freightDto = new FreightDto();
                        freightDto.setFfreightId(fskuBatch.getFfreightId());
                        freightDto.setFregionId(goodsDetailDto.getFdeliveryCityId());
                        freightDto.setFbatchId(fskuBatch.getFsupplierSkuBatchId());
                        freightDto.setFbuyNum(goodsDetailDto.getFnum());
                        Result<BigDecimal> bigDecimalResult = freightApi.queryFreight(freightDto);
                        if (null != bigDecimalResult.getData()) {
                            freightPrice = bigDecimalResult.getData().divide(MallConstants.ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP);
                        }
                    }
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
        SkuBatch stockSellResult = skuBatchApi.queryOneByCriteria(Criteria.of(SkuBatch.class)
                .andEqualTo(SkuBatch::getFsupplierSkuBatchId, goodsDetailDto.getFsupplierSkuBatchId())
                .fields(SkuBatch::getFsellNum, SkuBatch::getFstockRemianNum)).getData();
        GoodStockSellVo goodStockSellVo = new GoodStockSellVo();
        if (null == stockSellResult) {
            logger.info("商品fsupplierSkuBatchId {}获取该批次库存和销量失败", goodsDetailDto.getFsupplierSkuBatchId());
            throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        }
        if (null != stockSellResult.getFsellNum()) {
            goodStockSellVo.setFsellNum(stockSellResult.getFsellNum());
        }
        if (null != stockSellResult.getFstockRemianNum()) {
            goodStockSellVo.setFstockRemianNum(stockSellResult.getFstockRemianNum());
        }
        return goodStockSellVo;
    }

    //获取sku的库存和销量
    private GoodStockSellVo getSkuStockSell(GoodsDetailDto goodsDetailDto) {
        List<SkuBatch> skuStockSellResult = skuBatchApi.queryByCriteria(Criteria.of(SkuBatch.class)
                .andEqualTo(SkuBatch::getFskuId, goodsDetailDto.getFskuId())
                .fields(SkuBatch::getFsupplierSkuBatchId)).getData();
        GoodStockSellVo result = new GoodStockSellVo();
        result.setFsellNum(0l);
        result.setFstockRemianNum(0l);
        for (SkuBatch skuBatch : skuStockSellResult) {
            GoodsDetailDto param = new GoodsDetailDto();
            param.setFsupplierSkuBatchId(skuBatch.getFsupplierSkuBatchId());
            GoodStockSellVo batchStockSell = this.getBatchStockSell(param);
            if (null != batchStockSell.getFsellNum()) {
                result.setFsellNum(result.getFsellNum() + batchStockSell.getFsellNum());
            }
            if (null != batchStockSell.getFstockRemianNum()) {
                result.setFstockRemianNum(result.getFstockRemianNum() + batchStockSell.getFstockRemianNum());
            }
        }
        return result;
    }

    //获取spu的库存和销量
    private GoodStockSellVo getSpuStockSell(GoodsDetailDto goodsDetailDto) {
        List<GoodsSku> spuStockSellResult = goodsSkuApi.queryByCriteria(Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFgoodsId, goodsDetailDto.getFgoodsId())
                .fields(GoodsSku::getFskuId)).getData();
        GoodStockSellVo result = new GoodStockSellVo();
        result.setFsellNum(0l);
        result.setFstockRemianNum(0l);
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
        return result;
    }

    //获取到规格的价格
    private Long getPackagePrice(GoodsDetailDto goodsDetailDto) {
        Long price = 0l;
        //是否支持平台会员折扣
        if (new Integer(1).equals(this.getIsUserDiscount(goodsDetailDto.getFskuId()))) {
            Integer foperateType = userApi.queryById(goodsDetailDto.getFuid()).getData().getFoperateType();
            Result<SkuBatchUserPrice> skuBatchUserPriceResult = skuBatchUserPriceApi.queryOneByCriteria(Criteria.of(SkuBatchUserPrice.class)
                    .andEqualTo(SkuBatchUserPrice::getFbatchPackageId, goodsDetailDto.getFbatchPackageId())
                    .andEqualTo(SkuBatchUserPrice::getFuserTypeId, foperateType)
                    .fields(SkuBatchUserPrice::getFbatchSellPrice));
            if (skuBatchUserPriceResult.isSuccess() && null != skuBatchUserPriceResult.getData().getFbatchSellPrice()) {
                price = skuBatchUserPriceResult.getData().getFbatchSellPrice();
            }
        } else {
            Result<GoodsSkuBatchPrice> goodsSkuBatchPriceResult = goodsSkuBatchPriceApi.queryOneByCriteria(Criteria.of(GoodsSkuBatchPrice.class)
                    .andEqualTo(GoodsSkuBatchPrice::getFbatchPackageId, goodsDetailDto.getFbatchPackageId())
                    .fields(GoodsSkuBatchPrice::getFbatchSellPrice));
            if (goodsSkuBatchPriceResult.isSuccess() && null != goodsSkuBatchPriceResult.getData().getFbatchSellPrice()) {
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
        List<SkuBatchPackage> batchResult = skuBatchPackageApi.queryByCriteria(Criteria.of(SkuBatchPackage.class)
                .andEqualTo(SkuBatchPackage::getFsupplierSkuBatchId, goodsDetailDto.getFsupplierSkuBatchId())
                .fields(SkuBatchPackage::getFbatchPackageId)).getData();

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
        return priceVo;
    }

    //获取到sku的价格
    private GoodsPriceVo getSkuPrice(GoodsDetailDto goodsDetailDto) {
        GoodsPriceVo priceVo = new GoodsPriceVo();
        priceVo.setPriceStart(BigDecimal.ZERO);
        priceVo.setPriceEnd(BigDecimal.ZERO);
        List<SkuBatch> skuBatcheResult = skuBatchApi.queryByCriteria(Criteria.of(SkuBatch.class)
                .andEqualTo(SkuBatch::getFskuId, goodsDetailDto.getFskuId())
                .fields(SkuBatch::getFsupplierSkuBatchId)).getData();

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
        return priceVo;
    }

    //获取到spu的价格
    private GoodsPriceVo getSpuPrice(GoodsDetailDto goodsDetailDto) {
        GoodsPriceVo priceVo = new GoodsPriceVo();
        priceVo.setPriceStart(BigDecimal.ZERO);
        priceVo.setPriceEnd(BigDecimal.ZERO);
        List<GoodsSku> skuResult = goodsSkuApi.queryByCriteria(Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFgoodsId, goodsDetailDto.getFgoodsId())
                .fields(GoodsSku::getFskuId)).getData();
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
        return priceVo;
    }

    //获取是否支持平台会员折扣 0 取 GoodsSkuBatchPrice 1 取 SkuBatchUserPrice
    private Integer getIsUserDiscount(Long skuId) {
        return goodsSkuApi.queryById(skuId).getData().getFisUserTypeDiscount();
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
