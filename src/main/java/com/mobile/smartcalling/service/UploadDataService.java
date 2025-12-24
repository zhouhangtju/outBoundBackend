package com.mobile.smartcalling.service;

import com.mobile.smartcalling.dto.*;
import com.mobile.smartcalling.entity.NewResultRequest;
import org.springframework.scheduling.annotation.Async;

public interface UploadDataService {


    ReceiveResponse uploadResult(RemoteCallResultDto resultDto,String taskName);

    PoorQualityResultResponse uploadPoorQualityResult(PoorQualityResultRequest resultRequest);
    //@Async("asyncExecutor")
    void newUploadData(NewResultRequest newResultRequest,String taskId);

    void numberBatchReset(String taskId,String phoneNum);
}
