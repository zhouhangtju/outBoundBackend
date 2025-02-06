package com.mobile.smartcalling.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class CallbackData {

    private String task_name;

    private String task_uuid;

    private String task_batch_uuid;

    private String task_item_uuid;

    private String  tag_code;

    private String tag_desc;

    private String comment;

    private JSONArray voice_record;

    private JSONArray sms_record;

    private JSONObject task_status;
}
