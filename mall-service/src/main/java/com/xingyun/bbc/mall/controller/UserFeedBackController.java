package com.xingyun.bbc.mall.controller;

import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.JwtParser;
import com.xingyun.bbc.mall.model.dto.UserFeedBackDto;
import com.xingyun.bbc.mall.model.vo.TokenInfoVo;
import com.xingyun.bbc.mall.model.vo.UserFeedBackVo;
import com.xingyun.bbc.mall.service.UserFeedBackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api("用户问题反馈")
@RestController
@RequestMapping("/userFeedBack")
public class UserFeedBackController {

    public static final Logger logger = LoggerFactory.getLogger(UserFeedBackController.class);

    @Resource
    private JwtParser jwtParser;

    @Resource
    private UserFeedBackService userFeedBackService;

    @ApiOperation(value = "获取用户问题反馈类型", httpMethod = "GET")
    @GetMapping("/via/getUserFeedBackType")
    public Result<List<UserFeedBackVo>> getUserFeedBackType(){
        return userFeedBackService.getUserFeedBackType();
    }

    @ApiOperation(value = "保存用户问题反馈信息", httpMethod = "POST")
    @PostMapping("/saveUserFeedBack")
    public Result saveUserFeedBack(@RequestBody @Validated UserFeedBackDto dto, HttpServletRequest request){
        TokenInfoVo tokenInfo = jwtParser.getTokenInfo(request);
        dto.setFuid(tokenInfo.getFuid().longValue());
//        dto.setFuid(1l);
        logger.info("保存用户问题反馈信息 {}", JSON.toJSONString(dto));
        return userFeedBackService.saveUserFeedBack(dto);
    }

}
