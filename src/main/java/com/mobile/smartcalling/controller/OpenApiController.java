package com.mobile.smartcalling.controller;

import com.mobile.smartcalling.dao.ConfigInfoDao;
import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.dto.PerformanceInfo;
import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.entity.ConfigInfo;
import com.mobile.smartcalling.entity.UploadData;
import com.mobile.smartcalling.service.UploadDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/openapi")
@Slf4j
@Api(value = "Api接口", tags = {"对外提供接口供调用"})
public class OpenApiController {

    @Autowired
    private UploadDataService uploadDataService;
    @Autowired
    private ConfigInfoDao configInfoDao;
    @PostMapping("/performanceInfo")
    @ApiOperation("接收性能测数据")
    public CallBackResult useCallBack(@RequestBody PerformanceInfo performanceInfo){
        ConfigInfo configInfo = configInfoDao.selectById(1);
        String openApiSecret = configInfo.getOpenApiSecret();
        String xAccessKey = configInfo.getXAccessKey();
        String taskUuid = configInfo.getTaskUuid();
        log.info("接收到性能测数据{}",performanceInfo);
        CallBackResult result = new CallBackResult();
        if(ObjectUtils.isNotEmpty(performanceInfo)&& StringUtils.isNotEmpty(performanceInfo.getPhoneNum())){
            UploadDataList uploadDataList = new UploadDataList();
            UploadData uploadData = new UploadData();
            uploadData.setPhoneNum(performanceInfo.getPhoneNum());
            ArrayList<UploadData> list = new ArrayList<>();
            list.add(uploadData);
            uploadDataList.setUploadDataList(list);
            uploadDataService.UploadData(uploadDataList,taskUuid,xAccessKey,openApiSecret);
            result.setSuccess(true);
            return result;
        }else {
            result.setSuccess(false);
            return result;
        }
    }

}
