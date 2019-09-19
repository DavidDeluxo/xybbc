package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.AftersaleBackDto;
import com.xingyun.bbc.mall.model.dto.AftersaleLisDto;
import com.xingyun.bbc.mall.model.dto.ShippingCompanyDto;
import com.xingyun.bbc.mall.model.vo.AftersaleBackVo;
import com.xingyun.bbc.mall.model.vo.AftersaleDetailVo;
import com.xingyun.bbc.mall.model.vo.AftersaleListVo;
import com.xingyun.bbc.mall.model.vo.PageVo;

import java.util.List;


public interface AftersaleService {


    //查询售后订单列表
    Result<PageVo<AftersaleListVo>> getAftersaleLis(AftersaleLisDto aftersaleLisDto);

    //查询售后订单详情
    Result<AftersaleDetailVo> getAftersaleDetail(String faftersaleId);

    Result<List<ShippingCompanyDto>> getShippingCompanyLis(ShippingCompanyDto shippingCompanyDto);

    Result modifyAftersaleBack(AftersaleBackDto aftersaleBackDto);

    Result<AftersaleBackVo> getAftersaleBackShipping(String faftersaleId);
}
