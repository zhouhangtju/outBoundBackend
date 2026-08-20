package com.mobile.smartcalling.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mobile.smartcalling.entity.ResultDB;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ResultDBDao extends BaseMapper<ResultDB> {

    @Update("ALTER TABLE satisfy_result ADD PARTITION (PARTITION p_${ds} VALUES IN ('${ds}'))")
    void createPartition(String ds);
}
