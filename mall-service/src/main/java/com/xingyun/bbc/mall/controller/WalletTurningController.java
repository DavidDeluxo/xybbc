package com.xingyun.bbc.mall.controller;


import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.UserWalletDetailDto;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.WalletTurningService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * @author lll
 * @Description: 我的钱包收支明细
 * @createTime: 2019-09-17 11:00
 */
@Validated
@Api("我的钱包收支明细")
@RestController
@RequestMapping("/wallet")
@Slf4j
public class WalletTurningController {
    public static final Logger logger = LoggerFactory.getLogger(WalletTurningController.class);

    @Autowired
    WalletTurningService walletTurningService;


    @ApiOperation(value = "查询钱包收支明细列表")
    @PostMapping("/queryWalletTurningList")
    public Result<PageVo<UserWalletDetailVo>> queryWalletTurningList(@RequestBody UserWalletDetailDto userWalletDetailDto, HttpServletRequest request) {
        userWalletDetailDto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return Result.success(walletTurningService.queryWalletTurningList(userWalletDetailDto));
    }


}
