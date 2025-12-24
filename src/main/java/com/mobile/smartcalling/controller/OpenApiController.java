package com.mobile.smartcalling.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mobile.smartcalling.common.TaskUUIDEnum;
import com.mobile.smartcalling.dao.TaskPhoneDao;
import com.mobile.smartcalling.dto.CallBackResult;
import com.mobile.smartcalling.dto.PerformanceInfo;
import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.entity.NewResultRequest;
import com.mobile.smartcalling.entity.TaskPhone;
import com.mobile.smartcalling.entity.UploadData;
import com.mobile.smartcalling.service.ICallbackSevice;
import com.mobile.smartcalling.service.UploadDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/openapi")
@Slf4j
@Api(value = "Api接口", tags = {"对外提供接口供调用"})
public class OpenApiController {

    @Autowired
    private UploadDataService uploadDataService;

    @Autowired
    private TaskPhoneDao taskPhoneDao;




@PostMapping("/performanceInfo")
@ApiOperation("接收性能测数据")
public CallBackResult useCallBack(@RequestBody PerformanceInfo performanceInfo) {
        log.info("接收到性能测数据{}", performanceInfo);
        CallBackResult result = new CallBackResult();
        boolean isValid = ObjectUtils.isNotEmpty(performanceInfo) && StringUtils.isNotEmpty(performanceInfo.getPhoneNum());
        // 立即设置返回结果
        result.setSuccess(isValid);

        // 如果数据有效，异步执行上传逻辑，不等待结果
        if (isValid) {
            asyncUploadData(performanceInfo);
        }
        return result;
    }

        public void asyncUploadData(PerformanceInfo performanceInfo) {
        try {
            TaskUUIDEnum taskUUIDEnum = TaskUUIDEnum.fromTaskName(performanceInfo.getCallScene() + "-" + performanceInfo.getCity());
            LambdaQueryWrapper<TaskPhone> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TaskPhone::getPhone,performanceInfo.getPhoneNum());
            queryWrapper.eq(TaskPhone::getTaskName,taskUUIDEnum.getTaskName());
            List<TaskPhone> taskPhones = taskPhoneDao.selectList(queryWrapper);
            if(taskPhones.size()==0){
                //号码第一次拨打，调用号码上传接口,并存入数据库
                TaskPhone phone = new TaskPhone();
                NewResultRequest newResultRequest = new NewResultRequest();
                ArrayList<NewResultRequest.ContactData> data = new ArrayList<>();
                NewResultRequest.ContactData contactData = new NewResultRequest.ContactData();
                newResultRequest.setIgnore_crm(false);
                newResultRequest.setSkip_error(true);
                contactData.setPhone(performanceInfo.getPhoneNum());
                contactData.setExtra(performanceInfo.getOrder());
                contactData.setSort(20);
                data.add(contactData);
                newResultRequest.setData(data);
                uploadDataService.newUploadData(newResultRequest,taskUUIDEnum.getTaskId());
                phone.setPhone(performanceInfo.getPhoneNum());
                phone.setTaskName(taskUUIDEnum.getTaskName());
                taskPhoneDao.insert(phone);
            }else {
                //否则调用号码重置状态接口
                uploadDataService.numberBatchReset(taskUUIDEnum.getTaskId(),performanceInfo.getPhoneNum());
            }

            log.info("异步上传性能数据成功, phoneNum={},taskID={}", performanceInfo.getPhoneNum(),taskUUIDEnum.getTaskId());
        } catch (Exception e) {
            // 记录异常但不影响主流程
            log.error("异步上传性能数据失败, phoneNum={}", performanceInfo.getPhoneNum(), e);
        }
    }
}
