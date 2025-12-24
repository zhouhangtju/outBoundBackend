package com.mobile.smartcalling.service.impl;


import com.mobile.smartcalling.service.UploadCSVService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UploadCSVServiceImpl implements UploadCSVService {

//    @Autowired
//    private ResultDBDao resultDBDao;

//    @Override
//    public void uploadCSV(String dd) {
//        LambdaQueryWrapper<ResultDB> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(ResultDB::getDs,dd);
//        List<ResultDB> resultDBList = resultDBDao.selectList(queryWrapper);
//        //文件本地保存路径
//        String path = CsvUtil.exportToCsv(resultDBList, dd);
//        log.info("文件保存路径：{}",path);
//        //TODO FTP上传文件需测试
//        //FtpUtil.upLoad("10.76.148.39",21,"dcpp","D8is_F7n61#15",path,"/data1/dcpp/ZXAICL");
//    }
}
