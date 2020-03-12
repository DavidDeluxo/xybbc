package com.xingyun.bbc.mallpc.service.impl;

import com.google.common.collect.Lists;
import com.xingyun.bbc.core.operate.api.CountryApi;
import com.xingyun.bbc.core.operate.po.Country;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.*;
import com.xingyun.bbc.core.sku.po.*;
import com.xingyun.bbc.core.supplier.enums.TradeTypeEnums;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.constants.MallPcConstants;
import com.xingyun.bbc.mallpc.common.utils.DateUtils;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.dto.sku.SaleSkuExportDto;
import com.xingyun.bbc.mallpc.model.vo.excel.sku.SaleSkuExportVo;
import com.xingyun.bbc.mallpc.model.vo.excel.sku.SkuBatchExportVo;
import com.xingyun.bbc.mallpc.model.vo.excel.sku.SkuBatchPackageExportVo;
import com.xingyun.bbc.mallpc.service.SkuService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/4 11:44
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.service.impl
 */
@Service
public class SkuServiceImpl implements SkuService {

    private static final Logger logger = LoggerFactory.getLogger(SkuServiceImpl.class);

    @Resource
    private DozerHolder dozerHolder;

    @Resource
    private GoodsSkuApi goodsSkuApi;

    @Resource
    private GoodsApi goodsApi;

    @Resource
    private CountryApi countryApi;

    @Resource
    private SkuBatchApi skuBatchApi;

    @Resource
    private SkuBatchPackageApi skuBatchPackageApi;

    @Resource
    private GoodsSkuBatchPriceApi goodsSkuBatchPriceApi;

    @Resource
    private SkuUserDiscountConfigApi skuUserDiscountConfigApi;

    @Resource
    private SkuBatchUserPriceApi skuBatchUserPriceApi;

    @Override
    public List<Object> selectListForExcelExport(Object queryParams, int page) {
        Long start = System.currentTimeMillis();
        List<SaleSkuExportVo> exportDataList = new ArrayList<>();
        if (!(queryParams instanceof SaleSkuExportDto)) {
            return Lists.newArrayList(exportDataList);
        }
        SaleSkuExportDto saleSkuExportDto = (SaleSkuExportDto) queryParams;
        Integer foperateType = saleSkuExportDto.getFoperateType();
        Criteria<GoodsSku, Object> goodsSkuCriteria = Criteria.of(GoodsSku.class);
        //只统计在售上架的sku
        goodsSkuCriteria.andEqualTo(GoodsSku::getFgoodStatus, 1).andEqualTo(GoodsSku::getFskuStatus, 1).andEqualTo(GoodsSku::getFisDelete, 0);
        goodsSkuCriteria.page(page, saleSkuExportDto.getPageSize());
        List<GoodsSku> goodsSkuList = ResultUtils.getData(goodsSkuApi.queryByCriteria(goodsSkuCriteria));
        if (CollectionUtils.isEmpty(goodsSkuList)) {
            logger.info("文件生成完成，类型：{}", foperateType);
            return Lists.newArrayList(exportDataList);
        }
        //skuCode 集合
        List<String> skuCodeList = goodsSkuList.stream().map(GoodsSku::getFskuCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuCodeList)) {
            logger.info("文件生成完成,skuCodeList为空，类型：{}", foperateType);
            return Lists.newArrayList(exportDataList);
        }

        List<Long> skuIdList = goodsSkuList.stream().map(GoodsSku::getFskuId).collect(Collectors.toList());


        Criteria<SkuBatch, Object> skuBatchCriteria = Criteria.of(SkuBatch.class)
                .fields(SkuBatch::getFskuBatchId
                        , SkuBatch::getFsupplierSkuBatchId
                        , SkuBatch::getFskuCode
                        , SkuBatch::getFstockRemianNum
                        , SkuBatch::getFwarehouseName
                        , SkuBatch::getFqualityEndDate
                        , SkuBatch::getFqualityStartDate);
        //只统计在售上架的sku 批次
        skuBatchCriteria.andIn(SkuBatch::getFskuCode, skuCodeList).andEqualTo(SkuBatch::getFbatchStatus, 2);
        List<SkuBatch> skuBatchList = ResultUtils.getData(skuBatchApi.queryByCriteria(skuBatchCriteria));
        //supplierSkuBatchId 集合
        List<String> supplierSkuBatchIdList = skuBatchList.stream().map(SkuBatch::getFsupplierSkuBatchId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuBatchList)) {
            logger.info("文件生成完成,skuBatchList为空，类型：{}", foperateType);
            return Lists.newArrayList(exportDataList);
        }

        Criteria<SkuBatchPackage, Object> skuBatchPackageCriteria = Criteria.of(SkuBatchPackage.class)
                .fields(SkuBatchPackage::getFbatchPackageId
                        , SkuBatchPackage::getFbatchStartNum
                        , SkuBatchPackage::getFbatchPackageNum
                        , SkuBatchPackage::getFsupplierSkuBatchId);
        skuBatchPackageCriteria.andIn(SkuBatchPackage::getFsupplierSkuBatchId, supplierSkuBatchIdList).sort(SkuBatchPackage::getFbatchPackageNum);
        List<SkuBatchPackage> skuBatchPackageList = ResultUtils.getData(skuBatchPackageApi.queryByCriteria(skuBatchPackageCriteria));
        if (CollectionUtils.isEmpty(skuBatchPackageList)) {
            logger.info("文件生成完成,skuBatchPackageList为空，类型：{}", foperateType);
            return Lists.newArrayList(exportDataList);
        }
        List<Long> batchPackageIdList = skuBatchPackageList.stream().map(SkuBatchPackage::getFbatchPackageId).collect(Collectors.toList());

        Criteria<GoodsSkuBatchPrice, Object> goodsSkuBatchPriceCriteria = Criteria.of(GoodsSkuBatchPrice.class)
                .fields(GoodsSkuBatchPrice::getFbatchPackageId
                        , GoodsSkuBatchPrice::getFbatchSellPrice
                        , GoodsSkuBatchPrice::getFsupplierSkuBatchId
                        , GoodsSkuBatchPrice::getFbatchPriceValue);
        goodsSkuBatchPriceCriteria.andIn(GoodsSkuBatchPrice::getFsupplierSkuBatchId, supplierSkuBatchIdList);
        goodsSkuBatchPriceCriteria.orIn(GoodsSkuBatchPrice::getFbatchPackageId, batchPackageIdList);
        List<GoodsSkuBatchPrice> goodsSkuBatchPriceList = ResultUtils.getData(goodsSkuBatchPriceApi.queryByCriteria(goodsSkuBatchPriceCriteria));


        Criteria<SkuUserDiscountConfig, Object> skuUserDiscountConfigCriteria = Criteria.of(SkuUserDiscountConfig.class)
                .fields(SkuUserDiscountConfig::getFdiscountId,SkuUserDiscountConfig::getFskuId);
        skuUserDiscountConfigCriteria.andIn(SkuUserDiscountConfig::getFskuId, skuIdList);
        skuUserDiscountConfigCriteria.andEqualTo(SkuUserDiscountConfig::getFuserTypeId, foperateType);
        skuUserDiscountConfigCriteria.andEqualTo(SkuUserDiscountConfig::getFisDelete, 0);
        List<SkuUserDiscountConfig> skuUserDiscountConfigList = ResultUtils.getData(skuUserDiscountConfigApi.queryByCriteria(skuUserDiscountConfigCriteria));


        Criteria<SkuBatchUserPrice, Object> skuBatchUserPriceCriteria = Criteria.of(SkuBatchUserPrice.class)
                .fields(SkuBatchUserPrice::getFbatchPackageId
                        , SkuBatchUserPrice::getFbatchSellPrice
                        , SkuBatchUserPrice::getFsupplierSkuBatchId
                        , SkuBatchUserPrice::getFbatchSellPrice);
        skuBatchUserPriceCriteria.andIn(SkuBatchUserPrice::getFsupplierSkuBatchId, supplierSkuBatchIdList);
        skuBatchUserPriceCriteria.orIn(SkuBatchUserPrice::getFbatchPackageId, batchPackageIdList);
        List<SkuBatchUserPrice> skuBatchUserPriceList = ResultUtils.getData(skuBatchUserPriceApi.queryByCriteria(skuBatchUserPriceCriteria));

        for (GoodsSku goodsSku : goodsSkuList) {
            SaleSkuExportVo saleSkuExportVo = dozerHolder.convert(goodsSku, SaleSkuExportVo.class);

            List<SkuBatch> sortSkuBatchList = skuBatchList.stream().filter(skuBatch -> StringUtils.equals(skuBatch.getFskuCode(), goodsSku.getFskuCode())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(sortSkuBatchList)) {
                continue;
            }
            //填充批次信息
            buildSkuBatch(goodsSkuBatchPriceList, skuUserDiscountConfigList, skuBatchUserPriceList, saleSkuExportVo, goodsSku, foperateType, sortSkuBatchList, skuBatchPackageList);
            //过滤没有上架批次的sku
            if (CollectionUtils.isEmpty(saleSkuExportVo.getSkuBatchExportVoList())) {
                continue;
            }
            //原产地 + 贸易类型
            buildTradeAndSource(saleSkuExportVo, goodsSku);

            exportDataList.add(saleSkuExportVo);
        }
        logger.info("文件生成完成，类型：{}，页码：{},size:{},用时：{}", foperateType, page, saleSkuExportDto.getPageSize(), System.currentTimeMillis() - start);
        return Lists.newArrayList(exportDataList);
    }

    private void buildTradeAndSource(SaleSkuExportVo saleSkuExportVo, GoodsSku goodsSku) {
        Goods goods = ResultUtils.getDataNotNull(goodsApi.queryById(goodsSku.getFgoodsId()));
        saleSkuExportVo.setFtradeName(TradeTypeEnums.getTradeType(goods.getFtradeId().toString()));
        Country country = ResultUtils.getData(countryApi.queryById(goods.getForiginId()));
        saleSkuExportVo.setFcountryName(null != country ? country.getFcountryName() : "");
    }

    private void buildSkuBatch(List<GoodsSkuBatchPrice> goodsSkuBatchPriceList,
                               List<SkuUserDiscountConfig> skuUserDiscountConfigList,
                               List<SkuBatchUserPrice> skuBatchUserPriceList,
                               SaleSkuExportVo saleSkuExportVo, GoodsSku goodsSku, Integer foperateType, List<SkuBatch> sortSkuBatchList, List<SkuBatchPackage> skuBatchPackageList) {
        List<SkuBatchExportVo> skuBatchExportVoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sortSkuBatchList)) {
            for (SkuBatch skuBatch : sortSkuBatchList) {
                List<SkuBatchPackage> sortSkuBatchPackageList = skuBatchPackageList.stream().filter(skuBatchPackage -> StringUtils.equals(skuBatchPackage.getFsupplierSkuBatchId(), skuBatch.getFsupplierSkuBatchId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(sortSkuBatchPackageList)) {
                    continue;
                }
                SkuBatchExportVo skuBatchExportVo = new SkuBatchExportVo();
                skuBatchExportVo.setFwarehouseName(skuBatch.getFwarehouseName());
                skuBatchExportVo.setFstockRemianNum(skuBatch.getFstockRemianNum());
                skuBatchExportVo.setQualityDate(DateUtils.formatDate(skuBatch.getFqualityStartDate(), MallPcConstants.DATE_PATTERN_YYYY_MM_DD) + "至" + DateUtils.formatDate(skuBatch.getFqualityEndDate(), MallPcConstants.DATE_PATTERN_YYYY_MM_DD));

                buildSkuBatchPackage(goodsSkuBatchPriceList, skuUserDiscountConfigList, skuBatchUserPriceList, skuBatchExportVo, skuBatch, foperateType, goodsSku, sortSkuBatchPackageList);
                skuBatchExportVoList.add(skuBatchExportVo);
            }
        }
        saleSkuExportVo.setSkuBatchExportVoList(skuBatchExportVoList);
    }

    private void buildSkuBatchPackage(List<GoodsSkuBatchPrice> goodsSkuBatchPriceList,
                                      List<SkuUserDiscountConfig> skuUserDiscountConfigList,
                                      List<SkuBatchUserPrice> skuBatchUserPriceList, SkuBatchExportVo skuBatchExportVo, SkuBatch skuBatch, Integer foperateType, GoodsSku goodsSku, List<SkuBatchPackage> sortSkuBatchPackageList) {
        List<SkuBatchPackageExportVo> skuBatchPackageExportVoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sortSkuBatchPackageList)) {
            for (SkuBatchPackage skuBatchPackage : sortSkuBatchPackageList) {
                SkuBatchPackageExportVo skuBatchPackageExportVo = new SkuBatchPackageExportVo();
                skuBatchPackageExportVo.setFbatchPackageNum(skuBatchPackage.getFbatchPackageNum());
                skuBatchPackageExportVo.setFbatchStartNum(skuBatchPackage.getFbatchStartNum());
                skuBatchPackageExportVo.setFbatchPackagePrice(getPrice(goodsSkuBatchPriceList, skuUserDiscountConfigList, skuBatchUserPriceList, skuBatch, skuBatchPackage, foperateType, goodsSku));
                skuBatchPackageExportVoList.add(skuBatchPackageExportVo);
            }
        }
        skuBatchExportVo.setSkuBatchPackageExportVoList(skuBatchPackageExportVoList);
    }

    private String getPrice(List<GoodsSkuBatchPrice> goodsSkuBatchPriceList,
                            List<SkuUserDiscountConfig> skuUserDiscountConfigList,
                            List<SkuBatchUserPrice> skuBatchUserPriceList, SkuBatch skuBatch, SkuBatchPackage skuBatchPackage, Integer foperateType, GoodsSku goodsSku) {
        BigDecimal price;
        Integer fisUserTypeDiscount = goodsSku.getFisUserTypeDiscount();
        if (fisUserTypeDiscount == null || fisUserTypeDiscount == 0) {
            price = getSkuNormalPrice(goodsSkuBatchPriceList, skuBatch.getFsupplierSkuBatchId(), skuBatchPackage.getFbatchPackageId());
        } else {

            Optional<SkuUserDiscountConfig> skuUserDiscountConfigOptional = skuUserDiscountConfigList.stream().filter(t -> goodsSku.getFskuId().equals(t.getFskuId())).findFirst();

            if (!skuUserDiscountConfigOptional.isPresent()) {
                price = getSkuNormalPrice(goodsSkuBatchPriceList, skuBatch.getFsupplierSkuBatchId(), skuBatchPackage.getFbatchPackageId());
            } else {
                Optional<SkuBatchUserPrice> goodsSkuBatchPriceOptional = skuBatchUserPriceList.stream().filter(t ->
                        StringUtils.equals(t.getFsupplierSkuBatchId(), skuBatch.getFsupplierSkuBatchId()) &&
                                skuBatchPackage.getFbatchPackageId().equals(t.getFbatchPackageId())
                ).findFirst();
                price = goodsSkuBatchPriceOptional.isPresent() ? new BigDecimal(goodsSkuBatchPriceOptional.get().getFbatchSellPrice()) : getSkuNormalPrice(goodsSkuBatchPriceList, skuBatch.getFsupplierSkuBatchId(), skuBatchPackage.getFbatchPackageId());
            }
        }
        return price.divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private BigDecimal getSkuNormalPrice(List<GoodsSkuBatchPrice> goodsSkuBatchPriceList, String fbatchId, Long fbatchPackageId) {
        Optional<GoodsSkuBatchPrice> goodsSkuBatchPriceOptional = goodsSkuBatchPriceList.stream().filter(t ->
                StringUtils.equals(t.getFsupplierSkuBatchId(), fbatchId) &&
                        fbatchPackageId.equals(t.getFbatchPackageId())
        ).findFirst();
        GoodsSkuBatchPrice goodsSkuBatchPrice = goodsSkuBatchPriceOptional.get();
        return new BigDecimal(goodsSkuBatchPrice.getFbatchSellPrice());
    }
}