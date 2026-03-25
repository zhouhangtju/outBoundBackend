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
        headers.set("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxMyIsImp0aSI6IjQ0ZDM1YzI3YWMxNWI0MGRlNGRlZGU0Zjc5NzhlZWUxYzM3MTZiMjZmZDZmOTljMGUzMmRiNWU5MmI3OGY5NTJlNDVlNTNhMWFmMTA2YzUzIiwiaWF0IjoxNzc0MDg5NzA4LCJuYmYiOjE3NzQwODk3MDgsImV4cCI6MTgwNTYyNTcwOCwic3ViIjoiOWFhMmM3MjItMTJiYS00ZDljLTgwNTYtNzU3NDYxY2RlZDVlIiwic2NvcGVzIjpbXX0.ACIJTMdmp3N9qBFriQIwbB_GI5K9MJmEPbsL9-qsg6UiyhqZfPPRJ8ZwHIjURANOEQPrxIHXuAWAu6SP-6PSn0BOwQDd275FpXxV-Wgwow98Z_uKcXZKGyPoiRnqcJdenKctGgcA2xdRqoRWv2n1UW82HdR6wWkwAKggZRzXOj_kULz80mStaWOiVZdrunh5ff8qFEStmc59bRLIl-4y744x1XhIEsnHmiJ1zHuCBEpRKRo0NkRQnJFCHfvLdhWCnlZk9MJCa7x8_0nNKMBk0fPoQshJ9Qcb7vi5k_JSmtNO8dQAlGlQgCzcADRbw0VAQ3qAPqzpIPPHzBVYCqCJ6PRlR4DghVWAfbMcJomih0gnxqOg6jkj4AoRPhW94RBDYQm-elnZWEKWW4j2TNJm9cifWaoq-UKpAAyT54n-rVmw1XJzqW73BGy1UNadBhSA_w6T3xl5xdtjtOLJ7ybEgoH5KEhSuhNS7dcB2x53E3ftstEvP7c2FelEiLoCidNLiRXidyAswNa0JiT20YLC_LJ3qMMJud3hHEUgIG3pxVClPazOcIAja2Vriwgy4gt9Ygbpg-c2huqgKQpeNM1TQ_jw0QClprsanuAFmYIGj1muhVOIA1HnSbffvZg63TmXib-nYTyrRl-HkOYLkuN5O7mk6wMiyg7t8mDwnWjWuFk");

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
        headers.set("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxMyIsImp0aSI6IjQ0ZDM1YzI3YWMxNWI0MGRlNGRlZGU0Zjc5NzhlZWUxYzM3MTZiMjZmZDZmOTljMGUzMmRiNWU5MmI3OGY5NTJlNDVlNTNhMWFmMTA2YzUzIiwiaWF0IjoxNzc0MDg5NzA4LCJuYmYiOjE3NzQwODk3MDgsImV4cCI6MTgwNTYyNTcwOCwic3ViIjoiOWFhMmM3MjItMTJiYS00ZDljLTgwNTYtNzU3NDYxY2RlZDVlIiwic2NvcGVzIjpbXX0.ACIJTMdmp3N9qBFriQIwbB_GI5K9MJmEPbsL9-qsg6UiyhqZfPPRJ8ZwHIjURANOEQPrxIHXuAWAu6SP-6PSn0BOwQDd275FpXxV-Wgwow98Z_uKcXZKGyPoiRnqcJdenKctGgcA2xdRqoRWv2n1UW82HdR6wWkwAKggZRzXOj_kULz80mStaWOiVZdrunh5ff8qFEStmc59bRLIl-4y744x1XhIEsnHmiJ1zHuCBEpRKRo0NkRQnJFCHfvLdhWCnlZk9MJCa7x8_0nNKMBk0fPoQshJ9Qcb7vi5k_JSmtNO8dQAlGlQgCzcADRbw0VAQ3qAPqzpIPPHzBVYCqCJ6PRlR4DghVWAfbMcJomih0gnxqOg6jkj4AoRPhW94RBDYQm-elnZWEKWW4j2TNJm9cifWaoq-UKpAAyT54n-rVmw1XJzqW73BGy1UNadBhSA_w6T3xl5xdtjtOLJ7ybEgoH5KEhSuhNS7dcB2x53E3ftstEvP7c2FelEiLoCidNLiRXidyAswNa0JiT20YLC_LJ3qMMJud3hHEUgIG3pxVClPazOcIAja2Vriwgy4gt9Ygbpg-c2huqgKQpeNM1TQ_jw0QClprsanuAFmYIGj1muhVOIA1HnSbffvZg63TmXib-nYTyrRl-HkOYLkuN5O7mk6wMiyg7t8mDwnWjWuFk");

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
