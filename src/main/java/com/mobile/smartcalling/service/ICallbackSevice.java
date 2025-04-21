package com.mobile.smartcalling.service;

import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.entity.CallbackData;

public interface ICallbackSevice {

     CallBackResult getJsonData(CallbackData callbackData);

}
