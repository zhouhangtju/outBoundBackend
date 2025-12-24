package com.mobile.smartcalling;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mobile.smartcalling.dto.RemoteCallResultDto;
import com.mobile.smartcalling.service.ICallbackSevice;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.codec.digest.DigestUtils.md5;

@SpringBootTest
@Slf4j
class SmartcallingApplicationTests {
    // Q3相关的nodeName列表
    static List<String> q3NodeNames = Arrays.asList(
            "Q3","Q3-default", "Q3-A", "Q3-B",
            "Q3-C", "Q3-D"
    );

    // Q4相关的nodeName列表
    static List<String> q4NodeNames = Arrays.asList(
            "Q4-非满分", "Q4-非满分-default","Q4-10分", "Q4-A",
            "Q4-B",  "Q4-C",
            "Q4-C-非满分"
    );

    static List<String> q7NodeNames = Arrays.asList(
            "Q7", "Q7-B","Q4-10分", " Q7-B-default"
    );
    @Autowired
    private ICallbackSevice callbackSevice;

    private static final DecimalFormat df = new DecimalFormat("#0.00");
    @Test
    void Test2(){
        ArrayList<ListInfo> infos = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
        Date now = new Date();
        Random random = new Random();
        // 格式化并打印日期
        for (int i = 1; i < 8; i++) {
            Date baseDate = DateUtils.addDays(now, i * -1);

            // 随机生成时分秒（小时:0-23，分钟:0-59，秒:0-59）
            int randomHour = 9+random.nextInt(1);
            int randomMinute = random.nextInt(60);
            int randomSecond = random.nextInt(60);

            // 设置随机时分秒
            Date randomizedDate = DateUtils.setHours(baseDate, randomHour);
            randomizedDate = DateUtils.setMinutes(randomizedDate, randomMinute);
            randomizedDate = DateUtils.setSeconds(randomizedDate, randomSecond);

            // 格式化日期
            String formattedDate = sdf.format(randomizedDate);

            // 创建并添加到列表
            ListInfo listInfo = new ListInfo();
            listInfo.setData1(formattedDate);

            // 生成第一个随机数 (0-30)
            double num1 = random.nextDouble() * 30;


            // 生成第二个随机数 (0-60)
            double num2 = random.nextDouble() * 60;


            // 生成第三个随机数 (0-40)
            double num3 = random.nextDouble() * 40;

            listInfo.setData2(df.format(num1));
            listInfo.setData3(df.format(num2));
            listInfo.setData4(df.format(num3));

            infos.add(listInfo);
        }
    }

    @Test
    void test3(){

        if ("Q4".startsWith("Q4")) {
            // 第二步：排除 Q4-1 这个特殊节点（单独处理）
            if ("Q4-1".equals("Q4")){
                System.out.printf("111");
            } else {
                // 匹配 Q4、Q4-2、Q4-3 等所有 Q4 相关的其他节点
                System.out.printf("222");
            }
        }
    }

    public static boolean isAllFieldsNull(Object obj) {
        for (Field f : obj.getClass().getDeclaredFields()) {
            if (f.getName().equals("orderid")) {
                continue;
            }
            f.setAccessible(true);
            try {
                if (f.get(obj) != null && !"".equals(f.get(obj).toString().trim())) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
