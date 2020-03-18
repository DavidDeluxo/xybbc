package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.core.sku.po.Goods;
import com.xingyun.bbc.core.sku.po.GoodsSku;
import com.xingyun.bbc.core.sku.po.SkuBatch;
import com.xingyun.bbc.core.sku.po.SkuBatchPackage;
import com.xingyun.bbc.mallpc.model.dto.shoppingcart.ImportShoppingCartExcelDto;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ImportDetailVo;

import java.util.List;

public interface ImportShoppingCartService {

    /**
     * 校验导入商品业务数据是否满足
     *
     * @param importShoppingCartExcelDto
     * @return 返回业务异常信息
     */
    String checkSkuBusiness(ImportShoppingCartExcelDto importShoppingCartExcelDto, List<GoodsSku> goodsSkuList, List<Goods> goodsList, List<SkuBatch> skuBatchList, List<SkuBatchPackage> skuBatchPackageList);

    /**
     * 校验字段非空
     *
     * @param importShoppingCartExcelDto
     * @return
     */
    String checkParam(ImportShoppingCartExcelDto importShoppingCartExcelDto);

    /**
     * 获取所有sku
     *
     * @param importShoppingCartExcelDtoList
     * @return
     */
    void getAllGoodsSkuInfo(List<ImportShoppingCartExcelDto> importShoppingCartExcelDtoList, List<GoodsSku> goodsSkuList, List<Goods> goodsList, List<SkuBatch> skuBatchList, List<SkuBatchPackage> skuBatchPackageList);

    /**
     * 校验省市区
     *
     * @param importShoppingCartExcelDto
     * @return
     */
    String checkAddress(ImportShoppingCartExcelDto importShoppingCartExcelDto);

    /**
     * 导入的进货单存储到redis
     *
     * @param detailVoList
     * @return
     */
    String saveImportData(List<ImportDetailVo> detailVoList);
}
