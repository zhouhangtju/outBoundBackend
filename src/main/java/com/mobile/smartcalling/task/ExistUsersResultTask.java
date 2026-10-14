package com.mobile.smartcalling.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mobile.smartcalling.common.CommonBusinessStrEnum;
import com.mobile.smartcalling.dao.RemoteCallResultDao;
import com.mobile.smartcalling.entity.RemoteCallResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 存量维系外呼结果回传定时任务
 * 每天晚上8点：查询 RemoteCallResult 表中 存量维系、创建时间为昨天的数据，
 * 生成 手机号|上网质量满意度打分|安装维修满意度打分|不满意原因|是否需要上门|开始时间 格式的CSV，
 * 上传到 FTP /data1/dcpp/ZXAIRE 目录，文件名 yyyy-MM-dd_ExistUsersResult.csv（当天生成日期）
 */
@Slf4j
@Component
public class ExistUsersResultTask {

    private static final String FTP_HOST = "10.76.148.39";
    private static final int FTP_PORT = 21;
    private static final String FTP_USERNAME = "dcpp";
    private static final String FTP_PASSWORD = "D8is_F7n61#15";
    private static final String FTP_REMOTE_DIR = "/data1/dcpp/ZXAIRE";


    private static final String CSV_HEADER = "手机号|上网质量满意度打分|安装维修满意度打分|不满意原因|是否需要上门|开始时间";

    @Autowired
    private RemoteCallResultDao remoteCallResultDao;


    @Scheduled(cron = "0 0 20 * * ?")
    public void execute() {
        log.info("====开始执行存量维系外呼结果回传定时任务  查询remote_call_result 表 构建指定csv 推送到ftp===");

        try {

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date todayStart = cal.getTime();


            Calendar tomorrowCal = (Calendar) cal.clone();
            tomorrowCal.add(Calendar.DAY_OF_MONTH, 1);
            Date tomorrowStart = tomorrowCal.getTime();

            LambdaQueryWrapper<RemoteCallResult> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RemoteCallResult::getTask, CommonBusinessStrEnum.SATISFACTION_SURVEY_FOLLOW_UP_DD)
                    .ge(RemoteCallResult::getCreateTime, todayStart)
                    .lt(RemoteCallResult::getCreateTime, tomorrowStart);

            List<RemoteCallResult> dataList = remoteCallResultDao.selectList(queryWrapper);
            log.info("查询到昨天存量维系数据 {} 条", dataList.size());

            if (dataList.isEmpty()) {
                log.info("没有需要回传的存量维系数据，任务结束");
                return;
            }


            File csvFile = buildCsvFile(dataList);


            try {
                uploadToFtp(csvFile);
                log.info("存量维系外呼结果文件上传成功: {}", csvFile.getName());
            } finally {
                if (!csvFile.delete()) {
                    log.warn("临时文件删除失败: {}", csvFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            log.error("存量维系外呼结果回传任务执行失败", e);
        }
    }

    /**
     * 生成CSV临时文件
     * 格式：手机号|上网质量满意度打分|安装维修满意度打分|不满意原因|是否需要上门|开始时间
     * 字段：phoneNum|tag3|tag4|tag5|tag7|call_date
     */
    private File buildCsvFile(List<RemoteCallResult> dataList) throws Exception {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String fileName = dateStr + "_ExistUsersResult.csv";

        File tempFile = new File(File.createTempFile("existUsers_", ".csv").getParentFile(), fileName);
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write(CSV_HEADER);
            writer.write("\r\n");
            for (RemoteCallResult record : dataList) {
                StringBuilder line = new StringBuilder();
                line.append(nullSafe(record.getPhoneNum())).append("|")
                        .append(nullSafe(record.getTag3())).append("|")
                        .append(nullSafe(record.getTag4())).append("|")
                        .append(nullSafe(record.getTag5())).append("|")
                        .append(nullSafe(record.getTag7())).append("|")
                        .append(nullSafe(record.getCallDate()));
                writer.write(line.toString());
                writer.write("\r\n");
            }
            writer.flush();
        }
        log.info("CSV文件生成成功: {}, 数据 {} 条", tempFile.getAbsolutePath(), dataList.size());
        return tempFile;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    /**
     * 上传文件到指定FTP目录（带结果校验，失败抛异常）
     */
    private void uploadToFtp(File file) throws Exception {

        log.info("存量维系外呼结果开始上传文件到FTP: {}", file.getName());
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.setConnectTimeout(10000);
            ftpClient.setDataTimeout(300000);
            ftpClient.setSoTimeout(300000);

            ftpClient.connect(FTP_HOST, FTP_PORT);
//            ftpClient.login(FTP_USERNAME, FTP_PASSWORD);


            int connectReply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(connectReply)) {
                ftpClient.disconnect();
                throw new RuntimeException(String.format("FTP连接被拒绝, host=%s, port=%d, replyCode=%d",
                        FTP_HOST, FTP_PORT, connectReply));
            }


            boolean loginSuccess = ftpClient.login(FTP_USERNAME, FTP_PASSWORD);
            if (!loginSuccess) {
                throw new RuntimeException(String.format("FTP登录失败, user=%s, replyCode=%d, reply=%s",
                        FTP_USERNAME, ftpClient.getReplyCode(), ftpClient.getReplyString()));
            }
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            boolean changed = ftpClient.changeWorkingDirectory(FTP_REMOTE_DIR);
            if (!changed) {
                log.warn("切换目录失败，尝试创建目录: {}", FTP_REMOTE_DIR);
                // 递归创建目录
                String[] dirs = FTP_REMOTE_DIR.split("/");
                for (String dir : dirs) {
                    if (dir.isEmpty()) {
                        continue;
                    }
                    if (!ftpClient.changeWorkingDirectory(dir)) {
                        ftpClient.makeDirectory(dir);
                        ftpClient.changeWorkingDirectory(dir);
                    }
                }
            }

            boolean uploaded;
            try (FileInputStream fis = new FileInputStream(file)) {
                uploaded = ftpClient.storeFile(file.getName(), fis);
            }
            if (uploaded) {
                log.info("ExistUsersResultTask  文件上传成功: {}", file.getName());
            } else {
                log.error("ExistUsersResultTask  文件上传失败: {} | 回复码: {} | 回复信息: {}",
                        file.getName(), ftpClient.getReplyCode(), ftpClient.getReplyString());
                throw new RuntimeException(String.format("ExistUsersResultTask  FTP上传失败: %s | 回复码: %s | 回复信息: %s",
                        file.getName(), ftpClient.getReplyCode(), ftpClient.getReplyString()));
            }
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (Exception e) {
                    log.warn("关闭FTP连接失败", e);
                }
            }
        }
    }
}
