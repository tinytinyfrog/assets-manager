package com.vip.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * 文件管理服务
 *
 * @author alex
 */
@MapperScan(basePackages = {"com.vip.file.mapper"})
@SpringBootApplication
@ServletComponentScan
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }

}
