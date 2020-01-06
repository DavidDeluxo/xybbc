package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.vo.VersionVo;
import com.xingyun.bbc.mall.service.AppVersionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 版本控制
 */
@RestController
@RequestMapping("/verInfo/via")
public class AppVersionController {

    @Resource
    private AppVersionService appVersionService;

    @GetMapping()
    public Result<VersionVo> query(@RequestParam Integer fplatform, @RequestParam String fversion) {
        return Result.success(appVersionService.getAppVersionInfo(fplatform, fversion));
    }

}
