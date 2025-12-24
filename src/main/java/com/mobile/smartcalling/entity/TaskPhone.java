package com.mobile.smartcalling.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("task_phone")
public class TaskPhone {
    private String phone;

    private String taskName;
}
