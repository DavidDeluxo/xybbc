package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.UserFeedBackDto;
import com.xingyun.bbc.mall.model.vo.UserFeedBackVo;

import java.util.List;

public interface UserFeedBackService {

    Result<List<UserFeedBackVo>> getUserFeedBackType();

    Result saveUserFeedBack (UserFeedBackDto dto);


}
