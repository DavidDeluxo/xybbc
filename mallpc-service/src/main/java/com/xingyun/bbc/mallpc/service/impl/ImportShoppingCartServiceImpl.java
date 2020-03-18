package com.xingyun.bbc.mallpc.service.impl;

import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.common.redis.XyRedisManager;
import com.xingyun.bbc.core.operate.api.CityRegionApi;
import com.xingyun.bbc.core.operate.enums.TradeTypeEnums;
import com.xingyun.bbc.core.operate.po.CityRegion;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.GoodsApi;
import com.xingyun.bbc.core.sku.api.GoodsSkuApi;
import com.xingyun.bbc.core.sku.api.SkuBatchApi;
import com.xingyun.bbc.core.sku.api.SkuBatchPackageApi;
import com.xingyun.bbc.core.sku.enums.GoodsSkuEnums;
import com.xingyun.bbc.core.sku.po.Goods;
import com.xingyun.bbc.core.sku.po.GoodsSku;
import com.xingyun.bbc.core.sku.po.SkuBatch;
import com.xingyun.bbc.core.sku.po.SkuBatchPackage;
import com.xingyun.bbc.core.utils.DateUtil;
import com.xingyun.bbc.core.utils.IdGenerator;
import com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.dto.shoppingcart.ImportShoppingCartExcelDto;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ImportDetailVo;
import com.xingyun.bbc.mallpc.service.ImportShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/16 15:21
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.service.impl
 */
@Slf4j
@Service
public class ImportShoppingCartServiceImpl implements ImportShoppingCartService {

    @Resource
    private GoodsApi goodsApi;

    @Resource
    private GoodsSkuApi goodsSkuApi;

    @Resource
    private SkuBatchApi skuBatchApi;

    @Resource
    private SkuBatchPackageApi skuBatchPackageApi;

    @Resource
    private CityRegionApi cityRegionApi;

    @Resource
    private XyRedisManager xyRedisManager;

    @Override
    public String checkSkuBusiness(ImportShoppingCartExcelDto importShoppingCartExcelDto, List<GoodsSku> goodsSkuList, List<Goods> goodsList, List<SkuBatch> skuBatchList, List<SkuBatchPackage> skuBatchPackageList) {
        StringBuilder errorMsg = new StringBuilder();
        if (StringUtils.isEmpty(importShoppingCartExcelDto.getFskuCode())) {
            return "";
        }
        String fskuCode = importShoppingCartExcelDto.getFskuCode();
        if (StringUtils.isEmpty(fskuCode)) {
            return "";
        }
        Optional<GoodsSku> goodsSkuOptional = goodsSkuList.stream().filter(t -> StringUtils.equals(fskuCode, t.getFskuCode())).findFirst();
        if (!goodsSkuOptional.isPresent()) {
            errorMsg.append("\r\n");
            errorMsg.append("该商品不存在");
            return errorMsg.toString();
        }
        GoodsSku goodsSku = goodsSkuOptional.get();
        if (!GoodsSkuEnums.Status.OnShelves.getValue().equals(goodsSku.getFskuStatus()) || !Integer.valueOf(1).equals(goodsSku.getFgoodStatus())) {
            errorMsg.append("\r\n");
            errorMsg.append("该商品未上架售卖");
            return errorMsg.toString();
        }
        Optional<Goods> goodsOptional = goodsList.stream().filter(t -> goodsSku.getFgoodsId().equals(t.getFgoodsId())).findFirst();
        if (goodsOptional.isPresent()) {
            Goods goods = goodsOptional.get();
            if (StringUtils.equals(TradeTypeEnums.Type.three.getTradeId(), String.valueOf(goods.getFtradeId()))) {
                //TODO 暂不支持下单保税商品
                errorMsg.append("\r\n");
                errorMsg.append("暂不支持下单保税商品");
                return errorMsg.toString();
            }
        }

        List<SkuBatch> suitSkuBatchList = skuBatchList.stream().filter(skuBatch -> StringUtils.equals(skuBatch.getFskuCode(), goodsSku.getFskuCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(suitSkuBatchList)) {
            errorMsg.append("\r\n");
            errorMsg.append("该商品没有售卖批次");
            return errorMsg.toString();
        }
        if (StringUtils.isNotEmpty(importShoppingCartExcelDto.getFqualityDate())) {
            Date qualityDate = DateUtil.parse(importShoppingCartExcelDto.getFqualityDate(), "yyyy-MM");
            List<SkuBatch> qualityDateSkuBatchList = suitSkuBatchList.stream().filter(skuBatch -> qualityDate.after(skuBatch.getFqualityStartDate()) && qualityDate.before(skuBatch.getFqualityEndDate())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(qualityDateSkuBatchList)) {
                errorMsg.append("\r\n");
                errorMsg.append("该商品不存在此效期");
                return errorMsg.toString();
            }
            if (StringUtils.isNotEmpty(importShoppingCartExcelDto.getFbatchPackageNum()) && StringUtils.isNotEmpty(importShoppingCartExcelDto.getFskuNum())) {
                return dealSkuBatch(errorMsg, importShoppingCartExcelDto, qualityDateSkuBatchList, skuBatchPackageList).toString();
            }
        } else {
            //没有填写效期-默认效期最差的
            if (StringUtils.isNotEmpty(importShoppingCartExcelDto.getFbatchPackageNum()) && StringUtils.isNotEmpty(importShoppingCartExcelDto.getFskuNum())) {
                return dealSkuBatch(errorMsg, importShoppingCartExcelDto, suitSkuBatchList, skuBatchPackageList).toString();
            }
        }
        return "";
    }

    private StringBuilder dealSkuBatch(StringBuilder errorMsg, ImportShoppingCartExcelDto importShoppingCartExcelDto, List<SkuBatch> qualityDateSkuBatchList, List<SkuBatchPackage> skuBatchPackageList) {
        Long batchPackageNum = Long.parseLong(importShoppingCartExcelDto.getFbatchPackageNum());
        Long fskuNum = Long.parseLong(importShoppingCartExcelDto.getFskuNum());
        List<SkuBatch> sortList = qualityDateSkuBatchList.stream().sorted(Comparator.comparing(SkuBatch::getFqualityEndDate)).collect(Collectors.toList());
        SkuBatchPackage finalSkuBatchPackage = null;
        for (SkuBatch skuBatch : sortList) {
            Optional<SkuBatchPackage> skuBatchPackageOptional = skuBatchPackageList.stream().filter(t -> batchPackageNum.equals(t.getFbatchPackageNum()) && StringUtils.equals(t.getFsupplierSkuBatchId(), skuBatch.getFsupplierSkuBatchId())).findFirst();
            if (skuBatchPackageOptional.isPresent()) {
                finalSkuBatchPackage = skuBatchPackageOptional.get();
            }
        }
        if (null == finalSkuBatchPackage) {
            errorMsg.append("\r\n");
            errorMsg.append("该商品不存在该包装规格");
        }
        if (fskuNum < finalSkuBatchPackage.getFbatchStartNum()) {
            errorMsg.append("\r\n");
            errorMsg.append("该商品该规格最少支持" + finalSkuBatchPackage.getFbatchStartNum() + "件起发");
        }
        String supplierSkuBatchId = finalSkuBatchPackage.getFsupplierSkuBatchId();
        SkuBatch finalSkuBatch = sortList.stream().filter(skuBatch -> StringUtils.equals(skuBatch.getFsupplierSkuBatchId(), supplierSkuBatchId)).findFirst().get();
        if (batchPackageNum * fskuNum > finalSkuBatch.getFstockRemianNum()) {
            errorMsg.append("\r\n");
            errorMsg.append("该商品购买数量超过剩余库存数量:" + finalSkuBatch.getFstockRemianNum());
        }
        return errorMsg;
    }

    @Override
    public String checkParam(ImportShoppingCartExcelDto importShoppingCartExcelDto) {
        StringBuilder errorMsg = new StringBuilder();
        Field[] fields = ImportShoppingCartExcelDto.class.getDeclaredFields();
        for (Field field : fields) {
            if (null == importShoppingCartExcelDto) {
                errorMsg.append("字段不能全为空");
                continue;
            }
            NotEmpty notEmpty = field.getAnnotation(NotEmpty.class);
            if (null == notEmpty) {
                continue;
            }
            String value = notEmpty.message();
            Object o = null;
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                o = field.get(importShoppingCartExcelDto);
            } catch (IllegalAccessException e) {
                log.error("{}非空校验失败", field.getName());
            }
            if (o == null) {
                errorMsg.append("\r\n");
                errorMsg.append(value);
            }
        }
        return errorMsg.toString();
    }

    @Override
    public void getAllGoodsSkuInfo(List<ImportShoppingCartExcelDto> importShoppingCartExcelDtoList, List<GoodsSku> goodsSkuList, List<Goods> goodsList, List<SkuBatch> skuBatchList, List<SkuBatchPackage> skuBatchPackageList) {
        List<String> allSkuCode = importShoppingCartExcelDtoList.stream().map(ImportShoppingCartExcelDto::getFskuCode).collect(Collectors.toList());
        allSkuCode.removeAll(Collections.singleton(null));
        if (CollectionUtils.isEmpty(allSkuCode) || allSkuCode.isEmpty()) {
            return;
        }
        Criteria<GoodsSku, Object> goodsSkuCriteria = Criteria.of(GoodsSku.class);
        //未删除的sku
        goodsSkuCriteria.andEqualTo(GoodsSku::getFisDelete, 0);
        goodsSkuCriteria.andIn(GoodsSku::getFskuCode, allSkuCode);
        goodsSkuList.addAll(ResultUtils.getData(goodsSkuApi.queryByCriteria(goodsSkuCriteria)));
        if (CollectionUtils.isEmpty(goodsSkuList)) {
            return;
        }
        List<Long> goodsIdList = goodsSkuList.stream().map(GoodsSku::getFgoodsId).collect(Collectors.toList());
        Criteria<Goods, Object> goodsCriteria = Criteria.of(Goods.class)
                .fields(Goods::getFgoodsId
                        , Goods::getFtradeId
                        , Goods::getFgoodsName);
        //只统计在售上架的sku 批次
        goodsCriteria.andIn(Goods::getFgoodsId, goodsIdList);
        goodsList.addAll(ResultUtils.getData(goodsApi.queryByCriteria(goodsCriteria)));

        List<String> skuCodeList = goodsSkuList.stream().map(GoodsSku::getFskuCode).collect(Collectors.toList());
        Criteria<SkuBatch, Object> skuBatchCriteria = Criteria.of(SkuBatch.class)
                .fields(SkuBatch::getFskuBatchId
                        , SkuBatch::getFsupplierSkuBatchId
                        , SkuBatch::getFskuCode
                        , SkuBatch::getFstockRemianNum
                        , SkuBatch::getFqualityEndDate
                        , SkuBatch::getFqualityStartDate);
        //只统计在售上架的sku 批次
        skuBatchCriteria.andIn(SkuBatch::getFskuCode, skuCodeList).andEqualTo(SkuBatch::getFbatchStatus, 2);
        skuBatchList.addAll(ResultUtils.getData(skuBatchApi.queryByCriteria(skuBatchCriteria)));
        //supplierSkuBatchId 集合
        List<String> supplierSkuBatchIdList = skuBatchList.stream().map(SkuBatch::getFsupplierSkuBatchId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuBatchList)) {
            return;
        }

        Criteria<SkuBatchPackage, Object> skuBatchPackageCriteria = Criteria.of(SkuBatchPackage.class)
                .fields(SkuBatchPackage::getFbatchPackageId
                        , SkuBatchPackage::getFbatchStartNum
                        , SkuBatchPackage::getFbatchPackageNum
                        , SkuBatchPackage::getFsupplierSkuBatchId);
        skuBatchPackageCriteria.andIn(SkuBatchPackage::getFsupplierSkuBatchId, supplierSkuBatchIdList).sort(SkuBatchPackage::getFbatchPackageNum);
        skuBatchPackageList.addAll(ResultUtils.getData(skuBatchPackageApi.queryByCriteria(skuBatchPackageCriteria)));
    }

    @Override
    public String checkAddress(ImportShoppingCartExcelDto importShoppingCartExcelDto) {
        StringBuilder errorMsg = new StringBuilder();
        String provinceName = importShoppingCartExcelDto.getFdeliveryProvinceName();
        String cityName = importShoppingCartExcelDto.getFdeliveryCityName();
        String areaName = importShoppingCartExcelDto.getFdeliveryAreaName();
        if (StringUtils.isNotEmpty(provinceName) && StringUtils.isNotEmpty(cityName) && StringUtils.isNotEmpty(areaName)) {
            Criteria<CityRegion, Object> provinceCriteria = Criteria.of(CityRegion.class)
                    .andEqualTo(CityRegion::getFregionType, 2)
                    .andLike(CityRegion::getFcrName, provinceName + "%")
                    .fields(CityRegion::getFregionId, CityRegion::getFcrName);
            List<CityRegion> provinceList = ResultUtils.getData(cityRegionApi.queryByCriteria(provinceCriteria));
            if (CollectionUtils.isEmpty(provinceList)) {
                errorMsg.append("\r\n");
                errorMsg.append("省:" + provinceName + "不存在");
                return errorMsg.toString();
            }
            List<Integer> provinceIdList = provinceList.stream().map(CityRegion::getFregionId).collect(Collectors.toList());
            Criteria<CityRegion, Object> cityCriteria = Criteria.of(CityRegion.class)
                    .andEqualTo(CityRegion::getFregionType, 3)
                    .andLike(CityRegion::getFcrName, cityName + "%")
                    .andIn(CityRegion::getFpRegionId, provinceIdList)
                    .fields(CityRegion::getFregionId, CityRegion::getFcrName);
            List<CityRegion> cityList = ResultUtils.getData(cityRegionApi.queryByCriteria(cityCriteria));
            if (CollectionUtils.isEmpty(cityList)) {
                errorMsg.append("\r\n");
                errorMsg.append("市:" + cityName + "不存在");
                return errorMsg.toString();
            }
            List<Integer> cityIdList = cityList.stream().map(CityRegion::getFregionId).collect(Collectors.toList());
            Criteria<CityRegion, Object> areaCriteria = Criteria.of(CityRegion.class)
                    .andEqualTo(CityRegion::getFregionType, 4)
                    .andLike(CityRegion::getFcrName, areaName + "%")
                    .andIn(CityRegion::getFpRegionId, cityIdList)
                    .fields(CityRegion::getFregionId, CityRegion::getFcrName);
            List<CityRegion> areaList = ResultUtils.getData(cityRegionApi.queryByCriteria(areaCriteria));
            if (CollectionUtils.isEmpty(areaList)) {
                errorMsg.append("\r\n");
                errorMsg.append("省市区:" + provinceName + cityName + areaName + "不存在");
                return errorMsg.toString();
            }
        }
        return errorMsg.toString();
    }

    @Override
    public String saveImportData(List<ImportDetailVo> detailVoList) {
        String temporaryNo = IdGenerator.INSTANCE.nextId();
        String redisKey = MallPcRedisConstant.IMPORT_SHOPPING_CART_NO_PREFIX + temporaryNo;
        xyRedisManager.set(redisKey, JSON.toJSONString(detailVoList), 2 * MallPcRedisConstant.EXPIRE_TIME_HOUR);
        return temporaryNo;
    }
}