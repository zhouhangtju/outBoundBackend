package com.mobile.smartcalling.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mobile.smartcalling.dao.ConfigInfoDao;
import com.mobile.smartcalling.dto.UploadDataList;
import com.mobile.smartcalling.entity.ConfigInfo;
import com.mobile.smartcalling.entity.UploadData;
import com.mobile.smartcalling.service.IReadCSVService;
import com.mobile.smartcalling.service.UploadDataService;
import com.mobile.smartcalling.util.SftpUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReadCSVServiceImpl implements IReadCSVService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UploadDataService uploadDataService;

    @Autowired
    private ConfigInfoDao configInfoDao;

    public String getCSVPath() {

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
        } catch (Exception e) {
            log.error("Failed to start SFTP download", e);
        }

        Map<String, List<String>> downloadMap = SftpUtil.download(sftpDetails);
        String filePath = null;
        Set<String> keySet = downloadMap.keySet();
        List<String> list = keySet.stream().collect(Collectors.toList());
        if (list.size() == 1) {
            filePath = list.get(0);
        }
        return filePath;
       // return "test/test.csv";
    }

    @Override
    public void getUploadDataList(String filePath) {

        ConfigInfo configInfo = configInfoDao.selectById(1);
        String xAccessKey = configInfo.getXAccessKey();
        String openapiSecret =  configInfo.getOpenApiSecret();
        String taskUUID = configInfo.getTaskUuid();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date date = new Date();
        Date time = DateUtils.addDays(date,-1);
        String ds = sdf.format(time);


        RFC4180Parser parser = new RFC4180ParserBuilder()
                .withSeparator('|')
                .withQuoteChar('"')
                .withQuoteChar('“')
                .build();
        //try(InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName), Charset.forName("UTF-8"))){
        try(CSVReader reader = new CSVReaderBuilder(new FileReader(filePath)).withCSVParser(parser).withSkipLines(1).build()){
                // 创建映射策略并指定字段顺序
                ColumnPositionMappingStrategy<UploadData> strategy = new ColumnPositionMappingStrategy<>();
                strategy.setType(UploadData.class);

                // 按照文件中字段的实际顺序设置映射
                strategy.setColumnMapping(new String[]{
                        "areaName",
                        "phoneNum",
                });
                // 使用 CsvToBeanBuilder 将 CSV 数据转换为 Java 对象列表
                List<UploadData> list = new CsvToBeanBuilder<UploadData>(reader)//字段的分割符号  不指定则为默认的‘,’分割
                        .withSkipLines(1)     //跳过第一行标题行
                        .withSeparator('|')
                        .withMappingStrategy(strategy)
                        .build()
                        .parse();
                log.info("===file size: {}", list.size());
                log.info("===data demo: {}", list.get(0));
                log.info("读取文件success");


                UploadDataList uploadDataList = new UploadDataList();
                uploadDataList.setUploadDataList(list);

                int batch = 2000;
                if (uploadDataList.getUploadDataList() != null && uploadDataList.getUploadDataList().size() != 0) {
                    int times = uploadDataList.getUploadDataList().size() / batch;
                    for (int i = 0; i <= times; i++) {
                        if ((i + 1) * batch >= uploadDataList.getUploadDataList().size()) {
                            List<UploadData> subList = uploadDataList.getUploadDataList().subList(i * batch, uploadDataList.getUploadDataList().size());
                            UploadDataList subUploadDataList = new UploadDataList();
                            subUploadDataList.setUploadDataList(subList);
                            uploadDataService.UploadData(subUploadDataList,taskUUID,xAccessKey,openapiSecret);
                            log.info("upload batch: {}", (i + 1));
                        } else {
                            List<UploadData> subList = uploadDataList.getUploadDataList().subList(i * batch, (i + 1) * batch);
                            UploadDataList subUploadDataList = new UploadDataList();
                            subUploadDataList.setUploadDataList(subList);
                            uploadDataService.UploadData(subUploadDataList,taskUUID,xAccessKey,openapiSecret);
                            log.info("upload batch: {}", (i + 1));
                        }
                    }
                } else {
                    log.info("获取文件数据为空");
                }
            } catch (Exception e) {
                log.info("文件读取错误{}", e);
            }
    }
}