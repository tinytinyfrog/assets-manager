package com.vip.file.controller;

import cn.novelweb.tool.upload.local.pojo.UploadFileParam;
import com.vip.file.config.SystemException;
import com.vip.file.constant.SysConstant;
import com.vip.file.constant.UrlConstant;
import com.vip.file.dto.AddFileDto;
import com.vip.file.dto.GetFileDto;
import com.vip.file.entity.Files;
import com.vip.file.response.*;
import com.vip.file.service.IFileService;
import com.vip.file.utils.EmptyUtils;
import com.vip.file.utils.EncodingUtils;
import com.vip.file.utils.InputStreamUtils;
import com.vip.file.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * <p>
 * 文件上传下载 前端控制器
 * </p>
 *
 * @author alex
 * @since 2020-05-29
 */
@Slf4j
@RestController
@RequestMapping(UrlConstant.API + "/auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping(value = "/verify")
    public RestResponse verify(HttpServletRequest request) {
        String token = request.getParameter("token");
        return RestResponses.newResponseFromResult(Results.newSuccessResult(token, "verify success"));
    }
}

