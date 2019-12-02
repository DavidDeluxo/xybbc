package com.xingyun.bbc.mallpc.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.xingyun.bbc.common.jwt.XyUserJwtManager;
import com.xingyun.bbc.mallpc.model.vo.TokenInfoVo;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class JwtParser {

    private static final String ACCESS_TOKEN = "accessToken";

    @Autowired
    private XyUserJwtManager userJwtManager;

    public TokenInfoVo getTokenInfo(HttpServletRequest request) {
        if (StringUtils.isEmpty(request.getHeader(ACCESS_TOKEN))) {
            return getNotLoginVo();
        }

        String token = request.getHeader(ACCESS_TOKEN);
        log.info("-------------商城PC，token : ", token);
        Claims claims = userJwtManager.parseJwt(token);
        if (claims == null) {
            return getNotLoginVo();
        }
        String tokenInfoJson = claims.getSubject();
        TokenInfoVo infoVo;
        try {
            infoVo = JSON.parseObject(tokenInfoJson, TokenInfoVo.class);
        } catch (JSONException e) {
            infoVo = new TokenInfoVo();
        }
        String id = claims.getId();
        infoVo.setIsLogin(true);
        infoVo.setFuid(Integer.parseInt(id));
        return infoVo;
    }

    private TokenInfoVo getNotLoginVo() {
        TokenInfoVo infoVo = new TokenInfoVo();
        infoVo.setIsLogin(false);
        return infoVo;
    }
}
