package com.xingyun.bbc.mall.common.exception;

import com.xingyun.bbc.core.enums.IResultStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author LLL
 * @version V1.0
 * @Description: (采购中台错误码)
 * @Param:
 * @return:
 * @date 2019/8/20 14:03
 */
@Getter
@Setter
@Accessors(chain = true)
public class MallExceptionCode implements IResultStatus {

    /**
     * 系统级别异常
     */
    public static final MallExceptionCode SYSTEM_ERROR = new MallExceptionCode("9999", "系统异常");
    public static final MallExceptionCode SYSTEM_BUSY_ERROR = new MallExceptionCode("9998", "系统繁忙");
    public static final MallExceptionCode PARAM_ERROR = new MallExceptionCode("9997", "参数错误");
    public static final MallExceptionCode REQUIRED_PARAM_MISSING = new MallExceptionCode("9996", "请求参数缺失");


    /**
     * 业务级别异常
     */
    public static final MallExceptionCode SKU_BATCH_IS_NONE = new MallExceptionCode("6666", "没有批次");
    public static final MallExceptionCode NO_USER = new MallExceptionCode("6667", "没有用户信息");
    public static final MallExceptionCode NO_BATCH_USER_PRICE = new MallExceptionCode("6668", "没有SKU批次会员类型售价");
    public static final MallExceptionCode NO_BATCH_PRICE = new MallExceptionCode("6668", "没有SKU批次类型售价");
    public static final MallExceptionCode WITHDRAW_PROCESSING = new MallExceptionCode("6670", "您的操作太频繁啦,每次提现需间隔5分钟");
    public static final MallExceptionCode NO_USER_ID = new MallExceptionCode("6671", "未传用户id");
    public static final MallExceptionCode NO_USER_CATEGORY_ID = new MallExceptionCode("6672", "未传一级类目ID");
    public static final MallExceptionCode BATCH_PACKAGE_NUM_NOT_EXIST = new MallExceptionCode("6673", "查不到包装规格值");

    /**
     * 
     * pay模块错误信息
     */
    public static final MallExceptionCode ORDER_NOT_EXIST = new MallExceptionCode("7777", "订单不存在");
    public static final MallExceptionCode ORDER_NOT_MATCHING = new MallExceptionCode("7778", "订单与用户不匹配");
    public static final MallExceptionCode ORDER_AS_CANCELLED = new MallExceptionCode("7779", "该订单已取消");
    public static final MallExceptionCode ORDER_IS_COMPLETION = new MallExceptionCode("7779", "支付已完成的交易");
    public static final MallExceptionCode WITHDRAW_PSD_WRONG = new MallExceptionCode("7780", "支付密码错误");
    public static final MallExceptionCode PAY_PWD_IS_NOT_SET = new MallExceptionCode("7781", "支付密码未设置");
    public static final MallExceptionCode THIRD_PAY_NOTIFY_FAIL =new MallExceptionCode("7782", "返回回调失败字符串");
    public static final MallExceptionCode REMITTANCE_PAY_FAIL =new MallExceptionCode("7783", "线下汇款充值失败");
    public static final MallExceptionCode BALANCE_NOT_ENOUGH =new MallExceptionCode("7784", "用户余额为0,请充值!");
    public static final MallExceptionCode FREEZE_WITHDRAW =new MallExceptionCode("7785", "用户账户信息有误，请联系客服!");
    public static final MallExceptionCode ORDER_IS_OVERDUE = new MallExceptionCode("7786", "订单已过期");
    
    /**
     * 用户账号模块
     * 
     * */
    public static final MallExceptionCode USER_FREEZE_ERROR =new MallExceptionCode("8888", "该用户已被冻结或禁用");
    public static final MallExceptionCode NULL_ERROR_WITHDRAWPSD =new MallExceptionCode("8889", "尚未设置提现密码");

    /**
     * Mall优惠券错误信息
     */
    public static final MallExceptionCode COUPON_IS_INVALID =new MallExceptionCode("9000", "该优惠券已经失效");
    public static final MallExceptionCode COUPON_IS_MAX =new MallExceptionCode("9000", "该优惠券已经达到限领张数");


    private String code;
    private String msg;

    public MallExceptionCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public MallExceptionCode bulid(Object... args) {
        return new MallExceptionCode(this.getCode(), String.format(this.getMsg(), args));
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
