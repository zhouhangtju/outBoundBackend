package com.mobile.smartcalling.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("remote_call_result")
public class RemoteCallResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("phone_num")
    private String phoneNum;

    @TableField("order_id")
    private String orderId;

    @TableField("customer_account")
    private String customerAccount;

    @TableField("call_date")
    private String callDate;

    // 外呼任务名称
    private String task;

    // 城市
    private String city;

    private String tag1;

    private String tag2;

    private String tag3;

    private String tag4;

    private String tag5;

    private String tag6;

    private String tag7;

    private String tag8;

    private String tag9;

    private String tag10;

    private String tag11;

    @TableField("call_time")
    private String callTime;

    @TableField("is_arrive")
    private String isArrive;

    @TableField("line_status")
    private String lineStatus;

    @TableField("arrive_time")
    private String arriveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
