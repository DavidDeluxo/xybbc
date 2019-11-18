package com.xingyun.bbc.mallpc.controller;


import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author lll
 * @Description: 首页
 * @createTime: 2019-09-03 11:00
 */
@Validated
@Api("首页配置")
@RestController
@RequestMapping("/index")
@Slf4j
public class IndexController {
//    public static final Logger logger = LoggerFactory.getLogger(IndexController.class);
//    @Autowired
//    private IndexService indexService;
//
//    @Autowired
//    CategoryService categoryService;
//
//
//    @Autowired
//    GoodsService goodsService;
//
//    @Resource
//    private DozerHolder holder;
//
//    @Autowired
//    JwtParser jwtParser;
//
//    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "fposition", value = "导航栏位置(0Banner配置 1ICON配置 2专题位配置", required = false)})
//    @ApiOperation(value = "查询首页配置", httpMethod = "POST")
//    @PostMapping("/via/getConfig")
//    public Result<List<PageConfigVo>> getConfig(@RequestParam(value = "fposition", required = true) Integer fposition) {
//        return indexService.getConfig(fposition);
//    }
//
//
//    @ApiOperation(value = "引导页启动页查询", httpMethod = "POST")
//    @ApiResponses({@ApiResponse(code = 200, response = GuidePageVo.class, message = "操作成功!")})
//    @PostMapping(value = "/via/selectGuidePage", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public Result<List<GuidePageVo>> selectGuidePageVos(@ApiParam(name = "引导页启动页查询", value = "传入json格式", required = false) @RequestBody GuidePageDto guidePageDto) {
//        return indexService.selectGuidePageVos(guidePageDto.getFtype());
//    }

}
