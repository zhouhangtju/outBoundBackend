package com.mobile.smartcalling.task;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.dto.PerformanceInfo;
import com.mobile.smartcalling.util.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 存量维系外呼清单定时任务
 * 每天早上7点从FTP拉取 yyyy-MM-dd_OutboundCallList.csv（日期取昨天），
 * 每行格式：city|phoneNum，逐行调用 /openapi/performanceInfo 接口发起外呼
 */
@Slf4j
@Component
public class OutboundCallListTask {


    private static final String CALL_SCENE = "存量维系";


    @Value("${performance.api.url:http://127.0.0.1:8091/openapi/performanceInfo}")
    private String performanceApiUrl;

    @Value("${performance.api.threads:10}")
    private int performanceThreads;

    @Autowired
    private RestTemplate restTemplate;


    @Scheduled(cron = "0 0 7 * * ?")
    public void execute() {
        log.info("====开始执行存量维系外呼清单拉取定时任务===");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String ds = sdf.format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L));
        // 2025-06-27_OutboundCallList.csv  取昨天的
        String fileName = ds + "_OutboundCallList.csv";

        log.info("读取的文件名称是：{}",fileName);

        Map<String, Object> map = new HashMap<>();
        map.put("ConnectionString", "ftp://dcpp:D8is_F7n61#15@10.76.148.39:21");
        map.put("userName", "dcpp");
        map.put("password", "D8is_F7n61#15");
        map.put("Path", "/data1/dcpp/ZXAICL/");
        map.put("files", Collections.singletonList(fileName));

        try {
            String localPath = FtpUtil.downLoadNew(map);
            if (StringUtils.isBlank(localPath)) {
                log.info("文件 {} 下载失败或FTP上不存在，任务结束", fileName);
                return;
            }
            log.info("文件下载成功，本地路径: {}", localPath);

            List<PerformanceInfo> dataList = parseCsv(localPath);
            if (dataList.isEmpty()) {
                log.info("CSV 文件 {} 中没有有效数据", fileName);
                return;
            }

            log.info("准备调用性能信息接口，共{}条数据", dataList.size());

            callPerformanceInfo(dataList);
        } catch (Exception e) {
            log.error("存量维系外呼清单任务执行失败, fileName={}", fileName, e);
        }
    }


    private List<PerformanceInfo> parseCsv(String localPath) {
        List<PerformanceInfo> dataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(localPath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.trim().startsWith("area_name")) {
                    continue;
                }
                String[] columns = line.trim().split("\\|");
                if (columns.length < 2) {
                    log.warn("行格式不符合 city|phoneNum，跳过: {}", line);
                    continue;
                }
                String city = columns[0].trim();
                String phoneNum = columns[1].trim();

                if (StringUtils.isBlank(city) || StringUtils.isBlank(phoneNum)) {
                    log.warn("city 或 phoneNum 为空，跳过: {}", line);
                    continue;
                }

                if (city.endsWith("市")) {
                    city = city.substring(0, city.length() - 1);
                }

                PerformanceInfo info = new PerformanceInfo();
                info.setCity(city);
                info.setPhoneNum(phoneNum);
                info.setCallScene(CALL_SCENE);
                dataList.add(info);
            }
        } catch (Exception e) {
            log.error("解析CSV文件失败: {}", localPath, e);
        }
        log.info("解析完成，有效数据 {} 条", dataList.size());
        return dataList;
    }


    private void callPerformanceInfo(List<PerformanceInfo> dataList) {

        log.info("csv  读取完毕 PerformanceInfo参数组装完成了 开始调用performanceInfo 接口 ");
        if (CollUtil.isEmpty(dataList)) {
            log.info("PerformanceInfo list 为空");
            return;
        }

        log.info("=======================  开始调用performanceInfo 接口 ===================dataList 长度：{}，并发线程数：{}",
                dataList.size(), performanceThreads);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger finished = new AtomicInteger(0);
        long startMillis = System.currentTimeMillis();

        ExecutorService pool = Executors.newFixedThreadPool(performanceThreads);
        CountDownLatch latch = new CountDownLatch(dataList.size());
        for (PerformanceInfo info : dataList) {
            pool.submit(() -> {
                try {
                    // HttpHeaders 非线程安全，每个任务单独构建（开销可忽略）
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<PerformanceInfo> entity = new HttpEntity<>(info, headers);
                    CallBackResult result = restTemplate.postForObject(performanceApiUrl, entity, CallBackResult.class);
                    if (result != null && Boolean.TRUE.equals(result.getSuccess())) {
                        success.incrementAndGet();
                    } else {
                        fail.incrementAndGet();
                        log.warn("调用 performanceInfo 接口返回失败, phoneNum={}, city={}, result={}",
                                info.getPhoneNum(), info.getCity(), result);
                    }
                } catch (Exception e) {
                    fail.incrementAndGet();
                    log.error("调用 performanceInfo 接口异常, phoneNum={}, city={}", info.getPhoneNum(), info.getCity(), e);
                } finally {
                    int done = finished.incrementAndGet();
                    latch.countDown();
                    if (done % 2000 == 0) {
                        log.info("performanceInfo 调用进度: {}/{}, 成功 {}, 失败 {}", done, dataList.size(), success.get(), fail.get());
                    }
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("performanceInfo 并发调用被中断", e);
        } finally {
            pool.shutdown();
        }

        long costSeconds = (System.currentTimeMillis() - startMillis) / 1000;
        log.info("存量维系外呼清单处理完成，共 {} 条，成功 {} 条，失败 {} 条，耗时 {} 秒",
                dataList.size(), success.get(), fail.get(), costSeconds);
    }
}
