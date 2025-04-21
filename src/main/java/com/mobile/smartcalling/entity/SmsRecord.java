package com.mobile.smartcalling.entity;

import lombok.Data;

@Data
public class SmsRecord {
    String customer_phone;
    String start_time;
    String sms_content;
}
