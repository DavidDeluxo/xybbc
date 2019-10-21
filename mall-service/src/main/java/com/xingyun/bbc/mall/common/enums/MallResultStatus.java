package com.xingyun.bbc.mall.common.enums;

import com.xingyun.bbc.core.enums.IResultStatus;
import lombok.Getter;

/**
 * @author lll
 * @Description:
 * @createTime: 2019-08-21 12:00
 */
@Getter
public enum MallResultStatus implements IResultStatus {

    UPLOAD_FAILED("1053", "上传失败"),
    UPLOAD_FILE_TYPE_ERROR("1052", "上传文件类型错误"),
    NOT_LOGIN("1099","请先登陆"),

    BATCH_PACKAGE_NUM_NOT_EXIST("1080", "查不到包装规格值")
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
    }}
