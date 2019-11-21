package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.recharge.RechargeSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
@Api("充值")
@RestController
@RequestMapping("/recharge")
public class RechargeController {

    @ApiOperation("提交充值")
    public Result<?> rechargeSubmit(@RequestBody @Valid RechargeSubmitVO dto){
return null;
    }
}
