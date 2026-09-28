package com.mobile.smartcalling.task;

import com.mobile.smartcalling.service.IReadCSVService;
import com.mobile.smartcalling.util.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class DownloadFtpTask {

    @Autowired
    private IReadCSVService readCSVService;


    @Scheduled(cron = "0 0 7 * * ?")
    public void execute() {
        log.info("====执行拉取昨天获取外呼清单文件定时任务===");
        Map<String, Object> map = new HashMap<>();
        String connectionString = "ftp://dcpp:D8is_F7n61#15@10.76.148.39:21";
        map.put("ConnectionString",connectionString);
        map.put("userName","dcpp");
        map.put("password","D8is_F7n61#15");
        map.put("Path","/data1/dcpp/ZXAICL/");

        ArrayList<String> files = new ArrayList<>();
        //TODO 从你的参数中提取文件名
        //2025-04-14_OutboundCallList
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Date time = DateUtils.addDays(date,-1);
        String ds = sdf.format(time);
        log.info("文件名字===={}_OutboundCallList",ds);
        //files.add("2025-06-27_OutboundCallList.csv");
        files.add(ds + "_OutboundCallList.csv");
        map.put("files",files);
        log.info(map.toString());
        try {
            String path = FtpUtil.downLoadNew(map);

            if(StringUtils.isNotBlank(path)){
                log.info("===================开始读取CSV ");
                List<String> strings = readCSVService.readCsv(path);

            }else{
                log.info("服务器保存文件未成功 没有文件读取");
            }



        } catch (Exception e) {
            log.info("",e);
        }




    }
}
