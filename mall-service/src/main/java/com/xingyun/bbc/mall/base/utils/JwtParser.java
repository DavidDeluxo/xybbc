package com.xingyun.bbc.mall.base.utils;

import com.xingyun.bbc.common.jwt.XyUserJwtManager;
import com.xingyun.bbc.mall.model.vo.TokenInfoVo;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class JwtParser {

    private static final String ACCESS_TOKEN = "accessToken";

    @Autowired
    private XyUserJwtManager userJwtManager;

    public TokenInfoVo getTokenInfo(HttpServletRequest request){
        if(StringUtils.isEmpty(request.getHeader(ACCESS_TOKEN))){
            return getNotLoginVo();
        }

        String token = request.getHeader(ACCESS_TOKEN);
        Claims claims = userJwtManager.parseJwt(token);
        TokenInfoVo infoVo = new TokenInfoVo();
        if (claims == null) {
            return getNotLoginVo();
        }
        String id = claims.getId();
        infoVo.setIsLogin(true);
        infoVo.setFuid(Integer.parseInt(id));
        return infoVo;
    }

    private TokenInfoVo getNotLoginVo(){
        TokenInfoVo infoVo = new TokenInfoVo();
        infoVo.setIsLogin(false);
        return infoVo;
    }
}
