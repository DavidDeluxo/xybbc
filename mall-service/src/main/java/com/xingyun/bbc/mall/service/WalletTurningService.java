package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.CategoryDto;
import com.xingyun.bbc.mall.model.dto.UserWalletDetailDto;
import com.xingyun.bbc.mall.model.vo.IndexSkuGoodsVo;
import com.xingyun.bbc.mall.model.vo.PageConfigVo;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.model.vo.UserWalletDetailVo;

import java.util.List;

/**
 * @author lll
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
public interface WalletTurningService {



    PageVo<UserWalletDetailVo> queryWalletTurningList(UserWalletDetailDto userWalletDetailDto);
}
