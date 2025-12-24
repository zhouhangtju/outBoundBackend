package com.mobile.smartcalling.entity;

import java.util.List;
import lombok.Data;

@Data
public class NewResultRequest {
    private boolean ignore_crm;
    private boolean skip_error;
    private List<ContactData> data;

    @Data
    public static class ContactData {
       // private String name;
        private String phone;
       // private String email;
      //  private String company;
       // private Integer sex;
         private Integer sort;
    //    private Integer source_id;
     //   private Integer industry_id;
     //   private Integer grade_id;
        private String extra;
      //  private List<Component> components;
    }

    @Data
    public static class Component {
        private String id;
        private String value;
    }
}
