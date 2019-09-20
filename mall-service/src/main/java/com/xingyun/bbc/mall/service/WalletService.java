package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.mall.model.dto.WithdrawDto;
import com.xingyun.bbc.mall.model.dto.WithdrawRateDto;
import com.xingyun.bbc.mall.model.vo.BanksVo;
import com.xingyun.bbc.mall.model.vo.WalletAmountVo;
import com.xingyun.bbc.mall.model.vo.WithdrawRateVo;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;

/**
 * @author hekaijin
 * @date 2019/9/16 16:34
 * @Description
 */
@Validated
public interface WalletService {


    /**
     * 查询钱包金额
     *
     * @param uid
     * @return
     */
    WalletAmountVo queryAmount(Long uid);

    /**
     * 校验用户是否设置支付密码
     * @param uid
     * @return
     */
    Boolean checkPayPwd(Long uid);

    /**
     * 查询提现费率
     * @param withdrawRateDto
     * @return
     */
    List<WithdrawRateVo> queryWithdrawRate(WithdrawRateDto withdrawRateDto);

    /**
     * 查询银行卡开户行列表
     * @return
     */
    List<BanksVo> queryBankList();

    /**
     * 用户提现
     * @param withdrawDto
     * @return
     */
    Boolean withdraw(@Valid WithdrawDto withdrawDto);
}
