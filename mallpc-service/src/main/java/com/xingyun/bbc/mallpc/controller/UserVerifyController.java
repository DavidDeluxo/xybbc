package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.core.utils.StringUtil;
import com.xingyun.bbc.mallpc.common.constants.MallPcConstants;
import com.xingyun.bbc.mallpc.common.utils.BizHelper;
import com.xingyun.bbc.mallpc.model.dto.user.UserVerifyDTO;
import com.xingyun.bbc.mallpc.model.vo.user.UserVerifyVO;
import com.xingyun.bbc.mallpc.service.UserVerifyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
@Api("认证信息")
@RestController
@RequestMapping("/my/userVerify")
@Slf4j
public class UserVerifyController {

    @Resource
    private UserVerifyService userVerifyService;

    @ApiOperation("添加用户认证")
    @PostMapping("/verify")
    public Result verify(@RequestBody @Valid UserVerifyDTO dto) {
        checkVerify(dto);
        userVerifyService.verify(dto);
        return Result.success();
    }

    private void checkVerify(UserVerifyDTO userVerifyDTO){
        Integer foperateType = userVerifyDTO.getFoperateType();
        //根据认证类型不同,必填字段也不同
        switch (foperateType) {
            //1实体门店
            case MallPcConstants.FOPERATE_TYPE_PHYSICAL_STORE:
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFshopName()), "门店名称不能为空");
                Assert.isTrue(BizHelper.isNotLogicNull(userVerifyDTO.getFshopProvinceId()), "省份不能为空");
                Assert.isTrue(BizHelper.isNotLogicNull(userVerifyDTO.getFshopCityId()), "城市不能为空");
                Assert.isTrue(BizHelper.isNotLogicNull(userVerifyDTO.getFshopAreaId()), "区域不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFshopAddress()), "详细地址不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFbusinessLicensePic()), "营业执照照片不能为空");
                //相对于运营中台增加的
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFshopFront()), "店铺门头照片不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFshopInside()), "店铺实景照片不能为空");
                break;
            //2网络店铺
            case MallPcConstants.FOPERATE_TYPE_NETWORK_STORE:
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFshopName()), "店铺名称不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFshopWeb()), "店铺网址不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFplatform()), "销售平台不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFidcardFront()), "身份证正面照不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFidcardBack()), "身份证背面照不能为空");
                break;
            //3网络平台
            case MallPcConstants.FOPERATE_TYPE_NETWORK_PLATFORM:
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFshopName()), "平台名称不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFshopWeb()), "平台网址不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFcompanyName()), "企业名称不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFbusinessLicensePic()), "营业执照照片不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFidcardFront()), "身份证正面照不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFidcardBack()), "身份证背面照不能为空");
                break;
            //4批采企业
            case MallPcConstants.FOPERATE_TYPE_BATCH_MINING_ENTERPRISES:
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFcompanyName()), "企业名称不能为空");
                Assert.isTrue(BizHelper.isNotLogicNull(userVerifyDTO.getFshopProvinceId()), "省份不能为空");
                Assert.isTrue(BizHelper.isNotLogicNull(userVerifyDTO.getFshopCityId()), "城市不能为空");
                Assert.isTrue(BizHelper.isNotLogicNull(userVerifyDTO.getFshopAreaId()), "区域不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFshopAddress()), "详细地址不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFbusinessLicensePic()), "营业执照照片不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFidcardFront()), "身份证正面照不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFidcardBack()), "身份证背面照不能为空");
                break;
            //5微商代购
            case MallPcConstants.FOPERATE_TYPE_MICRO_SHOPPING_AGENT:
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFshopName()), "微商名称不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFidcardNo()), "身份证号码不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFidcardFront()), "身份证正面照不能为空");
                Assert.isTrue(StringUtil.isNotEmpty(userVerifyDTO.getFidcardBack()), "身份证背面照不能为空");
                break;
            default:
        }
    }

    @ApiOperation("用户认证信息详情")
    @PostMapping("/view")
    public Result<UserVerifyVO> view() {
        return Result.success(userVerifyService.view());
    }
}
