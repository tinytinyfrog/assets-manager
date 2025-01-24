package com.vip.file.filter;

import cn.novelweb.tool.http.Result;
import com.alibaba.fastjson.JSONObject;
import com.vip.file.constant.UrlConstant;
import com.vip.file.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = UrlConstant.API + "/*")
public class BusinessFilter implements Filter {
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setCharacterEncoding("UTF-8");
        //获取url
        String url = httpServletRequest.getRequestURI();
        log.info("url:{}", url);
        //判断是否包含login请求,是就放行
        if (url.contains("login")) {
            filterChain.doFilter(request, response);
            return;
        }
        //获取token
        String token = jwtUtils.getToken(httpServletRequest);
        //判断token是否存在
        if (StringUtils.isEmpty(token)) {
            Result error = Result.error("未登录");
            String noLogin = JSONObject.toJSONString(error);
            httpServletResponse.getWriter().write(noLogin);
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            log.info("url:{},{}", url, noLogin);
            return;
        }
        //解析token是否存在
        try {
            jwtUtils.parseJwt(token);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO
            //Result noLogin = Result.error("未登录");
            //String s = JSONObject.toJSONString(noLogin);
            //httpServletResponse.getWriter().write(s);
            //return;
        }
        log.info("令牌合法，放行:{}", token);
        filterChain.doFilter(request, response);
    }

}