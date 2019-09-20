package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;

public interface SearchRecordService {

    Result<Integer> insertSearchRecordAsync(String keyword, Integer fuid);
}
