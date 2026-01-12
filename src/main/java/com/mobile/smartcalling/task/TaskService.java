package com.mobile.smartcalling.task;



import com.mobile.smartcalling.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskService {
    private final String taskExecutor = "taskExecutor";

    @Autowired
    private RedisUtil redisUtil;

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 1 * * ? ")
   // @Scheduled(cron = "0/1 * * * * ?") // 0/1 表示从0秒开始，每1秒执行一次
    public void deleteRedisKey() {
        boolean b = redisUtil.deleteYesterdayPhoneKey();
        log.info("redis 昨日手机key删除状态:{}",b);
    }
}
