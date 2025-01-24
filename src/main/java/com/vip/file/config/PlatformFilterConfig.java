package com.vip.file.config;

import com.vip.file.filter.UrlFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class PlatformFilterConfig {
  //过滤器
  @Bean("urlFilter")
  public UrlFilter urlFilter() {
    System.out.println("注册过滤器1......");
    return new UrlFilter();
  }
  
  //注册过滤器
  @Bean
  public FilterRegistrationBean filterRegistrationBean() {
    System.out.println("注册过滤器2......");
    FilterRegistrationBean filterReg = new FilterRegistrationBean();
    filterReg.setFilter(urlFilter());
    filterReg.addUrlPatterns("/*");
    filterReg.setOrder(1);
    return filterReg;
  }
}