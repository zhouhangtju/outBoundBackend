package com.mobile.smartcalling.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.service.UploadDataService;
import com.mobile.smartcalling.util.XAccessSignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UploadDataServiceImpl implements UploadDataService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String UploadData(UploadDataList uploadDataList) {

        String xAccessSign = XAccessSignUtil.getXAccessSign("openapi_secret");
        if(StringUtils.isEmpty(xAccessSign)){
           return "签名获取失败";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-access-sign", xAccessSign);
        headers.add("x-access-key", "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体d
        Map<String, Object> body = new HashMap<>();
        body.put("task_uuid", "");
        body.put("upload_mode", "PARTIAL_SUCCESS");
        body.put("data", "");

        // 构建请求实体
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 构建URI
        String url = "https://10.79.212.55/api/v1/openapi/task/batchImport/v1";
        URI uri = UriComponentsBuilder.fromHttpUrl(url).build().toUri();

        try {
            Thread.sleep(20000);
        } catch (Exception e) {
            log.error("", e);
        }

        // 发送POST请求
        ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
        String fileName = null;
        if (response.getStatusCodeValue() == 200) {
            String res = response.getBody();
            JSONObject json = JSONObject.parseObject(res);
            Boolean success = json.getBoolean("success");
            String reasonCode = json.getString("reason_code");
            String reasonDesc = json.getString("reason_desc");
            JSONObject data = (JSONObject) json.get("data");
            JSONArray hdfsList = data.getJSONArray("hdfsList");
            Integer totalCount = data.getInteger("total_count");
            Integer successCount = data.getInteger("success_count");
            Integer failCount = data.getInteger("fail_count");
            Integer taskBatchUuid = data.getInteger("task_batch_uuid");
            JSONArray successDataList = data.getJSONArray("success_data_list");
            JSONArray failDataList = data.getJSONArray("fail_data_list");

        }
        return null;
    }
}
