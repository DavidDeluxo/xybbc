package com.xingyun.bbc.mallpc.model.vo.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class withDrawAccountDetailVo {
    private Integer type;
    private String name;
    private String account;
    private String bankName;
    private Integer amount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fcreateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fpassedTime;


}
