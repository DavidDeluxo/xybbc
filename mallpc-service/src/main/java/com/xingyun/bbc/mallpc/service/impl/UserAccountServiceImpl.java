package com.xingyun.bbc.mallpc.service.impl;


import com.google.common.collect.Lists;
import com.xingyun.bbc.core.order.enums.work.UserWorkStatus;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserAccountTransApi;
import com.xingyun.bbc.core.user.api.UserDetailApi;
import com.xingyun.bbc.core.user.dto.UserRechargeQueryDTO;
import com.xingyun.bbc.core.user.enums.AccountRechargeType;
import com.xingyun.bbc.core.user.enums.AccountTransType;
import com.xingyun.bbc.core.user.enums.UserAccountTransTypesEnum;
import com.xingyun.bbc.core.user.po.UserAccountTrans;
import com.xingyun.bbc.core.user.po.UserDetail;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.account.AccountRechargeRecordsVo;
import com.xingyun.bbc.mallpc.model.vo.account.InAndOutRecordsVo;
import com.xingyun.bbc.mallpc.model.vo.account.WithDrawRecordsVo;
import com.xingyun.bbc.mallpc.service.UserAccountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.xingyun.bbc.core.user.enums.UserDetailType.*;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    //充值提现状态
    private static Set<Integer> status = new HashSet<>(5);

    static {
        status.add(AccountTransType.Passed.getCode());
        status.add(AccountTransType.Canceled.getCode());
        status.add(AccountTransType.Rejected.getCode());

    }

    @Resource
    private UserAccountTransApi userAccountTransApi;

    @Resource
    private UserDetailApi userDetailApi;

    @Resource
    private DozerHolder dozerHolder;


    @Override
    public PageVo<AccountRechargeRecordsVo> rechargeRecords(PageVo pageVo, Long uid) {
        UserRechargeQueryDTO userRechargeQueryDTO = new UserRechargeQueryDTO();
        userRechargeQueryDTO.setUserIds(Lists.newArrayList(uid));
        userRechargeQueryDTO.setLimit((pageVo.getCurrentPage() - 1) * pageVo.getPageSize());
        userRechargeQueryDTO.setOffset(pageVo.getPageSize());
        Result<Integer> countResult = userAccountTransApi.countRechargeList(userRechargeQueryDTO);
        Ensure.that(countResult).isSuccess(new MallPcExceptionCode(countResult.getCode(), countResult.getMsg()));

        if (countResult.getData() < 1) {
            return new PageVo<>(0, pageVo.getCurrentPage(), pageVo.getPageSize(), new ArrayList<>(2));
        }

        Result<List<UserAccountTrans>> rechargeRecordsResult = userAccountTransApi.queryRechargeListByCreateTimeDesc(userRechargeQueryDTO);
        Ensure.that(rechargeRecordsResult).isSuccess(new MallPcExceptionCode(rechargeRecordsResult.getCode(), rechargeRecordsResult.getMsg()));

        List<AccountRechargeRecordsVo> data = new ArrayList<>(rechargeRecordsResult.getData().size());
        rechargeRecordsResult.getData().forEach(item -> {
            AccountRechargeRecordsVo convert = dozerHolder.convert(item, AccountRechargeRecordsVo.class);

            convert.setFpassedTime(null);
            //工单类型的只要不是待审核 都要设置时间
            if (convert.getFrechargeType() == 5 && convert.getFtransStatus().compareTo(UserWorkStatus.WAITVERIFY.getCode()) != 0) {
                convert.setFpassedTime(item.getFpassedTime());
            } else if (convert.getFtransStatus().compareTo(AccountTransType.Passed.getCode()) == 0
                    || convert.getFtransStatus().compareTo(AccountTransType.Rejected.getCode()) == 0) {
                //如果是正常的充值类型 则只有审核通过后者是审核不通过才会有完成时间

                //只有是支付宝 微信支付的才是支付时间
                if (item.getFrechargeType().compareTo(AccountRechargeType.AliPay.getCode()) == 0
                        ||
                        item.getFrechargeType().compareTo(AccountRechargeType.WechatPay.getCode()) == 0
                ) {
                    convert.setFpassedTime(item.getFpayTime());
                }
            }
            data.add(convert);
        });

        return new PageVo<>(countResult.getData(), pageVo.getCurrentPage(), pageVo.getPageSize(), data);

    }

    @Override
    public PageVo<WithDrawRecordsVo> withDrawRecords(PageVo pageVo, Long uid) {
        Criteria<UserAccountTrans, Object> criteria = Criteria.of(UserAccountTrans.class)
                .andEqualTo(UserAccountTrans::getFtransTypes, UserAccountTransTypesEnum.WITHDRAW.getCode())
                .sortDesc(UserAccountTrans::getFcreateTime)
                .page(pageVo.getCurrentPage(), pageVo.getPageSize());

        Result<Integer> countResult = userAccountTransApi.countByCriteria(criteria);
        Ensure.that(countResult).isSuccess(new MallPcExceptionCode(countResult.getCode(), countResult.getMsg()));
        if (countResult.getData() < 1) {
            return new PageVo<>(0, pageVo.getCurrentPage(), pageVo.getPageSize(), new ArrayList<>(2));
        }
        Result<List<UserAccountTrans>> listResult = userAccountTransApi.queryByCriteria(criteria);

        List<WithDrawRecordsVo> data = new ArrayList<>(listResult.getData().size());
        listResult.getData().forEach(item -> {
            WithDrawRecordsVo convert = dozerHolder.convert(item, WithDrawRecordsVo.class);
            convert.setFpassedTime(null);
            if (status.contains(item.getFtransStatus())
            ) {
                convert.setFpassedTime(item.getFpassedTime());
            }
            data.add(convert);
        });

        return new PageVo<>(countResult.getData(), pageVo.getCurrentPage(), pageVo.getPageSize(), data);
    }

    @Override
    public PageVo<InAndOutRecordsVo> inAndOutRecords(PageVo pageVo, Long uid) {
        //过滤掉明细类型为6支付宝下单，7微信下单， 14售后工单调整信用额度，18信用额度-可用余额，19信用额度下单
        Criteria<UserDetail, Object> criteria = Criteria.of(UserDetail.class)
                .andEqualTo(UserDetail::getFuid, uid)
                .andNotIn(UserDetail::getFdetailType, Lists.newArrayList(ALI_ORDER.getCode(),
                        WECHAT_ORDER.getCode(),AFTERSALE_WORK_CREDIT.getCode(),CREDIT_LIMIT_AVAILABLE_BALANCE.getCode(),CREDIT_LIMIT_ORDER.getCode()))
                .sortDesc(UserDetail::getFcreateTime)
                .page(pageVo.getCurrentPage(),pageVo.getPageSize());

        Result<Integer> integerResult = userDetailApi.countByCriteria(criteria);
        Ensure.that(integerResult).isSuccess(new MallPcExceptionCode(integerResult.getCode(),integerResult.getMsg()));

        if(integerResult.getData()<1){
            return new PageVo<>(0,pageVo.getCurrentPage(),pageVo.getPageSize(),new ArrayList<>(2));
        }

        Result<List<UserDetail>> listResult = userDetailApi.queryByCriteria(criteria);
        Ensure.that(listResult).isSuccess(new MallPcExceptionCode(listResult.getCode(),listResult.getMsg()));

        return new PageVo<>(integerResult.getData(),pageVo.getCurrentPage(),pageVo.getPageSize(), dozerHolder.convert(listResult.getData(),InAndOutRecordsVo.class));
    }

}
