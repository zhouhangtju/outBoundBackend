package com.mobile.smartcalling.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@TableName("satisfy_result")
public class ResultDB {
    @TableField("phone_num")
    String phoneNum;

    @TableField("customer_name")
    String customerName;
    @TableField("internet_quality_score")
    String internetQualityScore;

    @TableField("install_score")
    String installScore;

    @TableField("reason")
    String reason;

    @TableField("is_home")
    String isHome;

    @TableField("start_time")
    Date startTime;

    @TableField("is_contact")
    String isContact;

    String ds;
}
