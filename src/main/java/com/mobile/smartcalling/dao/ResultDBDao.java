package com.mobile.smartcalling.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobile.smartcalling.entity.ResultDB;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ResultDBDao extends BaseMapper<ResultDB> {

    @Update("ALTER TABLE satisfy_result ADD PARTITION (PARTITION p_${ds} VALUES IN ('${ds}'))")
    void createPartition(String ds);

    @Select("SELECT COUNT(1) FROM information_schema.PARTITIONS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'satisfy_result' AND PARTITION_NAME = 'p_${ds}'")
    int countPartition(String ds);
}
