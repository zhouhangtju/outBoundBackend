package com.mobile.smartcalling.service.impl;

import com.mobile.smartcalling.common.TaskTypeEnum;
import com.mobile.smartcalling.entity.RemoteCallResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class CsvExportService {

    @Autowired
    private FtpService ftpService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成CSV文件并上传到FTP
     */
    public void exportAndUploadCsv(List<RemoteCallResult> dataList, TaskTypeEnum taskType) throws IOException {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("任务类型 {} 没有数据，跳过生成", taskType.getChineseName());
            return;
        }

        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String fileName = String.format("%s_%s.csv", taskType.getFilePrefix(), dateStr);

        // 构建csv文件
        File tempFile = File.createTempFile("csv_", ".csv");
        try (FileOutputStream fos = new FileOutputStream(tempFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(osw, CSVFormat.DEFAULT.builder()
                     .setHeader(getHeaders(taskType))
                     .build())) {

            // 写入数据
            for (RemoteCallResult record : dataList) {
                printer.printRecord(getRowData(record, taskType));
            }
            printer.flush();
        }

        // @TODO  在yml 灵活配置  路径啥的   上传到FTP
        ftpService.uploadFile(tempFile, fileName);
        log.info("成功生成并上传CSV文件: {}", fileName);

        // 删除临时文件
        if (!tempFile.delete()) {
            log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
        }
    }

    /**
     * 获取CSV表头
     */
    private String[] getHeaders(TaskTypeEnum taskType) {
        return switch (taskType) {
            case INSTALLATION_NINGBO -> new String[]{
                    "task", "city", "order_id", "customer_account",
                    "line_status", "arrive_time", "tag1", "tag2", "tag3", "tag4"
            };
            case INSTALLATION_OTHER -> new String[]{
                    "task", "city", "order_id", "customer_account",
                    "line_status", "arrive_time", "tag1", "tag3", "tag4", "tag5"
            };
            case POOR_QUALITY_DISPATCH -> new String[]{
                    "task", "city", "order_id", "customer_account",
                    "line_status", "是否需要上门", "上门时间"
            };
            case POOR_QUALITY_REPAIR -> new String[]{
                    "task", "city", "order_id", "customer_account",
                    "line_status", "arrive_time", "tag1", "tag4", "tag5", "tag6"
            };
            case POOR_QUALITY_REPAIR_NOT_VISIT -> new String[]{
                    "task", "city", "order_id", "customer_account",
                    "line_status", "arrive_time", "tag1", "tag6", "tag7", "tag8"
            };
        };
    }

    /**
     * 获取CSV行数据
     */
    private Object[] getRowData(RemoteCallResult record, TaskTypeEnum taskType) {
        return switch (taskType) {
            case INSTALLATION_NINGBO -> new Object[]{
                    record.getTask(), record.getCity(), record.getOrderId(),
                    record.getCustomerAccount(), record.getLineStatus(),
                    record.getArriveTime(), record.getTag1(), record.getTag2(),
                    record.getTag3(), record.getTag4()
            };
            case INSTALLATION_OTHER -> new Object[]{
                    record.getTask(), record.getCity(), record.getOrderId(),
                    record.getCustomerAccount(), record.getLineStatus(),
                    record.getArriveTime(), record.getTag1(), record.getTag3(),
                    record.getTag4(), record.getTag5()
            };
            case POOR_QUALITY_DISPATCH -> new Object[]{
                    record.getTask(), record.getCity(), record.getOrderId(),
                    record.getCustomerAccount(), record.getLineStatus(),
                    record.getTag1(), // 是否需要上门
                    record.getTag2()  // 上门时间
            };
            case POOR_QUALITY_REPAIR -> new Object[]{
                    record.getTask(), record.getCity(), record.getOrderId(),
                    record.getCustomerAccount(), record.getLineStatus(),
                    record.getArriveTime(), record.getTag1(), record.getTag4(),
                    record.getTag5(), record.getTag6()
            };

            case  POOR_QUALITY_REPAIR_NOT_VISIT -> new Object[]{
                    record.getTask(), record.getCity(), record.getOrderId(),
                    record.getCustomerAccount(), record.getLineStatus(),
                    record.getArriveTime(), record.getTag1(), record.getTag6(),
                    record.getTag7(), record.getTag8()
            };
        };
    }
}