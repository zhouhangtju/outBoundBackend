package com.mobile.smartcalling.task;



import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.service.IReadCSVService;
import com.mobile.smartcalling.service.UploadDataService;
import com.mobile.smartcalling.util.DbUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

@Service
@Slf4j
public class TaskService {
    private final String taskExecutor = "taskExecutor";
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UploadDataService uploadDataService;
    @Autowired
    private IReadCSVService readCSVService;
    @Autowired
    private DbUtil dbUtil;



    @Async("taskExecutor")
    @Scheduled(cron = "0 0 2 * * ? ") //每天凌晨2点执行
    //@Scheduled(cron = "0 0/1 * * * ? ")         //1分钟执行一次 测试
    public void insertBatchDataTask() {
        log.info("执行批量导入数据定时任务");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Date time = DateUtils.addDays(date,-1);
        String ds = sdf.format(time);
        String dd = ds.replace("-", "");
        acquireUploadData(dd);
    }

    private void acquireUploadData(String dd) {
        UploadDataList uploadDataList = new UploadDataList();
        //数据文件存放路径
        String csvPath = readCSVService.getCSVPath();
        //TODO 读取解析文件数据 生成数据集合 做入库或者调用接口准备


        //调用上传数据接口 一次两千条数据
        uploadDataService.UploadData(uploadDataList);

    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 23 * * ?")    //每天23点执行
    //@Scheduled(cron = "0 0/1 * * * ? ")
    public void createDS() {
        log.info("执行了一次创建分区的任务");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Date time = DateUtils.addDays(date,1);
        String ds = sdf.format(time);
        String dd = ds.replace("-", "");
        // 创建分区
        acquireCreateDs(dd);
    }

    public void acquireCreateDs(String dd){
        dbUtil.addPartition("broadband_complaints", "p"+dd, dd);
        dbUtil.addPartition("bp_no_real_time_performance_main", "p"+dd, dd);
        dbUtil.addPartition("alarm_data", "p"+dd, dd);
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 1 * * ? ")        //每天凌晨1点执行
    //@Scheduled(cron = "0 0/1 * * * ? ")         //1分钟执行一次 测试
    /**
     * 删除三个月前的数据
     */
    public void deleteOldData() {
        log.info("执行了一次deleteOldData的定时任务");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
        Date now = new Date();
        // 格式化并打印日期
        String dd = sdf.format(DateUtils.addMonths(now, -3));
        acquireDelOldDs(dd);
    }


    public void acquireDelOldDs(String dd){
        log.info("获取到将要删除分区的dd{}",dd);
        dbUtil.delPartition("broadband_complaints", "p"+dd);
        dbUtil.delPartition("bp_no_real_time_performance_main", "p"+dd);
        dbUtil.delPartition("alarm_data", "p"+dd);
    }


}
