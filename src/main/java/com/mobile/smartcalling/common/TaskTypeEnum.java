package com.mobile.smartcalling.common;

import lombok.Getter;

@Getter
public enum TaskTypeEnum {

    INSTALLATION_NINGBO("装机竣工-宁波", "work_order_installation_completion_callback_result_ningbo"),
    INSTALLATION_OTHER("装机竣工-除宁波地市外", "work_order_installation_completion_callback_result"),
    POOR_QUALITY_DISPATCH("质差派单", "poor_quality_dispatch_callback_result"),
    POOR_QUALITY_REPAIR("质差修复已上门回访", "poor_quality_repair_callback_result"),
    POOR_QUALITY_REPAIR_NOT_VISIT("投诉单报结回访", "complaint_order_closure_callback_result");

    private final String chineseName;

    private final String filePrefix;

    TaskTypeEnum(String chineseName, String filePrefix) {
        this.chineseName = chineseName;
        this.filePrefix = filePrefix;
    }
}
