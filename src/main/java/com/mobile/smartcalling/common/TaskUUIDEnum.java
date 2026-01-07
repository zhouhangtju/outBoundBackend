package com.mobile.smartcalling.common;

public enum TaskUUIDEnum {
    INSTALLHANGZHOU("装机单竣工回访-杭州", "5673d783-13c9-4cc5-bcef-3abc22286c41"),
    INSTALLHUZHOU("装机单竣工回访-湖州", "593ef3d3-2fd2-4241-a171-3b3984f35f67"),
    INSTALLLISHUI("装机单竣工回访-丽水", "68f6c619-d084-48f9-ad8d-b67fef1b6f2a"),
    INSTALLNINGBO("装机单竣工回访-宁波", "08374546-f421-4d88-a3f7-2bf74811a005"),
    INSTALLSHAOXING("装机单竣工回访-绍兴", "2ff1d49c-44f1-479a-914d-08b98c151ab5"),
    INSTALLTAIZHOU("装机单竣工回访-台州", "eca85f1e-3558-40ec-8140-79f75f36eb57"),
    INSTALLZHOUSHAN("装机单竣工回访-舟山", "178fe85f-282e-4945-b9ec-64d2abf361ab"),
    HANGZHOU("存量维系-杭州", "771"),
    HUZHOU("存量维系-湖州", "781"),
    LISHUI("存量维系-丽水", "791"),
    NINGBO("存量维系-宁波", "801"),
    SHAOXING("存量维系-绍兴", "811"),
    TAIZHOU("存量维系-台州", "821"),
    ZHOUSHAN("存量维系-舟山", "831"),

    QualitySurveyHANGZHOU("质差修复已上门-杭州", "43fad8cd-ed0c-44e8-a739-5721725de6da"),
    QualitySurveyHUZHOU("质差修复已上门-湖州", "95e6eab9-32a1-41e8-96be-27beef62887d"),
    QualitySurveyLISHUI("质差修复已上门-丽水", "29c274df-0702-4aca-9089-174b3fce5f67"),
    QualitySurveyNINGBO("质差修复已上门-宁波", "7f021fd0-8691-4344-8523-47e2a1c55d39"),
    QualitySurveySHAOXING("质差修复已上门-绍兴", "0764b0db-4d9a-42a4-a772-f6e0168fc775"),
    QualitySurveyTAIZHOU("质差修复已上门-台州", "b01baa38-6e8b-41fc-a6c9-c8de27f580fb"),
    QualitySurveyZHOUSHAN("质差修复已上门-舟山", "65b22203-70d3-4719-8d8d-f4b876c8b6ed"),

    QualityDispatchHANGZHOU("质差派单-杭州", "71fb4197-0244-4289-9229-6f3ebf65f0be"),
    QualityDispatchHUZHOU("质差派单-湖州", "f54fe136-4469-42a7-a49e-5a5c3de66286"),
    QualityDispatchLISHUI("质差派单-丽水", "3bc99a51-78d1-4b9B-bc7c-b72e70361991"),
    QualityDispatchNINGBO("质差派单-宁波", "033f39f8-c4d6-4518-a342-f1fc174196d4"),
    QualityDispatchSHAOXING("质差派单-绍兴", "1a398f6e-f63c-4471-91d5-a0c96ca35937"),
    QualityDispatchTAIZHOU("质差派单-台州", "62c9e697-0350-45ed-a76b-447699ed1c47"),
    QualityDispatchZHOUSHAN("质差派单-舟山", "2e0abd56-218a-492e-9b68-3205bbfee9e5"),

    InstallationCompletionHANGZHOU("投诉单报结回访-杭州", "c7058289-7782-419d-b7b0-7359e3da1ccf"),
    InstallationCompletionHUZHOU("投诉单报结回访-湖州", "aa794389-ae0e-4ac0-be31-90d715465b54"),
    InstallationCompletionLISHUI("投诉单报结回访-丽水", "bf73a3fb-f412-40fc-b819-d05b6fb6e813"),
    InstallationCompletionNINGBO("投诉单报结回访-宁波", "5b81315f-9cac-42b6-8118-f8210655541e"),
    InstallationCompletionSHAOXING("投诉单报结回访-绍兴", "3fd099de-a62f-4d54-b713-0d417d313faa"),
    InstallationCompletionTAIZHOU("投诉单报结回访-台州", "29e374be-3313-49ed-8de4-c41d25589e0d"),
    InstallationCompletionZHOUSHAN("投诉单报结回访-舟山", "746e42f0-15c8-4ce0-b529-a0174d087160");
    private final String taskName;
    private final String taskId;

    TaskUUIDEnum(String taskName, String taskId) {
        this.taskName = taskName;
        this.taskId = taskId;
    }

    // 通过完整任务名称查找对应的枚举实例
    public static TaskUUIDEnum fromTaskName(String taskName) {
        for (TaskUUIDEnum task : values()) {
            if (task.taskName.equals(taskName)) {
                return task;
            }
        }
        throw new IllegalArgumentException("未找到对应任务: " + taskName);
    }


    // 通过id查找对应的名称
    public static TaskUUIDEnum fromTaskID(String taskId) {
        for (TaskUUIDEnum task : values()) {
            if (task.taskId.equals(taskId)) {
                return task;
            }
        }
        throw new IllegalArgumentException("未找到对应任务: " + taskId);
    }

    // Getters
    public String getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }
}