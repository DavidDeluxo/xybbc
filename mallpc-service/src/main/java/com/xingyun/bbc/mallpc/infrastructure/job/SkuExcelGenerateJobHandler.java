package com.xingyun.bbc.mallpc.infrastructure.job;

import com.xingyun.bbc.core.user.enums.UserOperateType;
import com.xingyun.bbc.mallpc.common.utils.ExcelUtils;
import com.xingyun.bbc.mallpc.model.dto.sku.SaleSkuExportDto;
import com.xingyun.bbc.mallpc.model.vo.excel.sku.SaleSkuExportVo;
import com.xingyun.bbc.mallpc.service.SkuService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/3 19:55
 * @description: 在售sku定时生成excel定时任务
 * @package com.xingyun.bbc.mallpc.infrastructure.job
 */
@Component
@JobHandler("skuExcelGenerateJobHandler")
public class SkuExcelGenerateJobHandler extends IJobHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkuExcelGenerateJobHandler.class);

    @Resource
    private SkuService skuService;

    @Value("${tmpFile.path.saleSku}")
    private String saleSkuFile;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        saleSkuFile = saleSkuFile + File.separator;
        if (StringUtils.isNotEmpty(param)) {
            try {
                Integer foperateType = Integer.parseInt(param);
                String type = UserOperateType.getName(foperateType);
                if (StringUtils.isNotEmpty(type)) {
                    LOGGER.info("任务-在售sku定时生成excel ({}) 开始", type);
                    XxlJobLogger.log("任务-在售sku定时生成excel ({}) 开始", type);
                    ExcelUtils.createExcelByEasyPoi(saleSkuFile, foperateType.toString(), new SaleSkuExportDto(foperateType), SaleSkuExportVo.class, skuService);
                    LOGGER.info("任务-在售sku定时生成excel ({}) 结束", type);
                    XxlJobLogger.log("任务-在售sku定时生成excel ({}) 结束", type);
                } else {
                    LOGGER.info("任务-在售sku定时生成excel (普通会员) 开始");
                    XxlJobLogger.log("任务-在售sku定时生成excel (普通会员) 开始");
                    ExcelUtils.createExcelByEasyPoi(saleSkuFile, "0", new SaleSkuExportDto(0), SaleSkuExportVo.class, skuService);
                    LOGGER.info("任务-在售sku定时生成excel (普通会员) 结束");
                    XxlJobLogger.log("任务-在售sku定时生成excel (普通会员) 结束");
                }
                return SUCCESS;
            } catch (Exception e) {
                return FAIL;
            }
        }
        //非会员折扣
        LOGGER.info("任务-在售sku定时生成excel (普通会员) 开始");
        XxlJobLogger.log("任务-在售sku定时生成excel (普通会员) 开始");
        ExcelUtils.createExcelByEasyPoi(saleSkuFile, "0", new SaleSkuExportDto(0), SaleSkuExportVo.class, skuService);
        LOGGER.info("任务-在售sku定时生成excel (普通会员) 结束");
        XxlJobLogger.log("任务-在售sku定时生成excel (普通会员) 结束");

        //1实体门店
        LOGGER.info("任务-在售sku定时生成excel (实体门店) 开始");
        XxlJobLogger.log("任务-在售sku定时生成excel (实体门店) 开始");
        ExcelUtils.createExcelByEasyPoi(saleSkuFile, "1", new SaleSkuExportDto(1), SaleSkuExportVo.class, skuService);
        LOGGER.info("任务-在售sku定时生成excel (实体门店) 结束");
        XxlJobLogger.log("任务-在售sku定时生成excel (实体门店) 结束");

        //2网络店铺
        LOGGER.info("任务-在售sku定时生成excel (网络店铺) 开始");
        XxlJobLogger.log("任务-在售sku定时生成excel (网络店铺) 开始");
        ExcelUtils.createExcelByEasyPoi(saleSkuFile, "2", new SaleSkuExportDto(2), SaleSkuExportVo.class, skuService);
        LOGGER.info("任务-在售sku定时生成excel (网络店铺) 结束");
        XxlJobLogger.log("任务-在售sku定时生成excel (网络店铺) 结束");

        //3网络平台
        LOGGER.info("任务-在售sku定时生成excel (网络平台) 开始");
        XxlJobLogger.log("任务-在售sku定时生成excel (网络平台) 开始");
        ExcelUtils.createExcelByEasyPoi(saleSkuFile, "3", new SaleSkuExportDto(3), SaleSkuExportVo.class, skuService);
        LOGGER.info("任务-在售sku定时生成excel (网络平台) 结束");
        XxlJobLogger.log("任务-在售sku定时生成excel (网络平台) 结束");

        //4批采企业
        LOGGER.info("任务-在售sku定时生成excel (批采企业) 开始");
        XxlJobLogger.log("任务-在售sku定时生成excel (批采企业) 开始");
        ExcelUtils.createExcelByEasyPoi(saleSkuFile, "4", new SaleSkuExportDto(4), SaleSkuExportVo.class, skuService);
        LOGGER.info("任务-在售sku定时生成excel (批采企业) 结束");
        XxlJobLogger.log("任务-在售sku定时生成excel (批采企业) 结束");

        //5微商代购
        LOGGER.info("任务-在售sku定时生成excel (微商代购) 开始");
        XxlJobLogger.log("任务-在售sku定时生成excel (微商代购) 开始");
        ExcelUtils.createExcelByEasyPoi(saleSkuFile, "5", new SaleSkuExportDto(5), SaleSkuExportVo.class, skuService);
        LOGGER.info("任务-在售sku定时生成excel (微商代购) 结束");
        XxlJobLogger.log("任务-在售sku定时生成excel (微商代购) 结束");
        return SUCCESS;
    }
}