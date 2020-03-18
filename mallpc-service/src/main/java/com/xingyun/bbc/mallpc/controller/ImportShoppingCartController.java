package com.xingyun.bbc.mallpc.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.google.common.collect.Lists;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.helper.api.FdfsApi;
import com.xingyun.bbc.core.sku.po.Goods;
import com.xingyun.bbc.core.sku.po.GoodsSku;
import com.xingyun.bbc.core.sku.po.SkuBatch;
import com.xingyun.bbc.core.sku.po.SkuBatchPackage;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.*;
import com.xingyun.bbc.mallpc.model.dto.shoppingcart.ImportShoppingCartExcelDto;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ImportDetailVo;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ImportShoppingCartExcelVo;
import com.xingyun.bbc.mallpc.service.ImportShoppingCartService;
import com.xingyun.bbc.order.api.ShopcarImportApi;
import com.xingyun.bbc.order.model.dto.shopcartimport.ShopcarImportDeleteDto;
import com.xingyun.bbc.order.model.dto.shopcartimport.ShopcarImportUpdateDeliveryDto;
import com.xingyun.bbc.order.model.dto.shopcartimport.ShopcarImportUpdateDto;
import com.xingyun.bbc.order.model.vo.shopcartimport.ShopcarImportListVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/16 10:15
 * @description: 导入进货单
 * @package com.xingyun.bbc.mallpc.controller
 */
@Slf4j
@RestController
@RequestMapping(value = "/importShoppingCart")
public class ImportShoppingCartController {

    private static final String ALLOWED_REPEAT_ORDER_NO = "allowedRepeatOrderNo";

    private static final Integer IMPORT_MAX_SIZE = 100;

    @Resource
    private DozerHolder dozerHolder;

    @Resource
    private FdfsApi fdfsApi;

    @Resource
    private ImportShoppingCartService importShoppingCartService;

    @Resource
    private ShopcarImportApi shopcarImportApi;

    @ApiOperation("导入进货单")
    @PostMapping(value = "/import")
    public Result<ImportShoppingCartExcelVo> importShoppingCart(@RequestBody byte[] fileByte, HttpServletRequest request) {
        Long fuid = RequestHolder.getUserId();
        String allowedRepeatOrderNo = request.getHeader(ALLOWED_REPEAT_ORDER_NO);
        ImportShoppingCartExcelVo result = new ImportShoppingCartExcelVo();
        Integer correctCount = 0;
        Integer errorCount = 0;
        Integer fileSize = 0;
        if (fileByte != null) {
            fileSize = fileByte.length;
        }
        if (fileSize.equals(Integer.valueOf(0))) {
            throw new BizException(MallPcExceptionCode.EXCEL_FILE_CONTENT_IS_EMPTY);
        }
        try {
            ImportParams importParams = new ImportParams();
            List<ImportShoppingCartExcelDto> importShoppingCartExcelDtoList = ExcelImportUtil.importExcel(new ByteArrayInputStream(fileByte), ImportShoppingCartExcelDto.class, importParams);

            if (importShoppingCartExcelDtoList.size() > IMPORT_MAX_SIZE) {
                throw new BizException(MallPcExceptionCode.FILE_SIZE_MAXIMUM.bulid(IMPORT_MAX_SIZE));
            }
            Integer userShopcartImport = ResultUtils.getDataNotNull(shopcarImportApi.selectUserShopcartImportCount(fuid));
            if (importShoppingCartExcelDtoList.size() > (IMPORT_MAX_SIZE - userShopcartImport)) {
                throw new BizException(MallPcExceptionCode.ORDER_OVER_ERROR.bulid(IMPORT_MAX_SIZE - userShopcartImport));
            }

            List<GoodsSku> goodsSkuList = new ArrayList<>();
            List<Goods> goodsList = new ArrayList<>();
            List<SkuBatch> skuBatchList = new ArrayList<>();
            List<SkuBatchPackage> skuBatchPackageList = new ArrayList<>();
            importShoppingCartService.getAllGoodsSkuInfo(importShoppingCartExcelDtoList, goodsSkuList, goodsList, skuBatchList, skuBatchPackageList);

            List<ImportDetailVo> detailVoList = new ArrayList<>();
            for (ImportShoppingCartExcelDto importShoppingCartExcelDto : importShoppingCartExcelDtoList) {
                ImportDetailVo importDetailVo = dozerHolder.convert(importShoppingCartExcelDto, ImportDetailVo.class);
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append(importShoppingCartService.checkParam(importShoppingCartExcelDto));
                errorMsg.append(importShoppingCartService.checkSkuBusiness(importShoppingCartExcelDto, goodsSkuList, goodsList, skuBatchList, skuBatchPackageList));
                errorMsg.append(importShoppingCartService.checkAddress(importShoppingCartExcelDto));

                if (StringUtils.isEmpty(allowedRepeatOrderNo) && StringUtils.isNotEmpty(importShoppingCartExcelDto.getFplatformOrderNo()) && StringUtils.isNotEmpty(importShoppingCartExcelDto.getFskuCode())) {
                    errorMsg.append(checkRepeatOrderNo(importShoppingCartExcelDto.getFplatformOrderNo(), importShoppingCartExcelDtoList, importShoppingCartExcelDto.getFskuCode()));
                }
                if (StringUtils.isNotEmpty(importShoppingCartExcelDto.getFdeliveryCardUrlBack())) {
                    importDetailVo.setFdeliveryCardUrlBack(uploadPicToFdfs(importShoppingCartExcelDto.getFdeliveryCardUrlBack()));
                }
                if (StringUtils.isNotEmpty(importShoppingCartExcelDto.getFdeliveryCardUrlFront())) {
                    importDetailVo.setFdeliveryCardUrlFront(uploadPicToFdfs(importShoppingCartExcelDto.getFdeliveryCardUrlFront()));
                }
                if (StringUtils.isNotEmpty(importShoppingCartExcelDto.getFplatformOrderNo()) && ResultUtils.getDataNotNull(shopcarImportApi.checkPlatformOrderExist(importShoppingCartExcelDto.getFplatformOrderNo()))) {
                    errorMsg.append("\r\n");
                    errorMsg.append("该商家订单号已存在批量导入进货单中，请勿重复导入");
                }
                if (StringUtils.isNotEmpty(errorMsg)) {
                    errorCount++;
                    importDetailVo.setErrorMsg(errorMsg.toString());
                } else {
                    correctCount++;
                }
                detailVoList.add(importDetailVo);
            }
            result.setCorrectCount(correctCount);
            result.setErrorCount(errorCount);
            if (errorCount.equals(0)) {
                result.setTemporaryNo(importShoppingCartService.saveImportData(detailVoList));
            }
            result.setDetailVoList(detailVoList);
            return Result.success(result);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.info("导入进货单失败，e:{}", XyLogUtil.logStackTrace(e));
            throw new BizException(new MallPcExceptionCode("9994", "表格解析异常"));
        }
    }

    private String checkRepeatOrderNo(String platformOrderNo, List<ImportShoppingCartExcelDto> importShoppingCartExcelDtoList, String fskuCode) {
        List<ImportShoppingCartExcelDto> repeatList = importShoppingCartExcelDtoList.stream().filter(importShoppingCartExcelDto -> StringUtils.equals(platformOrderNo, importShoppingCartExcelDto.getFplatformOrderNo())
                && StringUtils.equals(fskuCode, importShoppingCartExcelDto.getFskuCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(repeatList) && repeatList.size() > 1) {
            return "商家平台单号:" + platformOrderNo + "重复";
        }
        return "";
    }

    @ApiOperation("下载导入模板")
    @GetMapping("/via/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        ExportParams exportParams = new ExportParams();
        exportParams.setType(ExcelType.HSSF);
        exportParams.setSheetName("进货单批量导入模板");
        try (Workbook workbook = ExcelExportUtil.exportExcel(exportParams, ImportShoppingCartExcelDto.class, Lists.newArrayList())) {
            ExcelUtils.exportEmptyContentExcel(response, workbook, "进货单批量导入模板");
        } catch (Exception e) {
            throw new BizException(MallPcExceptionCode.SYSTEM_ERROR);
        }
    }

    /**
     * 上传图片到fdfs
     *
     * @param picUrl
     * @return
     */
    private String uploadPicToFdfs(String picUrl) {
        File file = new File(picUrl);
        if (!file.exists()) {
            log.info("文件{}不存在", picUrl);
            return "";
        }
        String suffix = StringUtils.substringAfterLast(picUrl, ".").toLowerCase();
        return ResultUtils.getDataNotNull(fdfsApi.uploadFileBytes(suffix, FileUtils.file2byte(file)));
    }


    @ApiOperation("查询导入进货单列表")
    @PostMapping("/selectList")
    public Result<ShopcarImportListVo> selectShopcartImportList(HttpServletRequest request) {
        Long fuid = RequestHolder.getUserId();
        return shopcarImportApi.selectShopcartImportList(fuid);
    }

    @ApiOperation("更新购物车数量")
    @PostMapping("/updateNum")
    public Result<Boolean> updateNum(@RequestBody @Valid ShopcarImportUpdateDto shopcarImportUpdateDto) {
        Long fuid = RequestHolder.getUserId();
        shopcarImportUpdateDto.setFuid(fuid);
        return shopcarImportApi.updateNum(shopcarImportUpdateDto);
    }

    @ApiOperation("更换地址")
    @PostMapping("/updateDelivery")
    public Result<Boolean> updateDelivery(@RequestBody @Valid ShopcarImportUpdateDeliveryDto shopcarImportUpdateDeliveryDto) {
        return shopcarImportApi.updateDelivery(shopcarImportUpdateDeliveryDto);
    }

    @ApiOperation("删除导入购物车")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Valid ShopcarImportDeleteDto shopcarImportDeleteDto) {
        return shopcarImportApi.delete(shopcarImportDeleteDto);
    }
}