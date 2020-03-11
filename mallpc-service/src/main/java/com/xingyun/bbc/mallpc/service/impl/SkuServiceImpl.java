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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
        List<SaleSkuExportVo> exportDataList = new ArrayList<>();
        if (!(queryParams instanceof SaleSkuExportDto)) {
            return Lists.newArrayList(exportDataList);
        }
        SaleSkuExportDto saleSkuExportDto = (SaleSkuExportDto) queryParams;
        Integer foperateType = saleSkuExportDto.getFoperateType();
        Criteria<GoodsSku, Object> goodsSkuCriteria = Criteria.of(GoodsSku.class);
        //只统计在售上架的sku
        goodsSkuCriteria.andEqualTo(GoodsSku::getFgoodStatus, 1).andEqualTo(GoodsSku::getFskuStatus, 1).andEqualTo(GoodsSku::getFisDelete,0);
        goodsSkuCriteria.page(page, saleSkuExportDto.getPageSize());
        List<GoodsSku> goodsSkuList = ResultUtils.getData(goodsSkuApi.queryByCriteria(goodsSkuCriteria));
        if (CollectionUtils.isEmpty(goodsSkuList)) {
            logger.info("文件生成完成，类型：{}",foperateType);
            return Lists.newArrayList(exportDataList);
        }
        for (GoodsSku goodsSku : goodsSkuList) {
            SaleSkuExportVo saleSkuExportVo = dozerHolder.convert(goodsSku, SaleSkuExportVo.class);
            //填充批次信息
            buildSkuBatch(saleSkuExportVo, goodsSku, foperateType);
            //过滤没有上架批次的sku
            if (CollectionUtils.isEmpty(saleSkuExportVo.getSkuBatchExportVoList())){
                continue;
            }
            //原产地 + 贸易类型
            buildTradeAndSource(saleSkuExportVo, goodsSku);

            exportDataList.add(saleSkuExportVo);
        }
        logger.info("文件生成完成，类型：{}，页码：{}",foperateType,page);
        return Lists.newArrayList(exportDataList);
    }

    private void buildTradeAndSource(SaleSkuExportVo saleSkuExportVo, GoodsSku goodsSku) {
        Goods goods = ResultUtils.getDataNotNull(goodsApi.queryById(goodsSku.getFgoodsId()));
        saleSkuExportVo.setFtradeName(TradeTypeEnums.getTradeType(goods.getFtradeId().toString()));
        Country country = ResultUtils.getData(countryApi.queryById(goods.getForiginId()));
        saleSkuExportVo.setFcountryName(null != country ? country.getFcountryName() : "");
    }

    private void buildSkuBatch(SaleSkuExportVo saleSkuExportVo, GoodsSku goodsSku, Integer foperateType) {
        List<SkuBatchExportVo> skuBatchExportVoList = new ArrayList<>();
        Criteria<SkuBatch, Object> skuBatchCriteria = Criteria.of(SkuBatch.class);
        //只统计在售上架的sku 批次
        skuBatchCriteria.andEqualTo(SkuBatch::getFskuCode, goodsSku.getFskuCode()).andEqualTo(SkuBatch::getFbatchStatus, 2);
        List<SkuBatch> skuBatchList = ResultUtils.getData(skuBatchApi.queryByCriteria(skuBatchCriteria));
        if (CollectionUtils.isNotEmpty(skuBatchList)) {
            for (SkuBatch skuBatch : skuBatchList) {
                SkuBatchExportVo skuBatchExportVo = new SkuBatchExportVo();
                skuBatchExportVo.setFwarehouseName(skuBatch.getFwarehouseName());
                skuBatchExportVo.setFstockRemianNum(skuBatch.getFstockRemianNum());
                skuBatchExportVo.setQualityDate(DateUtils.formatDate(skuBatch.getFqualityStartDate(), MallPcConstants.DATE_PATTERN_YYYY_MM_DD) + "至" + DateUtils.formatDate(skuBatch.getFqualityEndDate(), MallPcConstants.DATE_PATTERN_YYYY_MM_DD));

                buildSkuBatchPackage(skuBatchExportVo, skuBatch, foperateType, goodsSku);
                skuBatchExportVoList.add(skuBatchExportVo);
            }
        }
        saleSkuExportVo.setSkuBatchExportVoList(skuBatchExportVoList);
    }

    private void buildSkuBatchPackage(SkuBatchExportVo skuBatchExportVo, SkuBatch skuBatch, Integer foperateType, GoodsSku goodsSku) {
        List<SkuBatchPackageExportVo> skuBatchPackageExportVoList = new ArrayList<>();
        Criteria<SkuBatchPackage, Object> skuBatchPackageCriteria = Criteria.of(SkuBatchPackage.class)
                .andEqualTo(SkuBatchPackage::getFsupplierSkuBatchId, skuBatch.getFsupplierSkuBatchId()).sort(SkuBatchPackage::getFbatchPackageNum);
        List<SkuBatchPackage> skuBatchPackageList = ResultUtils.getData(skuBatchPackageApi.queryByCriteria(skuBatchPackageCriteria));
        if (CollectionUtils.isNotEmpty(skuBatchPackageList)) {
            for (SkuBatchPackage skuBatchPackage : skuBatchPackageList) {
                SkuBatchPackageExportVo skuBatchPackageExportVo = new SkuBatchPackageExportVo();
                skuBatchPackageExportVo.setFbatchPackageNum(skuBatchPackage.getFbatchPackageNum());
                skuBatchPackageExportVo.setFbatchStartNum(skuBatchPackage.getFbatchStartNum());
                skuBatchPackageExportVo.setFbatchPackagePrice(getPrice(skuBatch, skuBatchPackage, foperateType, goodsSku));
                skuBatchPackageExportVoList.add(skuBatchPackageExportVo);
            }
        }
        skuBatchExportVo.setSkuBatchPackageExportVoList(skuBatchPackageExportVoList);
    }

    private String getPrice(SkuBatch skuBatch, SkuBatchPackage skuBatchPackage, Integer foperateType, GoodsSku goodsSku) {
        BigDecimal price;
        Integer fisUserTypeDiscount = goodsSku.getFisUserTypeDiscount();
        if (fisUserTypeDiscount == null || fisUserTypeDiscount == 0) {
            price = getSkuNormalPrice(skuBatch.getFsupplierSkuBatchId(), skuBatchPackage.getFbatchPackageId());
        } else {
            SkuUserDiscountConfig query = new SkuUserDiscountConfig();
            query.setFskuId(goodsSku.getFskuId());
            query.setFuserTypeId(foperateType.longValue());
            query.setFisDelete(0);
            SkuUserDiscountConfig skuUserDiscountConfig = ResultUtils.getData(skuUserDiscountConfigApi.queryOne(query));
            if (null == skuUserDiscountConfig) {
                price = getSkuNormalPrice(skuBatch.getFsupplierSkuBatchId(), skuBatchPackage.getFbatchPackageId());
            } else {
                SkuBatchUserPrice skuBatchUserPriceQuery = new SkuBatchUserPrice();
                skuBatchUserPriceQuery.setFbatchPackageId(skuBatchPackage.getFbatchPackageId());
                skuBatchUserPriceQuery.setFsupplierSkuBatchId(skuBatch.getFsupplierSkuBatchId());
                skuBatchUserPriceQuery.setFuserTypeId(foperateType.longValue());
                SkuBatchUserPrice skuBatchUserPrice = ResultUtils.getData(skuBatchUserPriceApi.queryOne(skuBatchUserPriceQuery));
                price = null != skuBatchUserPrice ? new BigDecimal(skuBatchUserPrice.getFbatchSellPrice()) : getSkuNormalPrice(skuBatch.getFsupplierSkuBatchId(), skuBatchPackage.getFbatchPackageId());
            }
        }
        return price.divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private BigDecimal getSkuNormalPrice(String fbatchId, Long fbatchPackageId) {
        GoodsSkuBatchPrice goodsSkuBatchPrice = new GoodsSkuBatchPrice();
        goodsSkuBatchPrice.setFsupplierSkuBatchId(fbatchId);
        goodsSkuBatchPrice.setFbatchPackageId(fbatchPackageId);
        goodsSkuBatchPrice = ResultUtils.getData(goodsSkuBatchPriceApi.queryOne(goodsSkuBatchPrice));
        return new BigDecimal(goodsSkuBatchPrice.getFbatchSellPrice());
    }
}