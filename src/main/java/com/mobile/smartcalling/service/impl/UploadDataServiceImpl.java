package com.mobile.smartcalling.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.service.UploadDataService;
import com.mobile.smartcalling.util.XAccessSignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public void UploadData(UploadDataList uploadDataList,String taskUUID,String xAccessKey,String openapiSecret) {

        String xAccessSign = XAccessSignUtil.getXAccessSign(openapiSecret,xAccessKey);
        if(StringUtils.isEmpty(xAccessSign)){
            log.info("签名获取失败");
        }

        HttpHeaders headers = new HttpHeaders();

        //headers.add("x-access-sign", xAccessSign);
        headers.add("x-access-sign", xAccessSign);
        headers.add("x-access-key", xAccessKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体

        //将数据转换为jsonArray格式
        JSONArray dataArray = JSONArray.parseArray(JSON.toJSONString(uploadDataList.getUploadDataList()));

        Map<String, Object> body = new HashMap<>();
        body.put("task_uuid", taskUUID);
        body.put("upload_mode", "ALL_SUCCESS");
        body.put("data", dataArray);

        // 构建请求实体
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 构建URI
        String url = "http://188.107.245.56:8028/openapi/task/batchImport/v1";

        // 发送POST请求
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (response.getStatusCodeValue() == 200) {
            String res = response.getBody();
            JSONObject json = JSONObject.parseObject(res);
//            Boolean success = json.getBoolean("success");
//            String reasonCode = json.getString("reason_code");
//            String reasonDesc = json.getString("reason_desc");
//            JSONObject data = (JSONObject) json.get("data");
//            JSONArray hdfsList = data.getJSONArray("hdfsList");
//            Integer totalCount = data.getInteger("total_count");
//            Integer successCount = data.getInteger("success_count");
//            Integer failCount = data.getInteger("fail_count");
//            Integer taskBatchUuid = data.getInteger("task_batch_uuid");
//            JSONArray successDataList = data.getJSONArray("success_data_list");
//            JSONArray failDataList = data.getJSONArray("fail_data_list");
            log.info("批量导入接口调用情况{}",json.getBoolean("success"));
            log.info("批量导入接口返回数据{}",json);
        }else {
            log.info("批量导入接口调用失败");
        }
    }
}
