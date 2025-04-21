package com.mobile.smartcalling.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class CallbackData {

    private String task_name;

    private String task_uuid;

    private String task_batch_uuid;

    private String task_item_uuid;

    private String  tag_code;

    private String tag_desc;

    private String comment;

    private List<VoiceRecord> voice_record;

    private SmsRecord sms_record;

    private InputParams task_status;

    private InputParams input_params;


    public String getTag_code(){
        return tag_code;
    }

    public void setTag_code(){
        this.tag_code = tag_code;
    }
}
