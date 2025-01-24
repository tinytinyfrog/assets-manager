package com.vip.file.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 流zip工具类
 */
@Slf4j
public final class StreamZipUtil {

    public static String EXTRACT_PATH =  "extract";

    /**
     * 解压流
     *
     * @param inputStream
     * @return
     */
    @SneakyThrows
    public static InputStream unzipStream(InputStream inputStream) {
        //Fail-Fast
        if (inputStream == null) {
            return null;
        }

	    //1.采用jdk原生Zip流，会因为发送方和接收方，在处理文件或文件夹名称时用的【字符集编码】不匹配，而报MALFORMED错（畸形的）
        //ZipInputStream zipInputStream = new ZipInputStream(inputStream);
      
      	//2.Apach-commons-compress的Zip流，兼容性更好
      	ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(inputStream);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (zipInputStream.getNextEntry() != null) {
            int n;
            byte[] buff = new byte[1024];
            while ((n = zipInputStream.read(buff)) != -1) {
                bos.write(buff, 0, n);
            }
        }

        bos.flush();
        bos.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }

    /**
     * 压缩流
     *
     * @param txtName
     * @param inputStream
     * @return
     */
    @SneakyThrows
    public static ByteArrayOutputStream zipStream(String txtName, ByteArrayInputStream inputStream) {
        //Fail-Fast
        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(outputStream);
        zipOut.putNextEntry(new ZipEntry(txtName));

        int n;
        byte[] buffer = new byte[1024];
        while ((n = inputStream.read(buffer)) != -1) {
            zipOut.write(buffer, 0, n);
        }

        zipOut.close();
        inputStream.close();
        outputStream.close();
        return outputStream;
    }


    public static void unzip(File zipFile, String descDir) {
        try (ZipArchiveInputStream inputStream = getZipFile(zipFile)) {
            log.info(">>>>>>>>>>>开始解压>>>>>>>>>>>");
            File pathFile = new File(descDir);
            if (!pathFile.exists()) {
                //pathFile目录和压缩文件名newFilePrefix一致
                pathFile.mkdirs();
            }
            ZipArchiveEntry entry = null;
            while ((entry = inputStream.getNextZipEntry()) != null) {
                if (entry.isDirectory()) {
                    File directory = new File(descDir, entry.getName());
                    directory.mkdirs();
                } else {
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(new FileOutputStream(new File(descDir, entry.getName())));
                        //输出文件路径信息
                        log.info("解压文件的当前路径为:{}", descDir + entry.getName());
                        IOUtils.copy(inputStream, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
            //提取解压文件到pathFile目录
            extracted(pathFile);
            log.info("**********解压完毕**********");

        } catch (Exception e) {
            log.error("[unzip] 解压zip文件出错", e);
        }
    }

    private static void extracted(File pathFile) throws IOException {
        final File[] files = pathFile.listFiles();
        if (files != null && files.length == 1 && files[0].isDirectory()) {
            // 说明只有一个文件夹
            FileUtils.copyDirectory(files[0], pathFile);
            // 免得删除错误， 删除的文件必须在/xxx/extract/目录下。
            boolean isValid = files[0].getPath().contains(EXTRACT_PATH);
            if (isValid) {
                FileUtils.forceDelete(files[0]);
            }
        }
    }

    private static ZipArchiveInputStream getZipFile(File zipFile) throws Exception {
        return new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
    }
}
