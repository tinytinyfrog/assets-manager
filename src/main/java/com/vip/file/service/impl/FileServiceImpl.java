package com.vip.file.service.impl;

import cn.hutool.core.util.ZipUtil;
import cn.novelweb.tool.upload.local.LocalUpload;
import cn.novelweb.tool.upload.local.pojo.UploadFileParam;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vip.file.constant.SysConstant;
import com.vip.file.dto.AddFileDto;
import com.vip.file.dto.GetFileDto;
import com.vip.file.entity.Files;
import com.vip.file.mapper.FilesMapper;
import com.vip.file.response.ErrorCode;
import com.vip.file.response.Result;
import com.vip.file.response.Results;
import com.vip.file.service.IFileService;
import com.vip.file.utils.EmptyUtils;
import com.vip.file.utils.NovelWebUtils;
import com.vip.file.utils.StreamZipUtil;
import com.vip.file.utils.UuidUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;

/**
 * <p>
 * 文件上传下载 服务实现类
 * </p>
 *
 * @author alex
 * @since 2020-05-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final FilesMapper filesMapper;
    @Value("${file.save-path:/data-center/files/vip-assets-manager}")
    private String savePath;
    @Value("${file.conf-path:/data-center/files/vip-assets-manager/conf}")
    private String confFilePath;
    private String indexHtml = "index.html";

    @Override
    public Result<List<GetFileDto>> getFileList(Integer pageNo, Integer pageSize) {
        try {
            PageHelper.startPage(pageNo, pageSize);
            List<GetFileDto> result = filesMapper.selectFileList();
            PageInfo<GetFileDto> pageInfo = new PageInfo<>(result);
            return Results.newSuccessResult(pageInfo.getList(), "查询成功", pageInfo.getTotal());
        } catch (Exception e) {
            log.error("获取文件列表出错", e);
        }
        return Results.newFailResult(ErrorCode.DB_ERROR, "查询失败");
    }

    @Override
    public Result<String> uploadFiles(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (EmptyUtils.basicIsEmpty(originalFilename)) {
                return Results.newFailResult(ErrorCode.FILE_ERROR, "文件名不能为空");
            }
            if (file.getSize() > SysConstant.MAX_SIZE) {
                return Results.newFailResult(ErrorCode.FILE_ERROR, "文件过大，请使用大文件传输");
            }
            String suffixName = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : null;
            // 新文件名（如果是压缩文件，解压后的目录名也保持一致）
            String newFileId = UuidUtils.uuid();
            String newName = newFileId + suffixName;
            // 重命名文件
            File newFile = new File(savePath, newName);
            // 如果该存储路径为空则新建存储路径
            if (!newFile.getParentFile().exists()) {
                newFile.getParentFile().mkdirs();
            }
            // 文件写入
            file.transferTo(newFile);
            // 文件存储位置
            String descDir = "";
            // 处理压缩文件
            if (suffixName.equals(".zip") || suffixName.equals(".rar")) {
                // 解压目标文件夹对象（压缩文件解压到此文件夹中）
                descDir = savePath + File.separator + StreamZipUtil.EXTRACT_PATH + File.separator + newFileId;
                //File extractFolder = new File(descDir);
                //dealZip(file, newFile, extractFolder);
                StreamZipUtil.unzip(newFile, descDir);
            } else {
                descDir = savePath;
            }
            // 保存文件信息
            Files files = new Files().setFilePath(descDir);
            files.setOriginFileName(originalFilename);
            // 用文件后缀区分文件类型
            files.setFileType(suffixName == null ? null : suffixName.substring(1));
            files.setFileName(newName);
            files.setFileSize(String.valueOf(file.getSize()));
            files.setId(newFileId);
            files.setIsDelete(0);
            filesMapper.insert(files);

            return Results.newSuccessResult(files.getId(), "上传完成");
        } catch (Exception e) {
            log.error("上传文件出错", e);
        }
        return Results.newFailResult(ErrorCode.FILE_ERROR, "上传失败");
    }

    /**
     * 处理压缩包文件
     *
     * @param zipFile       上传压缩包
     * @param destFile      指定压缩包路径
     * @param extractFolder 解压后文件夹
     */
    private void dealZip(MultipartFile zipFile, File destFile, File extractFolder) throws FileNotFoundException {
        //判断解压后文件夹是否存在
        if (!extractFolder.exists()) {
            //不存在就创建
            extractFolder.mkdirs();
        }
        try {
            if (!destFile.exists()) {
                //步骤1、把上传的压缩包文件保存到指定压缩包路径
                zipFile.transferTo(destFile);
            }
        } catch (IOException e) {
            //运行报错直接返回错误信息
            throw new RuntimeException(e);
        }
        //步骤2、调用Hutool的ZipUtil压缩工具类的unzip方法来进行对压缩包文件的解压，解压到指定目录
        ZipUtil.unzip(destFile, extractFolder);
        //解压缩完删除原文件（可不删）
        //destFile.delete();

        //步骤3、获取解压后目录下所有的文件
        File[] files = extractFolder.listFiles();
        //这边对获取到的文件数组进行判空校验
        if (files == null || files.length == 0) {
            //不存在就把压缩文件夹删除（可不删）
            //extractFolder.delete();
            log.error("上传失败，压缩包为空");
        }
    }

    @Override
    public Result checkFileMd5(String md5, String fileName) {
        try {
            cn.novelweb.tool.http.Result result = LocalUpload.checkFileMd5(md5, fileName, confFilePath, savePath);
            return NovelWebUtils.forReturn(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Results.newFailResult(ErrorCode.FILE_UPLOAD, "上传失败");
    }

    @Override
    public Result breakpointResumeUpload(UploadFileParam param, HttpServletRequest request) {
        try {
            // 这里的 chunkSize(分片大小) 要与前端传过来的大小一致
            cn.novelweb.tool.http.Result result = LocalUpload.fragmentFileUploader(param, confFilePath, savePath, 5242880L, request);
            return NovelWebUtils.forReturn(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Results.newFailResult(ErrorCode.FILE_UPLOAD, "上传失败");
    }

    @Override
    public Result<String> addFile(AddFileDto dto) {
        try {
            Files file = new Files();
            BeanUtils.copyProperties(dto, file);
            if (filesMapper.fileIsExist(dto.getFileName())) {
                return Results.newSuccessResult(null, "添加成功");
            } else if (filesMapper.insert(file.setFilePath(dto.getFileName())) == 1) {
                return Results.newSuccessResult(null, "添加成功");
            }
        } catch (Exception e) {
            log.error("添加文件出错", e);
        }
        return Results.newFailResult(ErrorCode.FILE_UPLOAD, "添加失败");
    }

    @Override
    public InputStream getFileInputStream(String id) {
        try {
            Files files = filesMapper.selectById(id);
            File file = new File(files.getFilePath() + File.separator + files.getFileName());
            return new FileInputStream(file);
        } catch (Exception e) {
            log.error("获取文件输入流出错", e);
        }
        return null;
    }

    @Override
    public InputStream getExternalFileInputStream(Files files) {
        try {
            String filePath = files.getFilePath();
            File file = new File(filePath + File.separator + indexHtml);
            return new FileInputStream(file);
        } catch (Exception e) {
            log.error("获取外部文件输入流出错", e);
        }
        return null;
    }

    @Override
    public Result<Files> getFileDetails(String id) {
        try {
            Files files = filesMapper.selectById(id);
            return Results.newSuccessResult(files, "查询成功");
        } catch (Exception e) {
            log.error("获取文件详情出错", e);
        }
        return Results.newFailResult(ErrorCode.DB_ERROR, "查询失败");
    }
}
