package com.mobile.smartcalling.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mobile.smartcalling.service.IReadCSVService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReadCSVServiceImpl implements IReadCSVService {

    @Autowired
    private RestTemplate restTemplate;

    public String getCSVPath() {

        HttpHeaders headers = new HttpHeaders();
        headers.add("apiSecret", "45ec7010-1a45-4c60-81b2-ce302c0066ed");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> body = new HashMap<>();
        body.put("ftpPath", "/data1/hdfs_file");
        body.put("url", "_");

        // 构建请求实体
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 构建URI
        String url = "";
        URI uri = UriComponentsBuilder.fromHttpUrl(url).build().toUri();


        // 发送POST请求
        ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
        String fileName = null;

        if (response.getStatusCodeValue() == 200) {

        }
        return fileName;
}

}