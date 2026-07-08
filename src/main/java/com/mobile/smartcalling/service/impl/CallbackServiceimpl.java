package com.mobile.smartcalling.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.mobile.smartcalling.common.*;
import com.mobile.smartcalling.dao.RemoteCallResultDao;
import com.mobile.smartcalling.dao.ResultDBDao;
import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.dto.PoorQualityResultRequest;
import com.mobile.smartcalling.dto.RemoteCallResultDto;
import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.entity.*;
import com.mobile.smartcalling.service.ICallbackSevice;
import com.mobile.smartcalling.service.UploadDataService;
import com.mobile.smartcalling.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.springframework.web.client.RestTemplate;

import javax.swing.text.html.Option;


@Service
@Slf4j
public class CallbackServiceimpl implements ICallbackSevice {


    @Autowired
    private UploadDataService uploadDataService;
    @Autowired
    private ResultDBDao resultDBDao;
    @Autowired
    private RemoteCallResultDao remoteCallResultDao;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RedisUtil redisUtil;


    //判断对象值是否为空
    public static boolean isAllFieldsNull(Object obj) {
        for (Field f : obj.getClass().getDeclaredFields()) {
            if (f.getName().equals("orderid")) {
                continue;
            }
            f.setAccessible(true);
            try {
                if (f.get(obj) != null && !"".equals(f.get(obj).toString().trim())) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    @Override
    public void getNewJsonData(NewCallbackData newCallbackData) {
        try {
            if (newCallbackData.getData() != null && newCallbackData.getData().size() != 0) {
                List<NewCallbackData.CallRecordData> data = newCallbackData.getData();
                List<RemoteCallResult> remoteCallResultList = new LinkedList<>();

                // 获取所有的orderid 一次性查出所有的对应宽带账号  避免循环内多次调用redis
                List<String> orderIds = data.stream()
                        .map(item -> Optional.ofNullable(item)
                                .map(NewCallbackData.CallRecordData::getCustomer_data)
                                .map(NewCallbackData.CustomerData::getExtra).orElse(""))
                        .collect(Collectors.toList());
                log.info("=======> orderIds:{}", orderIds);
                // 组建 订单-宽带  映射Map
                Map<String, String> orderAndBroadbandMap = redisUtil.multiGetOrderAndBroadband(orderIds);

                log.info("=========================> orderAndBroadbandMap:{}", orderAndBroadbandMap);

                for (int i = 0; i < data.size(); i++) {
                    NewCallbackData.CallRecordData callRecordData = data.get(i);
                    NewCallbackData.Task task = callRecordData.getTask();

                    // 补充callDate 字段
                    String callDate = Optional.ofNullable(callRecordData)
                            .map(NewCallbackData.CallRecordData::getCalldate)
                            .orElse("");

                    //装机单竣工回访-杭州
                    String taskName = Optional.ofNullable(task).map(NewCallbackData.Task::getName).orElse("");


                    Integer status = callRecordData.getStatus();
                    String phoneNumber = "";

                    NewCallbackData.NumberData numberData = callRecordData.getNumber_data();
                    if (ObjectUtils.isNotEmpty(numberData)) {
                        phoneNumber = numberData.getNumber();
                        log.info("手机号：{}，状态码：{}", phoneNumber, status);
                    }

                    if (PhoneCallBackStatusEnum.notOutsidecodes().contains(status)) {
                        if (taskName.contains("质差修复已上门") || taskName.contains("装机单竣工回访") || taskName.contains("投诉单报结回访")) {
                            redisUtil.setPhoneByThirtyDays(phoneNumber);
                            log.info("满足要求状态 【30天禁止外呼】手机号 {} 已加入禁止外呼 放入缓存，状态码: {}", phoneNumber, status);
                        }

                        if (taskName.contains("质差派单")) {
                            redisUtil.setPhoneByThirtyDays(phoneNumber);
                            log.info("满足要求状态 【30天禁止外呼】手机号 {} 已加入禁止外呼 放入缓存，状态码: {}", phoneNumber, status);

                        }
                    }

                    String[] taskArray = taskName.split("-", 2);


//                    boolean phoneExists = redisUtil.isPhoneExists(phoneNumber);

                    String orderId = Optional.ofNullable(callRecordData)
                            .map(NewCallbackData.CallRecordData::getCustomer_data)
                            .map(NewCallbackData.CustomerData::getExtra)
                            .orElse("");

                    log.info("==============>orderId callRecordData.getCustomer_data().getExtra() 值为：{}", orderId);

                    // 从开始的所有 订单-宽带账号编号  获取
                    String broadband = orderAndBroadbandMap.getOrDefault(orderId, "");

//                    Object broadband = redisUtil.getOrderIdAndBroadband(orderId);
//                    String broadbandStr = Optional.ofNullable(broadband).map(Object::toString).orElse("");


                    log.info("==============>orderId 获取到的宽带账号的值是：{}", broadband);

                    //@TODO 构建RemoteCallResult 表 入库 加入callDate 字段
                    RemoteCallResult remoteCallResult = new RemoteCallResult().setPhoneNum(phoneNumber).setOrderId(orderId).setCustomerAccount(broadband).setCallDate(callDate);

                    RemoteCallResultDto resultDto = new RemoteCallResultDto();

                    if (taskName.contains("存量维系")) {
                        //   ResultDB resultDB = new ResultDB();
                        ResultDB resultDB = newStockMaintenanceType(callRecordData);
                        log.info("存量维系回调信息入库结果{}", resultDB);
                        resultDBDao.insert(resultDB);
                    }
                    if (taskName.contains("装机单竣工回访")) {
                        //RemoteCallResultDto resultDto = checkInstallationCompletion(callbackData);
                        resultDto = newCheckInstallationCompletion(callRecordData);
                        resultDto.setOrderid(orderId);
                        log.info("装机单竣工回访场景结果{}", resultDto);
                        uploadDataService.uploadResult(resultDto, task.getName());
                    }
                    if (taskName.contains("投诉单报结")) {
                        //RemoteCallResultDto resultDto = followUpResolvedComplaints(callbackData);
                        resultDto = newFollowUpResolvedComplaints(callRecordData);
                        resultDto.setOrderid(orderId);
                        log.info("投诉单报结回访场景结果{}", resultDto);
                        uploadDataService.uploadResult(resultDto, task.getName());
                    }
                    if (taskName.contains("质差修复已上门")) {
                        // RemoteCallResultDto resultDto = sendPoorQualitySurvey(callbackData);
                        resultDto = newSendPoorQualitySurvey(callRecordData);
                        resultDto.setOrderid(orderId);
                        log.info("质差修复已上门场景结果{}", resultDto);
                        uploadDataService.uploadResult(resultDto, task.getName());
                    }
                    if (taskName.contains("质差派单")) {
                        //RemoteCallResultDto resultDto = createPoorQualityDispatchSurvey(callbackData);
                        PoorQualityResultRequest resultDto2 = newCreatePoorQualityDispatchSurvey(callRecordData);
                        // resultDto.setOrderid(order);
                        log.info("质差派单场景结果{}", resultDto2);
                        uploadDataService.uploadPoorQualityResult(resultDto2);
                        log.info("质差派单 resultDto2：{}", JSONObject.toJSONString(resultDto2));
                        remoteCallResult
                                .setCallTime(StringUtils.defaultString(resultDto2.getCallTime()))
                                .setArriveTime(StringUtils.defaultString(resultDto2.getArriveTime()))
                                .setIsArrive(StringUtils.defaultString(resultDto2.getIsArrive()))
                                .setLineStatus(StringUtils.defaultString(resultDto2.getLineStatus()));
                    }
                    if (null != resultDto) {

                        // 将taskName进行分割  得到的外呼名称和城市
                        if (ArrayUtil.isNotEmpty(taskArray) && taskArray.length == 2) {
                            String taskStr = taskArray[0];
                            String taskCity = taskArray[1];
                            remoteCallResult.setTask(StringUtils.defaultString(taskStr));
                            remoteCallResult.setCity(StringUtils.defaultString(taskCity));
                        }

                        log.info("设置tag");
                        remoteCallResult
                                .setTag1(StringUtils.defaultString(resultDto.getQ1()))
                                .setTag2(StringUtils.defaultString(resultDto.getQ2()))
                                .setTag3(StringUtils.defaultString(resultDto.getQ3()))
                                .setTag4(StringUtils.defaultString(resultDto.getQ4()))
                                .setTag5(StringUtils.defaultString(resultDto.getQ5()))
                                .setTag6(StringUtils.defaultString(resultDto.getQ6()))
                                .setTag7(StringUtils.defaultString(resultDto.getQ7()))
                                .setTag8(StringUtils.defaultString(resultDto.getQ8()))
                                .setTag9(StringUtils.defaultString(resultDto.getQ9()))
                                .setTag10(StringUtils.defaultString(resultDto.getQ10()))
                                .setTag11(StringUtils.defaultString(resultDto.getQ11()));
                    }

                    remoteCallResultList.add(remoteCallResult);
                }


                if (CollUtil.isNotEmpty(remoteCallResultList)) {
                    log.info("=================> remoteCallResultList Size:{},demo:{}", remoteCallResultList.size(), remoteCallResultList.get(0));
                    Integer number = remoteCallResultDao.insertBatch(remoteCallResultList);
                    log.info("=============>  remoteCallResultList 入库完成 条数：{}", remoteCallResultList.size());
                }


            } else {
                log.info("回调数据为空");
            }
        } catch (Exception e) {
            log.error("=============》调用失败：{}", e);
        }

    }

    /**
     * 存量维系回调结果解析
     *
     * @param callRecordData
     * @return ResultDB
     */
    public ResultDB newStockMaintenanceType(NewCallbackData.CallRecordData callRecordData) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
        ResultDB resultDB = new ResultDB();
        List<NewCallbackData.Tag> tags = callRecordData.getTags();
        try {
            String datePart = callRecordData.getCalldate().substring(0, 10);
            String ds = datePart.replace("-", "");
            resultDB.setDs(ds);
            resultDB.setStartTime(sdf.parse(callRecordData.getCalldate()));
            resultDB.setPhoneNum(callRecordData.getNumber_data().getNumber());
            resultDB.setCustomerName(callRecordData.getCustomer_data().getName());
            if (tags == null || tags.size() == 0) {
                return resultDB;
            } else {
                for (int i = 0; i < tags.size(); i++) {
                    NewCallbackData.Tag tag = tags.get(i);
                    String name = tag.getName();
                    String[] split = name.split(":", 2);
                    if (split[0].contains("Q1")) {
                        resultDB.setIsContact(split[1]);
                    }
                    //Q3所有节点判断  上网质量满意度打分
                    if (NodeName.q3NodeNames.contains(split[0])) {
                        resultDB.setInternetQualityScore(split[1]);
                    }
                    if ("Q3-A-default".equals(split[0])) {
                        resultDB.setInternetQualityScore(split[1]);
                    }
                    if ("Q3-B-default".equals(split[0])) {
                        resultDB.setInternetQualityScore(split[1]);
                    }
                    if ("Q3-C-default".equals(split[0])) {
                        resultDB.setInternetQualityScore(split[1]);
                    }
                    if ("Q3-D-default".equals(split[0])) {
                        resultDB.setInternetQualityScore(split[1]);
                    }
                    //Q4所有节点判断  安装维修满意度打分
                    if (NodeName.q4NodeNames.contains(split[0])) {
                        resultDB.setInstallScore(split[1]);
                    }
                    if ("Q4-A-default".equals(split[0])) {
                        resultDB.setInstallScore(split[1]);
                    }
                    if ("Q4-B-default".equals(split[0])) {
                        resultDB.setInstallScore(split[1]);
                    }
                    if ("Q4-C-default".equals(split[0])) {
                        resultDB.setInstallScore(split[1]);
                    }
                    if ("Q4-C-非满分-default".equals(split[0])) {
                        resultDB.setInstallScore(split[1]);
                    }
                    //不满意原因
                    if ("Q5".equals(split[0])) {
                        resultDB.setReason(split[1]);
                    }
                    //是否需要上门
                    if (NodeName.q7NodeNames.contains(split[0])) {
                        resultDB.setIsHome(split[1]);
                    }
                }
            }
        } catch (ParseException e) {
            log.info("Exception:{}", e);
        } finally {
            return resultDB;
        }
    }


    /**
     * 装机单竣工回访回调结果解析
     *
     * @param callRecordData
     * @return RemoteCallResultDto
     */
    public RemoteCallResultDto newCheckInstallationCompletion(NewCallbackData.CallRecordData callRecordData) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
        RemoteCallResultDto resultDto = new RemoteCallResultDto();
        List<NewCallbackData.Tag> tags = callRecordData.getTags();
        if (tags == null || tags.size() == 0) {
            if (ObjectUtils.isNotEmpty(callRecordData.getStatus()) && callRecordData.getStatus() == 1) {
                resultDto.setQ1("接通未评价");
            }
            return resultDto;
        } else {
            for (int i = 0; i < tags.size(); i++) {
                NewCallbackData.Tag tag = tags.get(i);
                String name = tag.getName();
                String[] split = name.split(":", 2);

                if (split[0].contains("Q1") && !split[0].contains("Q10")) {
                    resultDto.setQ1(split[1]);
                }
                if (split[0].contains("Q2")) {
                    resultDto.setQ2(split[1]);
                }
                if (split[0].contains("Q3")) {
                    resultDto.setQ3(split[1]);
                }
//                if (NodeName.installQ4NodeNames.equals(split[0])) {
//                    resultDto.setQ4(split[1]);
//                }
//                if ("Q4-1".equals(split[0])) {
//                    resultDto.setQ7(split[1]);
//                }
                if (split[0].startsWith("Q4")) {
                    // 第二步：排除 Q4-1 这个特殊节点（单独处理）
                    // 匹配 Q4、Q4-2、Q4-3 等所有 Q4 相关的其他节点
                    resultDto.setQ4(split[1]);
                }
                if (split[0].contains("Q5")) {
                    resultDto.setQ5(split[1]);
                }
                if (split[0].contains("Q6")) {
                    resultDto.setQ6(split[1]);
                }
                if (split[0].contains("Q7")) {
                    resultDto.setQ7(split[1]);
                }
                if (split[0].contains("Q8")) {
                    resultDto.setQ8(split[1]);
                }
                if (split[0].contains("Q9")) {
                    resultDto.setQ9(split[1]);
                }
                if (split[0].contains("Q10")) {
                    resultDto.setQ10(split[1]);
                }
                //后处理用户说我满意，要把装机Q3Q4改成10分
                if (split[0].equals("改成十分"))
                {
                    resultDto.setQ3("10分");
                    resultDto.setQ4("10分");
                }
            }
            if (ObjectUtils.isNotEmpty(callRecordData.getStatus()) && isAllFieldsNull(resultDto) && callRecordData.getStatus() == 1) {
                resultDto.setQ1("接通未评价");
            }
            return resultDto;
        }
    }


    /**
     * 投诉单报结回调结果解析
     *
     * @param callRecordData
     * @return RemoteCallResultDto
     */
    public static RemoteCallResultDto newFollowUpResolvedComplaints(NewCallbackData.CallRecordData callRecordData) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
        RemoteCallResultDto resultDto = new RemoteCallResultDto();
        List<NewCallbackData.Tag> tags = callRecordData.getTags();
        if (tags == null || tags.size() == 0) {
            if (ObjectUtils.isNotEmpty(callRecordData.getStatus()) && callRecordData.getStatus() == 1) {
                resultDto.setQ1("接通未评价");
            }
            return resultDto;
        } else {
            for (int i = 0; i < tags.size(); i++) {
                NewCallbackData.Tag tag = tags.get(i);
                String name = tag.getName();
                String[] split = name.split(":", 2);
                if (split[0].contains("Q1") && split[0].matches(".*\\bQ1\\b.*")) {
                    resultDto.setQ1(split[1]);
                }
                if (split[0].contains("Q2")) {
                    resultDto.setQ2(split[1]);
                }
                if (split[0].contains("Q3")) {
                    resultDto.setQ3(split[1]);
                }
                if (split[0].contains("Q4")) {
                    resultDto.setQ4(split[1]);
                }
                if (split[0].contains("Q5")) {
                    resultDto.setQ5(split[1]);
                }
                if (split[0].contains("Q6")) {
                    resultDto.setQ6(split[1]);
                }
//                if ("Q6-A-default".equals(split[0])) {
//                    resultDto.setQ6(split[1]);
//                }
//                if ("Q6-B-default".equals(split[0])) {
//                    resultDto.setQ6(split[1]);
//                }
//                if ("Q6-C-default".equals(split[0])) {
//                    resultDto.setQ6(split[1]);
//                }
//                if ("Q6-D-default".equals(split[0])) {
//                    resultDto.setQ6(split[1]);
//                }
                if (split[0].contains("Q7")) {
                    resultDto.setQ7(split[1]);
                }
//                if ("Q7-A-default".equals(split[0])) {
//                    resultDto.setQ7(split[1]);
//                }
//                if ("Q7-B-default".equals(split[0])) {
//                    resultDto.setQ7(split[1]);
//                }
//                if ("Q7-C-满分-default".equals(split[0])) {
//                    resultDto.setQ7(split[1]);
//                }
//                if ("Q7-C-非满分-default".equals(split[0])) {
//                    resultDto.setQ7(split[1]);
//                }
                if (split[0].contains("Q8")) {
                    resultDto.setQ8(split[1]);
                }
                if (split[0].contains("Q9")) {
                    resultDto.setQ9(split[1]);
                }
                if (split[0].contains("Q10") && split[0].matches(".*\\bQ10\\b.*")) {
                    resultDto.setQ10(split[1]);
                }
                //投诉单报结改分逻辑
                if (split[0].equals("改成十分"))
                {
                    resultDto.setQ6("10分");
                    resultDto.setQ7("10分");
                }
            }
            if (ObjectUtils.isNotEmpty(callRecordData.getStatus()) && isAllFieldsNull(resultDto) && callRecordData.getStatus() == 1) {
                resultDto.setQ1("接通未评价");
            }
            return resultDto;
        }
    }


    /**
     * 质差修复已上门回调结果解析
     *
     * @param callRecordData
     * @return RemoteCallResultDto
     */
    public static RemoteCallResultDto newSendPoorQualitySurvey(NewCallbackData.CallRecordData callRecordData) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
        RemoteCallResultDto resultDto = new RemoteCallResultDto();
        List<NewCallbackData.Tag> tags = callRecordData.getTags();
        if (tags == null || tags.size() == 0) {
            if (ObjectUtils.isNotEmpty(callRecordData.getStatus()) && callRecordData.getStatus() == 1) {
                resultDto.setQ1("接通未评价");
            }
            return resultDto;
        } else {
            for (int i = 0; i < tags.size(); i++) {
                NewCallbackData.Tag tag = tags.get(i);
                String name = tag.getName();
                String[] split = name.split(":", 2);
                if (split[0].contains("Q1")) {
                    resultDto.setQ1(split[1]);
                }
                if (split[0].contains("Q3")) {
                    resultDto.setQ3(split[1]);
                }
                if (split[0].contains("Q4")) {
                    resultDto.setQ4(split[1]);
                }
//            if ("Q4-A-default".equals(split[0])) {
//                resultDto.setQ4(split[1]);
//            }
//            if ("Q4-B-default".equals(split[0]) || "Q4-D-default".equals(split[0])) {
//                resultDto.setQ4(split[1]);
//            }
//            if ("Q4-C-default".equals(split[0])) {
//                resultDto.setQ4(split[1]);
//            }
                if (split[0].contains("Q5")) {
                    resultDto.setQ5(split[1]);
                }
//            if ("Q5-A-default".equals(split[0])) {
//                resultDto.setQ5(split[1]);
//            }
//            if ("Q5-B-default".equals(split[0])) {
//                resultDto.setQ5(split[1]);
//            }
//            if ("Q5-C-满分-default".equals(split[0]) || "Q5-C-非满分-default".equals(split[0])) {
//                resultDto.setQ5(split[1]);
//            }
                if (split[0].contains("Q6")) {
                    resultDto.setQ6(split[1]);
                }
                if (split[0].contains("Q7")) {
                    resultDto.setQ7(split[1]);
                }
                //质差修复已上门改10分
                if (split[0].equals("改成十分"))
                {
                    resultDto.setQ4("10分");
                    resultDto.setQ5("10分");
                }
            }
            if (ObjectUtils.isNotEmpty(callRecordData.getStatus()) && isAllFieldsNull(resultDto) && callRecordData.getStatus() == 1) {
                resultDto.setQ1("接通未评价");
            }
            return resultDto;
        }
    }


    /**
     * 质差派单回调结果解析
     *
     * @param callRecordData
     * @return PoorQualityResultRequest
     */
    public PoorQualityResultRequest newCreatePoorQualityDispatchSurvey(NewCallbackData.CallRecordData callRecordData) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
        List<NewCallbackData.Tag> tags = callRecordData.getTags();
        PoorQualityResultRequest poorQualityResultRequest = new PoorQualityResultRequest();
        poorQualityResultRequest.setCallTime(callRecordData.getCalldate());
        poorQualityResultRequest.setPhoneNum(callRecordData.getNumber_data().getNumber());
        List<NewCallbackData.Components> components = callRecordData.getCustomer_data().getComponents();
        List<NewCallbackData.Outbound_collection_records> outboundCollectionRecords = callRecordData.getOutbound_collection_records();
//        if(components.size()!=0&&components.get(0).getValue()!=null){
//            TimeString string = new TimeString();
//            string.setTimeString(components.get(0).getValue());
//            poorQualityResultRequest.setArriveTime(getTime(string));
//        }

//        outboundCollectionRecords.forEach(item -> {
//            if (item.getComponent_id().equals("1a73603b-5fc5-4b2c-97be-51aae21736e7")) {
//                TimeString string = new TimeString();
//                string.setTimeString(item.getContent());
//                poorQualityResultRequest.setArriveTime(getTime(string));
//            }
//        });


        // poorQualityResultRequest.setLineStatus("呼叫成功");
        Integer status = null;
        status = callRecordData.getStatus();
        if (status != null) {
            if (status == 1) {
                poorQualityResultRequest.setLineStatus("200");
            } else {
                poorQualityResultRequest.setLineStatus(String.valueOf(status));
            }
        } else {
            poorQualityResultRequest.setLineStatus("200");
        }
        if (tags == null || tags.size() == 0) {
            return poorQualityResultRequest;
        } else {
            for (int i = 0; i < tags.size(); i++) {
                NewCallbackData.Tag tag = tags.get(i);
                String name = tag.getName();
                String[] split = name.split(":", 2);
                if (split[0].contains("Q1")) {
                    if (split[1].equals("质差需上门")) {
                        poorQualityResultRequest.setIsArrive("是");
                    } else {
                        poorQualityResultRequest.setIsArrive("否");
                    }
                }
                else if(split[0].contains("Q2")){
                    System.out.println(split[1]);
                    if (split[1].equals("抓取时间关键字")){
                        outboundCollectionRecords.forEach(item -> {
                            if (item.getComponent_id().equals("1a73603b-5fc5-4b2c-97be-51aae21736e7")) {
                                TimeString string = new TimeString();
                                string.setTimeString(item.getContent());
                                poorQualityResultRequest.setArriveTime(getTime(string));
                            }
                        });
                    }
                }
            }
        }

        if (StringUtils.isEmpty(poorQualityResultRequest.getIsArrive()) || poorQualityResultRequest.getIsArrive() == null) {
            poorQualityResultRequest.setIsArrive("否");
        }
//        log.info("poorQualityResultRequest");
        log.info("poorQualityResultRequest={}", JSONObject.toJSONString(poorQualityResultRequest));
        return poorQualityResultRequest;
    }


    /**
     * 时间处理逻辑
     *
     * @param timeString
     * @return String
     */
    public String getTime(TimeString timeString) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构建请求体
        // 构建URI
        String url = "http://188.107.245.55:8000/process";

        ResponseEntity<String> response = restTemplate.postForEntity(url, timeString, String.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("时间转换前{}", timeString.getTimeString());
            String res = response.getBody();
            JSONObject json = JSONObject.parseObject(res);
            String detail = json.getString("res_time");
            log.info("时间转换结果{}", json);
            return detail;
        } else {
            log.info("时间转换失败");
        }
        return null;
    }
}
