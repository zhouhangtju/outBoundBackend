package com.mobile.smartcalling.controller;

import com.mobile.smartcalling.dao.ConfigInfoDao;
import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.entity.ConfigInfo;
import com.mobile.smartcalling.entity.UploadData;
import com.mobile.smartcalling.service.IReadCSVService;
import com.mobile.smartcalling.service.UploadCSVService;
import com.mobile.smartcalling.service.UploadDataService;
import com.mobile.smartcalling.task.TaskService;
import com.mobile.smartcalling.util.FtpUtil;
import com.mobile.smartcalling.util.SftpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Slf4j
@Api(value = "测试接口", tags = {"测试接口"})
public class DemoController {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IReadCSVService readCSVService;

    @Autowired
    private UploadCSVService uploadCSVService;

    @Autowired
    private UploadDataService uploadDataService;

    @Autowired
    private ConfigInfoDao configInfoDao;


    @GetMapping("/updateConfigInfo")
    @ApiOperation("修改配置信息中的xAccessKey和openapiSecret")
    public void updateConfigInfo(@RequestParam("xAccessKey")String xAccessKey,@RequestParam("openapiSecret")String openapiSecret,
                                @RequestParam("taskUUID") String taskUUID){
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setOpenApiSecret(openapiSecret);
        configInfo.setXAccessKey(xAccessKey);
        configInfo.setTaskUuid(taskUUID);
        configInfo.setId(1);
        configInfoDao.updateById(configInfo);
    }

    @GetMapping("/getCsv")
    @ApiOperation("测试从服务器下载csv文件,返回文件存储地址")
    public void getCsv(){
        String csvPath = readCSVService.getCSVPath();
        log.info("获取到csvPath{}",csvPath);
    }

    @GetMapping("/uploadData")
    @ApiOperation("测试调用外呼系统接口")
    public void uploadData(String phoneNum){
        UploadDataList dataList = new UploadDataList();
        UploadData uploadData = new UploadData();
        uploadData.setPhoneNum(phoneNum);
        ArrayList<UploadData> list = new ArrayList<>();
        list.add(uploadData);
        dataList.setUploadDataList(list);
        ConfigInfo configInfo = configInfoDao.selectById(1);
        String xAccessKey = configInfo.getXAccessKey();
        String openapiSecret =  configInfo.getOpenApiSecret();
        String taskUUID = configInfo.getTaskUuid();
        uploadDataService.UploadData(dataList,taskUUID,xAccessKey,openapiSecret);
    }

    @GetMapping("/getCsvData")
    @ApiOperation("测试读取文件数据获取数据并入库接口")
    public void getCsvData(String filePath){
        readCSVService.getUploadDataList(filePath);
    }


    @GetMapping("/createDS")
    @ApiOperation("手动创建指定分区")
    public void createdDS(@RequestParam(value = "dd") String dd) {
        //dd为  20241219   格式
        taskService.acquireCreateDs(dd);
    }

    @GetMapping("/uploadCsv")
    @ApiOperation("手动上传指定天的文件")
    public void uploadCsv(@RequestParam(value = "dd") String dd) {
        //dd为  20241219   格式
        uploadCSVService.uploadCSV(dd);
    }


    @GetMapping("/SftpDownload")
    @ApiOperation("测试sftp拉取文件")
    public String testSftpDownload() {
        Map<String, Object> sftpDetails = new HashMap<>();
        List<Object> fileList = new ArrayList<>();

        Map<String, Object> fileDetails = new HashMap<>();
        //TODO 从你的参数中提取文件名
        fileDetails.put("fileName", "文件名");
        fileList.add(fileDetails);

        // 使用你提供的信息填充SFTP连接细节
        String connectionString = "sftp://dcpp:D8is_F7n61#15@10.76.148.39:21"; // 注意修改为正确的端口3001
        // 这里我们分解connectionString来获取用户名和密码，但实际上SftpUtil不使用这种方式连接
        // 直接根据你提供的信息设置用户名和密码
        sftpDetails.put("Path", "/data1/dcpp/ZXAICL"); // SFTP服务器上的文件路径
        sftpDetails.put("files", fileList);
        sftpDetails.put("userName", "dcpp");
        sftpDetails.put("password", "D8is_F7n61#15");

        try {
            SftpUtil.download(sftpDetails);
            return "Started SFTP download successfully.";
        } catch (Exception e) {
            log.error("Failed to start SFTP download", e);
            return "Failed to start SFTP download: " + e.getMessage();
        }
    }

    @GetMapping("/testFtp")
    @ApiOperation("测试ftp拉取文件")
    public void testFtp() {
        Map<String, Object> map = new HashMap<>();
        String connectionString = "ftp://dcpp:D8is_F7n61#15@10.76.148.39:21";
        map.put("userName","dcpp");
        map.put("password","D8is_F7n61#15");
        map.put("Path","/data1/dcpp/ZXAICL");
        ArrayList<String> files = new ArrayList<>();
        //TODO 从你的参数中提取文件名
        files.add("文件名");
        map.put("files",files);
        FtpUtil.downLoad(map);

        log.info("=====123");
    }
}




