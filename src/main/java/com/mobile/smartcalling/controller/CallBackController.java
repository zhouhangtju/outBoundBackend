package com.mobile.smartcalling.controller;

import com.mobile.smartcalling.entity.CallbackData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/callback")
@Slf4j
@Api(value = "回调接口", tags = {"回调接口"})
public class CallBackController {

    @PostMapping("/callback")
    @ApiOperation("回调接口")
    public String callback(@RequestBody CallbackData callbackData) {

        return "callback";
    }
}
