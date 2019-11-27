package com.xingyun.bbc.mallpc.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.xingyun.bbc.common.redis.order.OrderTypeEnum;
import com.xingyun.bbc.common.redis.order.RechargeOrderBizEnum;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.order.api.*;
import com.xingyun.bbc.core.order.enums.OrderAftersaleReasonType;
import com.xingyun.bbc.core.order.enums.work.UserWorkApplyReasons;
import com.xingyun.bbc.core.order.enums.work.UserWorkStatus;
import com.xingyun.bbc.core.order.po.*;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserAccountApi;
import com.xingyun.bbc.core.user.api.UserAccountTransApi;
import com.xingyun.bbc.core.user.api.UserDetailApi;
import com.xingyun.bbc.core.user.dto.UserRechargeQueryDTO;
import com.xingyun.bbc.core.user.enums.AccountTransType;
import com.xingyun.bbc.core.user.enums.AccountWithdrawType;
import com.xingyun.bbc.core.user.enums.UserAccountTransTypesEnum;
import com.xingyun.bbc.core.user.enums.UserDetailType;
import com.xingyun.bbc.core.user.po.UserAccount;
import com.xingyun.bbc.core.user.po.UserAccountTrans;
import com.xingyun.bbc.core.user.po.UserDetail;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.AccountUtil;
import com.xingyun.bbc.mallpc.common.utils.DateUtils;
import com.xingyun.bbc.mallpc.model.dto.PageDto;
import com.xingyun.bbc.mallpc.model.dto.account.AccountDetailDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.account.*;
import com.xingyun.bbc.mallpc.service.UserAccountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.xingyun.bbc.core.user.enums.UserDetailType.*;

@Service
public class UserAccountServiceImpl implements UserAccountService {
    private Date initTime = DateUtils.parseDate("1970-01-01 00:00:00");

    //充值提现状态
    private static Set<Integer> status = new HashSet<>(5);

    //充值明细表查询状态
    private static List<UserDetailType> detailStatus = Arrays.stream(values()).filter(item -> item.getCode().compareTo(ALI_ORDER.getCode()) != 0
            || item.getCode().compareTo(WECHAT_ORDER.getCode()) != 0
            || item.getCode().compareTo(AFTERSALE_WORK_CREDIT.getCode()) != 0
            || item.getCode().compareTo(CREDIT_LIMIT_AVAILABLE_BALANCE.getCode()) != 0
            || item.getCode().compareTo(CREDIT_LIMIT_ORDER.getCode()) != 0).collect(Collectors.toList());

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

    @Resource
    private OrderPaymentApi orderPaymentApi;

    @Resource
    private UserWorkApi userWorkApi;

    @Resource
    private OrderAftersaleApi orderAftersaleApi;

    @Resource
    private OrderApi orderApi;

    @Resource
    private OrderAftersaleVerifyApi orderAftersaleVerifyApi;

    @Resource
    private OrderAftersalePicApi orderAftersalePicApi;

    @Resource
    private UserAccountApi userAccountApi;


    @Override
    public PageVo<AccountRechargeRecordsVo> rechargeRecords(PageDto pageDto, Long uid) {
        UserRechargeQueryDTO userRechargeQueryDTO = new UserRechargeQueryDTO();
        userRechargeQueryDTO.setUserIds(Lists.newArrayList(uid));
        userRechargeQueryDTO.setLimit((pageDto.getCurrentPage() - 1) * pageDto.getPageSize());
        userRechargeQueryDTO.setOffset(pageDto.getPageSize());
        Result<Integer> countResult = userAccountTransApi.countRechargeListExcludeUserStatus4(userRechargeQueryDTO);
        Ensure.that(countResult).isSuccess(new MallPcExceptionCode(countResult.getCode(), countResult.getMsg()));

        if (countResult.getData() < 1) {
            return new PageVo<>(0, pageDto.getCurrentPage(), pageDto.getPageSize(), new ArrayList<>(2));
        }

        Result<List<UserAccountTrans>> rechargeRecordsResult = userAccountTransApi.queryRechargeListByCreateTimeDesc(userRechargeQueryDTO);
        Ensure.that(rechargeRecordsResult).isSuccess(new MallPcExceptionCode(rechargeRecordsResult.getCode(), rechargeRecordsResult.getMsg()));


        List<AccountRechargeRecordsVo> data = new ArrayList<>(rechargeRecordsResult.getData().size());

        rechargeRecordsResult.getData().forEach(item -> {
            AccountRechargeRecordsVo convert = dozerHolder.convert(item, AccountRechargeRecordsVo.class);

            convert.setFpassedTime(null);

            //工单类型的只要不是待审核 都要设置时间
            if (convert.getFrechargeType() == 5) {
                if (convert.getFtransStatus().compareTo(UserWorkStatus.WAITVERIFY.getCode()) != 0) {
                    convert.setFpassedTime(item.getFmodifyTime());
                }
                convert.setFtransStatus(userWorkStatusConventTransSttaus(convert.getFtransStatus()));
            } else if (convert.getFtransStatus().compareTo(AccountTransType.Passed.getCode()) == 0
                    || convert.getFtransStatus().compareTo(AccountTransType.Rejected.getCode()) == 0
                    || convert.getFtransStatus().compareTo(AccountTransType.Canceled.getCode()) == 0) {
                //如果是正常的充值类型 则只有审核通过后者是审核不通过才会有完成时间
                convert.setFpassedTime(item.getFmodifyTime());
            }
            convert.setFtransAmount(AccountUtil.divideOneHundred(convert.getFtransAmount().longValue()));
            data.add(convert);
        });

        return new PageVo<>(countResult.getData(), pageDto.getCurrentPage(), pageDto.getPageSize(), data);

    }

    @Override
    public PageVo<WithDrawRecordsVo> withDrawRecords(PageDto pageDto, Long uid) {
        Criteria<UserAccountTrans, Object> criteria = Criteria.of(UserAccountTrans.class)
                .andEqualTo(UserAccountTrans::getFtransTypes, UserAccountTransTypesEnum.WITHDRAW.getCode())
                .andEqualTo(UserAccountTrans::getFuid, uid)
                .sortDesc(UserAccountTrans::getFcreateTime)
                .page(pageDto.getCurrentPage(), pageDto.getPageSize());

        Result<Integer> countResult = userAccountTransApi.countByCriteria(criteria);
        Ensure.that(countResult).isSuccess(new MallPcExceptionCode(countResult.getCode(), countResult.getMsg()));
        if (countResult.getData() < 1) {
            return new PageVo<>(0, pageDto.getCurrentPage(), pageDto.getPageSize(), new ArrayList<>(2));
        }
        Result<List<UserAccountTrans>> listResult = userAccountTransApi.queryByCriteria(criteria);

        List<WithDrawRecordsVo> data = new ArrayList<>(listResult.getData().size());
        listResult.getData().forEach(item -> {
            WithDrawRecordsVo convert = dozerHolder.convert(item, WithDrawRecordsVo.class);
            convert.setFpassedTime(null);
            if (status.contains(item.getFtransStatus())
            ) {
                if (initTime.compareTo(item.getFpassedTime()) == 0) {
                    if (initTime.compareTo(item.getFpayTime()) == 0) {
                        convert.setFpassedTime(item.getFmodifyTime());
                    } else {
                        convert.setFpassedTime(item.getFpayTime());
                    }

                } else {
                    convert.setFpassedTime(item.getFpassedTime());
                }
            }
            convert.setFtransAmount(AccountUtil.divideOneHundred(convert.getFtransAmount().longValue()));
            convert.setFtransActualAmount(AccountUtil.divideOneHundred(convert.getFtransActualAmount().longValue()));
            convert.setFtransPoundage(AccountUtil.divideOneHundred(convert.getFtransPoundage().longValue()));
            data.add(convert);
        });
        return new PageVo<>(countResult.getData(), pageDto.getCurrentPage(), pageDto.getPageSize(), data);
    }

    @Override
    public PageVo<InAndOutRecordsVo> inAndOutRecords(PageDto pageDto, Long uid) {

        //过滤掉明细类型为6支付宝下单，7微信下单， 14售后工单调整信用额度，18信用额度-可用余额，19信用额度下单
        Criteria<UserDetail, Object> criteria = Criteria.of(UserDetail.class)
                .andEqualTo(UserDetail::getFuid, uid)
                .andIn(UserDetail::getFdetailType, detailStatus)
                .sortDesc(UserDetail::getFcreateTime)
                .page(pageDto.getCurrentPage(), pageDto.getPageSize());

        Result<Integer> integerResult = userDetailApi.countByCriteria(criteria);
        Ensure.that(integerResult).isSuccess(new MallPcExceptionCode(integerResult.getCode(), integerResult.getMsg()));

        if (integerResult.getData() < 1) {
            return new PageVo<>(0, pageDto.getCurrentPage(), pageDto.getPageSize(), new ArrayList<>(2));
        }

        Result<List<UserDetail>> listResult = userDetailApi.queryByCriteria(criteria);
        Ensure.that(listResult).isSuccess(new MallPcExceptionCode(listResult.getCode(), listResult.getMsg()));

        List<InAndOutRecordsVo> data = dozerHolder.convert(listResult.getData(), InAndOutRecordsVo.class);
        data.forEach(item -> {
            Optional.ofNullable(item.getFexpenseAmount()).ifPresent(i -> item.setFexpenseAmount(AccountUtil.divideOneHundred(i.longValue())));
            Optional.ofNullable(item.getFincomeAmount()).ifPresent(i -> item.setFincomeAmount(AccountUtil.divideOneHundred(i.longValue())));
        });

//        Map<Integer, List<InAndOutRecordsVo>> group = data.stream().collect(Collectors.groupingBy(InAndOutRecordsVo::getFdetailType));
//
//        Map<String, InAndOutRecordsVo> map = data.stream().collect(Collectors.toMap(InAndOutRecordsVo::getFdetailId, Function.identity()));

//        //去提现充值表里查
//        List<InAndOutRecordsVo> list1 = new ArrayList<>(10);
//        Optional.ofNullable(group.get(1)).ifPresent(list1::addAll);
//        Optional.ofNullable(group.get(2)).ifPresent(list1::addAll);
//        Optional.ofNullable(group.get(3)).ifPresent(list1::addAll);
//        Optional.of(group.get(4)).ifPresent(list1::addAll);
//        Optional.of(group.get(8)).ifPresent(list1::addAll);
//        if (list1.size() > 0) {
//            List<String> collect = list1.stream().map(InAndOutRecordsVo::getFdetailId).collect(Collectors.toList());
//            List<AccountDetailVo> transDetail = getTransDetail(collect);
//            Map<String, AccountDetailVo> map1 = transDetail.stream().collect(Collectors.toMap(AccountDetailVo::getFtransId, Function.identity()));
//            list1.forEach(item -> {
//                Optional.ofNullable(map1.get(item.getFdetailId())).ifPresent(i -> {
//                    item.setFcreateTime(i.getFcreateTime());
//                    item.setFpassedTime(i.getFpassedTime());
//                });
//            });
//        }
//        //支付单
//        List<InAndOutRecordsVo> list2 = new ArrayList<>(10);
//        Optional.of(group.get(5)).ifPresent(list2::addAll);
//        if (list2.size() > 0) {
//
//            Map<String, OrderPayment> map1 = orderPayments(list2.stream().map(InAndOutRecordsVo::getFdetailId)
//                    .collect(Collectors.toList()))
//                    .stream().collect(Collectors.toMap(OrderPayment::getForderPaymentId, Function.identity()));
//
//            list2.forEach(item -> {
//                Optional.ofNullable(map1.get(item.getFdetailId())).ifPresent(i -> {
//                    item.setFcreateTime(i.getFcreateTime());
//                    item.setFpassedTime(i.getFpayTime());
//                });
//            });
//        }
//        //客户售后工单审核 工单号13  //17 充值工单 工单号里
//        List<InAndOutRecordsVo> list3 = new ArrayList<>(10);
//        Optional.of(group.get(13)).ifPresent(list3::addAll);
//        Optional.of(group.get(17)).ifPresent(list3::addAll);
//        if (list3.size() > 0) {
//            Map<String, UserWork> map1 = userWorks(list3.stream().map(InAndOutRecordsVo::getFdetailId).collect(Collectors.toList()))
//                    .stream()
//                    .collect(Collectors.toMap(UserWork::getFuserWorkOrder, Function.identity()));
//
//            list3.forEach(item -> {
//                Optional.ofNullable(map1.get(item.getFdetailId())).ifPresent(i -> {
//                    item.setFcreateTime(i.getFcreateTime());
//                    item.setFpassedTime(i.getFmodifyTime());
//                });
//            });
//        }
//        //10 售后单号
//        List<InAndOutRecordsVo> list4 = new ArrayList<>(10);
//        Optional.of(group.get(10)).ifPresent(list4::addAll);
//        if (list4.size() > 0) {
//            Map<String, OrderAftersale> map1 = orderAftersales(list4.stream().map(InAndOutRecordsVo::getFdetailId).collect(Collectors.toList()))
//                    .stream()
//                    .collect(Collectors.toMap(OrderAftersale::getForderAftersaleId, Function.identity()));
//
//            list4.forEach(item -> {
//                Optional.ofNullable(map1.get(item.getFdetailId())).ifPresent(i -> {
//                    item.setFcreateTime(i.getFcreateTime());
//                    item.setFpassedTime(i.getFmodifyTime());
//                });
//            });
//        }
//        //9 11 12销售订单号
//        List<InAndOutRecordsVo> list5 = new ArrayList<>(10);
//        Optional.of(group.get(9)).ifPresent(list5::addAll);
//        Optional.of(group.get(11)).ifPresent(list5::addAll);
//        Optional.of(group.get(12)).ifPresent(list5::addAll);
//        if (list5.size() > 0) {
//            Map<String, Order> map1 = orders(list5.stream().map(InAndOutRecordsVo::getFdetailId).collect(Collectors.toList()))
//                    .stream()
//                    .collect(Collectors.toMap(Order::getForderId, Function.identity()));
//
//            list5.forEach(item -> {
//                Optional.ofNullable(map1.get(item.getFdetailId())).ifPresent(i -> {
//                    item.setFcreateTime(i.getFcreateTime());
//                    item.setFpassedTime(i.getFmodifyTime());
//                });
//            });
//        }
//        //15 就不先查了
//        //16就不先查了
        return new PageVo<>(integerResult.getData(), pageDto.getCurrentPage(), pageDto.getPageSize(), data);
    }

    @Override
    public AccountDetailVo accountDetail(AccountDetailDto accountDetailDto) {
        AccountDetailVo accountDetailVo = new AccountDetailVo();
        switch (accountDetailDto.getType()) {
            case 1:
            case 2:
                accountDetailVo = getTransDetail(accountDetailDto.getId());
                break;
            case 3:
                accountDetailVo = getInOutDetail(accountDetailDto.getId());
                accountDetailVo.setFtransStatus(AccountTransType.Passed.getCode());
                break;
            default:
                break;
        }

        return accountDetailVo;
    }


    @Override
    public AccountBaseInfoVo accountInfo(Long uid) {
        Result<List<UserAccount>> listResult = userAccountApi.queryByCriteria(Criteria.of(UserAccount.class)
                .andEqualTo(UserAccount::getFuid, uid));
        Ensure.that(listResult).isNotEmptyData(MallPcExceptionCode.PARAM_ERROR);

        UserAccount userAccount = listResult.getData().get(0);
        AccountBaseInfoVo accountBaseInfoVo = new AccountBaseInfoVo();

        accountBaseInfoVo.setBanlance(AccountUtil.divideOneHundred(userAccount.getFbalance() + userAccount.getFfreezeWithdraw()));
        accountBaseInfoVo.setCashInAble(AccountUtil.divideOneHundred(userAccount.getFbalance()));
        accountBaseInfoVo.setCashInIng(AccountUtil.divideOneHundred(userAccount.getFfreezeWithdraw()));


        return accountBaseInfoVo;
    }


    private AccountDetailVo getTransDetail(String id) {
        AccountDetailVo accountDetailVo = new AccountDetailVo();
        //充值工单从这里查
        if (id.startsWith(OrderTypeEnum.RECHARGE_ORDER.getCode() + RechargeOrderBizEnum.BACKGROUND.getCode())) {
            UserWork userWork = userWorks(id);

            accountDetailVo.setFtransId(userWork.getFuserWorkOrder());
            accountDetailVo.setFcreateTime(userWork.getFcreateTime());
            accountDetailVo.setOrderId(userWork.getForderId());
            accountDetailVo.setFtransStatus(userWorkStatusConventTransSttaus(userWork.getFstatus()));
            accountDetailVo.setFtransAmount(AccountUtil.divideOneHundred(userWork.getFapplyAmount()));
            accountDetailVo.setReson(UserWorkApplyReasons.getName(userWork.getFapplyReason()));
            accountDetailVo.setFremark(userWork.getFremark());
            accountDetailVo.setFapplyPic(userWork.getFapplyPic());
            accountDetailVo.setFtransTypes(UserAccountTransTypesEnum.RECHARGE.getCode());
            accountDetailVo.setType(RECHARGE_WORK_ADJUSTMENT_BALANCE.getCode());
            accountDetailVo.setFtransPoundage(null);
            accountDetailVo.setFtransActualAmount(null);
            accountDetailVo.setFpassedTime(userWork.getFmodifyTime());
        } else {
            Result<List<UserAccountTrans>> userAccountTransResult = userAccountTransApi.queryByCriteria(Criteria.of(UserAccountTrans.class)
                    .andEqualTo(UserAccountTrans::getFtransId, id));
            Ensure.that(userAccountTransResult).isNotEmptyData(new MallPcExceptionCode(userAccountTransResult.getCode(), userAccountTransResult.getMsg()));

            accountDetailVo = dozerHolder.convert(userAccountTransResult.getData().get(0), AccountDetailVo.class);
            if (accountDetailVo.getFtransTypes().compareTo(UserAccountTransTypesEnum.RECHARGE.getCode()) == 0) {
                accountDetailVo.setType(accountDetailVo.getFrechargeType());
                accountDetailVo.setFtransPoundage(null);
                accountDetailVo.setFtransActualAmount(null);
                accountDetailVo.setTradeType(accountDetailVo.getType());
            } else if (accountDetailVo.getFtransTypes().compareTo(UserAccountTransTypesEnum.WITHDRAW.getCode()) == 0) {
                accountDetailVo.setType(BALANCE_WITHDRAW.getCode());
                if (accountDetailVo.getFwithdrawType().compareTo(AccountWithdrawType.alipay.getCode()) == 0) {
                    accountDetailVo.setTradeType(5);
                } else if (accountDetailVo.getFwithdrawType().compareTo(AccountWithdrawType.unionpay.getCode()) == 0) {
                    accountDetailVo.setTradeType(6);
                } else if (accountDetailVo.getFwithdrawType().compareTo(AccountWithdrawType.WechatPay.getCode()) == 0) {
                    accountDetailVo.setTradeType(7);
                } else {
                    accountDetailVo.setTradeType(accountDetailVo.getType());
                }

                accountDetailVo.setFtransActualAmount(AccountUtil.divideOneHundred(accountDetailVo.getFtransActualAmount().longValue()));
                accountDetailVo.setFtransPoundage(AccountUtil.divideOneHundred(accountDetailVo.getFtransPoundage().longValue()));
            } else {
                throw new BizException(MallPcExceptionCode.PARAM_ERROR);
            }
            if (accountDetailVo.getFtransStatus().compareTo(AccountTransType.WaitPayment.getCode()) == 0
                    || AccountTransType.WaitVerify.getCode().compareTo(accountDetailVo.getFtransStatus()) == 0) {
                accountDetailVo.setFpassedTime(null);
            } else {
                if (initTime.compareTo(accountDetailVo.getFpassedTime()) == 0) {
                    if (initTime.compareTo(accountDetailVo.getFpayTime()) == 0) {
                        accountDetailVo.setFpassedTime(accountDetailVo.getFmodifyTime());
                    } else {
                        accountDetailVo.setFpassedTime(accountDetailVo.getFpayTime());
                    }

                }
            }
            accountDetailVo.setFtransAmount(AccountUtil.divideOneHundred(accountDetailVo.getFtransAmount().longValue()));
        }
        return accountDetailVo;
    }


    private AccountDetailVo getInOutDetail(String id) {
        Result<List<UserDetail>> listResult = userDetailApi.queryByCriteria(Criteria.of(UserDetail.class)
                .andEqualTo(UserDetail::getFtypeId, id));
        Ensure.that(listResult).isNotEmptyData(new MallPcExceptionCode(listResult.getCode(), listResult.getMsg()));
        AccountDetailVo accountDetail = (dozerHolder.convert(listResult.getData().get(0), AccountDetailVo.class));
        accountDetail.setFpassedTime(listResult.getData().get(0).getFmodifyTime());
        switch (listResult.getData().get(0).getFdetailType()) {
            //充值提现
            case 1:
            case 2:
            case 3:
            case 4:
            case 8:
                accountDetail = getTransDetail(id);
                break;
            case 5:
                List<OrderPayment> orderPayments = orderPayments(id);
                OrderPayment orderPayment = orderPayments.get(0);
                Result<List<Order>> orderListResult = orderApi.queryByCriteria(Criteria.of(Order.class)
                        .andEqualTo(Order::getForderPaymentId, orderPayment.getForderPaymentId()));
                Ensure.that(orderListResult).isNotEmptyData(new MallPcExceptionCode(orderListResult.getCode(), orderListResult.getMsg()));

                accountDetail.setOrderId(orderListResult.getData().stream().map(Order::getForderId).collect(Collectors.joining(",")));
                accountDetail.setFcreateTime(orderPayment.getFcreateTime());
                accountDetail.setFpassedTime(orderPayment.getFpayTime());
                break;
            //售后工单
            case 13:
            case 17:
                UserWork userWork = userWorks(id);
                accountDetail.setFcreateTime(userWork.getFcreateTime());
                accountDetail.setOrderId(userWork.getForderId());
                accountDetail.setFtransStatus(userWork.getFstatus());
                accountDetail.setReson(UserWorkApplyReasons.getName(userWork.getFapplyReason()));
                accountDetail.setFremark(userWork.getFremark());
                accountDetail.setFapplyPic(userWork.getFapplyPic());

                break;
            //售后单
            case 10:
                OrderAftersale orderAftersale = orderAftersales(id);

                accountDetail.setFcreateTime(orderAftersale.getFcreateTime());
                accountDetail.setAfterType(orderAftersale.getFaftersaleType());
                accountDetail.setOrderId(orderAftersale.getForderId());
                accountDetail.setReson(OrderAftersaleReasonType.getName(orderAftersale.getFaftersaleReason()));
                Result<List<OrderAftersaleVerify>> orderAftersaleVerifyResult = orderAftersaleVerifyApi.queryByCriteria(Criteria.of(OrderAftersaleVerify.class)
                        .andEqualTo(OrderAftersaleVerify::getForderAftersaleId, orderAftersale.getForderAftersaleId())
                        .andEqualTo(OrderAftersaleVerify::getFroleType, 1));
                Ensure.that(orderAftersaleVerifyResult).isSuccess(new MallPcExceptionCode(orderAftersaleVerifyResult.getCode(), orderAftersaleVerifyResult.getMsg()));
                if (CollectionUtil.isNotEmpty(orderAftersaleVerifyResult.getData())) {
                    accountDetail.setFremark(orderAftersaleVerifyResult.getData().get(0).getFremark());
                }

                Result<List<OrderAftersalePic>> orderAftersaleListResult = orderAftersalePicApi.queryByCriteria(Criteria.of(OrderAftersalePic.class)
                        .andEqualTo(OrderAftersalePic::getForderAftersaleId, orderAftersale.getForderAftersaleId())
                        .andEqualTo(OrderAftersalePic::getFpicType, 2));
                Ensure.that(orderAftersaleListResult).isSuccess(new MallPcExceptionCode(orderAftersaleListResult.getCode(), orderAftersaleListResult.getMsg()));
                if (CollectionUtil.isNotEmpty(orderAftersaleListResult.getData())) {
                    accountDetail.setFapplyPic(orderAftersaleListResult.getData().get(0).getFaftersalePic());
                }

            case 9:
            case 11:
            case 12:
                List<Order> orders = orders(id);
                Order order = orders.get(0);
                accountDetail.setFpassedTime(order.getFmodifyTime());

            default:
                break;
        }

        return accountDetail;
    }


    private List<OrderPayment> orderPayments(String id) {
        Result<List<OrderPayment>> listResult1 = orderPaymentApi.queryByCriteria(Criteria.of(OrderPayment.class)
                .andEqualTo(OrderPayment::getForderPaymentId, id));
        Ensure.that(listResult1).isNotNullData(new MallPcExceptionCode(listResult1.getCode(), listResult1.getMsg()));
        return listResult1.getData();
    }


    private UserWork userWorks(String id) {
        Result<List<UserWork>> listResult1 = userWorkApi.queryByCriteria(Criteria.of(UserWork.class)
                .andEqualTo(UserWork::getFuserWorkOrder, id));
        Ensure.that(listResult1).isNotEmptyData(new MallPcExceptionCode(listResult1.getCode(), listResult1.getMsg()));
        return listResult1.getData().get(0);
    }


    private OrderAftersale orderAftersales(String id) {
        Result<List<OrderAftersale>> listResult1 = orderAftersaleApi.queryByCriteria(Criteria.of(OrderAftersale.class)
                .andEqualTo(OrderAftersale::getForderAftersaleId, id));
        Ensure.that(listResult1).isNotEmptyData(new MallPcExceptionCode(listResult1.getCode(), listResult1.getMsg()));

        return listResult1.getData().get(0);
    }

    private List<Order> orders(String id) {
        Result<List<Order>> listResult1 = orderApi.queryByCriteria(Criteria.of(Order.class).andEqualTo(Order::getForderId, id));
        Ensure.that(listResult1).isNotNullData(new MallPcExceptionCode(listResult1.getCode(), listResult1.getMsg()));
        return listResult1.getData();
    }

    /**
     * 工单的状态和充值状态转换
     *
     * @param workStatus
     * @return
     */
    private int userWorkStatusConventTransSttaus(int workStatus) {
        switch (workStatus) {
            case 1:
                return AccountTransType.WaitVerify.getCode();
            case 2:
                return AccountTransType.Passed.getCode();
            case 3:
                return AccountTransType.Rejected.getCode();
            case 4:
                return AccountTransType.Canceled.getCode();
            default:
                return workStatus;
        }
    }

}
