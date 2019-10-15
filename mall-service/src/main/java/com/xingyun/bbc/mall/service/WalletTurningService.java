package com.xingyun.bbc.mall.service;


import com.xingyun.bbc.mall.model.dto.UserWalletDetailDto;

import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.model.vo.UserWalletDetailVo;


/**
 * @author lll
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
public interface WalletTurningService {


    /**
     * @author lll
     * @version V1.0
     * @Description: 查询钱包收支明细列表
     * @Param: [userWalletDetailDto]
     * @return: PageVo<UserWalletDetailVo>
     * @date 2019/9/20 13:49
     */
    PageVo<UserWalletDetailVo> queryWalletTurningList(UserWalletDetailDto userWalletDetailDto);
}
