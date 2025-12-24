package com.mobile.smartcalling.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mobile.smartcalling.dto.*;
import com.mobile.smartcalling.entity.NewNumRestRequest;
import com.mobile.smartcalling.entity.NewResultRequest;
import com.mobile.smartcalling.service.UploadDataService;
import com.mobile.smartcalling.util.XAccessSignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UploadDataServiceImpl implements UploadDataService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ReceiveResponse uploadResult(RemoteCallResultDto resultDto,String taskName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构建请求体


        // 构建URI
        String url = "http://10.212.223.150:8080/zhzd/outer/idp/outboundResult";
        // 发送POST请求
        ResponseEntity<String> response = restTemplate.postForEntity(url, resultDto, String.class);
        if (response.getStatusCodeValue() == 200) {
            String res = response.getBody();
            JSONObject json = JSONObject.parseObject(res);
            log.info(taskName+"外呼结果接收接口调用情况{}",json);

        }else {
            log.info("外呼结果接收接口调用失败");
        }
        return null;
    }

    @Override
    public PoorQualityResultResponse uploadPoorQualityResult(PoorQualityResultRequest resultRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构建请求体


        // 构建URI
       // String url = "http://10.76.148.79:36035/trackImportController/saveOutbound";
        String url = "http://10.76.148.49:36035/trackImportController/saveOutbound";
        // 发送POST请求
        ResponseEntity<String> response = restTemplate.postForEntity(url, resultRequest, String.class);
        if (response.getStatusCodeValue() == 200) {
            String res = response.getBody();
            JSONObject json = JSONObject.parseObject(res);
            log.info("质差结果接收接口调用情况{}",json);
        }else {
            log.info("质差结果接收接口调用失败");
        }
        return null;
    }

    @Override
    public void newUploadData(NewResultRequest newResultRequest,String taskId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiZDE5NGUyMDhkNmZjZjFiYTIxYjYwZGQ2NWQ2ZTU2ODlkYzFkOTAxMzk5ZTg3ZWJiZGVlYWFmNGFhNDA0YzVlMTllZDQwODc5ZmIwNzc2NWQiLCJpYXQiOjE3NTgzNjAyMzMsIm5iZiI6MTc1ODM2MDIzMywiZXhwIjoxNzg5ODk2MjMzLCJzdWIiOiI5YWEyYzcyMi0xMmJhLTRkOWMtODA1Ni03NTc0NjFjZGVkNWUiLCJzY29wZXMiOltdfQ.tQAr3M1WgUv-YbgFZuNSiMQI5j5Mlw5vw3s7Pcwh_XRo-n1UE08AaHEIHCjHxKAcy1oYGzPrEkp8W0dP9oh_acxtoAw-7_6NXintBSrCF8tESpDvBF2Sr1yxHIVmOdXQkAbGvbnGnTwKedNq0jUqqumTjABj1odi1WoX1_KAQ8hs7RSpYvXogk9D46tRlgrGRQS7JdbrmhHy-FvYrhG4kDM2BlHlQ-zH5suOGE6X_yewi0ENvvq1AQkSclApB4QsyeUpAn-l2LsM4QaSRg_wYDsnzeJkEKfKQMDEQIOudYP1jPjzwVgQBRMi_SBKRojv8KruuXl-JXcvVse_HXiM9yw_Kl-9vEnWL-o-eugyFV35CoSU7svvc2YFSL1JCCSD3GvitutchrorxqsYrPSF6Wgud8pGFyxICI61LmiWjLEs0xriXcvUA1Cr7o_pzj--fZFq4LvBM7XUNL6O6wW585rdLhryjunpjboQWzIn7-NzZ8vk-TObx1Pm4c75ENm0wAh7pHDz8lAx4rFU3iPBnW5dDH4N36YbQT2zXooiAmNxzJ6IXFvCWxX6D_ViRF6iHrFyl7jNNwYQcpyX_HjlAzNwJEGCTuQs9JEYBUgb0M4wsR-oIAvUCM6fndEOk2H3xITu1sfAMKGWBnKtHqNPoEwklwHTDoqY24DFkYZhhuA");

        // 构建请求体
        // 构建URI
        String url = "http://188.107.245.58:19999/agent-api/user/3ab14961-af35-4e89-8406-e3f89b92271a/task/"+taskId+"/number";
        // 创建HttpEntity对象封装请求头和请求体
        HttpEntity<NewResultRequest> requestEntity = new HttpEntity<>(newResultRequest, headers);
        log.info("{}",requestEntity);
        // 发送POST请求
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCodeValue() == 200) {
            String res = response.getBody();
            JSONObject json = JSONObject.parseObject(res);
            log.info("新外呼结果接收接口调用情况{}",json);
        }else {
            log.info("新外呼结果接收接口调用失败");
        }
    }

    @Override
    public void numberBatchReset(String taskId,String phoneNum) {
        NewNumRestRequest restRequest = new NewNumRestRequest();
        ArrayList<String> list = new ArrayList<>();
        list.add(phoneNum);
        restRequest.setPhones(list);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiZDE5NGUyMDhkNmZjZjFiYTIxYjYwZGQ2NWQ2ZTU2ODlkYzFkOTAxMzk5ZTg3ZWJiZGVlYWFmNGFhNDA0YzVlMTllZDQwODc5ZmIwNzc2NWQiLCJpYXQiOjE3NTgzNjAyMzMsIm5iZiI6MTc1ODM2MDIzMywiZXhwIjoxNzg5ODk2MjMzLCJzdWIiOiI5YWEyYzcyMi0xMmJhLTRkOWMtODA1Ni03NTc0NjFjZGVkNWUiLCJzY29wZXMiOltdfQ.tQAr3M1WgUv-YbgFZuNSiMQI5j5Mlw5vw3s7Pcwh_XRo-n1UE08AaHEIHCjHxKAcy1oYGzPrEkp8W0dP9oh_acxtoAw-7_6NXintBSrCF8tESpDvBF2Sr1yxHIVmOdXQkAbGvbnGnTwKedNq0jUqqumTjABj1odi1WoX1_KAQ8hs7RSpYvXogk9D46tRlgrGRQS7JdbrmhHy-FvYrhG4kDM2BlHlQ-zH5suOGE6X_yewi0ENvvq1AQkSclApB4QsyeUpAn-l2LsM4QaSRg_wYDsnzeJkEKfKQMDEQIOudYP1jPjzwVgQBRMi_SBKRojv8KruuXl-JXcvVse_HXiM9yw_Kl-9vEnWL-o-eugyFV35CoSU7svvc2YFSL1JCCSD3GvitutchrorxqsYrPSF6Wgud8pGFyxICI61LmiWjLEs0xriXcvUA1Cr7o_pzj--fZFq4LvBM7XUNL6O6wW585rdLhryjunpjboQWzIn7-NzZ8vk-TObx1Pm4c75ENm0wAh7pHDz8lAx4rFU3iPBnW5dDH4N36YbQT2zXooiAmNxzJ6IXFvCWxX6D_ViRF6iHrFyl7jNNwYQcpyX_HjlAzNwJEGCTuQs9JEYBUgb0M4wsR-oIAvUCM6fndEOk2H3xITu1sfAMKGWBnKtHqNPoEwklwHTDoqY24DFkYZhhuA");

        // 构建请求体
        // 构建URI
        String url = "http://188.107.245.58:19999/agent-api/user/3ab14961-af35-4e89-8406-e3f89b92271a/task/"+taskId+"/number-batch-reset";
        // 创建HttpEntity对象封装请求头和请求体
        HttpEntity<NewNumRestRequest> requestEntity = new HttpEntity<>(restRequest, headers);
        // 发送POST请求
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        if (response.getStatusCodeValue() == 200) {
            String res = response.getBody();
            JSONObject json = JSONObject.parseObject(res);
            log.info("新号码状态重置接口调用情况{}",json);
        }else {
            log.info("新号码状态重置接口调用失败");
        }
    }
}
