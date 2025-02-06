package com.mobile.smartcalling;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class SmartcallingApplicationTests {

    @Test
    void test() {
        HashSet<String> set = new HashSet<>();
        String[] list1 = {"47975936112","47439339732","47973048431","47974350959","47969980527","47975071855","47975071955","47975071856","47966318703","47436066927","47973474415","47969644655","47444627567","47439597979","47440793911","47445360752","47976640723","47444541751","47435489392","47966089428"};
        String[] list2 ={"48016593007","47835369583","47835369584","47309860975","47297695855","47310033007","47297503343","47836790895","47313269047","47312941167","47313670255","47837073520","47301017711","47846162543","47837782127","47309549679","47309893743"};
        String[] list3 = {"47987224687","47455510640","47460499567","47987228883","47990014064","47455858799","47455498651","47459991663","47457656943","47993409647","47462514799","47993978991","47450288239","47452029041"};
        String[] list4 ={"47384625263","47914930290","47378309231","47384596591","47377608815","47899422831","47373135983","47376355439"};
        String[] list5 = {"47358103663","47351672943","47352864879","47352770671","47342915696","47353487471","47358931055","47352238193","47351902320","47352119409","47333187696","47869554799","47333187695","47868940399","47343902832","47886409839","47352819824","47357976687","47338733680","47359524976","47343988847","47361642609"};
        String[] list6 ={"47390584943","47412150383","47937667183","47393386609","47406784626","47943028848","47395311727","47393865839","47388115055","47388110960","47408230511","47940751471","47389835375","47392292977","47390310511","47393325167","47392305263","47412101231","47407034479","47390650479","47393312879","47390478447","47940874351","47411781743","47393087600","47412093039","47387951215","47940075635","47412105327","47406047343","47936065647","47409520752","47388110959","47411621999"};
        String[] list7 ={"47789981809","47781523567","47790801007","47275225200","47276507250","47792185455","47283691631","47277391983","47249285231","47790567535","47269081202","47265022064","47790567539","47789056111","47283261552","47274213489","47781597295","47282585712","47282262127","47271239791","47785160815","47273103471","47783141487","47284039792","47785140336","47786393711","47274922096"};

        set.addAll(Arrays.asList(list1));
        set.addAll(Arrays.asList(list2));
        set.addAll(Arrays.asList(list3));
        set.addAll(Arrays.asList(list4));
        set.addAll(Arrays.asList(list5));
        set.addAll(Arrays.asList(list6));
        set.addAll(Arrays.asList(list7));

        List<Map<String, String>> dataList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream("C:\\Users\\47480\\Desktop\\111.xlsx");
             Workbook workbook = new XSSFWorkbook(fis)) {

            // 假设数据在第一个Sheet中
            Sheet sheet = workbook.getSheetAt(0);

            // 读取表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel文件中没有表头行");
            }

            // 用于存储表头列名
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            // 从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = getCellValue(cell);
                    rowData.put(headers.get(j), cellValue);
                }
                dataList.add(rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("=================="+String.valueOf(dataList.size())+"=================");
        System.out.printf(String.valueOf(dataList));
        System.out.printf("=================================\n");
        System.out.printf(String.valueOf(set)+"\n");
        for (int i = 0; i < 165; i++) {
            Map<String, String> map = dataList.get(i);
            if(map.get("距离判断n1n2异常").equals("是")){
                continue;
            }
            String eci = map.get("eci");
            set.forEach(item->{
                if(item.equals(eci)) {
                    for (String key : map.keySet()) {
                        if (key.equals("距离判断n1n2异常")) {
                            map.put(key, "是");
                        }
                    }
                }
            });
        }
        System.out.printf("=================="+String.valueOf(dataList.size())+"=================");
        System.out.printf(String.valueOf(dataList));


        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建Sheet
            Sheet sheet = workbook.createSheet("Sheet1");
        String[] headers = {"cgi", "trp_id", "初步异常", "距离判断n1n2异常", "eci", "小区名称", "城市"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 写入数据
        for (int i = 0; i < dataList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, String> rowData = dataList.get(i);
            for (int j = 0; j < headers.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(rowData.get(headers[j]));
            }
        }

        // 保存到文件
        try (FileOutputStream fos = new FileOutputStream("C:\\Users\\47480\\Desktop\\333.xlsx")) {
            workbook.write(fos);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    DecimalFormat format = new DecimalFormat();
                   String result = format.format(value);
                    if(result!=null && result.indexOf(",")>=0){
                        result = result.replaceAll(",","");
                    }
                    return result;
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

}
