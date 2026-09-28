package com.mobile.smartcalling.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mobile.smartcalling.common.CommonBusinessStrEnum;
import com.mobile.smartcalling.common.TaskTypeEnum;
import com.mobile.smartcalling.config.ScheduleConfig;
import com.mobile.smartcalling.dao.RemoteCallResultDao;
import com.mobile.smartcalling.entity.RemoteCallResult;
import com.mobile.smartcalling.service.impl.CsvExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SatisfactionTask {

    @Autowired
    private CsvExportService csvExportService;

    @Autowired
    private RemoteCallResultDao remoteCallResultDao;


    /**
     * 每天8:00-20:00每隔1小时上传
     */
    @Scheduled(cron = "0 0 8-20 * * ?")
    public void execute() {

        log.info("开始执行存量维系定时  上传任务");

        try {


            Date endTime = new Date();
            Date startTime = new Date(endTime.getTime() - 60 * 60 * 1000L);

            LambdaQueryWrapper<RemoteCallResult> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(RemoteCallResult::getTask, CommonBusinessStrEnum.SATISFACTION_SURVEY_FOLLOW_UP_DD)
                    .ge(RemoteCallResult::getCreateTime, startTime)
                    .lt(RemoteCallResult::getCreateTime, endTime);

            List<RemoteCallResult> allData = remoteCallResultDao.selectList(lambdaQueryWrapper);

            log.info("查询到{}条数据", allData.size());


            if (allData.isEmpty()) {
                log.info("没有数据需要导出");
                return;
            }

            // 按任务类型分组  每个任务类型对应的数据
            Map<String, List<RemoteCallResult>> groupedData = allData.stream()
                    .collect(Collectors.groupingBy(RemoteCallResult::getTask));

            // 定义任务类型映射
            Map<String, TaskTypeEnum> taskMapping = new HashMap<>();
            taskMapping.put("存量维系", TaskTypeEnum.SATISFACTION_SURVEY_FOLLOW_UP_DD);


            // 遍历生成CSV并上传
            for (Map.Entry<String, List<RemoteCallResult>> entry : groupedData.entrySet()) {
                String taskName = entry.getKey();
                List<RemoteCallResult> dataList = entry.getValue();


                if (taskName.equals(CommonBusinessStrEnum.SATISFACTION_SURVEY_FOLLOW_UP_DD)) {

                    getTaskTypeEnum(taskMapping, "存量维系", dataList);

                    log.info("存量维系执行完成");
                }

            }

            log.info("定时任务执行完成");

        } catch (Exception e) {
            log.error("定时任务执行失败", e);
        }
    }

    private TaskTypeEnum getTaskTypeEnum(Map<String, TaskTypeEnum> taskMapping, String taskName, List<RemoteCallResult> dataList) {
        TaskTypeEnum taskType = taskMapping.get(taskName);
        if (taskType == null) {
            log.warn("未识别的任务类型: {}", taskName);
            return null;
        }

        try {
            csvExportService.exportAndUploadCsvSatisfaction(dataList, taskType);
        } catch (Exception e) {
            log.error("处理任务类型 {} 失败", taskName, e);
        }
        return taskType;
    }
}
