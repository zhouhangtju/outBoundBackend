package com.mobile.smartcalling.task;



import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.entity.UploadData;
import com.mobile.smartcalling.service.IReadCSVService;
import com.mobile.smartcalling.service.UploadCSVService;
import com.mobile.smartcalling.service.UploadDataService;
import com.mobile.smartcalling.util.DbUtil;
import com.mobile.smartcalling.util.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Autowired
    private UploadCSVService uploadCSVService;


    @Async("taskExecutor")
    @Scheduled(cron = "0 0 2 * * ? ") //每天凌晨2点执行
    //@Scheduled(cron = "0 0/1 * * * ? ")         //1分钟执行一次 测试
    public void insertBatchDataTask() {
        log.info("====执行批量导入数据定时任务===");
        acquireUploadData();
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 7 * * ? ") //每天7点执行
    //@Scheduled(cron = "0 0/1 * * * ? ")         //1分钟执行一次 测试
    public void getCsv() {
        log.info("====执行获取外呼清单文件定时任务===");
        Map<String, Object> map = new HashMap<>();
        String connectionString = "ftp://dcpp:D8is_F7n61#15@10.76.148.39:21";
        map.put("userName","dcpp");
        map.put("password","D8is_F7n61#15");
        map.put("Path","/data1/dcpp/ZXAICL");
        ArrayList<String> files = new ArrayList<>();
        //TODO 从你的参数中提取文件名
        files.add("文件名");
        map.put("files",files);

        try {
            FtpUtil.downLoad(map);
        } catch (Exception e) {
            log.info("",e);
        }
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 7 * * ? ") //每天7点执行
    //@Scheduled(cron = "0 0/1 * * * ? ")         //1分钟执行一次 测试
    public void uploadCsv() {
        log.info("====执行上传外呼结果文件定时任务===");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Date time = DateUtils.addDays(date,1);
        String ds = sdf.format(time);
        String dd = ds.replace("-", "");
        uploadCSVService.uploadCSV(dd);

    }

    public void acquireUploadData() {
        //数据文件存放路径
        String csvPath = readCSVService.getCSVPath();
        //TODO 填写服务器文件路径
        //String csvPath = "本机服务器文件路径";
        log.info("获取到文件文件路径{}",csvPath);
        readCSVService.getUploadDataList(csvPath);
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
        dbUtil.addPartition("satisfy_result", "p"+dd, dd);
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
        dbUtil.delPartition("satisfy_result", "p"+dd);
    }


}
