package com.xingyun.bbc.mall.common.controller;


import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.helper.api.FdfsApi;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.core.utils.StringUtil;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.enums.MallResultStatus;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.vo.FileInfo;
import com.xingyun.bbc.mall.service.UserAddressService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * @author
 * @Description: 公用接口
 * @createTime: 2019-08-26 18:35
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private FdfsApi fdfsApi;
    @Autowired
    private UserAddressService userAddressService;

    @ApiOperation("上传图片")
    @PostMapping("/uploadPic")
    public Result<String> uploadPic(@RequestParam MultipartFile file){

        if (file.isEmpty()) throw new BizException(MallExceptionCode.PARAM_ERROR);

        Result<String> result = fdfsApi.uploadFile(file);

        return this.uploadResult(result);
    }


    /**
     * 上传文件
     * @param request
     * @param buffer
     * @return
     * @throws IOException
     */
    @ApiOperation("上传文件")
    @PostMapping("/uploadFile")
    public Result<String> uploadFile(HttpServletRequest request, @RequestBody byte[] buffer) {

        String suffix = request.getHeader("suffix");

        Ensure.that(buffer).isNotNull(MallExceptionCode.PARAM_ERROR);
        Ensure.that(suffix).isNotNull(MallExceptionCode.PARAM_ERROR);

        this.check(suffix);

        Result<String> result = fdfsApi.upFile(suffix,buffer);

        return this.uploadResult(result);
    }


    @ApiOperation("删除已上传图片")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "delete", dataType = "String", name = "filePath", value = "文件路径", required = true)})
    @PostMapping("/deleteFile")
    public Result<String> deleteFile(@RequestParam("filePath") String filePath){
        if (filePath.isEmpty()) throw new BizException(MallExceptionCode.PARAM_ERROR);
        Result result = fdfsApi.deleteFile(filePath);
        return result;
    }


    private Result<String> uploadResult(Result<String> result) {
        if (!result.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        Object data = result.getData();
        if (data == null || StringUtil.isEmpty(data + "")) {
            throw new BizException(MallResultStatus.UPLOAD_FAILED);
        }
        return Result.success(data + "");
    }

    private void check(String suffix) {
        suffix = suffix.toLowerCase();
        String [] types = {"jpg", "jpeg", "png", "gif", "pdf", "rar", "zip", "tar"};
        List<String> supportType = Arrays.asList(types);

        if (!supportType.contains(suffix)) {
            throw new BizException(MallResultStatus.UPLOAD_FILE_TYPE_ERROR);
        }

    }

    @ApiOperation(value = "查询基本文件信息", httpMethod = "POST")
    @PostMapping("/via/getFileInfo")
    public Result<FileInfo> getAddressFileInfo(HttpServletRequest req){
        FileInfo fileInfo = new FileInfo();
        fileInfo.setAddressFileInfo(userAddressService.getAddressFileInfo());
        return Result.success(fileInfo);
    }

}
