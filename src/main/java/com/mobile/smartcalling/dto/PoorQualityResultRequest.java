package com.mobile.smartcalling.dto;

import lombok.Data;

@Data
public class PoorQualityResultRequest {

    private String phoneNum;

    private String callTime;

    private String isArrive;

    private String lineStatus;

    private String arriveTime;
}
