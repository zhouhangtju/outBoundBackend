package com.mobile.smartcalling.service.impl;

import com.mobile.smartcalling.dao.DemoMapper;
import com.mobile.smartcalling.dao.ResultDBDao;
import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.entity.*;
import com.mobile.smartcalling.service.ICallbackSevice;
import com.mobile.smartcalling.service.UploadDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class CallbackServiceimpl implements ICallbackSevice {

    @Autowired
    private ResultDBDao resultDBDao;
    @Autowired
    private UploadDataService uploadDataService;

    @Override
    public CallBackResult getJsonData(CallbackData callbackData) {
        CallBackResult callBackResult = new CallBackResult();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date date = new Date();
        Date time = DateUtils.addDays(date,-1);
        String ds = sdf.format(time);
        String dd = ds.replace("-", "");
        ResultDB resultDB = new ResultDB();
        resultDB.setDs(dd);
        String taskUuid = callbackData.getTask_uuid();
        if(ObjectUtils.isNotEmpty(callbackData)) {
            List<VoiceRecord> voiceRecord = callbackData.getVoice_record();
            SmsRecord smsRecord = callbackData.getSms_record();
            InputParams inputParams = callbackData.getInput_params();
            String customerName = inputParams.getCustomerName();
            String phoneNum = inputParams.getPhoneNum();
            voiceRecord.forEach(item -> {
                List<Interaction> interactions = item.getInteractions();
                interactions.forEach(interaction -> {
                    String content = interaction.getContent();


//                    if ("满意度".equals(interaction.getNodeName())) {
//                        String content = interaction.getContent();
//                        //TODO 解析json中的值
//                    resultDB.setPhoneNum(phoneNum);
//                    resultDB.setCustomerName(customerName);
//                    resultDB.setDs(dd);
//                    }

                });
            });
            log.info("回调信息入库结果{}",resultDB);
            //resultDBDao.insert(resultDB);
            callBackResult.setSuccess(true);
            return callBackResult;
        }else {
            callBackResult.setSuccess(false);
            return callBackResult;
        }
//        UploadDataList uploadDataList = new UploadDataList();
//        UploadData uploadData = new UploadData();
//        uploadData.setPhoneNum(phoneNum);
//        uploadData.setAreaName(customerName);
        //TODO 判断uuid决定哪些批次需要立即上传
//        if(!StringUtils.isEmpty(taskUuid)&&taskUuid.equals("某个值")){
//            uploadDataService.UploadData(uploadDataList,taskUuid);
//        }
    }
}
