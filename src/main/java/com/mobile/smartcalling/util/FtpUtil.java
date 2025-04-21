package com.mobile.smartcalling.util;

import com.alibaba.fastjson.JSON;
//import com.mobile.business.config.ApplicationConfig;
//import com.mobile.business.load.aopgetway.utils.ZJWGSDKConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class FtpUtil {

    public static Map<String, List<String>> downLoad(Map<String, Object> map) {
        FTPClient ftpClient = new FTPClient();
        Map<String, List<String>> data = new HashMap<>();
        try {
            // fileName-ds
            log.info("===ftp downLoad: {}", JSON.toJSONString(map));
            String connectionString = (String) map.get("ConnectionString"); // FTP服务器地址
            String [] connection = connectionString.split("@");
            String[] remoteAddr = connection[1].split(":");
            String server = remoteAddr[0];
            int port = Integer.valueOf(remoteAddr[1]); // FTP服务器端口号
            String username = (String) map.get("userName"); // FTP登录用户名
            String password = (String) map.get("password"); // FTP登录密码
            String remoteDir = (String) map.get("Path"); // 远程路径
            String localPath = "/opt/massive/logs/"; // 本地文件保存路径
            List<Object> files = (List)map.get("files");  //文件名字

            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
            log.info("===ftp start connect: {},{}", server, port);
            ftpClient.connect(server, port);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setRemoteVerificationEnabled(false);
            //设置传输超时时间为300秒
            ftpClient.setDataTimeout(1000*300);
            //设置超时
            ftpClient.setSoTimeout(1000*300);
            //这句代码进行设置缓冲大小,这样的话就比原来快很多了
            ftpClient.setBufferSize(100000);

            for (Object obj: files) {
                Map<String, Object> file = (Map<String, Object>) obj;
                String dataStartTime = (String) file.get("dataStartTime");
                String ds = sdf2.format(sdf1.parse(dataStartTime));
                String fileName = (String) file.get("fileName");
                OutputStream outputStream = new FileOutputStream(localPath + fileName);
                boolean success = ftpClient.retrieveFile(remoteDir + fileName, outputStream);
                outputStream.close();

                if (success) {
                    log.info("File {} downloaded successfully.", (remoteDir + fileName));
                    List<String> dateStr = new ArrayList<>(2);
                    dateStr.add(ds);
                    dateStr.add(dataStartTime);
                    data.put(localPath + fileName, dateStr);
                } else {
                    log.error("File {} download failed.", (remoteDir + fileName));
                }
            }

        } catch (Exception e) {
            log.error("get 5G scheme data error ", e);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                log.error("close ftp client error", e);
            }
        }

        return data;
    }


    public static void upLoad(String server, int port, String username, String password,
                              String filePath, String remoteDirectoryPath) {
        FTPClient ftpClient = new FTPClient();
        try {
//            ApplicationConfig config = ZJWGSDKConfig.getBean(ApplicationConfig.class);
//            if (!StringUtils.isEmpty(config.getFtpUrl())) {
//                String tmp = new String(Base64.getDecoder().decode(config.getFtpUrl()), Charset.forName("UTF-8"));
//                String [] ftps = tmp.split("_");
//                server = ftps[0];
//                port = Integer.valueOf(ftps[1]);
//                username = ftps[2];
//                password = ftps[3];
//            }

            ftpClient.connect(server, port);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setRemoteVerificationEnabled(false);
            //设置传输超时时间为600秒
            ftpClient.setDataTimeout(1000*600);
            //设置超时
            ftpClient.setSoTimeout(1000*600);
            //这句代码进行设置缓冲大小,这样的话就比原来快很多了
            ftpClient.setBufferSize(100000);
            ftpClient.changeWorkingDirectory(remoteDirectoryPath);

            File localFile = new File(filePath);
            InputStream inputStream = new FileInputStream(localFile);

            String remoteFileName = localFile.getName();
            OutputStream outputStream = ftpClient.storeFileStream(remoteFileName);

            byte[] buffer = new byte[10240];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            boolean completed = ftpClient.completePendingCommand();
            if (completed) {
                log.info("The file is uploaded successfully.");
            } else {
                log.error("The file upload failed.");
            }

        } catch (Exception e) {
            log.error("upload file error", e);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                log.error("close ftp client error", e);
            }
        }
    }
}
