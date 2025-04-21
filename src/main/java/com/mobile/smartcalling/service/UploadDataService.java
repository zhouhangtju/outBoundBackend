package com.mobile.smartcalling.service;

import com.mobile.smartcalling.dto.UploadDataList;

public interface UploadDataService {
    void UploadData(UploadDataList uploadDataList,String taskUUID,String xAccessKey,String openapiSecret);
}
