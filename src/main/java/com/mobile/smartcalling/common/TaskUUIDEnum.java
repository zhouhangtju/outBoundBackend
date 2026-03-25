package com.mobile.smartcalling.common;

public enum TaskUUIDEnum {
    INSTALLHANGZHOU("装机单竣工回访-杭州", "a8e7c2b2-23ac-4986-8346-67735605dd5c"),
    INSTALLHUZHOU("装机单竣工回访-湖州", "e7c484aa-db16-486d-ac8c-534658e65139"),
    INSTALLLISHUI("装机单竣工回访-丽水", "1dcce3a0-48a0-4174-bb99-c464e8f796d3"),
    INSTALLNINGBO("装机单竣工回访-宁波", "83eecedd-90c5-4a2a-b4cf-ea2d68e4842f"),
    INSTALLSHAOXING("装机单竣工回访-绍兴", "a1cecf8b-1e15-4f3e-9201-c2398128a2ee"),
    INSTALLTAIZHOU("装机单竣工回访-台州", "62086274-9e11-40d5-9cf0-a40d944f5d60"),
    INSTALLZHOUSHAN("装机单竣工回访-舟山", "9ea09f9f-3b64-4037-a3fa-e83af34ecf12"),
    INSTALLJIAXING("装机单竣工回访-嘉兴", "6f810971-db36-4ba9-a687-3f7e0505bf0d"),

    HANGZHOU("存量维系-杭州", "771"),
    HUZHOU("存量维系-湖州", "781"),
    LISHUI("存量维系-丽水", "791"),
    NINGBO("存量维系-宁波", "801"),
    SHAOXING("存量维系-绍兴", "811"),
    TAIZHOU("存量维系-台州", "821"),
    ZHOUSHAN("存量维系-舟山", "831"),

    QualitySurveyHANGZHOU("质差修复已上门-杭州", "142c223a-6e91-4328-8aad-35ac01369c55"),
    QualitySurveyHUZHOU("质差修复已上门-湖州", "79b3c253-7408-4dbc-9b70-8fd7888bc216"),
    QualitySurveyLISHUI("质差修复已上门-丽水", "221dc6df-6c23-44a4-ab6b-cb2d9e2eae01"),
    QualitySurveyNINGBO("质差修复已上门-宁波", "1634ee8d-a387-47f8-8ba6-4cb62e59e701"),
    QualitySurveySHAOXING("质差修复已上门-绍兴", "9317f3b8-fd9f-4f26-a0f7-a449ab01a23a"),
    QualitySurveyTAIZHOU("质差修复已上门-台州", "8cc4a510-58fa-45a6-939d-576d6e5efc3f"),
    QualitySurveyZHOUSHAN("质差修复已上门-舟山", "812aaa9e-ac7d-45d3-8b85-e386a9c48f16"),
    QualitySurveyJIAXING("质差修复已上门-嘉兴", "af194d79-da5a-4ab8-b875-dd8593d15b8a"),


    QualityDispatchHANGZHOU("质差派单-杭州", "aaed1735-e562-4cdc-b9c0-494e53e91525"),
    QualityDispatchHUZHOU("质差派单-湖州", "f54fe136-4469-42a7-a49e-5a5c3de66286"),
    QualityDispatchLISHUI("质差派单-丽水", "928ea078-337e-4d43-a535-7516ff5bd5bc"),
    QualityDispatchNINGBO("质差派单-宁波", "c0a86462-1e30-407f-a662-25e79f764b04"),
    QualityDispatchSHAOXING("质差派单-绍兴", "1a398f6e-f63c-4471-91d5-a0c96ca35937"),
    QualityDispatchTAIZHOU("质差派单-台州", "cd943a3d-a375-402c-b82f-3e13370a72ba"),
    QualityDispatchZHOUSHAN("质差派单-舟山", "88b5fa9e-ecbd-491f-baad-25731a75e894"),
    QualityDispatchJIAXING("质差派单-嘉兴", "bd030aee-4203-4999-ab8f-48741f04e65a"),


    InstallationCompletionHANGZHOU("投诉单报结回访-杭州", "6277e6dc-49c8-4264-8b1f-585941ba7e46"),
    InstallationCompletionHUZHOU("投诉单报结回访-湖州", "3a418073-f156-49e3-afe1-d77a248f0388"),
    InstallationCompletionLISHUI("投诉单报结回访-丽水", "12ab2adb-cb8c-4fe9-b10f-bbce2ae4dff6"),
    InstallationCompletionNINGBO("投诉单报结回访-宁波", "ba508c01-c348-47eb-87f4-8e5e1fe98265"),
    InstallationCompletionSHAOXING("投诉单报结回访-绍兴", "6114b08a-c48e-4f53-a9bd-7ad1e69ef871"),
    InstallationCompletionTAIZHOU("投诉单报结回访-台州", "ba885176-28ce-47b9-8e00-8315e6affcd6"),
    InstallationCompletionZHOUSHAN("投诉单报结回访-舟山", "c0b53cc2-fdc1-4b64-bb54-4aaaf653f649"),
    InstallationCompletionJIAXING("投诉单报结回访-嘉兴", "371c444a-4f07-451f-8b78-7bf57a6b4f94");

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