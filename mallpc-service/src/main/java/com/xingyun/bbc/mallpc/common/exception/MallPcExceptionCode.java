package com.xingyun.bbc.mallpc.common.exception;

import com.xingyun.bbc.core.enums.IResultStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 运营中台错误码
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-18
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Getter
@Setter
@Accessors(chain = true)
public class MallPcExceptionCode implements IResultStatus {

    private String code;
    private String msg;

    public MallPcExceptionCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public MallPcExceptionCode bulid(Object... args) {
        return new MallPcExceptionCode(this.getCode(), String.format(this.getMsg(), args));
    }

    @Override
    public String toString() {
        return "MallPcExceptionCode{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }

    /**
     * 系统级别异常
     */
    public static final MallPcExceptionCode SYSTEM_ERROR = new MallPcExceptionCode("9999", "系统异常");
    public static final MallPcExceptionCode SYSTEM_BUSY_ERROR = new MallPcExceptionCode("9998", "系统繁忙");
    public static final MallPcExceptionCode PARAM_ERROR = new MallPcExceptionCode("9997", "参数错误");
    public static final MallPcExceptionCode REQUIRED_PARAM_MISSING = new MallPcExceptionCode("9996", "请求参数缺失");
    public static final MallPcExceptionCode OPERATION_FAILED = new MallPcExceptionCode("9995", "操作失败");
    public static final MallPcExceptionCode DATA_EXCEPTION = new MallPcExceptionCode("9994", "数据异常");
    public static final MallPcExceptionCode UPDATE_FAILED = new MallPcExceptionCode("9993", "更新失败");
    public static final MallPcExceptionCode UNSUPPORTED_OPERATION = new MallPcExceptionCode("9992", "不支持的操作");
    public static final MallPcExceptionCode UNSUPPORTED_INVALID_PERMISSIONS = new MallPcExceptionCode("9991", "token 无效");
    public static final MallPcExceptionCode FILE_NOT_VALID_EXCEL = new MallPcExceptionCode("9990", "文件不是有效的excel");
    public static final MallPcExceptionCode FILE_SIZE_MAXIMUM = new MallPcExceptionCode("9989", "文件不能大于%s");
    public static final MallPcExceptionCode EXCEL_FILE_CONTENT_IS_EMPTY = new MallPcExceptionCode("9988", "文件内容为空");
    public static final MallPcExceptionCode NOT_REPEAT_IMPORT = new MallPcExceptionCode("9987", "批量导入中，请稍后！");
    public static final MallPcExceptionCode RECORD_NOT_EXIST = new MallPcExceptionCode("9986", "记录不存在");
    public static final MallPcExceptionCode PERMISSION_DENIED = new MallPcExceptionCode("9985", "没有权限");

    /**
     * 业务级别异常
     */
    public static final MallPcExceptionCode AUTO_LOGIN_FAILED = new MallPcExceptionCode("1000", "自动登录失败");
    public static final MallPcExceptionCode LOGIN_FAILED = new MallPcExceptionCode("1001", "账号或密码错误");
    public static final MallPcExceptionCode ACCOUNT_FREEZE = new MallPcExceptionCode("1002", "账户冻结中");
    public static final MallPcExceptionCode REGISTER_MOBILE_EXIST = new MallPcExceptionCode("1003", "手机号已注册");
    public static final MallPcExceptionCode ACCOUNT_NOT_EXIST = new MallPcExceptionCode("1004", "账户不存在");
    public static final MallPcExceptionCode BIND_MOBILE_ERROR = new MallPcExceptionCode("1005", "请输入正确的手机号码");
    public static final MallPcExceptionCode SMS_AUTH_IS_SEND = new MallPcExceptionCode("1006", "验证码已发送,请稍后再试");
    public static final MallPcExceptionCode USER_SEND_SMS_FAILD = new MallPcExceptionCode("1007", "您提交的太频繁，请明天再试");
    public static final MallPcExceptionCode SMS_AUTH_NUM_ERROR = new MallPcExceptionCode("1008", "请输入正确的验证码");
    public static final MallPcExceptionCode PASSWORD_CAN_NOT_BE_NULL = new MallPcExceptionCode("1009", "密码不能为空");
    public static final MallPcExceptionCode PASSWORD_ILLEGAL = new MallPcExceptionCode("1010", "密码长度不符合要求(6-32位)");
    public static final MallPcExceptionCode USER_NOT_LOGGED_IN = new MallPcExceptionCode("1011", "用户未登录");
    public static final MallPcExceptionCode USER_CANNOT_VERIFY = new MallPcExceptionCode("1012", "只有未认证或认证失败的用户可以提交认证");
    public static final MallPcExceptionCode EXTENSION_CODE_NOT_EXIST = new MallPcExceptionCode("1013", "请输入正确的邀请码");
    public static final MallPcExceptionCode COUPON_NOT_EXIST = new MallPcExceptionCode("1014", "优惠券配置不存在");
    public static final MallPcExceptionCode PASSWORD_NOT_CHANGE = new MallPcExceptionCode("1015", "新密码不能和旧密码相同");
    public static final MallPcExceptionCode ID_CARD_NUMBER_ILLEGAL = new MallPcExceptionCode("1016", "请输入正确的身份证号码");
    public static final MallPcExceptionCode USER_DELIVERY_ADDRESS_NOT_EXISTS = new MallPcExceptionCode("1017", "用户收货地址不存在");
    public static final MallPcExceptionCode UPLOAD_FILE_TYPE_ERROR = new MallPcExceptionCode("1018", "上传文件类型错误");
    public static final MallPcExceptionCode UPLOAD_FAILED = new MallPcExceptionCode("1019", "上传失败");
    public static final MallPcExceptionCode USER_NOT_EXIST = new MallPcExceptionCode("1020", "用户不存在");
    public static final MallPcExceptionCode SKU_BATCH_IS_NONE = new MallPcExceptionCode("1021", "没有批次");
    public static final MallPcExceptionCode NO_USER = new MallPcExceptionCode("1022", "没有用户信息");
    public static final MallPcExceptionCode NO_BATCH_USER_PRICE = new MallPcExceptionCode("1023", "没有SKU批次会员类型售价");
    public static final MallPcExceptionCode NO_BATCH_PRICE = new MallPcExceptionCode("1024", "没有SKU批次类型售价");
    public static final MallPcExceptionCode WITHDRAW_PROCESSING = new MallPcExceptionCode("1025", "您的操作太频繁啦,每次提现需间隔5分钟");
    public static final MallPcExceptionCode NO_USER_ID = new MallPcExceptionCode("1026", "未传用户id");
    public static final MallPcExceptionCode NO_USER_CATEGORY_ID = new MallPcExceptionCode("1027", "未传一级类目ID");
    public static final MallPcExceptionCode BATCH_PACKAGE_NUM_NOT_EXIST = new MallPcExceptionCode("1028", "查不到包装规格值");
    public static final MallPcExceptionCode SHOPPING_CART_NOT_EXIST = new MallPcExceptionCode("1029", "进货单商品不存在");
    public static final MallPcExceptionCode BUSINESSLICENSENO_REPEAT = new MallPcExceptionCode("1030", "该营业执照编号已注册");

    public static final MallPcExceptionCode USER_PAY_PWD_NOT_SET = new MallPcExceptionCode("1031", "未设置提现支付密码");
    public static final MallPcExceptionCode WITHDRAW_ACCOUNT_EMPTY = new MallPcExceptionCode("1032","支付宝账号或银行卡号为空");
    public static final MallPcExceptionCode ACCOUNT_BALANCE_INSUFFICIENT = new MallPcExceptionCode("1033", "账户余额不足");
    public static final MallPcExceptionCode WITHDRAW_PASSWORD_ERROR = new MallPcExceptionCode("1034","提现密码不正确");
    public static final MallPcExceptionCode  BANK_NOT_CONFIG = new MallPcExceptionCode("1035","银行开户行数据未配置，稍后再试..");
    public static final MallPcExceptionCode  REEZE_WITHDRAW_ERROR = new MallPcExceptionCode("1036","提现冻结金额小于0");
    public static final MallPcExceptionCode  LESS_THAN_ONE_RMB = new MallPcExceptionCode("1037", "最低提现金额不得小于1元");
    public static final MallPcExceptionCode  WITHDRAW_LES_MIN_AMOUNT = new MallPcExceptionCode("1038", "不得小于最低提现金额");
    public static final MallPcExceptionCode  NAME_NOT_EXIST = new MallPcExceptionCode("1039", "账号不存在");
    public static final MallPcExceptionCode  ACCOUNT_NOT_AUTH = new MallPcExceptionCode("1040", "账户未认证或认证不通过");
    public static final MallPcExceptionCode SKU_PACKAGE_IS_NONE = new MallPcExceptionCode("1041", "没有批次包装规格");

    /**
     * pay模块错误信息
     */
    public static final MallPcExceptionCode ORDER_NOT_EXIST = new MallPcExceptionCode("7777", "订单不存在");
    public static final MallPcExceptionCode ORDER_NOT_MATCHING = new MallPcExceptionCode("7778", "订单与用户不匹配");
    public static final MallPcExceptionCode ORDER_AS_CANCELLED = new MallPcExceptionCode("7779", "该订单已取消");
    public static final MallPcExceptionCode ORDER_IS_COMPLETION = new MallPcExceptionCode("7779", "支付已完成的交易");
    public static final MallPcExceptionCode WITHDRAW_PSD_WRONG = new MallPcExceptionCode("7780", "支付密码错误");
    public static final MallPcExceptionCode PAY_PWD_IS_NOT_SET = new MallPcExceptionCode("7781", "支付密码未设置");
    public static final MallPcExceptionCode THIRD_PAY_NOTIFY_FAIL = new MallPcExceptionCode("7782", "返回回调失败字符串");
    public static final MallPcExceptionCode REMITTANCE_PAY_FAIL = new MallPcExceptionCode("7783", "线下汇款充值失败");
    public static final MallPcExceptionCode BALANCE_NOT_ENOUGH = new MallPcExceptionCode("7784", "用户余额为0,请充值!");
    public static final MallPcExceptionCode FREEZE_WITHDRAW = new MallPcExceptionCode("7785", "用户账户信息有误，请联系客服!");
    public static final MallPcExceptionCode ORDER_IS_OVERDUE = new MallPcExceptionCode("7786", "订单已过期");

    /**
     * 用户账号模块
     */
    public static final MallPcExceptionCode USER_FREEZE_ERROR = new MallPcExceptionCode("8888", "该用户已被冻结或禁用");
    public static final MallPcExceptionCode NULL_ERROR_WITHDRAWPSD = new MallPcExceptionCode("8889", "尚未设置提现密码");

    /**
     * Mall优惠券错误信息
     */
    public static final MallPcExceptionCode COUPON_IS_INVALID = new MallPcExceptionCode("6000", "优惠券已过期");
    public static final MallPcExceptionCode COUPON_IS_MAX = new MallPcExceptionCode("6001", "该优惠券已领取过");
    public static final MallPcExceptionCode COUPON_LINK_INEXUSTENCE = new MallPcExceptionCode("6002", "优惠券链接已失效");
    public static final MallPcExceptionCode COUPON_IS_PAID_OUT = new MallPcExceptionCode("6003", "优惠券已发放完");
    public static final MallPcExceptionCode COUPON_IS_NOT_EXIST = new MallPcExceptionCode("6004", "优惠券不存在");
    public static final MallPcExceptionCode COUPON_INELIGIBILITY = new MallPcExceptionCode("6005", "暂不满足领取资格哦~");
    public static final MallPcExceptionCode CODE_NOT_COUPON = new MallPcExceptionCode("6006", "券码无匹配优惠券");
    public static final MallPcExceptionCode USER_NOT_COUPON = new MallPcExceptionCode("6007", "用户无匹配优惠券");
    public static final MallPcExceptionCode USER_NOT_RIGHT_COUPON = new MallPcExceptionCode("6008", "用户暂无权限兑换该类优惠券");
    public static final MallPcExceptionCode COUPON_IS_NOT_TIME = new MallPcExceptionCode("6009", "优惠券不在领取时间内");
    public static final MallPcExceptionCode CODE_IS_USED = new MallPcExceptionCode("6010", "该券码已兑换过");

    /**
     * SKU错误信息
     */
    public static final MallPcExceptionCode SKU_IS_NONE = new MallPcExceptionCode("5000", "查不到sku信息");
}
