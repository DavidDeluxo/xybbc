package com.xingyun.bbc.mall.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.SubjectApi;
import com.xingyun.bbc.core.operate.api.SubjectApplicableSkuApi;
import com.xingyun.bbc.core.operate.api.SubjectApplicableSkuConditionApi;
import com.xingyun.bbc.core.operate.po.Subject;
import com.xingyun.bbc.core.operate.po.SubjectApplicableSku;
import com.xingyun.bbc.core.operate.po.SubjectApplicableSkuCondition;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.*;
import com.xingyun.bbc.core.sku.po.*;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.PriceUtil;
import com.xingyun.bbc.mall.base.utils.ResultUtils;
import com.xingyun.bbc.mall.model.dto.SubjectQueryDto;
import com.xingyun.bbc.mall.model.vo.SearchItemListVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;
import com.xingyun.bbc.mall.model.vo.SubjectVo;
import com.xingyun.bbc.mall.service.SubjectService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/1/13 13:46
 * @description:
 * @package com.xingyun.bbc.mall.service.impl
 */
@Service
public class SubjectServiceImpl implements SubjectService {

    public static final Logger logger = LoggerFactory.getLogger(SubjectServiceImpl.class);

    @Resource
    private SubjectApi subjectApi;

    @Resource
    private SubjectApplicableSkuApi subjectApplicableSkuApi;

    @Resource
    private SubjectApplicableSkuConditionApi subjectApplicableSkuConditionApi;

    @Resource
    private DozerHolder dozerHolder;

    @Resource
    private GoodsApi goodsApi;

    @Resource
    private GoodsSkuApi goodsSkuApi;

    @Resource
    private SkuBatchApi skuBatchApi;

    @Resource
    private SkuBatchPackageApi skuBatchPackageApi;

    @Resource
    private SkuUserDiscountConfigApi skuUserDiscountConfigApi;

    @Resource
    private SkuBatchUserPriceApi skuBatchUserPriceApi;

    @Resource
    private GoodsSkuBatchPriceApi goodsSkuBatchPriceApi;

    @Resource
    private GoodsTradeInfoApi goodsTradeInfoApi;

    @Override
    public SubjectVo getById(SubjectQueryDto subjectQueryDto) {
        Long fsubjectId = subjectQueryDto.getFsubjectId();
        SubjectVo subjectVo = new SubjectVo();
        Result<Subject> subjectResult = subjectApi.queryById(fsubjectId);
        if (!subjectResult.isSuccess()) {
            logger.info("查询专题信息异常，专题id[{}],error:{}", fsubjectId, subjectResult.getMsg());
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        Subject subject = subjectResult.getData();
        if (subject == null) {
            logger.info("专题id[{}]信息不存在", fsubjectId);
            return subjectVo;
        }
        return dozerHolder.convert(subject, SubjectVo.class);
    }

    @Override
    public SearchItemListVo<SearchItemVo> getSubjectGoods(SubjectQueryDto subjectQueryDto) {
        SearchItemListVo<SearchItemVo> pageVo = new SearchItemListVo<>();
        pageVo.setIsLogin(subjectQueryDto.getIsLogin());
        pageVo.setTotalCount(0);
        pageVo.setCurrentPage(subjectQueryDto.getPageIndex());
        pageVo.setPageSize(subjectQueryDto.getPageSize());
        pageVo.setPageCount(0);
        pageVo.setList(Lists.newArrayList());
        getSubjectSkuIdList(subjectQueryDto, pageVo);
        return pageVo;
    }

    /**
     * 返回skuId
     *
     * @param subjectQueryDto
     * @return
     */
    private void getSubjectSkuIdList(SubjectQueryDto subjectQueryDto, SearchItemListVo<SearchItemVo> pageVo) {
        Criteria<SubjectApplicableSku, Object> subjectSku = Criteria.of(SubjectApplicableSku.class)
                .fields(SubjectApplicableSku::getFskuId)
                .andEqualTo(SubjectApplicableSku::getFsubjectId, subjectQueryDto.getFsubjectId());

        Result<Integer> subjectSkuCountResult = subjectApplicableSkuApi.countByCriteria(subjectSku);
        Integer subjectSkuCount = ResultUtils.getData(subjectSkuCountResult);

        subjectSku.page(subjectQueryDto.getPageIndex(), subjectQueryDto.getPageSize());
        Result<List<SubjectApplicableSku>> skuIdResult = subjectApplicableSkuApi.queryByCriteria(subjectSku);
        List<SubjectApplicableSku> subjectApplicableSkuList = ResultUtils.getData(skuIdResult);

        if (!subjectSkuCount.equals(0)) {
            //skuId 直接存储
            pageVo.setTotalCount(subjectSkuCount);
            if (CollectionUtils.isNotEmpty(subjectApplicableSkuList)) {
                List<Long> fskuIds = subjectApplicableSkuList.stream().map(SubjectApplicableSku::getFskuId).collect(toList());
                List<SearchItemVo> list = new ArrayList<>(fskuIds.size());
                genarateSearchItemVoBySku(list, subjectQueryDto, fskuIds);
                pageVo.setList(list);
                return;
            }
            return;
        }
        //条件存储
        Criteria<SubjectApplicableSkuCondition, Object> subjectSkuCondition = Criteria.of(SubjectApplicableSkuCondition.class)
                .andEqualTo(SubjectApplicableSkuCondition::getFsubjectId, subjectQueryDto.getFsubjectId());

        Result<List<SubjectApplicableSkuCondition>> skuConditionResult = subjectApplicableSkuConditionApi.queryByCriteria(subjectSkuCondition);
        List<SubjectApplicableSkuCondition> skuConditionList = ResultUtils.getData(skuConditionResult);
        if (CollectionUtils.isEmpty(skuConditionList)) {
            return;
        }
        Criteria<GoodsSku, Object> skuObjectCriteria = Criteria.of(GoodsSku.class)
                .fields(GoodsSku::getFskuId,
                        GoodsSku::getFgoodsId,
                        GoodsSku::getFskuName,
                        GoodsSku::getFskuThumbImage,
                        GoodsSku::getFskuCode,
                        GoodsSku::getFlabelId)
                .sortDesc(GoodsSku::getFmodifyTime);
        skuConditionList.forEach(skuCondition -> {

            List<Long> tradeCodes = StringUtils.isNotBlank(skuCondition.getFtradeCode()) ? JSON.parseObject(skuCondition.getFtradeCode(), new TypeReference<List<Long>>() {
            }) : null;
            List<Long> labelIds = StringUtils.isNotBlank(skuCondition.getFlabelId()) ? JSON.parseObject(skuCondition.getFlabelId(), new TypeReference<List<Long>>() {
            }) : null;
            List<Long> brandIds = StringUtils.isNotBlank(skuCondition.getFbrandId()) ? JSON.parseObject(skuCondition.getFbrandId(), new TypeReference<List<Long>>() {
            }) : null;
            Map<String, List<Long>> categoryIds = StringUtils.isNotBlank(skuCondition.getFcategoryId()) ? JSON.parseObject(skuCondition.getFcategoryId(), new TypeReference<Map<String, List<Long>>>() {
            }) : null;

            // 商品id
            List<Long> goodsIds = null;

            if (CollectionUtils.isNotEmpty(tradeCodes)) {
                List<Goods> goods = getGoodsByTradeIds(tradeCodes);
                goodsIds = goods.stream().map(Goods::getFgoodsId).collect(Collectors.toList());
            }

            // 单组条件开始
            skuObjectCriteria.orLeft();
            if (MapUtils.isNotEmpty(categoryIds)) {

                // 分类间或关系开始
                skuObjectCriteria.andLeft();
                List<Long> one = categoryIds.get("1");
                if (CollectionUtils.isNotEmpty(one)) {
                    skuObjectCriteria.orIn(GoodsSku::getFcategoryId1, one);
                }
                List<Long> two = categoryIds.get("2");
                if (CollectionUtils.isNotEmpty(two)) {
                    skuObjectCriteria.orIn(GoodsSku::getFcategoryId2, two);
                }
                List<Long> three = categoryIds.get("3");
                if (CollectionUtils.isNotEmpty(three)) {
                    skuObjectCriteria.orIn(GoodsSku::getFcategoryId3, three);
                }
                // 分类间或关系结束
                skuObjectCriteria.addRight();
            }
            if (CollectionUtils.isNotEmpty(brandIds)) {
                skuObjectCriteria.andIn(GoodsSku::getFbrandId, brandIds);
            }
            if (CollectionUtils.isNotEmpty(labelIds)) {
                skuObjectCriteria.andIn(GoodsSku::getFlabelId, labelIds);
            }
            if (CollectionUtils.isNotEmpty(goodsIds)) {
                skuObjectCriteria.andIn(GoodsSku::getFgoodsId, goodsIds);
            }
            // 单组条件结束
            skuObjectCriteria.addRight();
        });
        Result<Integer> countResult = goodsSkuApi.countByCriteria(skuObjectCriteria);
        Integer skuCount = ResultUtils.getData(countResult);
        pageVo.setTotalCount(skuCount);
        if (skuCount.compareTo(0) > 0) {
            skuObjectCriteria.page(subjectQueryDto.getPageIndex(), subjectQueryDto.getPageSize());
            Result<List<GoodsSku>> goodsSkuResult = goodsSkuApi.queryByCriteria(skuObjectCriteria);
            List<GoodsSku> goodsSkuList = ResultUtils.getData(goodsSkuResult);
            List<SearchItemVo> list = new ArrayList<>(goodsSkuList.size());
            genarateSearchItemVo(list, subjectQueryDto, goodsSkuList);
            pageVo.setList(list);
        }
    }

    private void genarateSearchItemVoBySku(List<SearchItemVo> list, SubjectQueryDto subjectQueryDto, List<Long> fskuIds) {
        Criteria<GoodsSku, Object> skuObjectCriteria = Criteria.of(GoodsSku.class)
                .fields(GoodsSku::getFskuId,
                        GoodsSku::getFgoodsId,
                        GoodsSku::getFskuName,
                        GoodsSku::getFskuThumbImage,
                        GoodsSku::getFskuCode,
                        GoodsSku::getFlabelId)
                .sortDesc(GoodsSku::getFmodifyTime);
        skuObjectCriteria.andIn(GoodsSku::getFskuId, fskuIds);
        Result<List<GoodsSku>> goodsSkuResult = goodsSkuApi.queryByCriteria(skuObjectCriteria);
        List<GoodsSku> goodsSkuList = ResultUtils.getData(goodsSkuResult);
        if (CollectionUtils.isEmpty(goodsSkuList)) {
            return;
        }
        genarateSearchItemVo(list, subjectQueryDto, goodsSkuList);
    }

    private void genarateSearchItemVo(List<SearchItemVo> list, SubjectQueryDto subjectQueryDto, List<GoodsSku> goodsSkuList) {
        goodsSkuList.forEach(sku -> {
            SearchItemVo searchItemVo = new SearchItemVo();
            searchItemVo.setFimgUrl(sku.getFskuThumbImage());
            searchItemVo.setFgoodsId(sku.getFgoodsId().intValue());
            searchItemVo.setFlabelId(sku.getFlabelId().intValue());
            searchItemVo.setFskuId(sku.getFskuId().intValue());
            searchItemVo.setFskuName(sku.getFskuName());

            GoodsTradeInfo goodsTradeInfo = getGoodsTradeInfo(getGoods(sku.getFgoodsId()).getFtradeId());
            searchItemVo.setFtradeName(goodsTradeInfo.getFtradeName());
            searchItemVo.setFtradedId(goodsTradeInfo.getFtradeId().intValue());

            List<SkuBatch> skuBatchList = this.getSkuBatchList(sku.getFskuId());
            Long stockRemainNum = skuBatchList.stream().mapToLong(SkuBatch::getFstockRemianNum).sum();
            Long sellNum = skuBatchList.stream().mapToLong(SkuBatch::getFsellNum).sum();
            searchItemVo.setFsellNum(sellNum);
            searchItemVo.setFremainTotal(stockRemainNum.intValue());

            BigDecimal price = BigDecimal.ZERO;
            if (Objects.nonNull(subjectQueryDto.getFuid())) {
                List<BigDecimal> batchPrice = new ArrayList<>();
                for (SkuBatch skuBatch : skuBatchList) {
                    List<SkuBatchPackage> skuBatchPackageList = this.getSkuBatchPackageList(skuBatch);
                    for (SkuBatchPackage skuBatchPackage : skuBatchPackageList) {
                        try {
                            BigDecimal skuPrice = this.selectSkuPrice(subjectQueryDto.getFoperateType(), sku.getFskuCode(), skuBatch.getFsupplierSkuBatchId(), skuBatchPackage.getFbatchPackageId());
                            batchPrice.add(skuPrice);
                        } catch (Exception e) {
                            logger.warn("商品价格异常:{}", e.getMessage());
                            continue;
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(batchPrice)) {
                    List<BigDecimal> sort = batchPrice.stream().sorted().collect(toList());
                    price = sort.get(0);
                }
            }
            searchItemVo.setFbatchSellPrice(PriceUtil.toYuan(price));
            list.add(searchItemVo);
        });
    }

    private GoodsTradeInfo getGoodsTradeInfo(Long ftradeId) {
        return ResultUtils.getData(goodsTradeInfoApi.queryById(ftradeId));
    }

    private List<SkuBatch> getSkuBatchList(Long fskuId) {
        SkuBatch skuBatch = new SkuBatch();
        skuBatch.setFskuId(fskuId);
        skuBatch.setFbatchStatus(2);
        return ResultUtils.getData(skuBatchApi.queryList(skuBatch));
    }

    private List<SkuBatchPackage> getSkuBatchPackageList(SkuBatch sb) {
        SkuBatchPackage sbp = new SkuBatchPackage();
        sbp.setFsupplierSkuBatchId(sb.getFsupplierSkuBatchId());
        return ResultUtils.getData(skuBatchPackageApi.queryList(sbp));
    }

    /**
     * 根据贸易类型获取商品
     *
     * @param tradeIds
     * @return
     */
    private List<Goods> getGoodsByTradeIds(List<Long> tradeIds) {
        Criteria<Goods, Object> goodsObjectCriteria = Criteria.of(Goods.class)
                .andIn(Goods::getFtradeId, tradeIds);
        return ResultUtils.getData(goodsApi.queryByCriteria(goodsObjectCriteria));
    }

    private Goods getGoods(Long fgoodsId) {
        return ResultUtils.getData(goodsApi.queryById(fgoodsId));
    }

    private BigDecimal selectSkuPrice(Integer foperateType, String skuCode, String supplierSkuBatchId, Long batchPackageId) {

        BigDecimal skuPrice;

        GoodsSku goodsSku = new GoodsSku();
        goodsSku.setFskuCode(skuCode);
        goodsSku = ResultUtils.getData(goodsSkuApi.queryOne(goodsSku));
        Integer fisUserTypeDiscount = goodsSku.getFisUserTypeDiscount();
        //查询普通价格
        if (fisUserTypeDiscount != null && fisUserTypeDiscount == 1) {

            SkuUserDiscountConfig skuUserDiscountConfig = new SkuUserDiscountConfig();
            skuUserDiscountConfig.setFskuId(goodsSku.getFskuId());
            skuUserDiscountConfig.setFuserTypeId(foperateType.longValue());
            skuUserDiscountConfig.setFisDelete(0);
            skuUserDiscountConfig = ResultUtils.getData(skuUserDiscountConfigApi.queryOne(skuUserDiscountConfig));
            if (skuUserDiscountConfig == null) {
                skuPrice = this.selectSkuNormalPrice(supplierSkuBatchId, batchPackageId);
                return skuPrice;
            }

            SkuBatchUserPrice skuBatchUserPrice = new SkuBatchUserPrice();
            skuBatchUserPrice.setFbatchPackageId(batchPackageId);
            skuBatchUserPrice.setFsupplierSkuBatchId(supplierSkuBatchId);
            skuBatchUserPrice.setFuserTypeId(foperateType.longValue());
            skuBatchUserPrice = ResultUtils.getData(skuBatchUserPriceApi.queryOne(skuBatchUserPrice));
            if (skuBatchUserPrice == null) {
                skuPrice = this.selectSkuNormalPrice(supplierSkuBatchId, batchPackageId);
            } else {
                skuPrice = new BigDecimal(skuBatchUserPrice.getFbatchSellPrice());
            }
        } else {
            skuPrice = this.selectSkuNormalPrice(supplierSkuBatchId, batchPackageId);
        }
        return skuPrice;
    }

    private BigDecimal selectSkuNormalPrice(String fbatchId, Long fbatchPackageId) {
        GoodsSkuBatchPrice goodsSkuBatchPrice = new GoodsSkuBatchPrice();
        goodsSkuBatchPrice.setFsupplierSkuBatchId(fbatchId);
        goodsSkuBatchPrice.setFbatchPackageId(fbatchPackageId);
        goodsSkuBatchPrice = ResultUtils.getData(goodsSkuBatchPriceApi.queryOne(goodsSkuBatchPrice));
        BigDecimal skuPrice = new BigDecimal(goodsSkuBatchPrice.getFbatchSellPrice());
        return skuPrice;
    }
}