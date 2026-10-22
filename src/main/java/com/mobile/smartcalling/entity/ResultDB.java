package com.mobile.smartcalling.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("satisfy_result")
public class ResultDB {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("phone_num")
    private String phoneNum;

    @TableField("customer_name")
    private String customerName;

    @TableField("internet_quality_score")
    private String internetQualityScore;

    @TableField("install_score")
    private String installScore;

    @TableField("reason")
    private String reason;

    @TableField("is_home")
    private String isHome;

    @TableField("start_time")
    private Date startTime;

    @TableField("is_contact")
    private String isContact;

    @TableField("ds")
    private String ds;
}
