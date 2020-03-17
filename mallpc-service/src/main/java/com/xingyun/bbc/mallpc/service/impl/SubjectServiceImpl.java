package com.xingyun.bbc.mallpc.service.impl;

import com.google.common.collect.Lists;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.operate.api.SubjectApi;
import com.xingyun.bbc.core.operate.api.SubjectApplicableSkuApi;
import com.xingyun.bbc.core.operate.api.SubjectFloorApi;
import com.xingyun.bbc.core.operate.po.Subject;
import com.xingyun.bbc.core.operate.po.SubjectApplicableSku;
import com.xingyun.bbc.core.operate.po.SubjectFloor;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.*;
import com.xingyun.bbc.core.sku.po.*;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.utils.PriceUtil;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.dto.search.SearchItemDto;
import com.xingyun.bbc.mallpc.model.dto.subject.SubjectQueryDto;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;
import com.xingyun.bbc.mallpc.model.vo.subject.ChildSubjectVo;
import com.xingyun.bbc.mallpc.model.vo.subject.SubjectVo;
import com.xingyun.bbc.mallpc.service.GoodsService;
import com.xingyun.bbc.mallpc.service.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class SubjectServiceImpl implements SubjectService {

    public static final Logger logger = LoggerFactory.getLogger(SubjectServiceImpl.class);

    @Resource
    SubjectApi subjectApi;

    @Resource
    CouponApi couponApi;

    @Resource
    GoodsService goodsService;

    @Resource
    SubjectFloorApi subjectFloorApi;

    @Resource
    private DozerHolder dozerHolder;

    @Resource
    private SubjectApplicableSkuApi subjectApplicableSkuApi;

    @Resource
    private GoodsSkuApi goodsSkuApi;

    @Resource
    private GoodsTradeInfoApi goodsTradeInfoApi;

    @Resource
    private SkuBatchApi skuBatchApi;

    @Resource
    private GoodsApi goodsApi;

    @Resource
    private SkuBatchPackageApi skuBatchPackageApi;

    @Resource
    private SkuUserDiscountConfigApi skuUserDiscountConfigApi;

    @Resource
    private SkuBatchUserPriceApi skuBatchUserPriceApi;

    @Resource
    private GoodsSkuBatchPriceApi goodsSkuBatchPriceApi;

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
        if (subject.getFsubjectContentType().equals(3)) {
            Coupon coupon = ResultUtils.getDataNotNull(couponApi.queryById(subject.getFcouponId()));
            subject.setFsubjectName(coupon.getFcouponName());
            subject.setFsubjectStatus(2);
            subject.setFsubjectMobileLayout(3);
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
        SubjectVo subjectVo = getById(subjectQueryDto);
        if (null == subjectVo) {
            return pageVo;
        }
        if (subjectVo.getFsubjectContentType().equals(3)) {
            subjectQueryDto.setFsubjectId(null);
            subjectQueryDto.setCouponId(subjectVo.getFcouponId());
        }
        try {
            return goodsService.searchSkuList(dozerHolder.convert(subjectQueryDto, SearchItemDto.class)).getData();
        } catch (Exception e) {
//            logger.info("es 查询专题id[{}]商品失败，转查数据库", subjectQueryDto.getFsubjectId());
//            getSubjectSkuIdList(subjectQueryDto, pageVo);
            return pageVo;
        }
    }

    @Override
    public SearchItemListVo<ChildSubjectVo> getChildSubject(SubjectQueryDto subjectQueryDto) {
        SearchItemListVo<ChildSubjectVo> pageVo = new SearchItemListVo<>();
        pageVo.setIsLogin(subjectQueryDto.getIsLogin());
        pageVo.setTotalCount(0);
        pageVo.setCurrentPage(subjectQueryDto.getPageIndex());
        pageVo.setPageSize(subjectQueryDto.getPageSize());

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
        if (null == subjectVo) {
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
            //---------------------------------------
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

    private GoodsTradeInfo getGoodsTradeInfo(Long ftradeId) {
        return ResultUtils.getData(goodsTradeInfoApi.queryById(ftradeId));
    }

    private Goods getGoods(Long fgoodsId) {
        return ResultUtils.getData(goodsApi.queryById(fgoodsId));
    }

    private List<SkuBatchPackage> getSkuBatchPackageList(SkuBatch sb) {
        SkuBatchPackage sbp = new SkuBatchPackage();
        sbp.setFsupplierSkuBatchId(sb.getFsupplierSkuBatchId());
        return ResultUtils.getData(skuBatchPackageApi.queryList(sbp));
    }

    private List<SkuBatch> getSkuBatchList(Long fskuId) {
        SkuBatch skuBatch = new SkuBatch();
        skuBatch.setFskuId(fskuId);
        skuBatch.setFbatchStatus(2);
        return ResultUtils.getData(skuBatchApi.queryList(skuBatch));
    }
}
