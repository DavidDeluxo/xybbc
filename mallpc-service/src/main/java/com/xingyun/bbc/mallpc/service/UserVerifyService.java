package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.mallpc.model.dto.user.UserVerifyDTO;
import com.xingyun.bbc.mallpc.model.vo.user.UserVerifyVO;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
public interface UserVerifyService {

    /**
     * 添加用户认证
     * @param dto
     */
    void verify(UserVerifyDTO dto);

    /**
     * 用户认证信息详情
     * @return
     */
    UserVerifyVO view();
}
