package com.mobile.smartcalling.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mobile.smartcalling.dao.ResultDBDao;
import com.mobile.smartcalling.entity.ResultDB;
import com.mobile.smartcalling.service.UploadCSVService;
import com.mobile.smartcalling.util.CsvUtil;
import com.mobile.smartcalling.util.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UploadCSVServiceImpl implements UploadCSVService {

    @Autowired
    private ResultDBDao resultDBDao;

    @Override
    public void uploadCSV(String dd) {
        LambdaQueryWrapper<ResultDB> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResultDB::getDs,dd);
        List<ResultDB> resultDBList = resultDBDao.selectList(queryWrapper);
        //文件本地保存路径
        String path = CsvUtil.exportToCsv(resultDBList, dd);
        log.info("文件保存路径：{}",path);
        //TODO FTP上传文件需测试
        //FtpUtil.upLoad("10.76.148.39",21,"dcpp","D8is_F7n61#15",path,"/data1/dcpp/ZXAICL");
    }
}
