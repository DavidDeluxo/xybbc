package com.xingyun.bbc.mall.base.enums;

import com.xingyun.bbc.core.enums.IResultStatus;
import lombok.Getter;

@Getter
public enum MallResultStatus implements IResultStatus {
    LOGIN_FAILURE("1002", "用户名或密码错误"),
    NAME_NOT_EXIST("1003", "账号不存在"),
    BIND_MOBILE_ERROR("1004", "请输入正确的手机号码"),
    SMS_SEND_FAILD("1005", "短信发送失败"),
    SMS_AUTH_NUM_ERROR("1006", "请输入正确的验证码"),
    USER_SEND_SMS_FAILD("1007", "您提交的太频繁，请明天再试;"),
    REGISTER_MOBILE_EXIST("1008", "手机号已注册"),
    SMS_AUTH_NUM_OUT_TIME("1009", "手机验证码过期"),
    SMS_AUTH_IS_SEND("1010", "验证码已发送,请稍后再试"),
    PWD_MIDIFY_FAILED("1011", "密码修改失败"),
    ACCOUNT_NOT_EXIST("1012", "账户不存在"),
    COMMON_UPDATE_FAIL("1013", "更新失败"),

    ACCOUNT_FREEZE("1016", "账户冻结中"),
    ACCOUNT_NOT_AUTH("1017", "账户未认证或认证不通过"),
    ACCOUNT_MOBILE_NOT_VERIFY("1018", "账户手机号未验证"),
    ACCOUNT_MAIL_NOT_VERIFY("1019", "账户邮箱未验证"),
    EMAIL_AUTH_NUM_OUT_TIME("1020", "邮箱验证码过期"),
    REGISTER_NAME_EXIST("1021", "用户名已存在，请重新输入"),
    REGISTER_NAME_IS_NULL("1022", "请输入用户名"),
    USER_NICKNAME_EXIST("1023", "用户名已设置"),
    USER_WITHDRAW_RATE_NOT_CONFIG("1024","用户费率暂时未配置，稍后再试.."),
    BANK_NOT_CONFIG("1025","银行开户行数据未配置，稍后再试.."),
    BRAND_IS_DELETED("2001", "该品牌已被删除"),
    USER_PAY_PWD_NOT_SET("2009", "未设置提现支付密码"),
    ACCOUNT_BALANCE_INSUFFICIENT("2011", "账户余额不足"),
    WITHDRAW_PASSWORD_ERROR("2013","提现密码不正确"),
    REEZE_WITHDRAW_ERROR("2016","提现冻结金额小于0"),
    WITHDRAW_ACCOUNT_EMPTY("2019","支付宝账号或银行卡号为空"),
    USER_NOT_EXIST("2020", "用户不存在"),
    WITHDRAW_LES_MIN_AMOUNT("2028", "不得小于最低提现金额"),
    LESS_THAN_ONE_RMB("2029", "最低提现金额不得小于1元"),
    MOBLIE_CANNOT_BE_USED_AS_UNAME("2030", "手机号不能做用户名，请重新设置"),
    ILLEGAL_CHARACTER("2031", "用户名中不能含有@，请重新设置"),
    NO_SPECIAL_SYMBOLS("2032", "用户名中不能含有符号，请重新设置"),
    EXTENSION_CODE_NOT_EXIST("2033", "请输入正确的邀请码");
    ;
    private String code;
    private String msg;

    private MallResultStatus(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
