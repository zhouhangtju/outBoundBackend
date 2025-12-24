package com.mobile.smartcalling.entity;

import lombok.Data;
import java.util.List;

@Data
public class NewCallbackData {
    private Integer count;
    private List<CallRecordData> data;

    @Data
    public static class CallRecordData {
        private String calldate;

        private Integer status;
        private String answerdate;
        private Integer bill;
        private Integer duration;
        private String callid;
        private String customer_id;
        private String master_user_id;
        private String user_id;
        private Integer intention_results;
        private Integer rounds;
        private Integer score;
        private NumberData number_data;
        private Group group;
        private Task task;
        private User user;
        private Level level;
        private List<Tag> tags;
        private CustomerData customer_data;
        private String voice_url;
    }

    @Data
    public static class NumberData {
        private String number;
        private String hangupdate;
        private String province;
        private String city;
        private String operator;
    }

    @Data
    public static class Group {
        private Integer id;
        private String name;
    }

    @Data
    public static class Task {
        private String id;
        private String name;
    }

    @Data
    public static class User {
        private String id;
        private String name;
    }

    @Data
    public static class Level {
        private String level_id;
        private String level_name;
    }

    @Data
    public static class Tag {
        private String tag_id;
        private String name;
    }

    @Data
    public static class CustomerData {
        private String name;
        private String email;
        private String company;
        private String extra;
        private List<Components> components;
    }
    @Data
    public static class Components {
        private String id;
        private String name;
        private String type;
        private String value;
    }

}
    