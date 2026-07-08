package com.mobile.smartcalling.service.impl;

import com.mobile.smartcalling.config.FtpConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Service
public class FtpService {

    @Autowired
    private FtpConfig ftpConfig;

    /**
     * 获取FTP客户端（使用try-finally手动释放）
     */
    private FTPClient getFtpClient() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
        ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
        return ftpClient;
    }

    /**
     * 上传文件到FTP
     */
    public void uploadFile(File file, String remoteFileName) {
        FTPClient ftpClient = null;
        try {
            ftpClient = getFtpClient();

            // 切换到远程目录
            if (ftpConfig.getRemoteDir() != null && !ftpConfig.getRemoteDir().isEmpty()) {
                boolean changed = ftpClient.changeWorkingDirectory(ftpConfig.getRemoteDir());
                if (!changed) {
                    log.warn("切换目录失败，尝试创建目录: {}", ftpConfig.getRemoteDir());
                    // 递归创建目录
                    String[] dirs = ftpConfig.getRemoteDir().split("/");
                    for (String dir : dirs) {
                        if (dir.isEmpty()) continue;
                        if (!ftpClient.changeWorkingDirectory(dir)) {
                            ftpClient.makeDirectory(dir);
                            ftpClient.changeWorkingDirectory(dir);
                        }
                    }
                }
            }

            // 上传文件
            try (FileInputStream fis = new FileInputStream(file)) {
                boolean uploaded = ftpClient.storeFile(remoteFileName, fis);
                if (uploaded) {
                    log.info("文件上传成功: {}", remoteFileName);
                } else {
                    log.error("文件上传失败: {}", remoteFileName);
                    log.error("FTP回复码: {}", ftpClient.getReplyCode());
                    log.error("FTP回复信息: {}", ftpClient.getReplyString());
                }
            }

        } catch (IOException e) {
            log.error("FTP上传异常", e);
            throw new RuntimeException("FTP上传失败: " + e.getMessage(), e);
        } finally {
            // 手动关闭FTP连接
            if (ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    log.warn("关闭FTP连接失败", e);
                }
            }
        }
    }
}