package com.mobile.smartcalling.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("config_info")
public class ConfigInfo {

    private Integer id;
    private String xAccessKey;

    private String openApiSecret;

    private String taskUuid;
}
