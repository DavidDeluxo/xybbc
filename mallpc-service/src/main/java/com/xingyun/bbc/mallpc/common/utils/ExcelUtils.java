package com.xingyun.bbc.mallpc.common.utils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.handler.inter.IExcelExportServer;
import cn.hutool.core.util.StrUtil;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.mallpc.common.constants.MallPcConstants;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.model.dto.PageDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;

/**
 * @author nick
 * @ClassName: ExcelUtils
 * @Description: Excel 操作工具类
 * @date 2019年08月22日 12:51:36
 */
@Slf4j
public class ExcelUtils {

    /**
     * 导出一个空内容的Excel
     *
     * @param response
     * @param workbook excel对象
     * @param fileName 文件名入参时不包含后缀
     * @throws Exception
     */
    public static void exportEmptyContentExcel(HttpServletResponse response, Workbook workbook, String fileName) {
        try {
            //response重置之前需要判断是否已经committed，避免response throw new IllegalStateException("Committed")
            if (!response.isCommitted()) {
                response.reset();
            }
            response.setHeader("content-Type", "application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", StringUtils.join("attachment;filename=",
                    StrUtil.str(StrUtil.bytes(fileName, "utf-8"), "iso8859-1"), ".xls"));
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        } catch (Throwable e) {
            log.error("导出{}出错", fileName, e);
            throw new BizException(MallPcExceptionCode.SYSTEM_ERROR);
        }
    }

    /**
     * 通过easypoi导出excel文件
     *
     * @param fileName
     * @param pageDto
     * @param voClass
     * @param excelExportServer
     * @param response
     */
    public static void exportExcelByEasyPoi(String fileName, PageDto pageDto, Class<?> voClass,
                                            IExcelExportServer excelExportServer, HttpServletResponse response) {
        try {
            //每次查询1千的数据量
            pageDto.setPageSize(MallPcConstants.EASYPOI_EXPORT_EXCEL_APPEND_OFFSET);
            try (Workbook workbook = ExcelExportUtil.exportBigExcel(new ExportParams(null, fileName), voClass, excelExportServer, pageDto)) {
                response.setHeader("content-Type", "application/vnd.ms-excel;charset=utf-8");
                response.setHeader("Content-Disposition", StringUtils.join("attachment;filename=",
                        StrUtil.str(StrUtil.bytes(fileName, "utf-8"), "iso8859-1"), ".xls"));
                try (ServletOutputStream outputStream = response.getOutputStream()) {
                    workbook.write(outputStream);
                }
            }
        } catch (Throwable e) {
            log.error("导出{}出错", fileName, e);
            throw new BizException(MallPcExceptionCode.SYSTEM_ERROR);
        }
    }

    public static void createExcelByEasyPoi(String path, String fileName, PageDto pageDto, Class<?> voClass, IExcelExportServer excelExportServer) {
        try {
            //每次查询1千的数据量
            pageDto.setPageSize(MallPcConstants.EASYPOI_EXPORT_EXCEL_APPEND_OFFSET);
            try (Workbook workbook = ExcelExportUtil.exportBigExcel(new ExportParams(null, fileName), voClass, excelExportServer, pageDto)) {
                try (FileOutputStream output = new FileOutputStream(path + fileName + ".xlsx")) {
                    workbook.write(output);
                }
            }
        } catch (Throwable e) {
            log.error("生成文件{}出错", fileName, e);
            throw new BizException(MallPcExceptionCode.SYSTEM_ERROR);
        }
    }

    /**
     * 判断是否为excel文件
     *
     * @param file
     * @return
     */
    public static boolean isExcel(MultipartFile file) {
        return isExcel2003(file) || isExcel2007(file);
    }

    /**
     * 判断上传的EXCEL是否2003版本
     *
     * @param file
     * @return
     */
    public static boolean isExcel2003(MultipartFile file) {
        try {
            new HSSFWorkbook(file.getInputStream());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 判断上传的EXCEL是否2007版本
     *
     * @param file
     * @return
     */
    public static boolean isExcel2007(MultipartFile file) {
        try {
            new XSSFWorkbook(file.getInputStream());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获取对应的EXCEL文件的workbook
     *
     * @param file 文件
     * @return 返回文件对应的WORKBOOK
     */
    public static Workbook getWorkbook(MultipartFile file) {
        Workbook book = null;
        try {
            book = new XSSFWorkbook(file.getInputStream());
        } catch (Exception ex) {
            try {
                book = new HSSFWorkbook(file.getInputStream());
            } catch (Exception e) {
            }
        }
        return book;
    }


}
