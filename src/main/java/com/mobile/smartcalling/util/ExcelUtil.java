package com.mobile.smartcalling.util;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.mobile.smartcalling.entity.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExcelUtil {

    /**
     * 导出 Excel 文件, 使用 Object 可以不局限与只能导出一种对象类型的尴尬
     * @param
     */
    public static String outPutExcel(List<Object> dateList,String cgi, String path) {
        // 默认导出地址 obj.getClass().getName() 使用反射获取类的名称
        String fileName = path + "input-" + cgi +".xlsx";
        //log.info("===outPutExcel param: {},{}", dateList.size(),fileName);
        // 创建一个输出流，将文件输出
        try (
                FileOutputStream outputStream = new FileOutputStream(fileName);
                // EasyExcel.write(outputStream) 为了使用流将数据导出d
                ExcelWriter writer = EasyExcel.write(outputStream).build();
                ){
            // writerSheet 中 sheetNo 表示导出的是 Excel 中第几页数据，sheetName 表示导出的该页数据名称
            // head 表示 Excel 数据需要映射的类
            WriteSheet sheet = EasyExcel.writerSheet(0, "Sheet1").head(
                    Object.class).build();
            // 开始写入数据
            writer.write(dateList, sheet);
            // 刷新数据
            writer.finish();
            outputStream.flush();
            //log.info("Excel文件: {} 生成完毕", fileName);
        } catch (IOException e) {
            log.error("",e);
        }
        return fileName;
    }

    public static List<Data> readExcel(String path) {

        //数据量大时 批量导入
//        EasyExcel.read(path, objectClass, new PageReadListener<Object>(dataList -> {
//            // 并且每行数据，并将其 add 至 list 中
//            for (Object excelData : dataList) {
//                if (excelData != null) {
//                    list.add(excelData);
//                }
//            }
//        })).excelType(ExcelTypeEnum.XLSX).sheet().doRead(); // 指定 Excel 的文件后缀，开始分析读取

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        List<Data> list = EasyExcel.read(inputStream).head(Data.class).sheet().doReadSync();
        System.out.printf(String.valueOf(list.size()));
        return list;
    }

}
