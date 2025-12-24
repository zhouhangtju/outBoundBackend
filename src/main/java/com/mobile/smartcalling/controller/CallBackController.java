package com.mobile.smartcalling.controller;

import com.alibaba.fastjson.JSONArray;
import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.entity.*;
import com.mobile.smartcalling.service.ICallbackSevice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/callback")
@Slf4j
@Api(value = "回调接口", tags = {"回调接口"})
public class CallBackController {
    @Autowired
    private ICallbackSevice callbackSevice;


@PostMapping("/newInsertData")
@ApiOperation("回调接口")
    public CallBackResult callback(@RequestBody NewCallbackData callbackData) {
    CallBackResult backResult = new CallBackResult();
    log.info("回调接口接收数据{}", callbackData);

    // 立即校验参数并设置返回结果
    boolean isValid = ObjectUtils.isNotEmpty(callbackData);
    backResult.setSuccess(isValid);

    // 如果数据有效，异步处理业务逻辑，不等待结果
    if (isValid) {
        asyncProcessCallbackData(callbackData);
    }
    return backResult;
}

        public void asyncProcessCallbackData(NewCallbackData callbackData) {
        try {
            callbackSevice.getNewJsonData(callbackData);
            log.info("异步处理回调数据成功");
        } catch (Exception e) {
            // 记录异常但不影响主流程
            log.error("异步处理回调数据失败", e);
        }
    }
}
