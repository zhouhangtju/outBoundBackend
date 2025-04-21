package com.mobile.smartcalling.entity;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

@Data
public class UploadData {

    @CsvBindByPosition(position = 1)
    private String phoneNum;

}
