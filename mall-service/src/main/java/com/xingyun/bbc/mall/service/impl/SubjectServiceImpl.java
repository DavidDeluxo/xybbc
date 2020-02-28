package com.xingyun.bbc.mall.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.xingyun.bbc.common.elasticsearch.config.EsCriteria;
import com.xingyun.bbc.common.elasticsearch.config.EsManager;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.market.enums.CouponApplicableSkuEnum;
import com.xingyun.bbc.core.operate.api.SubjectApi;
import com.xingyun.bbc.core.operate.api.SubjectApplicableSkuApi;
import com.xingyun.bbc.core.operate.api.SubjectApplicableSkuConditionApi;
import com.xingyun.bbc.core.operate.api.SubjectFloorApi;
import com.xingyun.bbc.core.operate.po.Subject;
import com.xingyun.bbc.core.operate.po.SubjectApplicableSku;
import com.xingyun.bbc.core.operate.po.SubjectApplicableSkuCondition;
import com.xingyun.bbc.core.operate.po.SubjectFloor;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.*;
import com.xingyun.bbc.core.sku.po.*;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.PriceUtil;
import com.xingyun.bbc.mall.base.utils.ResultUtils;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.dto.SubjectQueryDto;
import com.xingyun.bbc.mall.model.dto.SubjectSkuQueryDto;
import com.xingyun.bbc.mall.model.vo.ChildSubjectVo;
import com.xingyun.bbc.mall.model.vo.SearchItemListVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;
import com.xingyun.bbc.mall.model.vo.SubjectVo;
import com.xingyun.bbc.mall.service.GoodsService;
import com.xingyun.bbc.mall.service.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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
@Slf4j
@Service
public class SubjectServiceImpl implements SubjectService {

    public static final Logger logger = LoggerFactory.getLogger(SubjectServiceImpl.class);

    private static final String SUBJECT_ALIAS_PREFIX = "subject_";

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

    @Resource
    private EsManager esManager;

    @Resource
    private GoodsService goodsService;

    @Resource
    private SubjectFloorApi subjectFloorApi;

    @Override
    public SubjectVo getById(SubjectQueryDto subjectQueryDto) {
        Long fsubjectId = subjectQueryDto.getFsubjectId();
        Result<Subject> subjectResult = subjectApi.queryById(fsubjectId);
        if (!subjectResult.isSuccess()) {
            logger.info("查询专题信息异常，专题id[{}],error:{}", fsubjectId, subjectResult.getMsg());
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        Subject subject = subjectResult.getData();
        if (subject == null) {
            logger.info("专题id[{}]信息不存在", fsubjectId);
            return null;
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
        try {
            return goodsService.searchSkuList(dozerHolder.convert(subjectQueryDto, SearchItemDto.class)).getData();
        } catch (Exception e) {
            logger.info("es 查询专题id[{}]商品失败，转查数据库", subjectQueryDto.getFsubjectId());
            getSubjectSkuIdList(subjectQueryDto, pageVo);
            return pageVo;
        }
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
                .andEqualTo(SubjectApplicableSku::getFsubjectId, subjectQueryDto.getFsubjectId()).andEqualTo(SubjectApplicableSku::getFsubjectFloorId, 0);

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
                .andEqualTo(GoodsSku::getFskuStatus, 1)
                .andEqualTo(GoodsSku::getFisDelete, 0)
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
                .andEqualTo(GoodsSku::getFskuStatus, 1)
                .andEqualTo(GoodsSku::getFisDelete, 0)
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
            searchItemVo.setFremainTotal(stockRemainNum);

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

    @Override
    public void updateSubjectInfoToEsByAliasAll(List<Long> fsubjectIds, Integer pageSize, Integer pageIndex) {
        Criteria<Subject, Object> criteria = Criteria.of(Subject.class).fields(Subject::getFsubjectId).page(pageIndex, pageSize);
        if (CollectionUtils.isNotEmpty(fsubjectIds)) {
            criteria.andIn(Subject::getFsubjectId, fsubjectIds);
        }
        Result<List<Subject>> subjectResult = subjectApi.queryByCriteria(criteria);
        List<Subject> updateList = ResultUtils.getData(subjectResult);
        for (Subject subject : updateList) {
            try {
                this.updateSubjectInfoToEsByAlias(subject);
            } catch (Exception e) {
                log.info("更新专题:{}失败", subject.getFsubjectId());
                e.printStackTrace();
            }
        }
    }

    @Override
    public SearchItemListVo<ChildSubjectVo> getChildSubject(SubjectQueryDto subjectQueryDto) {
        SearchItemListVo<ChildSubjectVo> pageVo = new SearchItemListVo<>();
        pageVo.setIsLogin(subjectQueryDto.getIsLogin());
//        pageVo.setTotalCount(0);
        pageVo.setCurrentPage(subjectQueryDto.getPageIndex());
        pageVo.setPageSize(subjectQueryDto.getPageSize());
//        pageVo.setPageCount(0);

        Criteria<SubjectFloor, Object> subjectFloorCriteria = Criteria.of(SubjectFloor.class)
                .fields(SubjectFloor::getFsubjectFloorId,
                        SubjectFloor::getFsubjectParentId,
                        SubjectFloor::getFsubjectId,
                        SubjectFloor::getFsubjectFloorSort,
                        SubjectFloor::getFsubjectMobileLayout,
                        SubjectFloor::getFsubjectFloorContentType)
                .andEqualTo(SubjectFloor::getFsubjectParentId, subjectQueryDto.getFsubjectId())
                .sort(SubjectFloor::getFsubjectFloorSort);

        Integer subjectFloorCount = ResultUtils.getData(subjectFloorApi.countByCriteria(subjectFloorCriteria));
        if (subjectFloorCount == null || subjectFloorCount.equals(0)) {
            return pageVo;
        }
        pageVo.setTotalCount(subjectFloorCount);
        subjectFloorCriteria.page(subjectQueryDto.getPageIndex(), subjectQueryDto.getPageSize());
        List<SubjectFloor> subjectFloorList = ResultUtils.getData(subjectFloorApi.queryByCriteria(subjectFloorCriteria));
        List<ChildSubjectVo> childSubjectVoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subjectFloorList)) {
            for (SubjectFloor subjectFloor : subjectFloorList) {
                childSubjectVoList.add(generateChildSubjectVo(subjectFloor, subjectQueryDto));
            }
        }
        pageVo.setList(childSubjectVoList);
        return pageVo;
    }


    private ChildSubjectVo generateChildSubjectVo(SubjectFloor subjectFloor, SubjectQueryDto subjectQueryDto) {
        ChildSubjectVo childSubjectVo = dozerHolder.convert(subjectFloor, ChildSubjectVo.class);
        SubjectQueryDto subjectQuery = new SubjectQueryDto();
        subjectQuery.setFsubjectId(subjectFloor.getFsubjectId());

        SubjectVo subjectVo = getById(subjectQuery);
        if (null == subjectVo){
            return childSubjectVo;
        }
        childSubjectVo.setFsubjectName(subjectVo.getFsubjectName());
        childSubjectVo.setFsubjectDescription(subjectVo.getFsubjectDescription());
        childSubjectVo.setFsubjectStatus(subjectVo.getFsubjectStatus());
        childSubjectVo.setFsubjectBackgroundColor(subjectVo.getFsubjectBackgroundColor());
        childSubjectVo.setFsubjectMobileBackgroundPic(subjectVo.getFsubjectMobileBackgroundPic());
        childSubjectVo.setFsubjectMobilePic(subjectVo.getFsubjectMobilePic());
        List<SearchItemVo> searchItemVoList = generateChildSubjectGoods(childSubjectVo, subjectQueryDto);
        childSubjectVo.setSearchItemVoList(searchItemVoList);
        return childSubjectVo;
    }

    private List<SearchItemVo> generateChildSubjectGoods(ChildSubjectVo childSubjectVo, SubjectQueryDto subjectQueryDto) {
        List<SearchItemVo> searchItemVoList = new ArrayList<>();
        Criteria<SubjectApplicableSku, Object> subjectSku = Criteria.of(SubjectApplicableSku.class)
                .fields(SubjectApplicableSku::getFskuId).andEqualTo(SubjectApplicableSku::getFsubjectFloorId, childSubjectVo.getFsubjectFloorId());

        Result<List<SubjectApplicableSku>> skuIdResult = subjectApplicableSkuApi.queryByCriteria(subjectSku);
        List<SubjectApplicableSku> subjectApplicableSkuList = ResultUtils.getData(skuIdResult);

        if (CollectionUtils.isNotEmpty(subjectApplicableSkuList)) {
            List<Long> fskuIds = subjectApplicableSkuList.stream().map(SubjectApplicableSku::getFskuId).collect(toList());
            genarateSearchItemVoBySku(searchItemVoList, subjectQueryDto, fskuIds);
        }
        return searchItemVoList;
    }


    @Override
    public void updateSubjectInfoToEsByAlias(Subject subject) throws Exception {
        //查询主题信息
        Result<Subject> subjectResult = subjectApi.queryOneByCriteria(Criteria.of(Subject.class).andEqualTo(Subject::getFsubjectId, subject.getFsubjectId()));
        Subject subjectDB = ResultUtils.getDataNotNull(subjectResult, MallExceptionCode.SUBJECT_NOT_FOUND);
        //校验优惠券状态
//        if (subjectDB.getFsubjectStatus() != 2) {
//            log.info("主题不是已发布状态, id:{}, status:{}", subject.getFsubjectId(), subject.getFsubjectStatus());
//            return;
//        }
        //校验主题Alias是否存在
        String aliasName = getSubjectAliasName(subjectDB.getFsubjectId());
        AliasActions action = new AliasActions(AliasActions.Type.ADD).alias(aliasName);
        if (esManager.isAliasExist(aliasName)) {
            AliasActions deleteAction = new AliasActions(AliasActions.Type.REMOVE).alias(aliasName);
            esManager.updateAlias(deleteAction);
        }
        this.setActionFilter(subjectDB, action);
        esManager.updateAlias(action);
    }

    @Override
    public void deleteCouponInfoFromEsByAlias(Subject subject) {
        try {
            Long fsubjectId = subject.getFsubjectId();
            String aliasName = this.getSubjectAliasName(fsubjectId);
            if (esManager.isAliasExist(aliasName)) {
                AliasActions deleteAction = new AliasActions(AliasActions.Type.REMOVE).alias(aliasName);
                esManager.updateAlias(deleteAction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据优惠券id查询指定可用skuId
     *
     * @param coupon
     * @return
     */
    private List<String> getApplicableSkuIds(Subject subject) {

        Criteria<SubjectApplicableSku, Object> criteria = Criteria.of(SubjectApplicableSku.class)
                .fields(SubjectApplicableSku::getFskuId).andEqualTo(SubjectApplicableSku::getFsubjectId, subject.getFsubjectId()).andEqualTo(SubjectApplicableSku::getFsubjectFloorId,0);

        Result<List<SubjectApplicableSku>> skuResult = subjectApplicableSkuApi.queryByCriteria(criteria);
        List<SubjectApplicableSku> subjectApplicableSkus = ResultUtils.getData(skuResult);
        if (CollectionUtil.isNotEmpty(subjectApplicableSkus)) {
            List<String> skuIds = subjectApplicableSkus.stream().map(SubjectApplicableSku::getFskuId).map(String::valueOf).collect(toList());
            return skuIds;
        }
        return Lists.newArrayList();
    }


    /**
     * @param coupon
     * @param action
     */
    private void setActionFilter(Subject subject, AliasActions action) {
        //全部商品可用
        if (subject.getFapplicableSku().equals(CouponApplicableSkuEnum.ALL.getCode())) {
            return;
        }
        //指定商品可用
        if (subject.getFapplicableSku().equals(CouponApplicableSkuEnum.SOME.getCode())) {
            //指定sku
            List<String> applicableSkuIds = this.getApplicableSkuIds(subject);
            if (CollectionUtils.isNotEmpty(applicableSkuIds)) {
                IdsQueryBuilder queryBuilder = new IdsQueryBuilder();
                queryBuilder.addIds(EsCriteria.listToArray(applicableSkuIds));
                action.filter(queryBuilder);
                return;
            }
            //指定sku条件
            List<SubjectSkuQueryDto> conditionList = this.getApplicableSkuCondition(subject);
            Ensure.that(!CollectionUtils.isEmpty(conditionList)).isTrue(MallExceptionCode.SYSTEM_ERROR);
            DisMaxQueryBuilder orConditions = new DisMaxQueryBuilder();
            for (SubjectSkuQueryDto skuQueryDto : conditionList) {
                BoolQueryBuilder condition = new BoolQueryBuilder();

                List<Long> oneLevel = Lists.newArrayList();
                List<Long> twoLevel = Lists.newArrayList();
                List<Long> threeLevel = Lists.newArrayList();
                if (skuQueryDto.getCategoryIds() != null) {
                    oneLevel = skuQueryDto.getCategoryIds().get("1");
                    twoLevel = skuQueryDto.getCategoryIds().get("2");
                    threeLevel = skuQueryDto.getCategoryIds().get("3");
                }
                // 品牌id
                List<Long> brandIds = skuQueryDto.getBrandIds();
                // 标签id
                List<Long> labelIds = skuQueryDto.getLabelIds();
                // 贸易类型
                List<Long> tradeIds = skuQueryDto.getTradeIds();
                // sku code
                String skuCode = skuQueryDto.getSkuCode();
                // 商品id
                List<Long> goodsIds = null;
                if (CollectionUtils.isNotEmpty(tradeIds)) {
                    List<Goods> goods = getGoodsByTradeIds(tradeIds);
                    goodsIds = goods.stream().map(Goods::getFgoodsId).collect(Collectors.toList());
                }
                // 分类间或关系开始
                if (CollectionUtils.isNotEmpty(oneLevel) || CollectionUtils.isNotEmpty(twoLevel) || CollectionUtils.isNotEmpty(threeLevel)) {
                    DisMaxQueryBuilder orCategory = new DisMaxQueryBuilder();
                    if (CollectionUtils.isNotEmpty(oneLevel)) {
                        orCategory.add(QueryBuilders.termsQuery("fcategory_id1", oneLevel));
                    }
                    if (CollectionUtils.isNotEmpty(twoLevel)) {
                        orCategory.add(QueryBuilders.termsQuery("fcategory_id2", twoLevel));
                    }
                    if (CollectionUtils.isNotEmpty(threeLevel)) {
                        orCategory.add(QueryBuilders.termsQuery("fcategory_id3", threeLevel));
                    }
                    condition.must(orCategory);
                }
                //品牌
                if (CollectionUtils.isNotEmpty(brandIds)) {
                    condition.must(QueryBuilders.termsQuery("fbrand_id", brandIds));
                }
                //标签
                if (CollectionUtils.isNotEmpty(labelIds)) {
                    condition.must(QueryBuilders.termsQuery("flabel_id", labelIds));
                }
                //商品id
                if (CollectionUtils.isNotEmpty(goodsIds)) {
                    condition.must(QueryBuilders.termsQuery("fgoods_id", goodsIds));
                }
                orConditions.add(condition);
            }
            action.filter(orConditions);
        }

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
        Result<List<Goods>> goodsResult = goodsApi.queryByCriteria(goodsObjectCriteria);
        if (!goodsResult.isSuccess()) {
            throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        }
        return goodsResult.getData();
    }


    private List<SubjectSkuQueryDto> getApplicableSkuCondition(Subject subject) {
        List<SubjectSkuQueryDto> subjectSkuQueryDtos = Lists.newArrayList();

        Result<List<SubjectApplicableSkuCondition>> conditionResult = subjectApplicableSkuConditionApi.queryByCriteria(Criteria.of(SubjectApplicableSkuCondition.class)
                .andEqualTo(SubjectApplicableSkuCondition::getFsubjectId, subject.getFsubjectId()));

        Ensure.that(conditionResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
        List<SubjectApplicableSkuCondition> conditionList = conditionResult.getData();
        if (CollectionUtils.isEmpty(conditionList)) {
            return subjectSkuQueryDtos;
        }
        conditionList.stream().forEach(couponApplicableSkuCondition -> {
            //对应字段转成java object
            SubjectSkuQueryDto subjectSkuQueryDto = new SubjectSkuQueryDto();
            parseJsonAndSetFields(subjectSkuQueryDto, couponApplicableSkuCondition);
            subjectSkuQueryDtos.add(subjectSkuQueryDto);
        });
        return subjectSkuQueryDtos;
    }

    private void parseJsonAndSetFields(SubjectSkuQueryDto subjectSkuQueryDto, SubjectApplicableSkuCondition subjectApplicableSkuCondition) {
        try {
            subjectSkuQueryDto.setCategoryIds(JSON.parseObject(subjectApplicableSkuCondition.getFcategoryId(), Map.class));
            subjectSkuQueryDto.setBrandIds(JSON.parseArray(subjectApplicableSkuCondition.getFbrandId(), Long.class));
            subjectSkuQueryDto.setLabelIds(JSON.parseArray(subjectApplicableSkuCondition.getFlabelId(), Long.class));
            subjectSkuQueryDto.setTradeIds(JSON.parseArray(subjectApplicableSkuCondition.getFtradeCode(), Long.class));
        } catch (JSONException e) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getSubjectAliasName(Long fsubjectId) {
        if (Objects.isNull(fsubjectId)) {
            throw new IllegalArgumentException("优惠券id不能为空");
        }
        return SUBJECT_ALIAS_PREFIX + fsubjectId;
    }
}