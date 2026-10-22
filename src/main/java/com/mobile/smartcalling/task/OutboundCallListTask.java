package com.mobile.smartcalling.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mobile.smartcalling.common.TaskUUIDEnum;
import com.mobile.smartcalling.dao.TaskPhoneDao;
import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.dto.PerformanceInfo;
import com.mobile.smartcalling.entity.NewResultRequest;
import com.mobile.smartcalling.entity.TaskPhone;
import com.mobile.smartcalling.service.UploadDataService;
import com.mobile.smartcalling.util.FtpUtil;
import com.mobile.smartcalling.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import java.util.*;
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
    private UploadDataService uploadDataService;

    @Autowired
    private TaskPhoneDao taskPhoneDao;

    @Autowired
    private RedisUtil redisUtil;


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
//                    HttpHeaders headers = new HttpHeaders();
//                    headers.setContentType(MediaType.APPLICATION_JSON);
//                    HttpEntity<PerformanceInfo> entity = new HttpEntity<>(info, headers);
//                    CallBackResult result = restTemplate.postForObject(performanceApiUrl, entity, CallBackResult.class);
                    CallBackResult result = useCallBackTwo(info);
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


    public CallBackResult useCallBackTwo(PerformanceInfo performanceInfo) {
        log.info("接收到性能测数据{}", performanceInfo);
        long start = System.currentTimeMillis();
        // 如果订单号为空，则生成 UUID+时间戳 作为订单号
        if (performanceInfo != null && StrUtil.isBlank(performanceInfo.getOrder())) {
            String generatedOrder = IdUtil.fastSimpleUUID() + System.currentTimeMillis();
            performanceInfo.setOrder(generatedOrder);
            log.info("订单号为空，已自动生成订单号: {}", generatedOrder);
        }

        CallBackResult result = new CallBackResult();
        //@TODO 判断订单和宽带账号是不是每次都是必传的
        boolean isValid = ObjectUtils.isNotEmpty(performanceInfo) && StringUtils.isNotEmpty(performanceInfo.getPhoneNum());
        // 立即设置返回结果
        result.setSuccess(isValid);

        String phoneNum = "";
        if (isValid) {
            phoneNum = performanceInfo.getPhoneNum();
        }

        if (null != performanceInfo && StrUtil.isNotBlank(performanceInfo.getAccount()) && StrUtil.isNotBlank(performanceInfo.getOrder())) {
            // 将订单id 和宽带账号关联保存到缓存   回调时再取出 加上Q的值入库到mysql  @TODO 宽带账号待补充
            redisUtil.setOrderIdAndBroadband(performanceInfo.getOrder(), performanceInfo.getAccount());
            log.info("orderId 关联宽带账号入库成功");
        } else {
            log.info("Order Customeraccount 工单号和宽带账号为空");
        }


        boolean phoneExists = redisUtil.isPhoneExists(phoneNum);
        long end = System.currentTimeMillis();
        log.info("redis 耗时：{}",end-start);

        if (phoneExists) {
            long ttlSeconds = redisUtil.getPhoneTtl(phoneNum);
            long ttlDays = ttlSeconds / (24 * 3600);
            log.info("该手机号：{}之前回调状态满足部分状态，禁止进行外呼 已经在缓存中,剩余天数：{}", phoneNum, ttlDays);
        } else {
            log.info("该手机号：{}可以进行外呼", phoneNum);
            // 如果数据有效，异步执行上传逻辑，不等待结果
            if (isValid) {
                asyncUploadData(performanceInfo);
            }
        }


        return result;
    }

    public void asyncUploadData(PerformanceInfo performanceInfo) {
        try {

            long queryStart = System.currentTimeMillis();
            TaskUUIDEnum taskUUIDEnum = TaskUUIDEnum.fromTaskName(performanceInfo.getCallScene() + "-" + performanceInfo.getCity());
            LambdaQueryWrapper<TaskPhone> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TaskPhone::getPhone, performanceInfo.getPhoneNum());
            queryWrapper.eq(TaskPhone::getTaskName, taskUUIDEnum.getTaskName());
            List<TaskPhone> taskPhones = taskPhoneDao.selectList(queryWrapper);

            String phoneStr = Optional.ofNullable(performanceInfo)
                    .map(PerformanceInfo::getPhoneNum)
                    .orElse("");

            String targetTaskId = Optional.ofNullable(taskUUIDEnum)
                    .map(TaskUUIDEnum::getNextTaskId)
                    .orElse("");
            log.info("========================= asyncUploadData phone: {} targetTaskId: {}", phoneStr,targetTaskId);
            long queryEnd = System.currentTimeMillis();
            log.info("taskPhoneDao.selectList 耗时：{}",queryEnd -queryStart);


            if (taskPhones.size() == 0) {
                long uploadOneStart = System.currentTimeMillis();
                //号码第一次拨打，调用号码上传接口,并存入数据库
                TaskPhone phone = new TaskPhone();
                NewResultRequest newResultRequest = new NewResultRequest();
                ArrayList<NewResultRequest.ContactData> data = new ArrayList<>();
                NewResultRequest.ContactData contactData = new NewResultRequest.ContactData();
                newResultRequest.setIgnore_crm(false);
                newResultRequest.setSkip_error(true);
                contactData.setPhone(performanceInfo.getPhoneNum());
                contactData.setExtra(performanceInfo.getOrder());
                contactData.setName(performanceInfo.getOrder());
                contactData.setSort(20);
                data.add(contactData);
                newResultRequest.setData(data);
                uploadDataService.newUploadData(newResultRequest, targetTaskId);
                phone.setPhone(performanceInfo.getPhoneNum());
                phone.setTaskName(taskUUIDEnum.getTaskName());
                if (performanceInfo.getCallScene().equals("质差派单")) {
                    redisUtil.markPhoneCalledToday(performanceInfo.getPhoneNum());
                }
                taskPhoneDao.insert(phone);
                long uploadOneEnd = System.currentTimeMillis();
                log.info("号码第一次拨打 耗时：{}",uploadOneEnd-uploadOneStart);
            } else {
                long uploadStart = System.currentTimeMillis();
                //调用号码重置状态接口
                //判断质差派单场景这个号码是否今天进行过外呼
                if (performanceInfo.getCallScene().equals("质差派单")) {
                    boolean calledToday = redisUtil.isPhoneCalledToday(performanceInfo.getPhoneNum());
                    if (calledToday) {
                        log.info("{}此号码今天已进行过质差派单外呼", performanceInfo.getPhoneNum());
                    } else {
                        redisUtil.markPhoneCalledToday(performanceInfo.getPhoneNum());
                        uploadDataService.numberBatchReset(targetTaskId, performanceInfo.getPhoneNum());
                        log.info("异步上传性能数据成功, phoneNum={},taskID={}", performanceInfo.getPhoneNum(), targetTaskId);
                    }
                } else {
                    uploadDataService.numberBatchReset(targetTaskId, performanceInfo.getPhoneNum());
                    log.info("异步上传性能数据成功, phoneNum={},taskID={}", performanceInfo.getPhoneNum(), targetTaskId);
                }
                long uploadEnd = System.currentTimeMillis();
                log.info("异步上传性能数据耗时：{}",uploadEnd-uploadStart);
            }
        } catch (Exception e) {
            // 记录异常但不影响主流程
            log.error("异步上传性能数据失败, phoneNum={}", performanceInfo.getPhoneNum(), e);
        }
    }
}
