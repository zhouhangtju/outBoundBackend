package com.mobile.smartcalling.service;

import com.mobile.smartcalling.dto.UploadDataList;

import java.util.List;

public interface IReadCSVService {
    List<String> readCsv(String filePath);

    //  String getCSVPath();

   //  void getUploadDataList(String filePath);
}
