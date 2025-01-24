package com.vip.file.filter;

import com.vip.file.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class UrlFilter implements Filter {
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        StringBuffer urlBuffer = httpServletRequest.getRequestURL();
        String urlPath = urlBuffer.toString();
        String token = httpServletRequest.getHeader("Authorization");
        System.out.println("UrlFilter中的requestURL = " + urlPath +"\t Authorization = " + token);
        if (urlPath.contains("platform/work")) {
            //使用forward请求转发
            System.out.println("过滤器doFilter中调用,forward请求转发.");
            httpServletRequest.getRequestDispatcher("/fujian/shangban").forward(httpServletRequest, httpServletResponse);
            return;
        } else if (urlPath.contains("platform/touzi")) {
            //使用sendRedirect请求转发
            System.out.println("过滤器doFilter中调用,sendRedirect请求转发.");
            String redirectUrl = "http://localhost:8080/platform/fujian/touzi?num=1024";
            httpServletResponse.sendRedirect(redirectUrl);
            return;
        }

        // TODO:此处自定义访问控制逻辑
        if (StringUtils.isEmpty(token)) {
            token = httpServletRequest.getParameter("token");
            System.out.println("QueryString token = " + token);
        }

        Object cacheObject = redisUtil.getCacheObject(urlPath);
        if (cacheObject != null) {
            token = cacheObject.toString();
        }
        if (Objects.isNull(token)) {
            reject(httpServletResponse);
            return;
        }else{
            redisUtil.deleteObject(urlPath);
            redisUtil.setCacheObject(urlPath, token, 10, TimeUnit.MINUTES);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getOutputStream().write("Access Denied".getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().close();
    }
}