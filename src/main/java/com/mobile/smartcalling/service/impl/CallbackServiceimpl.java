package com.mobile.smartcalling.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mobile.smartcalling.common.ChineseAffirmativeChecker;
import com.mobile.smartcalling.common.TaskUUIDEnum;
import com.mobile.smartcalling.common.TimeConverter;
import com.mobile.smartcalling.dao.ResultDBDao;
import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.dto.PoorQualityResultRequest;
import com.mobile.smartcalling.dto.RemoteCallResultDto;
import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.entity.*;
import com.mobile.smartcalling.service.ICallbackSevice;
import com.mobile.smartcalling.service.UploadDataService;
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

import com.mobile.smartcalling.common.NodeName;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class CallbackServiceimpl implements ICallbackSevice {


    @Autowired
    private UploadDataService uploadDataService;
    @Autowired
    private ResultDBDao resultDBDao;
    @Autowired
    private RestTemplate restTemplate;



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
            if(newCallbackData.getData()!=null&&newCallbackData.getData().size()!=0){
                List<NewCallbackData.CallRecordData> data = newCallbackData.getData();
                for (int i = 0; i < data.size(); i++) {
                    NewCallbackData.CallRecordData callRecordData = data.get(i);
                    NewCallbackData.Task task = callRecordData.getTask();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
//            Date date = new Date();
//            Date time = DateUtils.addDays(date,-1);
//            String ds = sdf.format(time);
//            String dd = ds.replace("-", "");

                    // List<NewCallbackData.Tag> tags = callRecordData.getTags();
                    if(task.getName().contains("存量维系")){
                        //   ResultDB resultDB = new ResultDB();
                        ResultDB resultDB = newStockMaintenanceType(callRecordData);
                        log.info("存量维系回调信息入库结果{}", resultDB);
                        resultDBDao.insert(resultDB);
                    }
                    if(task.getName().contains("装机单竣工回访")){
                        //RemoteCallResultDto resultDto = checkInstallationCompletion(callbackData);
                        RemoteCallResultDto resultDto = newCheckInstallationCompletion(callRecordData);
                        resultDto.setOrderid(callRecordData.getCustomer_data().getExtra());
                        log.info("装机单竣工回访场景结果{}",resultDto);
                        uploadDataService.uploadResult(resultDto,task.getName());
                    }
                    if(task.getName().contains("投诉单报结")){
                        //RemoteCallResultDto resultDto = followUpResolvedComplaints(callbackData);
                        RemoteCallResultDto resultDto = newFollowUpResolvedComplaints(callRecordData);
                        resultDto.setOrderid(callRecordData.getCustomer_data().getExtra());
                        log.info("投诉单报结回访场景结果{}",resultDto);
                        uploadDataService.uploadResult(resultDto,task.getName());
                    }
                    if(task.getName().contains("质差修复已上门")){
                        // RemoteCallResultDto resultDto = sendPoorQualitySurvey(callbackData);
                        RemoteCallResultDto resultDto = newSendPoorQualitySurvey(callRecordData);
                        resultDto.setOrderid(callRecordData.getCustomer_data().getExtra());
                        log.info("质差修复已上门场景结果{}",resultDto);
                        uploadDataService.uploadResult(resultDto,task.getName());
                    }
                    if(task.getName().contains("质差派单")){
                        //RemoteCallResultDto resultDto = createPoorQualityDispatchSurvey(callbackData);
                        PoorQualityResultRequest resultDto = newCreatePoorQualityDispatchSurvey(callRecordData);
                        // resultDto.setOrderid(order);
                        log.info("质差派单场景结果{}",resultDto);
                        uploadDataService.uploadPoorQualityResult(resultDto);
                    }
                }
            }else {
                log.info("回调数据为空");
            }
        }catch (Exception e){
            log.info("{}",e);
        }

    }

    public ResultDB newStockMaintenanceType(NewCallbackData.CallRecordData callRecordData){
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
            } catch(ParseException e){
                log.info("Exception:{}", e);
            }finally{
                return resultDB;
            }
    }




    public RemoteCallResultDto newCheckInstallationCompletion(NewCallbackData.CallRecordData callRecordData) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
        RemoteCallResultDto resultDto = new RemoteCallResultDto();
        List<NewCallbackData.Tag> tags = callRecordData.getTags();
        if (tags == null || tags.size() == 0) {
            if(ObjectUtils.isNotEmpty(callRecordData.getStatus())&&callRecordData.getStatus()==1){
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
//                if (NodeName.installQ4NodeNames.equals(split[0])) {
//                    resultDto.setQ4(split[1]);
//                }
//                if ("Q4-1".equals(split[0])) {
//                    resultDto.setQ7(split[1]);
//                }
                if (split[0].startsWith("Q4")) {
                    // 第二步：排除 Q4-1 这个特殊节点（单独处理）
                    if ("Q4-1".equals(split[0])) {
                        resultDto.setQ7(split[1]);
                    } else {
                        // 匹配 Q4、Q4-2、Q4-3 等所有 Q4 相关的其他节点
                        resultDto.setQ4(split[1]);
                    }
                }
                if (split[0].contains("Q5")) {
                    resultDto.setQ5(split[1]);
                }
                if (split[0].contains("Q6")) {
                    resultDto.setQ6(split[1]);
                }
            }
            if(ObjectUtils.isNotEmpty(callRecordData.getStatus())&& isAllFieldsNull(resultDto)&&callRecordData.getStatus()==1){
                resultDto.setQ1("接通未评价");
            }
            return resultDto;
        }
    }



    public static RemoteCallResultDto newFollowUpResolvedComplaints(NewCallbackData.CallRecordData callRecordData) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
        RemoteCallResultDto resultDto = new RemoteCallResultDto();
        List<NewCallbackData.Tag> tags = callRecordData.getTags();
        if (tags == null || tags.size() == 0) {
            if(ObjectUtils.isNotEmpty(callRecordData.getStatus())&&callRecordData.getStatus()==1){
                resultDto.setQ1("接通未评价");
            }
            return resultDto;
        } else {
            for (int i = 0; i < tags.size(); i++) {
                NewCallbackData.Tag tag = tags.get(i);
                String name = tag.getName();
                String[] split = name.split(":", 2);
                if (split[0].contains("Q1")&& split[0].matches(".*\\bQ1\\b.*")) {
                    resultDto.setQ1(split[1]);
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
                if (split[0].contains("Q10")&&split[0].matches(".*\\bQ10\\b.*")) {
                    resultDto.setQ10(split[1]);
                }
            }
            if(ObjectUtils.isNotEmpty(callRecordData.getStatus())&& isAllFieldsNull(resultDto)&&callRecordData.getStatus()==1){
                resultDto.setQ1("接通未评价");
            }
            return resultDto;
        }
    }



    public static RemoteCallResultDto newSendPoorQualitySurvey(NewCallbackData.CallRecordData callRecordData){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
        RemoteCallResultDto resultDto = new RemoteCallResultDto();
        List<NewCallbackData.Tag> tags = callRecordData.getTags();
        if(tags==null||tags.size()==0){
            if(ObjectUtils.isNotEmpty(callRecordData.getStatus())&&callRecordData.getStatus()==1){
                resultDto.setQ1("接通未评价");
            }
            return resultDto;
        }else {
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
            }
            if(ObjectUtils.isNotEmpty(callRecordData.getStatus())&& isAllFieldsNull(resultDto)&&callRecordData.getStatus()==1){
                resultDto.setQ1("接通未评价");
            }
        return resultDto;
    }
    }




    public PoorQualityResultRequest newCreatePoorQualityDispatchSurvey(NewCallbackData.CallRecordData callRecordData){
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
            outboundCollectionRecords.forEach(item ->{
                if(item.getComponent_id().equals("1a73603b-5fc5-4b2c-97be-51aae21736e7")){
                    TimeString string = new TimeString();
                    string.setTimeString(item.getContent());
                    poorQualityResultRequest.setArriveTime(getTime(string));
                }
            });
        
        
       // poorQualityResultRequest.setLineStatus("呼叫成功");
        Integer status = null;
                status = callRecordData.getStatus();
            if(status!=null){
                if(status==1){
                    poorQualityResultRequest.setLineStatus("200");
                }else {
                    poorQualityResultRequest.setLineStatus(String.valueOf(status));
                }
        }else {
                poorQualityResultRequest.setLineStatus("200");
            }
            if(tags==null||tags.size()==0){
                return poorQualityResultRequest;
            }else {
                for (int i = 0; i < tags.size(); i++) {
                    NewCallbackData.Tag tag = tags.get(i);
                    String name = tag.getName();
                    String[] split = name.split(":", 2);
                    if (split[0].contains("Q1")) {
                        if(split[1].equals("质差需上门")){
                            poorQualityResultRequest.setIsArrive("是");
                        }else {
                            poorQualityResultRequest.setIsArrive("否");
                        }
                    }
                }
            }

            if(StringUtils.isEmpty(poorQualityResultRequest.getIsArrive())||poorQualityResultRequest.getIsArrive()==null){
                poorQualityResultRequest.setIsArrive("否");
            }
            return poorQualityResultRequest;
        }


    public String getTime(TimeString timeString) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构建请求体
        // 构建URI
        String url = "http://188.107.245.55:8000/process";

        ResponseEntity<String> response = restTemplate.postForEntity(url, timeString, String.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("时间转换前{}",timeString.getTimeString());
            String res = response.getBody();
            JSONObject json = JSONObject.parseObject(res);
            String detail = json.getString("res_time");
            log.info("时间转换结果{}",json);
            return detail;
        }else {
            log.info("时间转换失败");
        }
        return null;
    }
}
