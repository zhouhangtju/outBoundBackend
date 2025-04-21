package com.mobile.smartcalling.controller;

import com.alibaba.fastjson.JSONArray;
import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.entity.CallbackData;
import com.mobile.smartcalling.entity.InputParams;
import com.mobile.smartcalling.entity.SmsRecord;
import com.mobile.smartcalling.entity.VoiceRecord;
import com.mobile.smartcalling.service.ICallbackSevice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/insertData")
    @ApiOperation("回调接口")
    public CallBackResult callback(@RequestBody CallbackData callbackData) {
        log.info("回调接口接收数据{}",callbackData);
        return callbackSevice.getJsonData(callbackData);
    }
}
