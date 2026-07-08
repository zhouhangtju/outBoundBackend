package com.mobile.smartcalling.common;

import com.mobile.smartcalling.entity.RemoteCallResult;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CommonBusinessStrEnum {

    public static final String INSTALLATION_COMPLETION = "装机单竣工回访";
    public static final String POOR_QUALITY_DISPATCHING_ORDERS = "质差派单";
    public static final String POOR_QUALITY_REPAIR_HAS_BEEN_FOLLOWED_UP = "质差修复已上门";
    public static final String COMPLAINT_FORM_CLOSING_FOLLOW_UP = "投诉单报结回访";
}
