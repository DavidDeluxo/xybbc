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

    /**
     * 业务级别异常
     */
    public static final MallPcExceptionCode AUTO_LOGIN_FAILED = new MallPcExceptionCode("1000", "自动登录失败");
    public static final MallPcExceptionCode LOGIN_FAILED = new MallPcExceptionCode("1000", "登录失败");
    public static final MallPcExceptionCode ACCOUNT_FREEZE = new MallPcExceptionCode("1001", "账户冻结中");
    public static final MallPcExceptionCode REGISTER_MOBILE_EXIST = new MallPcExceptionCode("1002", "手机号已注册");
    public static final MallPcExceptionCode ACCOUNT_NOT_EXIST = new MallPcExceptionCode("1003", "账户不存在");
    public static final MallPcExceptionCode BIND_MOBILE_ERROR = new MallPcExceptionCode("1004", "请输入正确的手机号码");
    public static final MallPcExceptionCode SMS_AUTH_IS_SEND = new MallPcExceptionCode("1005", "验证码已发送,请稍后再试");
    public static final MallPcExceptionCode USER_SEND_SMS_FAILD = new MallPcExceptionCode("1006", "您提交的太频繁，请明天再试");
    public static final MallPcExceptionCode SMS_AUTH_NUM_ERROR = new MallPcExceptionCode("1007", "请输入正确的验证码");
    public static final MallPcExceptionCode PASSWORD_CAN_NOT_BE_NULL = new MallPcExceptionCode("1008", "密码不能为空");
    public static final MallPcExceptionCode PASSWORD_ILLEGAL = new MallPcExceptionCode("1009", "密码长度不符合要求(6-32位)");
    public static final MallPcExceptionCode USER_NOT_LOGGED_IN = new MallPcExceptionCode("1010", "用户未登录");
    public static final MallPcExceptionCode USER_CANNOT_VERIFY = new MallPcExceptionCode("1011", "只有未认证或认证失败的用户可以提交认证");

}
