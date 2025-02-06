package com.mobile.smartcalling.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SftpUtil {

    public static Map<String, List<String>> download(Map<String, Object> map) {
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        Map<String, List<String>> data = new HashMap<>(); // 用于存储文件路径和日期信息的Map

        try {
            String connectionString = map.get("ConnectionString").toString();
            // 解析 ConnectionString
            String[] parts = connectionString.split("@");
            String[] authParts = parts[0].split("//")[1].split(":");
            String[] hostParts = parts[1].split(":");

            String username = authParts[0];
            String password = authParts[1];
            String server = hostParts[0];
            int port = Integer.parseInt(hostParts[1]); // 端口
            if (port==3000) {
                // 过渡期强行写值
                port = 3001;
            }

            String remoteDir = map.get("Path").toString(); // 远程目录路径
            List<Map<String, String>> files = (List<Map<String, String>>) map.get("files"); // 文件列表

            JSch jsch = new JSch();
            session = jsch.getSession(username, server, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();

            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(remoteDir);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
            for (Map<String, String> fileDetail : files) {
                String fileName = fileDetail.get("fileName");
                String localPath = "/opt/massive/logs/" + fileName; // 假定的本地路径
                try (FileOutputStream fos = new FileOutputStream(localPath)) {
                    channelSftp.get(fileName, fos);
                    log.info("Successfully downloaded file: {}", fileName);

                    String dataStartTime = fileDetail.get("dataStartTime");
                    String ds = sdf2.format(sdf1.parse(dataStartTime));

                    List<String> fileInfo = new ArrayList<>(); // 用于存储文件的额外信息
                    fileInfo.add(ds);
                    fileInfo.add(fileDetail.get("dataStartTime")); // 假设每个文件都有一个与之关联的日期时间
                    data.put(localPath, fileInfo); // 将文件路径和其日期时间存储在Map中
                } catch (Exception e) {
                    log.error("Failed to download file: {}", fileName, e);
                }
            }
        } catch (Exception e) {
            log.error("Download via SFTP failed", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.exit();
            }
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        return data; // 返回包含下载的文件路径和日期信息的Map
    }
    public static void upload(String server, int port, String username, String password,
                              String filePath, String remoteDirectoryPath) {
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, server, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();

            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(remoteDirectoryPath);

            File file = new File(filePath);
            channelSftp.put(new FileInputStream(file), file.getName());

            log.info("File {} uploaded successfully.", file.getName());

        } catch (Exception e) {
            log.error("Upload via SFTP failed", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.exit();
            }
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
