package com.xingyun.bbc.mallpc.common.controller;


import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.helper.api.FdfsApi;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.ensure.EnsureHelper;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * @author pengaoluo
 * @version 1.0.0
 */
@Api(tags = "公共接口")
@RestController
@RequestMapping("/common")
public class CommonController {

    /**
     * 允许上传的数据类型
     */
    private static final Collection<String> SUPPORT_TYPE = Collections.unmodifiableCollection(Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "rar", "zip", "tar"));

    @Resource
    private FdfsApi fdfsApi;

    @ApiOperation("上传图片")
    @PostMapping("/uploadPic")
    public Result<String> uploadPic(@RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            throw new BizException(MallPcExceptionCode.PARAM_ERROR);
        }
        return Result.success(EnsureHelper.checkNotNullAndGetData(fdfsApi.uploadFile(file), MallPcExceptionCode.UPLOAD_FAILED));
    }

    /**
     * 上传文件
     *
     * @param request
     * @param buffer
     * @return
     * @throws IOException
     */
    @ApiOperation("上传文件")
    @PostMapping("/uploadFile")
    public Result<String> uploadFile(HttpServletRequest request, @RequestBody byte[] buffer) {
        String suffix = request.getHeader("suffix");
        Ensure.that(suffix).isNotNull(MallPcExceptionCode.PARAM_ERROR);
        this.check(suffix);
        Result<String> result = fdfsApi.upFile(suffix, buffer);
        return Result.success(EnsureHelper.checkNotNullAndGetData(result, MallPcExceptionCode.UPLOAD_FAILED));
    }


    @ApiOperation("删除已上传图片")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "delete", dataType = "String", name = "filePath", value = "文件路径", required = true)})
    @PostMapping("/deleteFile")
    public Result<String> deleteFile(@RequestParam("filePath") String filePath) {
        if (filePath.isEmpty()) {
            throw new BizException(MallPcExceptionCode.PARAM_ERROR);
        }
        Result result = fdfsApi.deleteFile(filePath);
        return result;
    }

    private void check(String suffix) {
        Ensure.that(SUPPORT_TYPE.contains(suffix.toLowerCase())).isTrue(MallPcExceptionCode.UPLOAD_FILE_TYPE_ERROR);
    }

}
