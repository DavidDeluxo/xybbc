package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.mallpc.model.dto.recharge.OfflineRechargeVoucherDTO;
import com.xingyun.bbc.mallpc.model.dto.recharge.RechargeSubmitDTO;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
public interface RechargeService {

    /**
     * 提交充值
     *
     * @param dto
     * @return
     */
    String save(RechargeSubmitDTO dto);

    /**
     * 线下汇款提交凭证
     *
     * @param dto
     * @return
     */
    void offlineVoucher(OfflineRechargeVoucherDTO dto);

    /**
     * 根据充值单生成付款码
     * @param transId
     * @return
     */
    Object getQRCode(String transId);
}
