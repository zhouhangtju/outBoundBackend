package com.mobile.smartcalling.service;

import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.dto.ReceiveResponse;
import com.mobile.smartcalling.entity.CallbackData;
import com.mobile.smartcalling.entity.NewCallbackData;
import org.springframework.scheduling.annotation.Async;

public interface ICallbackSevice {

   // @Async("callbackExecutor")
     void getNewJsonData(NewCallbackData newCallbackData);

}
