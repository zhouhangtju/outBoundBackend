package com.mobile.smartcalling.dto;

import lombok.Data;

@Data
public class PerformanceInfo {
    private String city;

    // 手机号
    private String phoneNum;

    // 外呼场景	质差派单、
    //质差修复已上门
    private String callScene;

    // 工单号，非必填，有线平台要给
    private String order;

    // 工单宽带账号
    private String account;

}
