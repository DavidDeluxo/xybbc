package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.aftersale.AftersaleBackDto;
import com.xingyun.bbc.mallpc.model.dto.aftersale.AftersaleLisDto;
import com.xingyun.bbc.mallpc.model.dto.aftersale.ShippingCompanyDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.aftersale.AftersaleBackVo;
import com.xingyun.bbc.mallpc.model.vo.aftersale.AftersaleDetailVo;
import com.xingyun.bbc.mallpc.model.vo.aftersale.AftersaleListVo;
import com.xingyun.bbc.mallpc.service.AftersaleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api("用户售后")
@RestController
@RequestMapping("/aftersale")
public class AftersaleController {

    @Autowired
    private AftersaleService aftersaleService;

    @ApiOperation(value = "获取售后列表", httpMethod = "GET")
    @GetMapping("/getAftersaleLis")
    public Result<PageVo<AftersaleListVo>> getAftersaleLis(@ModelAttribute AftersaleLisDto aftersaleLisDto, HttpServletRequest request) {
        Long xyid = Long.parseLong(request.getHeader("xyid"));
        aftersaleLisDto.setFuserId(xyid);
        return aftersaleService.getAftersaleLis(aftersaleLisDto);
    }

    @ApiOperation(value = "获取售后详情", httpMethod = "GET")
    @GetMapping("/getAftersaleDetail")
    public Result<AftersaleDetailVo> getAftersaleDetail(@RequestParam String faftersaleId) {
        return aftersaleService.getAftersaleDetail(faftersaleId);
    }

    @ApiOperation(value = "售后详情查询物流公司", httpMethod = "GET")
    @GetMapping("/getShippingCompany")
    public Result<List<ShippingCompanyDto>> getShippingCompanyLis(@ModelAttribute ShippingCompanyDto shippingCompanyDto) {
        return aftersaleService.getShippingCompanyLis(shippingCompanyDto);
    }

    @ApiOperation(value = "售后详情查询回寄物流信息", httpMethod = "GET")
    @GetMapping("/getAftersaleBackShipping")
    public Result<AftersaleBackVo> getAftersaleBackShipping(@RequestParam String faftersaleId) {
        return aftersaleService.getAftersaleBackShipping(faftersaleId);
    }

    @ApiOperation(value = "售后详情用户上传回寄物流信息", httpMethod = "POST")
    @PostMapping("/modifyAftersaleBack")
    public Result modifyAftersaleBack(@RequestBody @Validated AftersaleBackDto aftersaleBackDto) {
        return aftersaleService.modifyAftersaleBack(aftersaleBackDto);
    }

}
