package com.mobile.smartcalling.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobile.smartcalling.entity.RemoteCallResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RemoteCallResultDao extends BaseMapper<RemoteCallResult> {


    /**
     * 批量插入 RemoteCallResult
     * 使用 @Insert 注解实现
     */
    @Insert({
            "<script>",
            "INSERT INTO remote_call_result (",
            "phone_num, order_id, customer_account, call_date, ",
            "tag1, tag2, tag3, tag4, tag5, ",
            "tag6, tag7, tag8, tag9, tag10, tag11, ",
            "call_time, is_arrive, line_status, arrive_time, create_time",
            ") VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "( ",
            "#{item.phoneNum}, #{item.orderId}, #{item.customerAccount}, #{item.callDate}, ",
            "#{item.tag1}, #{item.tag2}, #{item.tag3}, #{item.tag4}, #{item.tag5}, ",
            "#{item.tag6}, #{item.tag7}, #{item.tag8}, #{item.tag9}, #{item.tag10}, #{item.tag11}, ",
            "#{item.callTime}, #{item.isArrive}, #{item.lineStatus}, #{item.arriveTime}, #{item.createTime}",
            ")",
            "</foreach>",
            "</script>"
    })
    int insertBatch(@Param("list") List<RemoteCallResult> remoteCallResultList);

}
