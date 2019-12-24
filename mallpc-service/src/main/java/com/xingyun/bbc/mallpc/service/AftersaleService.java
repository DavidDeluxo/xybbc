package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.aftersale.AftersaleBackDto;
import com.xingyun.bbc.mallpc.model.dto.aftersale.AftersalePcLisDto;
import com.xingyun.bbc.mallpc.model.dto.aftersale.ShippingCompanyDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.aftersale.AftersaleBackVo;
import com.xingyun.bbc.mallpc.model.vo.aftersale.AftersaleDetailVo;
import com.xingyun.bbc.mallpc.model.vo.aftersale.AftersaleListVo;

import java.util.List;


public interface AftersaleService {


    //查询售后订单列表
    Result<PageVo<AftersaleListVo>> getAftersaleLis(AftersalePcLisDto aftersaleLisDto);

    //查询售后订单详情
    Result<AftersaleDetailVo> getAftersaleDetail(String faftersaleId);

    Result<List<ShippingCompanyDto>> getShippingCompanyLis(ShippingCompanyDto shippingCompanyDto);

    Result modifyAftersaleBack(AftersaleBackDto aftersaleBackDto);

    Result<AftersaleBackVo> getAftersaleBackShipping(String faftersaleId);
}
