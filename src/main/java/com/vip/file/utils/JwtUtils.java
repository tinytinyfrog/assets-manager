package com.vip.file.utils;

import com.vip.file.constant.UrlConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    // 令牌自定义标识
    @Value("${token.header:Authorization}")
    private String header;

    // 令牌秘钥
    @Value("${token.secret:abcdefghijklmnopqrstuvwxyz}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    public String getToken(HttpServletRequest request) throws UnsupportedEncodingException {
        String token =  request.getHeader(header);
        if(StringUtils.isEmpty(token)){
            token = request.getParameter("token");
        }

        if (StringUtils.isNotEmpty(token) ) {
            token = URLDecoder.decode(token,"UTF-8");
            if(token.startsWith(UrlConstant.TOKEN_PREFIX)){
                token = token.replace(UrlConstant.TOKEN_PREFIX, "");
            }
        }
        return token;
    }

    //生成
    public String generateJwt(Map<String,Object> claims){
        return Jwts.builder()
                .setClaims(claims) //自定义内容
                .signWith(SignatureAlgorithm.HS256, secret)//签名算法
                .setExpiration(new Date(System.currentTimeMillis() + 12 * 3600)) //有效期
                .compact();
    }
    //解析
    public Claims parseJwt(String token){
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}