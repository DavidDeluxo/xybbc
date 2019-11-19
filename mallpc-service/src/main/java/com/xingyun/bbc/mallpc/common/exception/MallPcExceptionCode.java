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
    public static final MallPcExceptionCode USER_NOT_LOGGED_IN = new MallPcExceptionCode("1002", "用户未登录");
    public static final MallPcExceptionCode USER_CANNOT_VERIFY = new MallPcExceptionCode("1003", "只有未认证或认证失败的用户可以提交认证");

}
