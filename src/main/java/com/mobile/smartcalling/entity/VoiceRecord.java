package com.mobile.smartcalling.entity;

import lombok.Data;

import java.util.List;

@Data
public class VoiceRecord {
    String customer_phone;

    String start_time;
    String Caller;
    String call_type;
    String call_status;
    String duration;
    List<String> tag_list;
    String file_id;
    List<Interaction> interactions;
}
