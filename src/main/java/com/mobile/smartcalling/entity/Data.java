package com.mobile.smartcalling.entity;

import com.alibaba.excel.annotation.ExcelProperty;

@lombok.Data
public class Data {

    @ExcelProperty(index= 0)

    String imsi;

    @ExcelProperty(index= 1)

    String  cell_id ;

    @ExcelProperty(index= 2)

    String procedure_start_time_on_probe;

    @ExcelProperty(index= 3)

    String procedure_end_time_on_probe;

    @ExcelProperty(index= 4)

    String p_city;

    @ExcelProperty(index= 5)

    String p_hour;
}

