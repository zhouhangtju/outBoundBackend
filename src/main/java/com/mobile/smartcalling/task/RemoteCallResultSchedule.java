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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RemoteCallResultSchedule {



    @Autowired
    private CsvExportService csvExportService;

    @Autowired
    private ScheduleConfig scheduleConfig;

    @Autowired
    private RemoteCallResultDao remoteCallResultDao;


    /**
     * 每天凌晨3点执行定时任务
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void execute() {
        // 检查定时任务开关
        if (!scheduleConfig.isEnabled()) {
            log.info("定时任务已禁用，跳过执行");
            return;
        }

        log.info("开始执行外呼结果数据导出定时任务");

        try {

            //  获取今天的全部数据
            LambdaQueryWrapper<RemoteCallResult> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.apply("DATE(create_time) = CURDATE()");

            List<RemoteCallResult> allData = remoteCallResultDao.selectList(lambdaQueryWrapper);


            if (allData.isEmpty()) {
                log.info("没有数据需要导出");
                return;
            }

            // 按任务类型分组  每个任务类型对应的数据
            Map<String, List<RemoteCallResult>> groupedData = allData.stream()
                    .collect(Collectors.groupingBy(RemoteCallResult::getTask));

            // 定义任务类型映射
            Map<String, TaskTypeEnum> taskMapping = Map.of(
                    "装机竣工-宁波", TaskTypeEnum.INSTALLATION_NINGBO,
                    "装机竣工-除宁波地市外", TaskTypeEnum.INSTALLATION_OTHER,
                    "质差派单", TaskTypeEnum.POOR_QUALITY_DISPATCH,
                    "质差修复已上门回访", TaskTypeEnum.POOR_QUALITY_REPAIR,
                    "投诉单报结回访", TaskTypeEnum.POOR_QUALITY_REPAIR_NOT_VISIT
            );

            // 遍历生成CSV并上传
            for (Map.Entry<String, List<RemoteCallResult>> entry : groupedData.entrySet()) {
                String taskName = entry.getKey();
                List<RemoteCallResult> dataList = entry.getValue();

                if(taskName.equals(CommonBusinessStrEnum.INSTALLATION_COMPLETION)){
                    List<RemoteCallResult> ningboList = dataList.stream().filter(r -> r.getCity().equals("宁波")).collect(Collectors.toList());

                    getTaskTypeEnum(taskMapping, "装机竣工-宁波", ningboList);

                    List<RemoteCallResult> noNingboList = dataList.stream().filter(r -> !r.getCity().equals("宁波")).collect(Collectors.toList());

                    getTaskTypeEnum(taskMapping, "装机竣工-除宁波地市外", noNingboList);

                    log.info("装机竣工执行完成");

                }


                if(taskName.equals(CommonBusinessStrEnum.POOR_QUALITY_DISPATCHING_ORDERS)){

                    getTaskTypeEnum(taskMapping, "质差派单", dataList);

                    log.info("质差派单执行完成");

                }

                if(taskName.equals(CommonBusinessStrEnum.POOR_QUALITY_REPAIR_HAS_BEEN_FOLLOWED_UP)){

                    getTaskTypeEnum(taskMapping, "质差修复已上门回访", dataList);

                    log.info("质差修复已上门回访执行完成");

                }

                if(taskName.equals(CommonBusinessStrEnum.COMPLAINT_FORM_CLOSING_FOLLOW_UP)){

                    getTaskTypeEnum(taskMapping, "投诉单报结回访", dataList);

                    log.info("投诉单报结回访执行完成");

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
            csvExportService.exportAndUploadCsv(dataList, taskType);
        } catch (Exception e) {
            log.error("处理任务类型 {} 失败", taskName, e);
        }
        return taskType;
    }
}