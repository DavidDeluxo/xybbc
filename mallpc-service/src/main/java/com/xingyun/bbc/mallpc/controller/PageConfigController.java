package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.vo.pageconfig.ModuleVo;
import com.xingyun.bbc.mallpc.model.vo.pageconfig.PageConfigPcVo;
import com.xingyun.bbc.mallpc.service.PageConfigPcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("pageconfig")
@Slf4j
public class PageConfigController {
    @Resource
    private PageConfigPcService pageConfigPcService;

    /**
     * 导航静态文件缓存 前端获取数据接口
     *
     * @return
     */
    @GetMapping("/navigation")
    public Result<List<PageConfigPcVo>> navigation() {
        log.info("请求pcmall首页导航内容");
        return Result.success(pageConfigPcService.navigation());
    }

    @GetMapping("/module")
    public Result<List<ModuleVo>> PageConfigPcQuery() {
        return Result.success(pageConfigPcService.selectModules());
    }


}
